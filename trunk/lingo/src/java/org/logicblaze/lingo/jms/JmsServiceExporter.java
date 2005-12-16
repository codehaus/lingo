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

import org.logicblaze.lingo.jms.impl.DefaultJmsProducer;
import org.logicblaze.lingo.jms.impl.MultiplexingRequestor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

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
public class JmsServiceExporter extends JmsServiceExporterMessageListener implements InitializingBean, DisposableBean {
    private Destination destination;
    private MessageConsumer consumer;
    private String messageSelector;
    private String subscriberName;
    private boolean noLocal;

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        // do we have a destination specified, if so consume
        if (destination != null) {
            consumer = createConsumer();
            consumer.setMessageListener(this);
        }
    }

    public void destroy() throws Exception {
        if (consumer != null) {
            consumer.close();
        }
        super.destroy();
    }

    public Destination getDestination() {
        return destination;
    }

    /**
     * If specified then the service will be auto-subscribed to this destination
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getMessageSelector() {
        return messageSelector;
    }

    /**
     * Sets the message selector applied to the subscription
     */
    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    public boolean isNoLocal() {
        return noLocal;
    }

    /**
     * Sets whether or not topic subscriptions should receive locally produced
     * messages
     */
    public void setNoLocal(boolean noLocal) {
        this.noLocal = noLocal;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    /**
     * Sets the durable subscriber name and enables a durable subscription.
     */
    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    // Implementation methods
    // -------------------------------------------------------------------------


    /**
     * Factory method to create the consumer
     */
    protected MessageConsumer createConsumer() throws JMSException {
        Session session = getProducer().getSession();
        if (subscriberName != null) {
            if (destination instanceof Topic) {
                Topic topic = (Topic) destination;
                return session.createDurableSubscriber(topic, subscriberName, messageSelector, noLocal);
            }
            else {
                throw new IllegalArgumentException("Cannot specify the subscriberName property when using a Queue destination");
            }

        }
        else {
            return session.createConsumer(destination, messageSelector, noLocal);
        }
    }

}
