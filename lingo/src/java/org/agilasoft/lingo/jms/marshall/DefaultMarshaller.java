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
package org.agilasoft.lingo.jms.marshall;

import org.agilasoft.lingo.MethodMetadata;
import org.agilasoft.lingo.jms.Requestor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

/**
 * Represents the strategy of marshalling of requests and responses in and out of JMS messages
 *
 * @version $Revision$
 */
public class DefaultMarshaller implements Marshaller {

    private static final Log log = LogFactory.getLog(DefaultMarshaller.class);

    private boolean ignoreInvalidMessages;

    public Message createRequestMessage(Requestor requestor, RemoteInvocation invocation, MethodMetadata metadata) throws JMSException {
        return requestor.getSession().createObjectMessage(invocation);
    }

    public RemoteInvocationResult extractInvocationResult(Message message) throws JMSException {
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
        if (message instanceof ObjectMessage) {
            ObjectMessage objectMessage = (ObjectMessage) message;
            Object body = objectMessage.getObject();
            if (body instanceof RemoteInvocation) {
                return (RemoteInvocation) body;
            }
        }
        return onInvalidMessage(message);
    }


    // Properties
    //-------------------------------------------------------------------------
    public boolean isIgnoreInvalidMessages() {
        return ignoreInvalidMessages;
    }

    /**
     * Sets whether invalidly formatted messages should be silently ignored or not
     */
    public void setIgnoreInvalidMessages(boolean ignoreInvalidMessages) {
        this.ignoreInvalidMessages = ignoreInvalidMessages;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected RemoteInvocationResult onInvalidClientMessage(Message message) throws JMSException {
        throw new JMSException("Invalid response message: " + message);
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
}
