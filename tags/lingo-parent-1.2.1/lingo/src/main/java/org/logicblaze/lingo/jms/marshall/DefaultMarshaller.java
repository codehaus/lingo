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
package org.logicblaze.lingo.jms.marshall;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.LingoInvocation;
import org.logicblaze.lingo.jms.Requestor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import java.io.Serializable;

/**
 * Represents the strategy of object marshalling of requests and responses in and out
 * of JMS messages. Collaborates with {@link HeaderMarshaller} which captures the strategy
 * for JMS header marshalling.
 * 
 * @version $Revision: 84 $
 */
public class DefaultMarshaller implements Marshaller, HeaderMarshaller {

    private static final Log log = LogFactory.getLog(DefaultMarshaller.class);

    private boolean ignoreInvalidMessages;

    //required lingo header marshaller
    protected HeaderMarshaller nativeHeaderMarshaller = new NativeHeaderMarshaller();

    //user customizable HeaderMarshaller
    protected HeaderMarshaller headerMarshaller;


    public void setHeaderMarshaller(HeaderMarshaller headerMarshaller) {
        this.headerMarshaller = headerMarshaller;
    }

    public Message createRequestMessage(Requestor requestor, LingoInvocation invocation) throws JMSException {
        ObjectMessage message = requestor.getSession().createObjectMessage(invocation);
        appendMessageHeaders(message, requestor, invocation);
        return message;
    }

    public Message createResponseMessage(Session session, RemoteInvocationResult result, Message requestMessage) throws JMSException {
        ObjectMessage answer = session.createObjectMessage(result);
        addResponseMessageHeaders(answer, result, requestMessage);
        return answer;
    }

    public RemoteInvocationResult extractInvocationResult(Message message) throws JMSException {
        handleInvocationResultHeaders(message);
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Object body = objectMessage.getObject();
            if (body instanceof RemoteInvocationResult) {
                return (RemoteInvocationResult) body;
            }
        }
        return onInvalidClientMessage(message);
    }

    public RemoteInvocation readRemoteInvocation(Message message) throws JMSException {
        handleInvocationHeaders(message);
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Object body = objectMessage.getObject();
            if (body instanceof RemoteInvocation) {
                return (RemoteInvocation) body;
            }
        }
        return onInvalidMessage(message);
    }

    public Message createObjectMessage(Session session, Object value) throws JMSException {
        Message message = null;
        if (value instanceof String) {
            message = session.createTextMessage((String) value);
        }
        else {
            message = session.createObjectMessage((Serializable) value);
        }
        appendMessageHeaders(message, session, value);
        return message;
    }

    public Object readMessage(Message message) throws JMSException {
        handleMessageHeaders(message);
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            return objectMessage.getObject();
        }
        else if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            return textMessage.getText();
        }
        return onInvalidMessage(message);
    }

    // Properties
    // -------------------------------------------------------------------------
    public boolean isIgnoreInvalidMessages() {
        return ignoreInvalidMessages;
    }

    /**
     * Sets whether invalidly formatted messages should be silently ignored or
     * not
     */
    public void setIgnoreInvalidMessages(boolean ignoreInvalidMessages) {
        this.ignoreInvalidMessages = ignoreInvalidMessages;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected RemoteInvocationResult onInvalidClientMessage(Message message) throws JMSException {
        throw new JMSException("Invalid response message: " + message);
    }

    /**
     * Handle invalid messages by just logging, though a different
     * implementation may wish to throw exceptions
     */
    protected RemoteInvocation onInvalidMessage(Message message) {
        String text = "Invalid message will be discarded: " + message;
        log.info(text);
        if (!ignoreInvalidMessages) {
            throw new RuntimeException(text);
        }
        return null;
    }


    public void appendMessageHeaders(Message message, Requestor requestor, LingoInvocation invocation) throws JMSException {
        nativeHeaderMarshaller.appendMessageHeaders(message, requestor, invocation);
        if(headerMarshaller != null) headerMarshaller.appendMessageHeaders(message, requestor, invocation);
    }

    public void appendMessageHeaders(Message message, Session session, Object value) {
        nativeHeaderMarshaller.appendMessageHeaders(message, session, value);
        if(headerMarshaller != null) headerMarshaller.appendMessageHeaders(message, session, value);
    }

    public void addResponseMessageHeaders(ObjectMessage answer, RemoteInvocationResult result, Message requestMessage) throws JMSException {
        nativeHeaderMarshaller.addResponseMessageHeaders(answer, result, requestMessage);
        if(headerMarshaller != null) headerMarshaller.addResponseMessageHeaders(answer, result, requestMessage);
    }

    public void handleInvocationHeaders(Message message) {
        nativeHeaderMarshaller.handleInvocationHeaders(message);
        if(headerMarshaller != null) headerMarshaller.handleInvocationHeaders(message);
    }

    public final void handleInvocationResultHeaders(Message message) {
        nativeHeaderMarshaller.handleInvocationResultHeaders(message);
        if(headerMarshaller != null) headerMarshaller.handleInvocationResultHeaders(message);
    }

    public final void handleMessageHeaders(Message message) {
        nativeHeaderMarshaller.handleMessageHeaders(message);
        if(headerMarshaller != null) headerMarshaller.handleMessageHeaders(message);
    }
}
