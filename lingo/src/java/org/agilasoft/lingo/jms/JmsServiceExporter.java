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

import org.agilasoft.lingo.jms.impl.OneWayRequestor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * A JMS MessageListener that exports the specified service bean as a JMS service
 * endpoint, accessible via a JMS proxy.
 * <p/>
 * <p>Note: JMS services exported with this class can be accessed by
 * any JMS client, as there isn't any special handling involved.
 *
 * @author James Strachan
 * @see JmsProxyFactoryBean
 */
public class JmsServiceExporter extends JmsServiceExporterSupport implements InitializingBean {
    private static final Log log = LogFactory.getLog(JmsServiceExporter.class);

    private JmsProducer producer;

    public void afterPropertiesSet() throws Exception {
        if (producer == null) {
            throw new IllegalArgumentException("template is required");
        }
        Requestor responseRequestor = getResponseRequestor();
        if (responseRequestor == null) {
            setResponseRequestor(new OneWayRequestor(producer, null));
        }
        super.afterPropertiesSet();
    }

    public JmsProducer getProducer() {
        return producer;
    }

    public void setProducer(JmsProducer producer) {
        this.producer = producer;
    }

    /**
     * Send the given RemoteInvocationResult as a JMS message to the originator
     *
     * @param message current HTTP message
     * @param result  the RemoteInvocationResult object
     * @throws javax.jms.JMSException if thrown by trying to send the message
     */
    protected void writeRemoteInvocationResult(final Message message, final RemoteInvocationResult result) throws JMSException {
        Message responseMessage = createResponseMessage(producer.getSession(), message, result);
        producer.getMessageProducer().send(message.getJMSReplyTo(), responseMessage);
    }

}
