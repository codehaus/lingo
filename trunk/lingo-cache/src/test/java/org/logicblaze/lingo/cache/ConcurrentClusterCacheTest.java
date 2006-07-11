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
import org.logicblaze.lingo.cache.TransactionalCacheManager;
import org.logicblaze.lingo.cache.TransactionalCacheManagerFactory;
import org.logicblaze.lingo.cache.impl.ActiveMQClusteredCacheManagerFactory;

import javax.cache.Cache;

import junit.framework.Test;

/**
 * @version $Revision$
 */
public class ConcurrentClusterCacheTest extends ConcurrentCacheTest {

    public static Test suite() throws Exception {
        return suite(ConcurrentClusterCacheTest.class);
    }

    public ConcurrentClusterCacheTest() {
        super(new ActiveMQClusteredCacheManagerFactory("vm://localhost"));
    }

    public void setUp() throws Exception {
        super.setUp();

        TransactionalCache transactionalCache1 = manager1.getTransactionalCache(cacheName);
        TransactionalCache transactionalCache2 = manager2.getTransactionalCache(cacheName);

        Cache cache1 = transactionalCache1.getBackingCache();
        Cache cache2 = transactionalCache2.getBackingCache();

        assertTrue("The backing caches must be different objects", cache1 != cache2);

        System.out.println("transactionalCache1: " + System.identityHashCode(transactionalCache1));
        System.out.println("transactionalCache2: " + System.identityHashCode(transactionalCache2));

        System.out.println("cache1: " + System.identityHashCode(cache1));
        System.out.println("cache2: " + System.identityHashCode(cache2));
    }

    public ConcurrentClusterCacheTest(TransactionalCacheManagerFactory cacheManagerFactory) {
        super(cacheManagerFactory);
    }

    protected TransactionalCacheManager createCacheManager2() throws Exception {
        return createCacheManager();
    }
}
