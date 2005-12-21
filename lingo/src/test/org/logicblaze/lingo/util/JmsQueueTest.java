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

import edu.emory.mathcs.backport.java.util.Queue;

import org.activemq.ActiveMQConnectionFactory;
import org.activemq.command.ActiveMQQueue;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * 
 * @version $Revision$
 */
public class JmsQueueTest extends TestCase {

    protected Queue queue;

    public void testQueueOperations() throws Exception {
        assertTrue("queue should be empty", queue.isEmpty());
        assertEquals("size()", 0, queue.size());

        queue.add("1");
        assertEquals("size()", 1, queue.size());
        assertTrue("queue should be non empty", !queue.isEmpty());

        queue.add("2");
        assertEquals("size()", 2, queue.size());
        assertTrue("queue should be non empty", !queue.isEmpty());

        queue.add("3");
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

    protected Queue createQueue() {
        JmsClient client = new JmsClient(new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false"), new ActiveMQQueue(getName()));
        return new JmsQueue(client);
    }
}
