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
import org.agilasoft.lingo.jms.impl.AsyncReplyHandler;
import org.agilasoft.lingo.jms.impl.MultiplexingRequestor;
import org.agilasoft.lingo.jms.marshall.DefaultMarshaller;
import org.agilasoft.lingo.jms.marshall.Marshaller;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
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
    private Marshaller marshaller;

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
            throw new IllegalArgumentException("requestor is required");
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

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected void populateHeaders(Message requestMessage) throws JMSException {
        if (correlationID != null) {
            requestMessage.setJMSCorrelationID(correlationID);
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
