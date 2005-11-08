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
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * A default implementation of the {@link org.logicblaze.lingo.jms.JmsProducer} which contains a reference to the
 * connection, session and producer so that it can easily close down all its resources properly.
 *
 * @version $Revision$
 */
public class DefaultJmsProducer extends JmsProducerImpl {

    private Connection connection;

    public static DefaultJmsProducer newInstance(ConnectionFactory factory, JmsProducerConfig config) throws JMSException {
        Connection connection = factory.createConnection();

        // lets start the connection in case that we consume on the same connection
        connection.start();

        return newInstance(connection, config);
    }

    public static DefaultJmsProducer newInstance(Connection connection, JmsProducerConfig config) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return new DefaultJmsProducer(connection, session, config);
    }

    public DefaultJmsProducer(Connection connection, Session session, JmsProducerConfig config) throws JMSException {
        super(session, config);
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() throws JMSException {
        super.close();
        if (connection != null) {
            Connection tmp = connection;
            connection = null;
            tmp.close();
        }
    }
}
