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

import junit.framework.TestCase;
import org.activemq.ActiveMQConnectionFactory;
import org.agilasoft.lingo.jms.impl.DefaultJmsProducer;
import org.agilasoft.lingo.jms.impl.SingleThreadedRequestor;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * A useful base class for any JMS related test cases
 *
 * @version $Revision$
 */
public abstract class JmsTestSupport extends TestCase {
    protected ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
    protected Connection connection;

    protected JmsProducer createJmsProducer() throws JMSException {
        return DefaultJmsProducer.newInstance(connectionFactory);
    }

    protected Session createSession() throws JMSException {
        return getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    protected Connection getConnection() throws JMSException {
        if (connection == null) {
            connection = connectionFactory.createConnection();
            connection.start();
        }
        return connection;
    }

    protected Requestor createRequestor(String name) throws Exception {
        Session session = createSession();
        JmsProducer producer = createJmsProducer();
        return new SingleThreadedRequestor(session, producer, session.createQueue(name));
    }

    protected void closeSession(JmsProxyFactoryBean factoryBean) throws JMSException {
        Requestor requestor = factoryBean.getRequestor();
        requestor.getSession().close();
    }

    protected String getDestinationName() {
        return "test." + getClass().getName() + "." + getName();
    }
}
