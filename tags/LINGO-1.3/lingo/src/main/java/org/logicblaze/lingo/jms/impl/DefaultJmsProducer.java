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
import org.springframework.beans.factory.DisposableBean;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * An implementation of the {@link org.logicblaze.lingo.jms.JmsProducer} which
 * contains a reference to the connection, session and producer so that it can
 * easily close down all its resources properly. The connection may be owned by
 * another object and so it may not be automatically closed.
 * 
 * @version $Revision$
 */
public class DefaultJmsProducer implements JmsProducer, DisposableBean {

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private boolean ownsConnection = true;

    public static DefaultJmsProducer newInstance(ConnectionFactory factory, JmsProducerConfig config) throws JMSException {
        Connection connection = config.createConnection(factory);
        return newInstance(connection, config, true);
    }

    public static DefaultJmsProducer newInstance(Connection connection, JmsProducerConfig config, boolean ownsConnection) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return new DefaultJmsProducer(connection, session, config, ownsConnection);
    }

    public DefaultJmsProducer(Connection connection, Session session, MessageProducer producer, boolean ownsConnection) throws JMSException {
        this.connection = connection;
        this.session = session;
        this.producer = producer;
        this.ownsConnection = ownsConnection;
    }

    public DefaultJmsProducer(Connection connection, Session session, JmsProducerConfig config, boolean ownsConnection) throws JMSException {
        this.connection = connection;
        this.session = session;
        this.ownsConnection = ownsConnection;
        this.producer = session.createProducer(null);
        config.configure(producer);
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
    }

    public MessageProducer getMessageProducer() {
        return producer;
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
        getMessageProducer().send(destination, message);
    }

    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException {
        getMessageProducer().send(destination, message, deliveryMode, priority, timeToLive);
    }
}
