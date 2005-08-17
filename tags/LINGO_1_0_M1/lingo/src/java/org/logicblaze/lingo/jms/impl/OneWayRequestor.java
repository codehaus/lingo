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
import org.logicblaze.lingo.jms.Requestor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.DeliveryMode;

/**
 * A simple requestor which only supports one-way and so does not need a consumer.
 *
 * @version $Revision$
 */
public class OneWayRequestor implements Requestor {
    private static final Log log = LogFactory.getLog(OneWayRequestor.class);

    private JmsProducer producer;
    private Destination serverDestination;
    private long counter;
    private int deliveryMode = DeliveryMode.NON_PERSISTENT;
    private int priority = 5;
    private long timeToLive = 30000;

    public OneWayRequestor(JmsProducer producer, Destination serverDestination) {
        this.producer = producer;
        this.serverDestination = serverDestination;
    }

    /**
     * The default delivery mode of request messages
     */
    public int getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(int deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    /**
     * The default priority of request messages
     */
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * The default time to live on request messages
     */
    public long getTimeToLive() {
        return timeToLive;
    }

    /**
     * Sets the maximum time to live for requests
     */ 
    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void oneWay(Destination destination, Message message) throws JMSException {
        oneWay(destination, message, timeToLive);
    }

    public void oneWay(Destination destination, Message message, long timeToLive) throws JMSException {
        populateHeaders(message);
        doSend(destination, message, timeToLive);
    }

    public Session getSession() {
        return producer.getSession();
    }

    public void close() throws JMSException {
        producer.close();
    }

    public Message receive(long timeout) throws JMSException {
        throw new JMSException("receive(timeToLive) not implemented for OneWayRequestor");
    }

    public Message request(Destination destination, Message message) throws JMSException {
        throw new JMSException("request() not implemented for OneWayRequestor");
    }

    public Message request(Destination destination, Message message, long timeout) throws JMSException {
        throw new JMSException("request() not implemented for OneWayRequestor");
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
        producer.getMessageProducer().send(destination, message, deliveryMode, priority, timeToLive);
    }

    /**
     * Creates a new correlation ID. Note that because the correlationID is used
     * on a per-temporary destination basis, it does not need to be unique across
     * more than one destination. So a simple counter will suffice.
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
