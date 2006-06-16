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
import org.logicblaze.lingo.jms.JmsProducerConfig;
import org.logicblaze.lingo.jms.ReplyHandler;
import org.logicblaze.lingo.jms.Requestor;
import org.springframework.beans.factory.DisposableBean;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * A simple requestor which only supports one-way and so does not need a
 * consumer.
 * 
 * @version $Revision$
 */
public class OneWayRequestor implements Requestor, DisposableBean {
    private static final Log log = LogFactory.getLog(OneWayRequestor.class);

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private boolean ownsConnection = true;
    private Destination serverDestination;
    private long counter;

    public static OneWayRequestor newInstance(Connection connection, JmsProducerConfig config, boolean ownsConnection) throws JMSException {
        Session session = config.createSession(connection);
        MessageProducer producer = config.createMessageProducer(session);
        return new OneWayRequestor(connection, session, producer, null, ownsConnection);
    }

    public OneWayRequestor(JmsProducerConfig config, Destination serverDestination) throws JMSException {
        this.serverDestination = serverDestination;
        this.ownsConnection = true;
        this.connection = config.createConnection();
        this.session = config.createSession(connection);
        this.producer = config.createMessageProducer(session);
    }

    public OneWayRequestor(Connection connection, Session session, MessageProducer producer, Destination serverDestination, boolean ownsConnection) {
        this.connection = connection;
        this.session = session;
        this.producer = producer;
        this.serverDestination = serverDestination;
        this.ownsConnection = ownsConnection;
    }

    public void close() throws JMSException {
        if (producer != null) {
            MessageProducer tmp = producer;
            producer = null;
            tmp.close();
        }
        if (session != null) {
            Session tmp = session;
            session = null;
            tmp.close();
        }
        if (connection != null) {
            Connection tmp = connection;
            connection = null;
            if (ownsConnection) {
                tmp.close();
            }
        }
    }

    public void destroy() throws Exception {
        close();
    }

    public void send(Destination destination, Message message) throws JMSException {
        send(destination, message, getTimeToLive());
    }

    public void send(Destination destination, Message message, long timeToLive) throws JMSException {
        doSend(destination, message, timeToLive);
    }

    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        doSend(destination, message, deliveryMode, priority, timeToLive);
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

    // Properties
    // -------------------------------------------------------------------------
    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }

    public MessageProducer getMessageProducer() {
        return producer;
    }

    public int getDeliveryMode() throws JMSException {
        return getMessageProducer().getDeliveryMode();
    }

    /**
     * Sets the default delivery mode of request messages
     * 
     * @throws JMSException
     */
    public void setDeliveryMode(int deliveryMode) throws JMSException {
        getMessageProducer().setDeliveryMode(deliveryMode);
    }

    public int getPriority() throws JMSException {
        return getMessageProducer().getPriority();
    }

    /**
     * Sets the default priority of request messages
     * 
     * @throws JMSException
     */
    public void setPriority(int priority) throws JMSException {
        getMessageProducer().setPriority(priority);
    }

    /**
     * The default time to live on request messages
     * 
     * @throws JMSException
     */
    public long getTimeToLive() throws JMSException {
        return getMessageProducer().getTimeToLive();
    }

    /**
     * Sets the maximum time to live for requests
     * 
     * @throws JMSException
     */
    public void setTimeToLive(long timeToLive) throws JMSException {
        getMessageProducer().setTimeToLive(timeToLive);
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * A hook to allow custom implementations to process headers differently.
     */
    protected void populateHeaders(Message message) throws JMSException {
    }

    protected void doSend(Destination destination, Message message, long timeToLive) throws JMSException {
        destination = validateDestination(destination);
        if (log.isDebugEnabled()) {
            log.debug("Sending message to: " + destination + " message: " + message);
        }
        getMessageProducer().send(destination, message, getDeliveryMode(), getPriority(), timeToLive);
    }

    protected void doSend(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        destination = validateDestination(destination);
        if (log.isDebugEnabled()) {
            log.debug("Sending message to: " + destination + " message: " + message);
        }
        getMessageProducer().send(destination, message, deliveryMode, priority, timeToLive);
    }

    protected Destination validateDestination(Destination destination) {
        if (destination == null) {
            destination = serverDestination;
        }
        return destination;
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
