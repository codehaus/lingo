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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * A default implementation of the {@link org.agilasoft.lingo.jms.JmsProducer} which contains a reference to the
 * connection, session and producer so that it can easily close down all its resources properly.
 *
 * @version $Revision$
 */
public class DefaultJmsProducer extends JmsProducerImpl {

    private Connection connection;

    public static JmsProducer newInstance(ConnectionFactory factory) throws JMSException {
        return newInstance(factory.createConnection());
    }

    public static JmsProducer newInstance(Connection connection) throws JMSException {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(null);
        return new DefaultJmsProducer(connection, session, producer);
    }

    public DefaultJmsProducer(Connection connection, Session session, MessageProducer producer) {
        super(session, producer);
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
