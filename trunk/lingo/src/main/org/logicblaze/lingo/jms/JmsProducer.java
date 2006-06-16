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
package org.logicblaze.lingo.jms;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * A simple abstraction layer for applications wishing to send JMS messages
 * 
 * @version $Revision$
 */
public interface JmsProducer {

    public Connection getConnection();
    public Session getSession();

    public MessageProducer getMessageProducer();

    public void close() throws JMSException;

    /**
     * Sends a message to the given destination in a way that can be implemented
     * in JMS 1.0.2b as well as using the JMS 1.1 send() method on
     * {@link MessageProducer}
     * 
     * @throws JMSException if the message could not be sent
     */
    public void send(Destination destination, Message message) throws JMSException;

    /**
     * Sends a message to the given destination in a way that can be implemented
     * in JMS 1.0.2b as well as using the JMS 1.1 send() method on
     * {@link MessageProducer}
     * 
     * @throws JMSException if the message could not be sent
     */
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException;
}
