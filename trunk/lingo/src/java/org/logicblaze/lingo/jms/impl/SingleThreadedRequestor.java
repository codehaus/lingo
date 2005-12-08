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

import org.logicblaze.lingo.jms.JmsProducer;
import org.logicblaze.lingo.jms.JmsProducerConfig;
import org.logicblaze.lingo.jms.Requestor;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
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
    private Session session;
    private Destination inboundDestination;
    private MessageConsumer receiver;
    private boolean deleteTemporaryDestinationsOnClose;

    public static Requestor newInstance(ConnectionFactory connectionFactory, JmsProducerConfig config, Destination serverDestination) throws JMSException {
        JmsProducer producer = DefaultJmsProducer.newInstance(connectionFactory, config);
        return new SingleThreadedRequestor(producer.getSession(), producer, serverDestination);
    }

    public SingleThreadedRequestor(Session session, JmsProducer producer, Destination serverDestination, Destination clientDestination) throws JMSException {
        super(producer, serverDestination);
        this.session = session;
        this.inboundDestination = clientDestination;
        if (inboundDestination == null) {
            inboundDestination = createTemporaryDestination(session);
        }
        receiver = session.createConsumer(inboundDestination);
    }

    public SingleThreadedRequestor(Session session, JmsProducer producer, Destination serverDestination) throws JMSException {
        this(session, producer, serverDestination, null);
    }

    public Message request(Destination destination, Message message) throws JMSException {
        oneWay(destination, message);
        long timeout = getTimeToLive();
        return receive(timeout);
    }

    public Message request(Destination destination, Message message, long timeout) throws JMSException {
        oneWay(destination, message, timeout);
        return receive(timeout);
    }

    public Message receive(long timeout) throws JMSException {
        if (timeout < 0) {
            return receiver.receive();
        }
        else if (timeout == 0) {
            return receiver.receiveNoWait();
        }
        return receiver.receive(timeout);
    }

    public synchronized void close() throws JMSException {
        // producer and consumer created by constructor are implicitly closed.
        session.close();
        super.close();

        if (deleteTemporaryDestinationsOnClose) {
            if (inboundDestination instanceof TemporaryQueue) {
                ((TemporaryQueue) inboundDestination).delete();
            }
            else if (inboundDestination instanceof TemporaryTopic) {
                ((TemporaryTopic) inboundDestination).delete();
            }
        }

        session = null;
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
    protected TemporaryQueue createTemporaryDestination(Session session) throws JMSException {
        return session.createTemporaryQueue();
    }

    protected void populateHeaders(Message message) throws JMSException {
        message.setJMSReplyTo(inboundDestination);
    }

    protected MessageConsumer getReceiver() {
        return receiver;
    }

}
