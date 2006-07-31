/** 
 * 
 * Copyright 2006 LogicBlaze, Inc.
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
package org.logicblaze.lingo.cache;

import junit.framework.Test;

import org.logicblaze.lingo.cache.TransactionException;
import org.logicblaze.lingo.cache.TransactionalCache;
import org.logicblaze.lingo.cache.TransactionalCacheManager;
import org.logicblaze.lingo.cache.TransactionalCacheManagerFactory;
import org.sysunit.SystemTestCase;

/**
 * @version $Revision$
 */
public class RepeatableReadCacheTest extends MultiThreadedCacheTestSupport {

    public static Test suite() throws Exception {
        return suite(RepeatableReadCacheTest.class);
    }

    public RepeatableReadCacheTest() {
    }

    public RepeatableReadCacheTest(TransactionalCacheManagerFactory cacheManagerFactory) {
        super(cacheManagerFactory);
    }

    public void threadOne() throws Exception {
        TransactionalCacheManager manager = manager1;
        TransactionalCache cache = manager.getTransactionalCache(cacheName);
        cache.setThreadID("threadOne");

        assertEquals(REPEATABLE_READ, manager.getTransactionPolicy().getTransactionIsolation());

        cache.put("x", "1234");
        manager.commit();

        sync("a");
        assertEquals("x", "1234", cache.get("x"));

        sync("b");

        sync("c");
        cache.put("x", "2345");
        manager.commit();
        assertEquals("x", "2345", cache.get("x"));

        sync("d");

        sync("e");
        assertEquals("x", "2345", cache.get("x"));
        assertEquals("size", 1, cache.size());
    }

    public void threadTwo() throws Exception {
        TransactionalCacheManager manager = manager2;
        TransactionalCache cache = manager.getTransactionalCache(cacheName);
        cache.setThreadID("threadTwo");

        assertEquals(REPEATABLE_READ, manager.getTransactionPolicy().getTransactionIsolation());

        sync("a");
        assertEquals("x", "1234", cache.get("x"));

        sync("b");
        cache.put("y", "4567");

        sync("c");

        sync("d");
        assertEquals("x", "1234", cache.get("x"));

        try {
            manager.commit();
            fail("Should have thrown an optimistic transaction exception on cache: " + cache);
        }
        catch (TransactionException e) {
            System.out.println("Correctly caught exception: " + e);
        }

        sync("e");
        assertEquals("size", 1, cache.size());
        assertEquals("x", "2345", cache.get("x"));
    }

    protected void configureTransactionPolicy(TransactionalCacheManager manager) {
        manager.getTransactionPolicy().setTransactionIsolation(REPEATABLE_READ);
    }
}
