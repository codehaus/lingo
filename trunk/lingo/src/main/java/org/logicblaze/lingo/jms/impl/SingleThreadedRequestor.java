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
package org.logicblaze.lingo.jms.impl;

import org.logicblaze.lingo.jms.JmsProducerConfig;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

/**
 * A simple {@link org.logicblaze.lingo.jms.Requestor} which can only be used by
 * one thread at once and only used for one message exchange at once.
 * 
 * @version $Revision$
 */
public class SingleThreadedRequestor extends OneWayRequestor {
    private Destination inboundDestination;
    private MessageConsumer receiver;
    private boolean deleteTemporaryDestinationsOnClose;

    public SingleThreadedRequestor(JmsProducerConfig config, Destination serverDestination, Destination clientDestination) throws JMSException {
        super(config, serverDestination);
        this.inboundDestination = clientDestination;
    }

    public SingleThreadedRequestor(Connection connection, Session session, MessageProducer producer, Destination serverDestination,
            Destination clientDestination, boolean ownsConnection) throws JMSException {
        super(connection, session, producer, serverDestination, ownsConnection);
        this.inboundDestination = clientDestination;
    }
    
    public SingleThreadedRequestor(Connection connection, Session session, MessageProducer producer, Destination serverDestination, boolean ownsConnection) throws JMSException {
        this(connection, session, producer, serverDestination, createTemporaryDestination(session), ownsConnection);
    }

    public Message request(Destination destination, Message message) throws JMSException {
        populateHeaders(message);
        send(destination, message);
        long timeout = getTimeToLive();
        return receive(timeout);
    }

    public Message request(Destination destination, Message message, long timeout) throws JMSException {
        populateHeaders(message);
        send(destination, message, timeout);
        return receive(timeout);
    }

    public Message receive(long timeout) throws JMSException {
        if (timeout < 0) {
            return getReceiver().receive();
        }
        else if (timeout == 0) {
            return getReceiver().receiveNoWait();
        }
        return getReceiver().receive(timeout);
    }

    public synchronized void close() throws JMSException {
        // producer and consumer created by constructor are implicitly closed.
        super.close();

        if (deleteTemporaryDestinationsOnClose) {
            if (inboundDestination instanceof TemporaryQueue) {
                ((TemporaryQueue) inboundDestination).delete();
            }
            else if (inboundDestination instanceof TemporaryTopic) {
                ((TemporaryTopic) inboundDestination).delete();
            }
        }
        inboundDestination = null;
    }

    // Properties
    // -------------------------------------------------------------------------
    public boolean isDeleteTemporaryDestinationsOnClose() {
        return deleteTemporaryDestinationsOnClose;
    }

    public void setDeleteTemporaryDestinationsOnClose(boolean deleteTemporaryDestinationsOnClose) {
        this.deleteTemporaryDestinationsOnClose = deleteTemporaryDestinationsOnClose;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected static TemporaryQueue createTemporaryDestination(Session session) throws JMSException {
        return session.createTemporaryQueue();
    }

    protected void populateHeaders(Message message) throws JMSException {
        message.setJMSReplyTo(inboundDestination);
    }

    protected MessageConsumer getReceiver() throws JMSException {
        if (receiver == null) {
            if (inboundDestination == null) {
                inboundDestination = createTemporaryDestination(getSession());
            }
            receiver = getSession().createConsumer(inboundDestination);
        }
        return receiver;
    }

    public Destination getInboundDestination() throws JMSException {
        return inboundDestination;
    }

}
