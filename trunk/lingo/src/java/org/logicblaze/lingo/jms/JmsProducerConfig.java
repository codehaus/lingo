/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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
package org.logicblaze.lingo.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;

/**
 * A simple bean of JMS producer configuration options.
 * 
 * @version $Revision$
 */
public class JmsProducerConfig {

    private String clientID;
    private int deliveryMode;
    private boolean disableMessageID;
    private boolean disableMessageTimestamp;
    private int priority = -1;
    private int timeToLive = 30000;

    public void configure(MessageProducer producer) throws JMSException {
        if (deliveryMode > 0) {
            producer.setDeliveryMode(deliveryMode);
        }
        producer.setDisableMessageID(disableMessageID);
        producer.setDisableMessageTimestamp(disableMessageTimestamp);
        if (priority >= 0) {
            producer.setPriority(priority);
        }
        if (timeToLive >= 0) {
            producer.setTimeToLive(timeToLive);
        }
    }

    /**
     * Creates a new JMS connection and starts it
     * @throws JMSException 
     */
    public Connection createConnection(ConnectionFactory factory) throws JMSException {
        Connection connection = factory.createConnection();

        if (clientID != null) {
            connection.setClientID(clientID);
        }

        // lets start the connection in case that we consume on the same
        // connection
        connection.start();

        return connection;
    }

    // Properties
    // -------------------------------------------------------------------------
    public int getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(int deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public boolean isDisableMessageID() {
        return disableMessageID;
    }

    public void setDisableMessageID(boolean disableMessageID) {
        this.disableMessageID = disableMessageID;
    }

    public boolean isDisableMessageTimestamp() {
        return disableMessageTimestamp;
    }

    public void setDisableMessageTimestamp(boolean disableMessageTimestamp) {
        this.disableMessageTimestamp = disableMessageTimestamp;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    public String getClientID() {
        return clientID;
    }

    /**
     * Sets the JMS connections unique clientID. This is optional unless you
     * wish to use durable topic subscriptions. Only one connection can have a
     * given clientID at any time.
     */
    public void setClientID(String clientID) {
        this.clientID = clientID;
    }
}
