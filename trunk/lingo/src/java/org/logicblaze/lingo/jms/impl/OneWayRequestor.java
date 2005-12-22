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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.jms.JmsProducer;
import org.logicblaze.lingo.jms.ReplyHandler;
import org.logicblaze.lingo.jms.Requestor;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * A simple requestor which only supports one-way and so does not need a
 * consumer.
 * 
 * @version $Revision$
 */
public class OneWayRequestor implements Requestor {
    private static final Log log = LogFactory.getLog(OneWayRequestor.class);

    private JmsProducer producer;
    private Destination serverDestination;
    private long counter;

    public OneWayRequestor(JmsProducer producer, Destination serverDestination) {
        this.producer = producer;
        this.serverDestination = serverDestination;
    }

    public int getDeliveryMode() throws JMSException {
        return producer.getMessageProducer().getDeliveryMode();
    }

    /**
     * Sets the default delivery mode of request messages
     * 
     * @throws JMSException
     */
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        producer.getMessageProducer().setDeliveryMode(deliveryMode);
    }

    public int getPriority() throws JMSException {
        return producer.getMessageProducer().getPriority();
    }

    /**
     * Sets the default priority of request messages
     * 
     * @throws JMSException
     */
    public void setPriority(int priority) throws JMSException {
        producer.getMessageProducer().setPriority(priority);
    }

    /**
     * The default time to live on request messages
     * 
     * @throws JMSException
     */
    public long getTimeToLive() throws JMSException {
        return producer.getMessageProducer().getTimeToLive();
    }

    /**
     * Sets the maximum time to live for requests
     * 
     * @throws JMSException
     */
    public void setTimeToLive(long timeToLive) throws JMSException {
        producer.getMessageProducer().setTimeToLive(timeToLive);
    }

    public void oneWay(Destination destination, Message message) throws JMSException {
        oneWay(destination, message, getTimeToLive());
    }

    public void oneWay(Destination destination, Message message, long timeToLive) throws JMSException {
        populateHeaders(message);
        doSend(destination, message, timeToLive);
    }

    public Connection getConnection() {
        return producer.getConnection();
    }

    public Session getSession() {
        return producer.getSession();
    }

    public void close() throws JMSException {
        producer.close();
    }

    public Message receive(long timeout) throws JMSException {
        throw new JMSException("receive(long) not implemented for OneWayRequestor");
    }

    public Message request(Destination destination, Message message) throws JMSException {
        throw new JMSException("request(Destination, Message) not implemented for OneWayRequestor");
    }

    public Message request(Destination destination, Message message, long timeout) throws JMSException {
        throw new JMSException("request(Destination, Message, long) not implemented for OneWayRequestor");
    }

    public void request(Destination destination, Message requestMessage, ReplyHandler handler, long timeout) throws JMSException {
        throw new JMSException("request(Destination, Message, ReplyHandler, long) not implemented for OneWayRequestor");
    }

    protected void populateHeaders(Message message) throws JMSException {
    }

    protected void doSend(Destination destination, Message message, long timeToLive) throws JMSException {
        if (destination == null) {
            destination = serverDestination;
        }
        if (log.isDebugEnabled()) {
            log.debug("Sending message to: " + destination + " message: " + message);
        }
        producer.send(destination, message, getDeliveryMode(), getPriority(), timeToLive);
    }

    /**
     * Creates a new correlation ID. Note that because the correlationID is used
     * on a per-temporary destination basis, it does not need to be unique
     * across more than one destination. So a simple counter will suffice.
     * 
     * @return
     */
    public String createCorrelationID() {
        return Long.toString(nextCounter());
    }

    protected synchronized long nextCounter() {
        return ++counter;
    }
}
