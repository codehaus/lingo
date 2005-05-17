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
package org.logicblaze.lingo.jms;

import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * A regular JMS message listener which can be used from inside a message driven object
 * container.
 *
 * @version $Revision$
 */
public class JmsServiceExporterMessageListener extends JmsServiceExporterSupport {
    private JmsProducer producer;

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (producer == null) {
            throw new IllegalArgumentException("producer is required");
        }
    }

    public JmsProducer getProducer() {
        return producer;
    }

    public void setProducer(JmsProducer producer) {
        this.producer = producer;
    }

    protected void writeRemoteInvocationResult(Message message, RemoteInvocationResult result) throws JMSException {
        Message responseMessage = createResponseMessage(producer.getSession(), message, result);
        producer.getMessageProducer().send(message.getJMSReplyTo(), responseMessage);
    }
}
