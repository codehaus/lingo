/**
 *
 * Copyright 2005 AgilaSoft Ltd
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

package org.agilasoft.lingo.jms;

import org.agilasoft.lingo.LingoInvocation;
import org.agilasoft.lingo.LingoRemoteInvocationFactory;
import org.agilasoft.lingo.MetadataStrategy;
import org.agilasoft.lingo.MethodMetadata;
import org.agilasoft.lingo.SimpleMetadataStrategy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Interceptor for accessing a JMS based service which must be configured with a
 * {@link org.agilasoft.lingo.LingoRemoteInvocationFactory} instance.
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
    private String correlationID;

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
            Message requestMessage = createRequestMessage(invocation, metadata);
            populateHeaders(requestMessage);
            if (metadata.isOneWay()) {
                requestor.oneWay(destination, requestMessage);
                return null;
            }
            else {
                Message response = requestor.request(destination, requestMessage);
                RemoteInvocationResult result = extractInvocationResult(response);
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
            throw new IllegalArgumentException("requestor is required");
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

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }
    
    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Creates the request message
     *
     * @param invocation the remote invocation to send
     * @param metadata
     * @throws javax.jms.JMSException if the message could not be created
     */
    protected Message createRequestMessage(RemoteInvocation invocation, MethodMetadata metadata) throws JMSException {
        return requestor.getSession().createObjectMessage(invocation);
    }

    protected void populateHeaders(Message requestMessage) throws JMSException {
        if (correlationID != null) {
            requestMessage.setJMSCorrelationID(correlationID);
        }
    }

    /**
     * Extracts the invocation result from the response message
     *
     * @param message the response message
     * @return the invocation result
     * @throws javax.jms.JMSException is thrown if a JMS exception occurs
     */
    protected RemoteInvocationResult extractInvocationResult(Message message) throws JMSException {
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Object body = objectMessage.getObject();
            if (body instanceof RemoteInvocationResult) {
                return (RemoteInvocationResult) body;
            }
        }
        return onInvalidMessage(message);
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
            multiplexingRequestor.registerHandler(correlationID, new AsyncReplyHandler(value));
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

    protected RemoteInvocationResult onInvalidMessage(Message message) throws JMSException {
        throw new JMSException("Invalid response message: " + message);
    }

}
