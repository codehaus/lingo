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

import org.logicblaze.lingo.cache.TransactionIsolation;
import org.logicblaze.lingo.cache.TransactionalCacheManager;
import org.logicblaze.lingo.cache.TransactionalCacheManagerFactory;
import org.sysunit.SystemTestCase;

/**
 * @version $Revision$
 */
public class MultiThreadedCacheTestSupport extends SystemTestCase implements TransactionIsolation {
    protected TransactionalCacheManagerFactory cacheManagerFactory;

    protected TransactionalCacheManager manager1;
    protected TransactionalCacheManager manager2;
    protected String regionName = getClass().getName();
    protected String cacheName = "Cheese";

    public MultiThreadedCacheTestSupport() {
        this(new TransactionalCacheManagerFactory());
    }

    public MultiThreadedCacheTestSupport(TransactionalCacheManagerFactory cacheManagerFactory) {
        this.cacheManagerFactory = cacheManagerFactory;
    }


    public void setUp() throws Exception {
        super.setUp();

        manager1 = createCacheManager();
        manager1.createTransactionalCache(cacheName);

        manager2 = createCacheManager2();
        manager2.createTransactionalCache(cacheName);

        configureTransactionPolicy(manager1);
        configureTransactionPolicy(manager2);
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

    protected void configureTransactionPolicy(TransactionalCacheManager manager) {
    }
}
