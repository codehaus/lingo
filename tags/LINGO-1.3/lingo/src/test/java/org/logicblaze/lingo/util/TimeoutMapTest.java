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

import edu.emory.mathcs.backport.java.util.concurrent.ScheduledThreadPoolExecutor;
import junit.framework.TestCase;

/**
 * 
 * @version $Revision$
 */
public class TimeoutMapTest extends TestCase {
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    protected DefaultTimeoutMap map = new DefaultTimeoutMap(executor, 200);
    protected long timeout = 500L;
    protected int loop = 10;

    public void testRequestMap() throws Exception {
        String medium = "Recent";
        String longLived = "Old";
        String quick = "Quick";
        
        String mediumValue = "Value of Recent";
        String longValue = "Value of Long";
        String quickValue = "Value of Quick";

        map.put(quick, quickValue, timeout / 10);
        Thread.sleep(timeout);
        map.purge();
        assertEntry(quick, null);

        map.put(medium, mediumValue, timeout);
        assertEntry(medium, mediumValue);

        map.put(longLived, longValue, timeout * 100);
        assertEntry(longLived, longValue);
        assertEquals("handler should still be there for: " + longLived, longValue, map.get(longLived));


        for (int i = 0; i < loop; i++) {
            System.out.println("Sleeping at loop: " + i);
            Thread.sleep(timeout / 2);

            assertEntry(medium, mediumValue);
        }

        Thread.sleep(timeout * 2);

        assertEntry(medium, null);
        assertEntry(longLived, longValue);

    }
    
    protected void assertEntry(Object key, Object expected) {
        Object actual = map.get(key);
        assertEquals("value for: " + key, expected, actual);
    }

}
