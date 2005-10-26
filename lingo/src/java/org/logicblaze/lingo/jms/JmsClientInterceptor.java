/**
 *
 * Copyright 2005 LogicBlaze, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/

package org.logicblaze.lingo.jms;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.logicblaze.lingo.LingoInvocation;
import org.logicblaze.lingo.LingoRemoteInvocationFactory;
import org.logicblaze.lingo.MetadataStrategy;
import org.logicblaze.lingo.MethodMetadata;
import org.logicblaze.lingo.SimpleMetadataStrategy;
import org.logicblaze.lingo.jms.impl.AsyncReplyHandler;
import org.logicblaze.lingo.jms.impl.MultiplexingRequestor;
import org.logicblaze.lingo.jms.marshall.DefaultMarshaller;
import org.logicblaze.lingo.jms.marshall.Marshaller;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Interceptor for accessing a JMS based service which must be configured with a
 * {@link org.logicblaze.lingo.LingoRemoteInvocationFactory} instance.
 *
 * @author James Strachan
 * @see #setServiceInterface
 * @see #setServiceUrl
 * @see JmsServiceExporter
 * @see JmsProxyFactoryBean
 */
public class JmsClientInterceptor extends RemoteInvocationBasedAccessor
        implements MethodInterceptor, InitializingBean, DisposableBean {

    private Map remoteObjects = new WeakHashMap();
    private Requestor requestor;
    private Destination destination;
    private Destination responseDestination;
    private String correlationID;
    private Marshaller marshaller;
    private ConnectionFactory connectionFactory;
    private String jmsType;
    private Map messageProperties;

    public JmsClientInterceptor() {
        setRemoteInvocationFactory(createRemoteInvocationFactory());
    }

    public JmsClientInterceptor(Requestor requestor) {
        this.requestor = requestor;
        setRemoteInvocationFactory(createRemoteInvocationFactory());
    }

    public JmsClientInterceptor(Requestor requestor, LingoRemoteInvocationFactory factory) {
        this.requestor = requestor;
        setRemoteInvocationFactory(factory);
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
            return "JMS invoker proxy for service URL [" + getServiceUrl() + "]";
        }
        LingoInvocation invocation = (LingoInvocation) createRemoteInvocation(methodInvocation);
        MethodMetadata metadata = invocation.getMetadata();
        replaceRemoteReferences(invocation, metadata);
        try {
            Message requestMessage = marshaller.createRequestMessage(requestor, invocation);
            populateHeaders(requestMessage);
            if (metadata.isOneWay()) {
                requestor.oneWay(destination, requestMessage);
                return null;
            }
            else {
                Message response = requestor.request(destination, requestMessage);
                RemoteInvocationResult result = marshaller.extractInvocationResult(response);
                return recreateRemoteInvocationResult(result);
            }
        }
        catch (JMSException ex) {
            throw new RemoteAccessException("Cannot access JMS invoker remote service at [" + getServiceUrl() + "]", ex);
        }
    }

    public void afterPropertiesSet() throws JMSException {
        RemoteInvocationFactory factory = getRemoteInvocationFactory();
        if (!(factory instanceof LingoRemoteInvocationFactory)) {
            throw new IllegalArgumentException("remoteInvocationFactory must be an instance of LingoRemoteInvocationFactory but was: " + factory);

        }
        if (requestor == null) {
            if (connectionFactory == null) {
                throw new IllegalArgumentException("requestor or connectionFactory is required");
            }
            else {
                requestor = MultiplexingRequestor.newInstance(connectionFactory, destination, responseDestination);
            }
        }
        if (marshaller == null) {
            // default to standard JMS marshalling
            marshaller = new DefaultMarshaller();
        }
    }

    public void destroy() throws Exception {
        requestor.close();
    }

    // Properties
    //-------------------------------------------------------------------------
    public Requestor getRequestor() {
        return requestor;
    }

    public void setRequestor(Requestor requestor) {
        this.requestor = requestor;
    }

    public Destination getDestination() {
        return destination;
    }

    /**
     * Sets the destination used to make requests
     *
     * @param destination
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Destination getResponseDestination() {
        return responseDestination;
    }

    /**
     * Sets the destination used to consume responses on - or null
     * and a temporary queue will be created.
     *
     * @param responseDestination
     */
    public void setResponseDestination(Destination responseDestination) {
        this.responseDestination = responseDestination;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    
    public String getJmsType() {
        return jmsType;
    }

    /**
     * Sets the JMS message type string which is appended to messages if set
     */
    public void setJmsType(String jmsType) {
        this.jmsType = jmsType;
    }

    public Map getMessageProperties() {
        return messageProperties;
    }

    /**
     * Sets the message properties to be added to each message. Note that the keys should be Strings
     * and the values should be primitive types.
     */
    public void setMessageProperties(Map messageProperties) {
        this.messageProperties = messageProperties;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Used to create a default {@link Requestor} if no requestor is explicitly
     * configured.
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected void populateHeaders(Message requestMessage) throws JMSException {
        if (correlationID != null) {
            requestMessage.setJMSCorrelationID(correlationID);
        }
        if (jmsType != null) {
            requestMessage.setJMSType(jmsType);
        }
        if (messageProperties != null) {
            for (Iterator iter = messageProperties.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = entry.getKey().toString();
                Object value = entry.getValue();
                requestMessage.setObjectProperty(name, value);
            }
        }
    }


    /**
     * Recreate the invocation result contained in the given RemoteInvocationResult
     * object. The default implementation calls the default recreate method.
     * <p>Can be overridden in subclass to provide custom recreation, potentially
     * processing the returned result object.
     *
     * @param result the RemoteInvocationResult to recreate
     * @return a return value if the invocation result is a successful return
     * @throws Throwable if the invocation result is an exception
     * @see org.springframework.remoting.support.RemoteInvocationResult#recreate
     */
    protected Object recreateRemoteInvocationResult(RemoteInvocationResult result) throws Throwable {
        return result.recreate();
    }

    protected void replaceRemoteReferences(LingoInvocation invocation, MethodMetadata metadata) {
        Object[] arguments = invocation.getArguments();
        Class[] parameterTypes = invocation.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (metadata.isRemoteParameter(i)) {
                arguments[i] = remoteReference(parameterTypes[i], arguments[i]);
            }
        }
    }

    protected Object remoteReference(Class type, Object value) {
        if (value == null) {
            return null;
        }
        String correlationID = (String) remoteObjects.get(value);
        if (correlationID == null) {
            correlationID = requestor.createCorrelationID();
            remoteObjects.put(value, correlationID);
        }
        if (requestor instanceof MultiplexingRequestor) {
            MultiplexingRequestor multiplexingRequestor = (MultiplexingRequestor) requestor;
            multiplexingRequestor.registerHandler(correlationID, new AsyncReplyHandler(value, marshaller));
        }
        else {
            throw new IllegalArgumentException("You can only pass remote references with a MultiplexingRequestor");
        }
        return correlationID;
    }

    /**
     * Factory method to create a default lingo based invocation factory if none is configured
     */
    protected LingoRemoteInvocationFactory createRemoteInvocationFactory() {
        return new LingoRemoteInvocationFactory(createMetadataStrategy());
    }

    /**
     * Factory method to create a default metadata strategy if none is configured
     *
     * @return
     */
    protected MetadataStrategy createMetadataStrategy() {
        return new SimpleMetadataStrategy();
    }

}
