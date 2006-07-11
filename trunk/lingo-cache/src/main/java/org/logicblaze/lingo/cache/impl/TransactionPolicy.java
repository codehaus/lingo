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
import org.logicblaze.lingo.cache.TransactionException;
import org.logicblaze.lingo.cache.TransactionIsolation;

import javax.cache.Cache;

/**
 * The Transaction policy used with a cache including its transaction
 * isolation level and the registration scheme for transactions (such as JCA or Spring).
 *
 * @version $Revision$
 * @see TransactionIsolation
 * @see TransactionRegistration
 */
public class TransactionPolicy implements TransactionIsolation {

    private TransactionRegistration transactionRegistration = NullTransactionRegistration.getInstance();
    private int transactionIsolation = READ_COMMITTED;


    /**
     * Returns the {@link TransactionIsolation} value to use for transactions
     */
    public int getTransactionIsolation() {
        return transactionIsolation;
    }

    /**
     * Sets the {@link TransactionIsolation} value to use for transactions
     */
    public void setTransactionIsolation(int transactionIsolation) {
        this.transactionIsolation = transactionIsolation;
    }

    /**
     * Returns the current registration mechanisms for transactions, such as JTA or Spring etc
     */
    public TransactionRegistration getTransactionRegistration() {
        return transactionRegistration;
    }

    /**
     * Sets the current registration mechanisms for transactions, such as JTA or Spring etc
     */
    public void setTransactionRegistration(TransactionRegistration transactionRegistration) {
        this.transactionRegistration = transactionRegistration;
    }

    /**
     * Factory method to create a new thread local view of a backing cache which uses the
     * transaction isolation level.
     */
    public ThreadCache createThreadCache(Cache backingCache) {
        switch (transactionIsolation) {
            case READ_COMMITTED:
                return new ThreadCache(backingCache);
            case REPEATABLE_READ:
                return new RepeatableReadThreadCache(backingCache);
            default:
                throw new TransactionException("Unsupported transaction isolation level: " + transactionIsolation);
        }
    }

    /**
     * Registers the given thread with the transaction associated with the current thread
     */
    public void register(ActiveCacheManager cacheManager) {
        getTransactionRegistration().register(cacheManager);
    }
}
