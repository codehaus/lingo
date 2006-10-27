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

import org.logicblaze.lingo.cache.TransactionalCache;

import javax.cache.Cache;

/**
 * @version $Revision$
 */
public class TransactionalCacheTest extends TransactionalCacheTestSupport {
    public void testTransaction() throws Exception {
        TransactionalCache cache = cacheManager.createTransactionalCache("A");
        Cache backingCache = cache.getBackingCache();

        assertTrue(cache.isEmpty());
        assertTrue(backingCache.isEmpty());

        cache.put("name", "James");
        cache.put("location", "London");

        assertTrue("Should not be empty now!", !cache.isEmpty());
        assertTrue(backingCache.isEmpty());

        assertEquals("Size of transactional cache", 2, cache.size());
        assertEquals("Size of backingCache cache", 0, backingCache.size());

        assertTrue(cache.containsKey("name"));
        assertTrue(!backingCache.containsKey("name"));

        assertTrue(cache.containsValue("London"));
        assertTrue(!backingCache.containsValue("London"));

        cacheManager.commit();

        assertTrue("Should not be empty now!", !cache.isEmpty());
        assertTrue("Should not be empty now!", !backingCache.isEmpty());

        assertEquals("Size of transactional cache", 2, cache.size());
        assertEquals("Size of backingCache cache", 2, backingCache.size());

        assertTrue(cache.containsKey("name"));
        assertTrue(backingCache.containsKey("name"));

        assertTrue(cache.containsValue("London"));
        assertTrue(backingCache.containsValue("London"));


        cache.put("food", "Cheddar");
        assertTrue(cache.containsKey("food"));
        assertEquals("Size of transactional cache", 3, cache.size());
        assertEquals("Size of backingCache cache", 2, backingCache.size());

        cacheManager.rollback();
        assertTrue(!cache.containsKey("food"));
        assertEquals("Size of transactional cache", 2, cache.size());
        assertEquals("Size of backingCache cache", 2, backingCache.size());

    }

    public void testCommands() throws Exception {
        // TODO     
    }
}
