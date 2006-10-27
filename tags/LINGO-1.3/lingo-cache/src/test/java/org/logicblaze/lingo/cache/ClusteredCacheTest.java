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

import org.logicblaze.lingo.cache.ClusteredCacheManagerFactory;
import org.logicblaze.lingo.cache.TransactionalCache;
import org.logicblaze.lingo.cache.TransactionalCacheManager;
import org.logicblaze.lingo.cache.impl.ActiveMQClusteredCacheManagerFactory;

/**
 * @version $Revision$
 */
public class ClusteredCacheTest extends TransactionalCacheTest {
    protected ClusteredCacheManagerFactory cacheManagerFactory = new ActiveMQClusteredCacheManagerFactory("vm://localhost");
    private TransactionalCacheManager secondaryCacheManager;
    private TransactionalCache secondaryCache;

    public void testTransaction() throws Exception {
        assertTrue(secondaryCache.isEmpty());

        super.testTransaction();

        // lets check that the transactions have been applied to our copy
        assertEquals("Secondary cache should not be empty: " + secondaryCache, 2, secondaryCache.size());
        assertEquals("James", secondaryCache.get("name"));
        assertEquals("London", secondaryCache.get("location"));
    }

    protected void setUp() throws Exception {
        secondaryCacheManager = cacheManagerFactory.createCacheManager(getName());
        secondaryCache = secondaryCacheManager.createTransactionalCache("A");
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        secondaryCacheManager.stop();
    }

    protected TransactionalCacheManager createCacheManager() throws Exception {
        return cacheManagerFactory.createCacheManager(getName());
    }
}
