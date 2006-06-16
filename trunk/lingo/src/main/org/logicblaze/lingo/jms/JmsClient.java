/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.jms.impl.DefaultJmsProducer;
import org.logicblaze.lingo.jms.marshall.DefaultMarshaller;
import org.logicblaze.lingo.jms.marshall.Marshaller;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;

import java.util.Enumeration;

/**
 * A helper class for working with JMS from inside collection classes
 * 
 * @version $Revision$
 */
public class JmsClient {
    private static final Log log = LogFactory.getLog(JmsClient.class);

    private ConnectionFactory connectionFactory;
    private Destination destination;
    private JmsProducer producer;
    private MessageConsumer consumer;
    private Marshaller marshaller = new DefaultMarshaller();
    private JmsProducerConfig config = new JmsProducerConfig();
    private String selector;
    private boolean noLocal;
    private long noWaitTimeout = 500;

    public JmsClient() {
    }

    public JmsClient(ConnectionFactory connectionFactory, Destination destination) {
        this.connectionFactory = connectionFactory;
        this.destination = destination;
    }

    public JmsClient(Destination destination, JmsProducer producer, MessageConsumer consumer) {
        this.destination = destination;
        this.producer = producer;
        this.consumer = consumer;
    }

    public Message receiveNoWait() throws JMSException {
        return getConsumer().receive(noWaitTimeout);
    }

    public Message receive() throws JMSException {
        return getConsumer().receive();
    }

    public Message receive(long timeout, TimeUnit unit) throws JMSException {
        long millis = unit.convert(timeout, TimeUnit.MILLISECONDS);
        return getConsumer().receive(millis);
    }

    public QueueBrowser createBrowser() throws JMSException {
        Destination destination = getDestination();
        if (destination instanceof Queue) {
            return getProducer().getSession().createBrowser((Queue) destination);
        }
        else {
            throw new UnsupportedOperationException("You can only peek() inside a collection based on a Queue: " + destination);
        }
    }

    public Message peek() throws JMSException {
        QueueBrowser browser = createBrowser();
        try {
            Enumeration iter = browser.getEnumeration();
            if (iter.hasMoreElements()) {
                return (Message) iter.nextElement();
            }
            return null;
        }
        finally {
            try {
                browser.close();
            }
            catch (JMSException e) {
                onBrowserCloseException(e);
                return null;
            }
        }
    }

    public void send(Message message) throws JMSException {
        getProducer().send(getDestination(), message);
    }

    public Message createMessage(Object element) throws JMSException {
        return getMarshaller().createObjectMessage(getProducer().getSession(), element);
    }

    public void handleException(JMSException e) {
        throw new RuntimeJMSException(e);
    }

    public Object readMessage(Message message) throws JMSException {
        return getMarshaller().readMessage(message);
    }

    public void close(QueueBrowser browser) {
        try {
            browser.close();
        }
        catch (JMSException e) {
            // ignore exception as we're called from iterators
            log.warn("Could not close queue browser due to: " + e, e);
        }
    }

    public void close() {
        try {
            if (consumer != null) {
                consumer.close();
            }
            if (producer != null) {
                producer.close();
            }
        }
        catch (JMSException e) {
            e.printStackTrace();
        }
        finally {
            consumer = null;
            producer = null;
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public MessageConsumer getConsumer() throws JMSException {
        if (consumer == null) {
            consumer = getProducer().getSession().createConsumer(getDestination(), selector, noLocal);
        }
        return consumer;
    }

    public void setConsumer(MessageConsumer consumer) {
        this.consumer = consumer;
    }

    public Destination getDestination() {
        if (destination == null) {
            throw new IllegalArgumentException("No destination property configured");
        }
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public Marshaller getMarshaller() {
        return marshaller;
    }

    public void setMarshaller(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public JmsProducer getProducer() throws JMSException {
        if (producer == null) {
            if (connectionFactory == null) {
                throw new IllegalArgumentException("No producer or connectionFactory property configured");
            }
            producer = DefaultJmsProducer.newInstance(connectionFactory, config);
        }
        return producer;
    }

    public void setProducer(JmsProducer producer) {
        this.producer = producer;
    }

    public JmsProducerConfig getConfig() {
        return config;
    }

    public void setConfig(JmsProducerConfig config) {
        this.config = config;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public boolean isNoLocal() {
        return noLocal;
    }

    /**
     * Sets if messages sent by this process should be visible to this JVM
     */
    public void setNoLocal(boolean noLocal) {
        this.noLocal = noLocal;
    }

    public String getSelector() {
        return selector;
    }

    /**
     * Sets the JMS message selector to filter out messages from the consumer
     */
    public void setSelector(String selector) {
        this.selector = selector;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected void onBrowserCloseException(JMSException e) {
        log.warn("Failed to close Queue Browser: " + e, e);
    }
}
