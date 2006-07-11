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
package org.logicblaze.lingo.cache.impl;

import org.logicblaze.lingo.cache.ActiveCacheManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Registers a new synchronization with the current transaction synchronization manager
 * in Spring
 *
 * @version $Revision$
 */
public class SpringTransactionRegistration implements TransactionRegistration {

    public void register(ActiveCacheManager cacheManager) {
        SpringSynchronization synchronization = (SpringSynchronization) cacheManager.getTransactionSynchronizer();
        if (synchronization == null) {
            synchronization = new SpringSynchronization(cacheManager);
            cacheManager.setTransactionSynchronizer(synchronization);
        }
        if (synchronization.isRequiresRegistration()) {
            TransactionSynchronizationManager.registerSynchronization(synchronization);
            synchronization.setRequiresRegistration(false);
        }
    }
}
