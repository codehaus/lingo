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
package org.agilasoft.lingo.jms.impl;

import org.agilasoft.lingo.jms.JmsProducer;
import org.agilasoft.lingo.jms.Requestor;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;

/**
 * A simple {@link org.agilasoft.lingo.jms.Requestor} which can only be used by one thread at once
 * and only used for one message exchange at once.
 *
 * @version $Revision$
 */
public class SingleThreadedRequestor extends OneWayRequestor {
    private Connection connection;
    private Session session;
    private Destination temporaryDestination;
    private MessageConsumer receiver;
    private long maximumTimeout = 20000L;


    public static Requestor newInstance(ConnectionFactory connectionFactory, Destination serverDestination) throws JMSException {
        JmsProducer producer = DefaultJmsProducer.newInstance(connectionFactory);
        return new SingleThreadedRequestor(producer.getSession(), producer, serverDestination);
    }

    public SingleThreadedRequestor(Session session, JmsProducer producer, Destination serverDestination) throws JMSException {
        super(producer, serverDestination);
        this.session = session;
        temporaryDestination = createTemporaryDestination(session);
        receiver = session.createConsumer(temporaryDestination);
    }

    public Message request(Destination destination, Message message) throws JMSException {
        oneWay(destination, message);
        long timeout = getMaximumTimeout();
        return receive(timeout);
    }

    public Message request(Destination destination, Message message, long timeout) throws JMSException {
        oneWay(destination, message);
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

    public void close() throws JMSException {
        // producer and consumer created by constructor are implicitly closed.
        session.close();
        if (temporaryDestination instanceof TemporaryQueue) {
            ((TemporaryQueue) temporaryDestination).delete();
        }
        else if (temporaryDestination instanceof TemporaryTopic) {
            ((TemporaryTopic) temporaryDestination).delete();
        }
        super.close();

        if (connection != null) {
            connection.close();
        }
        connection = null;
        session = null;
        temporaryDestination = null;
    }

    public long getMaximumTimeout() {
        return maximumTimeout;
    }

    /**
     * Sets the maximum default timeout used for remote requests. If set to <= 0 then
     * the timeout is ignored.
     *
     * @param maximumTimeout
     */
    public void setMaximumTimeout(long maximumTimeout) {
        this.maximumTimeout = maximumTimeout;
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected TemporaryQueue createTemporaryDestination(Session session) throws JMSException {
        return session.createTemporaryQueue();
    }

    protected void populateHeaders(Message message) throws JMSException {
        message.setJMSReplyTo(temporaryDestination);
    }

    protected MessageConsumer getReceiver() {
        return receiver;
    }

}
