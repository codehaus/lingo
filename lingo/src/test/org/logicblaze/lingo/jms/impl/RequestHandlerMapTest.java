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
package org.logicblaze.lingo.jms.impl;

import org.logicblaze.lingo.jms.ReplyHandler;

import javax.jms.JMSException;
import javax.jms.Message;

import junit.framework.TestCase;

/**
 * 
 * @version $Revision$
 */
public class RequestHandlerMapTest extends TestCase {
    protected DefaultRequestHandlerMap map = new DefaultRequestHandlerMap(200);
    protected ReplyHandler handler = new ReplyHandler() {
        public boolean handle(Message message) throws JMSException {
            return false;
        }
    };
    protected long timeout = 500L;
    protected int loop = 10;

    public void testRequestMap() throws Exception {
        String medium = "Recent";
        String longLived = "Old";
        String quick = "Quick";

        map.put(quick, handler, timeout / 10);
        Thread.sleep(timeout);
        map.purgeOldRequests();
        assertEntry(quick, null);

        map.put(medium, handler, timeout);
        assertEntry(medium, handler);

        map.put(longLived, handler, timeout * 100);
        assertEntry(longLived, handler);
        assertEquals("handler should still be there for: " + longLived, handler, map.get(longLived));


        for (int i = 0; i < loop; i++) {
            System.out.println("Sleeping at loop: " + i);
            Thread.sleep(timeout / 2);

            assertEntry(medium, handler);
        }

        Thread.sleep(timeout * 2);

        assertEntry(medium, null);
        assertEntry(longLived, handler);

    }
    
    protected void assertEntry(String correlationID, ReplyHandler expected) {
        ReplyHandler actual = map.get(correlationID);
        assertEquals("handler for: " + correlationID, expected, actual);
    }

}
