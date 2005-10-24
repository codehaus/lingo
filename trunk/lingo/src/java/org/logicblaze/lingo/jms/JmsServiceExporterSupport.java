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
import org.logicblaze.lingo.MethodMetadata;
import org.logicblaze.lingo.SimpleMetadataStrategy;
import org.logicblaze.lingo.jms.marshall.DefaultMarshaller;
import org.logicblaze.lingo.jms.marshall.Marshaller;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationFactory;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;

/**
 * @version $Revision$
 */
public abstract class JmsServiceExporterSupport extends RemoteInvocationBasedExporter implements MessageListener, InitializingBean {
    private static final Log log = LogFactory.getLog(JmsServiceExporterSupport.class);

    protected Object proxy;
    private boolean ignoreFailures;
    private Marshaller marshaller;
    private MetadataStrategy metadataStrategy;
    private RemoteInvocationFactory invocationFactory;
    private Requestor responseRequestor;

    public void afterPropertiesSet() throws Exception {
        this.proxy = getProxyForService();
        if (proxy == null) {
            throw new IllegalArgumentException("proxy is required");
        }
        if (responseRequestor == null) {
            throw new IllegalArgumentException("responseRequestor is required");
        }
        if (marshaller == null) {
            marshaller = new DefaultMarshaller();
        }
        if (metadataStrategy == null) {
            metadataStrategy = new SimpleMetadataStrategy(true);
        }
        if (invocationFactory == null) {
            invocationFactory = new LingoRemoteInvocationFactory(metadataStrategy);
        }
    }

    public void onMessage(Message message) {
        try {
            RemoteInvocation invocation = marshaller.readRemoteInvocation(message);
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
        catch (JMSException e) {
            onException(message, e);
        }
    }

    // Properties
    //-------------------------------------------------------------------------
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
     * Sets whether or not failures should be ignored (and just logged) or thrown as
     * runtime exceptions into the JMS provider
     */
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures = ignoreFailures;
    }


    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Send the given RemoteInvocationResult as a JMS message to the originator
     *
     * @param message current HTTP message
     * @param result  the RemoteInvocationResult object
     * @throws javax.jms.JMSException if thrown by trying to send the message
     */
    protected abstract void writeRemoteInvocationResult(Message message, RemoteInvocationResult result) throws JMSException;

    /**
     * Creates the invocation result response message
     *
     * @param session the JMS session to use
     * @param message the original request message, in case we want to attach any properties etc.
     * @param result  the invocation result
     * @return the message response to send
     * @throws javax.jms.JMSException if creating the messsage failed
     */
    protected Message createResponseMessage(Session session, Message message, RemoteInvocationResult result) throws JMSException {
        // an alternative strategy could be to use XStream and text messages
        // though some JMS providers, like ActiveMQ, might do this kind of thing for us under the covers
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
        factory.setCorrelationID((String) argument);
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
}
