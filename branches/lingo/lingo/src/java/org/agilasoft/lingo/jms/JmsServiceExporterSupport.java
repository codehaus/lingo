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

import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.beans.factory.InitializingBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.MessageListener;
import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 * @version $Revision$
 */
public abstract class JmsServiceExporterSupport extends RemoteInvocationBasedExporter implements MessageListener, InitializingBean {
    private static final Log log = LogFactory.getLog(JmsServiceExporterSupport.class);

    protected Object proxy;
    private boolean ignoreFailures;
    private boolean ignoreInvalidMessages;

    public void afterPropertiesSet() {
        this.proxy = getProxyForService();
        if (proxy == null) {
            throw new IllegalArgumentException("proxy is required");
        }
    }

    public void onMessage(Message message) {
        try {
            RemoteInvocation invocation = readRemoteInvocation(message);
            if (invocation != null) {
                RemoteInvocationResult result = invokeAndCreateResult(invocation, this.proxy);
                writeRemoteInvocationResult(message, result);
            }
        }
        catch (JMSException e) {
            onException(message, e);
        }
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

    public boolean isIgnoreInvalidMessages() {
        return ignoreInvalidMessages;
    }

    /**
     * Sets whether invalidly formatted messages should be silently ignored or not
     */
    public void setIgnoreInvalidMessages(boolean ignoreInvalidMessages) {
        this.ignoreInvalidMessages = ignoreInvalidMessages;
    }

    /**
     * Read a RemoteInvocation from the given JMS message
     *
     * @param message current JMS message
     * @return the RemoteInvocation object
     */
    protected RemoteInvocation readRemoteInvocation(Message message) throws JMSException {
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Object body = objectMessage.getObject();
            if (body instanceof RemoteInvocation) {
                return (RemoteInvocation) body;
            }
        }
        return onInvalidMessage(message);
    }

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
        ObjectMessage answer = session.createObjectMessage(result);

        // lets preserve the correlation ID
        answer.setJMSCorrelationID(message.getJMSCorrelationID());
        return answer;
    }

    /**
     * Handle invalid messages by just logging, though a different implementation
     * may wish to throw exceptions
     */
    protected RemoteInvocation onInvalidMessage(Message message) {
        String text = "Invalid message will be discarded: " + message;
        log.info(text);
        if (!ignoreInvalidMessages) {
            throw new RuntimeException(text);
        }
        return null;
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
