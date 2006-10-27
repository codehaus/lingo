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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.LingoInvocation;
import org.logicblaze.lingo.LingoRemoteInvocationFactory;
import org.logicblaze.lingo.MetadataStrategy;
import org.logicblaze.lingo.MetadataStrategyHelper;
import org.logicblaze.lingo.MethodMetadata;
import org.logicblaze.lingo.jms.impl.DefaultJmsProducer;
import org.logicblaze.lingo.jms.impl.MultiplexingRequestor;
import org.logicblaze.lingo.jms.marshall.DefaultMarshaller;
import org.logicblaze.lingo.jms.marshall.Marshaller;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

/**
 * A JMS MessageListener that exports the specified service bean as a JMS
 * service endpoint, accessible via a JMS proxy. <p/>
 * <p>
 * Note: JMS services exported with this class can be accessed by any JMS
 * client, as there isn't any special handling involved.
 * 
 * @org.apache.xbean.XBean element="serviceExporterMessageListener"
 * 
 * @author James Strachan
 * @see JmsProxyFactoryBean
 * @version $Revision$
 */
public class JmsServiceExporterMessageListener extends RemoteInvocationBasedExporter implements MessageListener, InitializingBean, DisposableBean {
    private static final Log log = LogFactory.getLog(JmsServiceExporterMessageListener.class);

    private Object proxy;
    private ConnectionFactory connectionFactory;
    private Requestor responseRequestor;
    private JmsProducerConfig producerConfig = new JmsProducerConfig();
    private boolean ignoreFailures;
    private Marshaller marshaller;
    private MetadataStrategy metadataStrategy;
    private RemoteInvocationFactory invocationFactory;

    public JmsServiceExporterMessageListener() {
    }

    public JmsServiceExporterMessageListener(Object proxy) {
        this.proxy = proxy;
    }

    public void afterPropertiesSet() throws Exception {
        if (proxy == null) {
            this.proxy = getProxyForService();
            if (proxy == null) {
                throw new IllegalArgumentException("proxy is required");
            }
        }
        if (responseRequestor == null) {
            responseRequestor = MultiplexingRequestor.newInstance(connectionFactory, producerConfig, null);
        }
        if (marshaller == null) {
            marshaller = new DefaultMarshaller();
        }
        if (metadataStrategy == null) {
            metadataStrategy = MetadataStrategyHelper.newInstance();
        }
        if (invocationFactory == null) {
            invocationFactory = new LingoRemoteInvocationFactory(metadataStrategy);
        }
    }

    public void onMessage(Message message) {
        try {
            RemoteInvocation invocation = marshaller.readRemoteInvocation(message);
            doInvoke(message, invocation);
        }
        catch (JMSException e) {
            onException(message, e);
        }
    }

    public void destroy() throws Exception {
        if (responseRequestor != null) {
            responseRequestor.close();
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * Used to create a default {@link JmsProducer} if no producer is explicitly
     * configured.
     */
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Requestor getResponseRequestor() {
        return responseRequestor;
    }

    public void setResponseRequestor(Requestor responseRequestor) {
        this.responseRequestor = responseRequestor;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public RemoteInvocationFactory getInvocationFactory() {
        return invocationFactory;
    }

    public void setInvocationFactory(RemoteInvocationFactory invocationFactory) {
        this.invocationFactory = invocationFactory;
    }

    public boolean isIgnoreFailures() {
        return ignoreFailures;
    }

    /**
     * Sets whether or not failures should be ignored (and just logged) or
     * thrown as runtime exceptions into the JMS provider
     */
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }

    public JmsProducerConfig getProducerConfig() {
        return producerConfig;
    }

    /**
     * Sets the configuration of the producer used to send back responses
     */
    public void setProducerConfig(JmsProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
    }

    public boolean isPersistentDelivery() {
        return producerConfig.getDeliveryMode() == DeliveryMode.PERSISTENT;
    }

    /**
     * Sets the delivery mode to be persistent or non-persistent.
     */
    public void setPersistentDelivery(boolean persistent) {
        producerConfig.setDeliveryMode(persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
    }

    public String getClientID() {
        return producerConfig.getClientID();
    }

    /**
     * Sets the JMS connections unique clientID. This is optional unless you
     * wish to use durable topic subscriptions. Only one connection can have a
     * given clientID at any time.
     */
    public void setClientID(String clientID) {
        producerConfig.setClientID(clientID);
    }

    public Object getProxy() {
        return proxy;
    }

    public MetadataStrategy getMetadataStrategy() {
        return metadataStrategy;
    }

    public void setMetadataStrategy(MetadataStrategy metadataStrategy) {
        this.metadataStrategy = metadataStrategy;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected void doInvoke(Message message, RemoteInvocation invocation) throws JMSException {
        if (invocation != null) {
            boolean oneway = false;
            if (invocation instanceof LingoInvocation) {
                LingoInvocation lingoInvocation = (LingoInvocation) invocation;
                oneway = lingoInvocation.getMetadata().isOneWay();
                introduceRemoteReferences(lingoInvocation, message);
            }
            RemoteInvocationResult result = invokeAndCreateResult(invocation, this.proxy);
            if (!oneway) {
                writeRemoteInvocationResult(message, result);
            }
        }
    }

    /**
     * Creates the invocation result response message
     * 
     * @param session
     *            the JMS session to use
     * @param message
     *            the original request message, in case we want to attach any
     *            properties etc.
     * @param result
     *            the invocation result
     * @return the message response to send
     * @throws javax.jms.JMSException
     *             if creating the messsage failed
     */
    protected Message createResponseMessage(Session session, Message message, RemoteInvocationResult result) throws JMSException {
        // an alternative strategy could be to use XStream and text messages
        // though some JMS providers, like ActiveMQ, might do this kind of thing
        // for us under the covers
        if (result == null) {
            throw new IllegalArgumentException("result cannot be null");
        }

        Message answer = getMarshaller().createResponseMessage(session, result, message);

        // lets preserve the correlation ID
        answer.setJMSCorrelationID(message.getJMSCorrelationID());
        return answer;
    }

    /**
     * Lets replace any remote object correlation IDs with dynamic proxies
     * 
     * @param invocation
     * @param requestMessage
     */
    protected void introduceRemoteReferences(LingoInvocation invocation, Message requestMessage) throws JMSException {
        MethodMetadata metadata = invocation.getMetadata();
        Object[] arguments = invocation.getArguments();
        Class[] parameterTypes = invocation.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (metadata.isRemoteParameter(i)) {
                arguments[i] = createRemoteProxy(requestMessage, parameterTypes[i], arguments[i]);
            }
        }
    }

    protected Object createRemoteProxy(Message message, Class parameterType, Object argument) throws JMSException {
        JmsProxyFactoryBean factory = new JmsProxyFactoryBean();
        factory.setDestination(message.getJMSReplyTo());
        String correlationID = (String) argument;
        if (log.isDebugEnabled()) {
            log.debug("Creating a server side remote proxy for correlationID: " + correlationID);
        }
        factory.setCorrelationID(correlationID);
        factory.setMarshaller(getMarshaller());
        factory.setRemoteInvocationFactory(invocationFactory);
        factory.setServiceInterface(parameterType);
        factory.setRequestor(responseRequestor);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * Handle the processing of an exception when processing an inbound messsage
     */
    protected void onException(Message message, JMSException e) {
        String text = "Failed to process inbound message due to: " + e + ". Message will be discarded: " + message;
        log.info(text, e);
        if (!ignoreFailures) {
            throw new RuntimeException(text, e);
        }
    }
    /**
     * Send the given RemoteInvocationResult as a JMS message to the originator
     * 
     * @param message
     *            current HTTP message
     * @param result
     *            the RemoteInvocationResult object
     * @throws javax.jms.JMSException
     *             if thrown by trying to send the message
     */
    protected void writeRemoteInvocationResult(final Message message, final RemoteInvocationResult result) throws JMSException {
        Message responseMessage = createResponseMessage(getResponseRequestor().getSession(), message, result);
        getResponseRequestor().send(message.getJMSReplyTo(), responseMessage);
    }
}
