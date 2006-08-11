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
public class ConcurrentCacheTest extends SystemTestCase {
    protected TransactionalCacheManagerFactory cacheManagerFactory;

    protected TransactionalCacheManager manager1;
    protected TransactionalCacheManager manager2;
    protected String regionName = getClass().getName();
    protected String cacheName = "Cheese";

    public static Test suite() throws Exception {
        return suite(ConcurrentCacheTest.class);
    }

    public ConcurrentCacheTest() {
        this(new TransactionalCacheManagerFactory());
    }

    public ConcurrentCacheTest(TransactionalCacheManagerFactory cacheManagerFactory) {
        this.cacheManagerFactory = cacheManagerFactory;
    }


    public void threadOne() throws Exception {
        TransactionalCacheManager manager = manager1;
        TransactionalCache cache = manager.getTransactionalCache(cacheName);

        cache.setThreadID("threadOne");

        assertNotNull(cache);
        assertTrue(cache.isEmpty());

        sync("a");
        cache.put("x", "1234");
        manager.commit();

        sync("b");
        assertEquals("size", 1, cache.size());
        assertEquals("x", "1234", cache.get("x"));

        sync("c");

        sync("d");

        assertEquals("size: " + cache, 2, cache.size());
        assertEquals("y", "4567", cache.get("y"));


        // overlapping inserts
        sync("insert-1");
        cache.put("z", "xyz");

        sync("insert-2");
        manager.commit();

        sync("insert-3");
        assertEquals("z", "xyz", cache.get("z"));


        // overlapping updates
        sync("update-1");
        System.out.println();
        System.out.println();
        System.out.println("##### starting update");

        cache.put("x", "shouldFail");

        sync("update-2");

        sync("update-3");

        try {
            manager.commit();
            fail("Should have thrown an optimistic transaction exception on cache: " + cache);
        }
        catch (TransactionException e) {
            System.out.println("Correctly caught exception: " + e);

            assertEquals("abc", cache.get("x"));
        }

        // remove missing update
        sync("remove-1");
        cache.remove("y");

        sync("remove-2");

        sync("remove-3");

        try {
            manager.commit();
            fail("Should have thrown an optimistic transaction exception on cache: " + cache);
        }
        catch (TransactionException e) {
            System.out.println("Correctly caught exception: " + e);

            assertEquals("updatedY", cache.get("y"));
        }

        System.out.println("threadOne sees: " + cache);
    }

    public void threadTwo() throws Exception {
        TransactionalCacheManager manager = manager2;
        TransactionalCache cache = manager.getTransactionalCache(cacheName);

        cache.setThreadID("threadTwo");

        assertNotNull(cache);
        assertTrue(cache.isEmpty());

        sync("a");

        sync("b");
        assertEquals("size", 1, cache.size());
        assertEquals("x", "1234", cache.get("x"));

        sync("c");
        cache.put("y", "4567");
        manager.commit();

        sync("d");
        assertEquals("size", 2, cache.size());
        assertEquals("y", "4567", cache.get("y"));


        // overlapping inserts
        sync("insert-1");
        cache.put("z", "zzz");

        sync("insert-2");

        sync("insert-3");

        try {
            manager.commit();
            fail("Should have thrown an optimistic transaction exception on cache: " + cache);
        }
        catch (TransactionException e) {
            System.out.println("Correctly caught exception: " + e);

            assertEquals("xyz", cache.get("z"));
        }

        // overlapping updates
        sync("update-1");
        cache.put("x", "abc");

        sync("update-2");
        manager.commit();

        sync("update-3");
        assertEquals("abc", cache.get("x"));

        // remove missing update
        sync("remove-1");

        sync("remove-2");
        cache.put("y", "updatedY");
        manager.commit();

        sync("remove-3");
        assertEquals("updatedY", cache.get("y"));

        System.out.println("threadTwo sees: " + cache);
    }

    public void setUp() throws Exception {
        super.setUp();

        manager1 = createCacheManager();
        manager1.createTransactionalCache(cacheName);

        manager2 = createCacheManager2();
        manager2.createTransactionalCache(cacheName);
    }

    public void tearDown() throws Exception {
        if (manager2 != null) {
            manager2.stop();
        }
        if (manager1 != null) {
            manager1.stop();
        }
        super.tearDown();
    }


    protected TransactionalCacheManager createCacheManager2() throws Exception {
        // use same manager for non-remote tests cases
        return manager1;
    }

    protected TransactionalCacheManager createCacheManager() throws Exception {
        return cacheManagerFactory.createCacheManager(regionName);
    }
}
