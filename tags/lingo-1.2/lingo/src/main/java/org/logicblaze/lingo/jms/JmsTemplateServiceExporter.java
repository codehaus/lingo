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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * A JMS MessageListener that exports the specified service bean as a JMS
 * service endpoint, accessible via a JMS proxy. <p/>
 * <p>
 * Note: JMS services exported with this class can be accessed by any JMS
 * client, as there isn't any special handling involved.
 * 
 * @author James Strachan
 * @see JmsProxyFactoryBean
 */
public class JmsTemplateServiceExporter extends JmsServiceExporterMessageListener implements InitializingBean {
    private JmsTemplate template;

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        if (template == null) {
            throw new IllegalArgumentException("template is required");
        }
    }

    public JmsTemplate getTemplate() {
        return template;
    }

    /**
     * Sets the JMS template used to send replies back for the request
     * 
     * @param template
     *            the JMS template to use
     */
    public void setTemplate(JmsTemplate template) {
        this.template = template;
    }

    /**
     * Send the given RemoteInvocationResult as a JMS message to the originator
     * 
     * @param message
     *            current HTTP message
     * @param result
     *            the RemoteInvocationResult object
     * @throws javax.jms.JMSException
     *             if thrown by trying to send the message
     */
    protected void writeRemoteInvocationResult(final Message message, final RemoteInvocationResult result)
            throws JMSException {
        template.send(message.getJMSReplyTo(), new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                return createResponseMessage(session, message, result);
            }
        });
    }

}
