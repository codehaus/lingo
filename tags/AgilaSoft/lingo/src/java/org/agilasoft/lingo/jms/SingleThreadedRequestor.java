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

import org.agilasoft.lingo.jms.Requestor;

import javax.jms.*;

/**
 * A simple {@link Requestor} which can only be used by one thread at once
 * and only used for one message exchange at once.
 *
 * @version $Revision$
 */
public class SingleThreadedRequestor implements Requestor {
    private Session session;
    private Destination destination;
    private Destination temporaryDestination;
    private JmsProducer producer;
    private MessageConsumer receiver;
    private long maximumTimeout = 20000L;

    public SingleThreadedRequestor(Session session, JmsProducer producer, Destination destination) throws JMSException {
        this.session = session;
        this.producer = producer;
        this.destination = destination;
        temporaryDestination = createTemporaryDestination(session);
        receiver = session.createConsumer(temporaryDestination);
    }

    public void oneWay(Message message) throws JMSException {
        populateHeaders(message);
        doSend(destination, message);
    }

    public Message request(Message message) throws JMSException {
        oneWay(message);
        long timeout = getMaximumTimeout();
        return receive(timeout);
    }

    public Message request(Message message, long timeout) throws JMSException {
        oneWay(message);
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

    public Session getSession() {
        return session;
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
        producer.close();
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

    protected TemporaryQueue createTemporaryDestination(Session session) throws JMSException {
        return session.createTemporaryQueue();
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void populateHeaders(Message message) throws JMSException {
        message.setJMSReplyTo(temporaryDestination);
    }

    protected void doSend(Destination destination, Message message) throws JMSException {
        producer.getMessageProducer().send(destination, message);
    }

    protected MessageConsumer getReceiver() {
        return receiver;
    }
}
