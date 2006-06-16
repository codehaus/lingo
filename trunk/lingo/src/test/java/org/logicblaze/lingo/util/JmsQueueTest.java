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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.logicblaze.lingo.jms.JmsClient;
import org.logicblaze.lingo.jms.JmsQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * 
 * @version $Revision$
 */
public class JmsQueueTest extends TestCase {

    protected BlockingQueue queue;

    public void testQueueOperations() throws Exception {
        assertTrue("queue should be empty", queue.isEmpty());
        assertEquals("size()", 0, queue.size());

        queue.add("1");
        assertEquals("size()", 1, queue.size());
        assertTrue("queue should be non empty", !queue.isEmpty());

        queue.offer("2");
        assertEquals("size()", 2, queue.size());
        assertTrue("queue should be non empty", !queue.isEmpty());

        queue.offer("3", 1000, TimeUnit.MILLISECONDS);
        assertEquals("size()", 3, queue.size());
        assertTrue("queue should be non empty", !queue.isEmpty());

        assertEquals("peek()", "1", queue.peek());
        assertTrue("contains 1", queue.contains("1"));

        assertTrue("contains all of 2", queue.containsAll(Arrays.asList(new Object[] { "2" })));
        assertTrue("contains all of 1, 2", queue.containsAll(Arrays.asList(new Object[] { "1", "2" })));
        assertTrue("contains all of 2, 3", queue.containsAll(Arrays.asList(new Object[] { "2", "3" })));
        assertTrue("contains all of 1, 2, 3", queue.containsAll(Arrays.asList(new Object[] { "1", "2", "3" })));

        assertTrue("contains all of 1, 2, 4", !queue.containsAll(Arrays.asList(new Object[] { "1", "2", "4" })));

        System.out.println("Created a queue: " + queue);

        // now lets remove some stuff
        Object value = queue.remove();
        assertEquals("remove()", "1", value);

        value = queue.poll();
        assertEquals("poll()", "2", value);
        assertEquals("size()", 1, queue.size());
        assertTrue("queue should be non empty", !queue.isEmpty());

        queue.clear();
        assertTrue("queue should be empty", queue.isEmpty());
        assertEquals("size()", 0, queue.size());

        assertTrue("remainingCapacity", queue.remainingCapacity() > 0);
    }

    /*
    public void testRemoveAll() throws Exception {
        List sample = Arrays.asList(new Object[] { "A", "B", "C" });
        queue.addAll(sample);
        assertEquals("size()", 3, queue.size());
        queue.removeAll(sample);
        assertEquals("size()", 0, queue.size());
    }
    */

    public void testDrain() throws Exception {
        List sample = Arrays.asList(new Object[] { "A", "B", "C" });
        queue.addAll(sample);
        assertEquals("size()", 3, queue.size());

        List list = new ArrayList();
        queue.drainTo(list);

        assertEquals("size()", 0, queue.size());

        assertEquals("size of drained list", 3, list.size());
    }

    public void testDrainConstrained() throws Exception {
        List sample = Arrays.asList(new Object[] { "A", "B", "C" });
        queue.addAll(sample);
        assertEquals("size()", 3, queue.size());

        List list = new ArrayList();
        queue.drainTo(list, 2);

        assertEquals("size()", 1, queue.size());

        assertEquals("size of drained list", 2, list.size());
    }

    public void testRemoveOnEmptyQueue() throws Exception {
        assertEquals("poll()", null, queue.poll());
        try {
            queue.remove();
            fail("Should have thrown exception: NoSuchElementException");
        }
        catch (NoSuchElementException e) {
            System.out.println("Caught expected exception: " + e);
        }

    }

    protected void setUp() throws Exception {
        queue = createQueue();
    }

    protected void tearDown() throws Exception {
        if (queue instanceof JmsQueue) {
            JmsQueue service = (JmsQueue) queue;
            service.close();
        }
    }

    protected BlockingQueue createQueue() {
        JmsClient client = new JmsClient(new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false"), new ActiveMQQueue(getDestinationName()));
        return new JmsQueue(client);
    }

    protected String getDestinationName() {
        return getClass().getName() + "." + getName();
    }
}
