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

import org.springframework.beans.factory.DisposableBean;

import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.JMSException;

/**
 * An implementation of the {@link JmsProducer} which is designed to work in
 * message driven POJO type scenarios where the session and producer can be deduced
 * from the current consumption thread & the session can be reused from the consumer.
 *
 * @version $Revision$
 */
public class JmsProducerImpl implements JmsProducer, DisposableBean {
    private Session session;
    private MessageProducer producer;

    public JmsProducerImpl(Session session) throws JMSException {
        this.session = session;
        this.producer = session.createProducer(null);
    }

    public JmsProducerImpl(Session session, MessageProducer producer) {
        this.session = session;
        this.producer = producer;
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
    }

    public void destroy() throws Exception {
        close();
    }
}
