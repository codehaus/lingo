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

import org.logicblaze.lingo.cache.impl.TransactionPolicy;
import org.logicblaze.lingo.cache.impl.TransactionSynchronizer;
import org.logicblaze.lingo.query.QueryContext;
import org.logicblaze.lingo.query.QueryManager;

import javax.cache.CacheManager;

import java.util.List;

/**
 * Represents the API of an Active CacheManager implementation which adds a number of new APIs for
 * transaction handling, querying and executing commands. The common implementations are either
 * a clustered collection of caches or a single in JVM transaction caches.
 *
 * @version $Revision$
 */
public abstract class ActiveCacheManager extends CacheManager {
    private TransactionPolicy transactionPolicy = new TransactionPolicy();
    private TransactionSynchronizer transactionSynchronizer;

    public ActiveCacheManager() {
        transactionPolicy = new TransactionPolicy();
    }

    public ActiveCacheManager(TransactionPolicy transactionPolicy) {
        this.transactionPolicy = transactionPolicy;
    }

    /**
     * Shuts down this class, freeing any resources used up (such as open files or sockets).
     * One this method has been called this class should not be used again.
     */
    public abstract void stop() throws Exception;

    /**
     * Allows the query to be queried using an arbitrary query language defined via the query manager
     * returning a list of results
     */
    public abstract List select(String query);

    /**
     * Performs the query operation on the data set and returns the result, if any.
     */
    public abstract Object evaluate(String query);

    /**
     * Performs a thread local commit of all operations on all the caches
     * used in this tranaction.
     *
     * @throws TransactionException if the transaction could not be committed
     *                              due to an optimistic transaction failure or deadlock.
     */
    public abstract void commit() throws TransactionException;

    /**
     * Performs rollback of all pending transactions on all the caches
     * accessed in the current thread.
     */
    public abstract void rollback();

    /**
     * Executes the command on the cache, waiting until it has been completely
     * processed on this local cache.
     */
    public abstract void execute(CacheCommand command);

    /**
     * Executes the command on the cache asynchronously at some point
     * in the future, without waiting for the command to be applied.
     */
    public abstract void executeAsync(CacheCommand command);

    /**
     * Returns the current transaction policy (isolation level, registration mechanism etc).
     */
    public TransactionPolicy getTransactionPolicy() {
        return transactionPolicy;
    }

    /**
     * Sets the current transaction policy (isolation level, registration mechanism etc).
     */
    public void setTransactionPolicy(TransactionPolicy transactionPolicy) {
        this.transactionPolicy = transactionPolicy;
    }

    /**
     * Returns the query manager used for performing queries on the caches
     */
    public abstract QueryManager getQueryManager();

    /**
     * Sets the query manager to be used for querying caches
     */
    public abstract void setQueryManager(QueryManager queryManager);

    /**
     * Returns the context used when querying caches
     */
    public abstract QueryContext getQueryContext();

    /**
     * Sets the context used when querying
     */
    public abstract void setQueryContext(QueryContext queryContext);

    public TransactionSynchronizer getTransactionSynchronizer() {
        return transactionSynchronizer;
    }

    /**
     * Used by the implementation to register some kind of transaction sychronizer
     * with the cache manager, to handle transactions (Spring, JTA) or units of work
     */
    public void setTransactionSynchronizer(TransactionSynchronizer transactionSynchronizer) {
        this.transactionSynchronizer = transactionSynchronizer;
    }
}
