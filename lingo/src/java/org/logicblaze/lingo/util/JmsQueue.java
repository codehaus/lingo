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
package org.logicblaze.lingo.util;

import edu.emory.mathcs.backport.java.util.concurrent.BlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueBrowser;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An implementation of the Queue interface which maps to a JMS Destination.
 * 
 * Note that when you have finished using this object you should call the
 * {@link #close()} method to free up any resources.
 * 
 * @version $Revision$
 */
public class JmsQueue extends AbstractCollection implements BlockingQueue {

    private JmsClient jmsClient;

    public JmsQueue(JmsClient jmsClient) {
        this.jmsClient = jmsClient;
    }

    public Iterator iterator() {
        try {
            QueueBrowser browser = getJmsClient().createBrowser();
            Enumeration enumeration = browser.getEnumeration();
            return new JmsQueueIterator(enumeration, browser);
        }
        catch (JMSException e) {
            getJmsClient().handleException(e);
            return Collections.EMPTY_LIST.iterator();
        }
    }

    public boolean isEmpty() {
        try {
            Message message = getJmsClient().peek();
            if (message != null) {
                return false;
            }
        }
        catch (JMSException e) {
            getJmsClient().handleException(e);
        }
        return true;
    }

    /**
     * This method could be quite slow for huge queues as this results in
     * iterating through all of the available objects to count them.
     */
    public int size() {
        int count = 0;
        Iterator iter = iterator();
        while (iter.hasNext()) {
            count++;
        }
        return count;
    }

    /**
     * This method could be quite slow for huge queues as this method may have
     * to iterate through the entire queue
     */
    public boolean contains(Object element) {
        JmsQueueIterator iter = (JmsQueueIterator) iterator();
        try {
            while (iter.hasNext()) {
                Object value = iter.next();
                if (equals(element, value)) {
                    return true;
                }
            }
            return false;
        }
        finally {
            iter.close();
        }
    }

    /**
     * This method could be quite slow for huge queues as this results in
     * iterating through all of the available objects to count them.
     */
    public boolean containsAll(Collection coll) {
        JmsQueueIterator iter = (JmsQueueIterator) iterator();
        Set set = new HashSet(coll);
        try {
            while (iter.hasNext()) {
                Object value = iter.next();
                if (set.remove(value)) {
                    if (set.isEmpty()) {
                        return true;
                    }
                }
            }
            return false;
        }
        finally {
            iter.close();
        }
    }

    public boolean add(Object element) {
        try {
            Message message = getJmsClient().createMessage(element);
            getJmsClient().send(message);
            return true;
        }
        catch (JMSException e) {
            getJmsClient().handleException(e);
            return false;
        }
    }

    public boolean offer(Object element) {
        return add(element);
    }

    public Object remove() {
        try {
            Message message = getJmsClient().receiveNoWait();
            if (message != null) {
                return getJmsClient().readMessage(message);
            }
            else {
                throw new NoSuchElementException();
            }
        }
        catch (JMSException e) {
            getJmsClient().handleException(e);
        }
        return null;
    }

    public Object poll() {
        try {
            Message message = getJmsClient().receiveNoWait();
            if (message != null) {
                return getJmsClient().readMessage(message);
            }
        }
        catch (JMSException e) {
            getJmsClient().handleException(e);
        }
        return null;
    }

    public Object element() {
        return peek();
    }

    public Object peek() {
        try {
            Message message = getJmsClient().peek();
            if (message != null) {
                return getJmsClient().readMessage(message);
            }
        }
        catch (JMSException e) {
            getJmsClient().handleException(e);
        }
        return null;
    }

    public void put(Object element) throws InterruptedException {
        add(element);
    }

    public boolean offer(Object element, long timeout, TimeUnit unit) throws InterruptedException {
        return add(element);
    }

    public Object take() throws InterruptedException {
        try {
            Message message = getJmsClient().receive();
            if (message != null) {
                return getJmsClient().readMessage(message);
            }
        }
        catch (JMSException e) {
            getJmsClient().handleException(e);
        }
        return null;
    }

    public Object poll(long timeout, TimeUnit unit) throws InterruptedException {
        try {
            Message message = getJmsClient().receive(timeout, unit);
            if (message != null) {
                return getJmsClient().readMessage(message);
            }
        }
        catch (JMSException e) {
            getJmsClient().handleException(e);
        }
        return null;
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    public int drainTo(Collection coll) {
        int count = 0;
        while (true) {
            Object answer = poll();
            if (answer != null) {
                coll.add(answer);
                count++;
            }
            else {
                return count;
            }
        }
    }

    public int drainTo(Collection coll, int maximumElements) {
        int count = 0;
        while (count < maximumElements) {
            Object answer = poll();
            if (answer != null) {
                coll.add(answer);
                count++;
            }
            else {
                break;
            }
        }
        return count;
    }

    // Extension APIs
    // -------------------------------------------------------------------------
    public void close() {
        jmsClient.close();
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected JmsClient getJmsClient() {
        return jmsClient;
    }

    /**
     * returns true if both values are null or identical or equal to each other
     */
    protected boolean equals(Object element, Object value) {
        if (element == value) {
            return true;
        }
        else if (element == null || value == null) {
            return false;
        }
        else {
            return element.equals(value);
        }
    }

    protected final class JmsQueueIterator implements Iterator {
        private final Enumeration enumeration;
        private final QueueBrowser browser;

        private boolean closed = false;
        private Message message;
        private Object element;

        public JmsQueueIterator(Enumeration enumeration, QueueBrowser browser) {
            this.enumeration = enumeration;
            this.browser = browser;
        }

        public boolean hasNext() {
            boolean answer = enumeration.hasMoreElements();
            message = (Message) enumeration.nextElement();
            element = null;
            if (!answer) {
                close();
            }
            return answer;
        }

        public void close() {
            if (!closed) {
                closed = true;
                getJmsClient().close(browser);
            }
        }

        public Object next() {
            if (element == null) {
                try {
                    element = getJmsClient().readMessage(message);
                }
                catch (JMSException e) {
                    getJmsClient().handleException(e);
                }
            }
            return element;
        }

        public void remove() {
            throw new UnsupportedOperationException("remove() not supported");
        }
    }

}
