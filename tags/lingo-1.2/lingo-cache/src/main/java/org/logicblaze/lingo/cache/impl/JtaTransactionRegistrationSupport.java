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

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

/**
 * A useful base class for any JTA based {@link TransactionRegistration} class
 *
 * @version $Revision$
 */
public abstract class JtaTransactionRegistrationSupport implements TransactionRegistration  {
    public void register(ActiveCacheManager cacheManager) {
        TransactionManager transactionManager = getTransactionManager();
        if (transactionManager == null) {
            throw new IllegalArgumentException("transactionManager not available so cannot register for transaction sychronisation");
        }
        JtaSynchronization synchronization = (JtaSynchronization) cacheManager.getTransactionSynchronizer();
        if (synchronization == null) {
            synchronization = new JtaSynchronization(cacheManager);
            cacheManager.setTransactionSynchronizer(synchronization);
        }
        if (synchronization.isRequiresRegistration()) {
            try {
                Transaction transaction = transactionManager.getTransaction();
                transaction.registerSynchronization(synchronization);
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to register with JTA transaction: " + e, e);
            }
            synchronization.setRequiresRegistration(false);
        }
    }

    public abstract TransactionManager getTransactionManager();
}
