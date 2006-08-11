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

import org.apache.activemq.util.IntSequenceGenerator;
import org.logicblaze.lingo.cache.impl.CacheFacade;
import org.logicblaze.lingo.cache.impl.ThreadCache;
import org.logicblaze.lingo.cache.impl.TransactionPolicy;
import org.logicblaze.lingo.cache.impl.TransactionalCacheCommand;

import javax.cache.Cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p><code>TransactionalCache</code> can be used by multiple threads concurrently
 * and it will maintain a thread local transactional view of an underlying
 * shared cache, tracking changes to the underlying cache,
 * yet not applying them until a transaction commits.
 * This allows many threads to concurrently modify the same cache concurrently
 * and in a transactional way.
 * </p>
 *
 * @version $Revision$
 */
public class TransactionalCache extends CacheFacade {
    private IntSequenceGenerator idGenerator = new IntSequenceGenerator();
    private Cache backingCache;
    private TransactionalCacheManager cacheManager;
    private TransactionPolicy transactionPolicy = new TransactionPolicy();
    private String name;

    /**
     * Stores a map of pending transactions indexed by threadID and values are the ThreadCache instances
     */
    private Map pendingTransactedCachesByID = Collections.synchronizedMap(new HashMap());

    /**
     * Stores some thread local state
     */
    private ThreadLocal threadLocal = new ThreadLocal() {
        protected Object initialValue() {
            return createThreadState(new Integer(idGenerator.getNextSequenceId()).toString());
        }
    };

    public TransactionalCache(Cache backingCache, TransactionalCacheManager cacheManager) {
        this.backingCache = backingCache;
        this.cacheManager = cacheManager;
    }

    public String toString() {
        return getThreadCache().toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cache getBackingCache() {
        return backingCache;
    }

    public TransactionalCacheCommand createTransactionCommand() {
        return getThreadCache().createTransactionCommand();
    }

    public void resetLocalChanges() {
        getThreadCache().resetLocalChanges();
    }

    public boolean isModifiedInTransaction() {
        return getThreadCache().isModifiedInTransaction();
    }

    public ThreadCache getThreadCache() {
        ThreadLocalState state = getThreadState();
        return state.threadCache;
    }

    /**
     * Returns the ThreadCache which is currently processing a transaction
     * or null if no such local ThreadCache exists for the given ID
     */
    public ThreadCache getThreadCache(String originator) {
        return (ThreadCache) pendingTransactedCachesByID.get(originator);
    }

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

    public TransactionalCacheManager getCacheManager() {
        return cacheManager;
    }

    /**
     * A helper method used for testing, which allows the ID for a thread's cache to
     * be explicitly configured rather than being randomly generated, which can make debugging
     * easier.
     */
    public void setThreadID(String id) {
        threadLocal.set(createThreadState(id));
    }

    public Collection values() {
        // TODO we could probably be more efficient maybe
        return getSnapshot().values();
    }

    public Set entrySet() {
        return getSnapshot().entrySet();
    }

    public Set keySet() {
        // TODO we could be way more efficient here
        return getSnapshot().keySet();
    }

    /**
     * Returns a local, temporary  snapshot of the cache
     */
    public Map getSnapshot() {
        TransactionalCacheCommand command = createTransactionCommand();
        if (command == null) {
            return Collections.unmodifiableMap(getBackingCache());
        }
        return command.getSnapshot(this);
    }


    // Implementation methods
    //-------------------------------------------------------------------------
    protected Cache getDelegate() {
        return getThreadCache();
    }

    protected ThreadLocalState createThreadState(String id) {
        ThreadLocalState state = new ThreadLocalState();
        state.id = id;
        state.threadCache = createCacheForCurrentThread(state);
        return state;
    }

    protected ThreadCache createCacheForCurrentThread(ThreadLocalState state) {
        ThreadCache threadCache = getTransactionPolicy().createThreadCache(backingCache);
        threadCache.setName(name);
        threadCache.setID(state.id);
        return threadCache;
    }

    protected ThreadLocalState getThreadState() {
        ThreadLocalState state = (ThreadLocalState) threadLocal.get();
        if (!state.startedTransaction) {
            // only keep the cache in the shared map while a transaction is pending
            state.startedTransaction = true;
            System.out.println("#### adding to pending caches: " + state.id + " for cache: " + hashCode());
            pendingTransactedCachesByID.put(state.id, state.threadCache);
            registerCacheWithTransactionManager(state);
        }
        return state;
    }


    protected void clearThreadState(ThreadLocalState state) {
        // this thread is no longer part of a transaction so remove it from the shared map
        pendingTransactedCachesByID.remove(state.id);
        System.out.println("#### removing to from pending caches: " + state.id + " for cache: " + hashCode());
        state.startedTransaction = false;
    }

    /**
     * A hook to allow us to register the cache with the current transaction if one is in progress
     * or start one if one is not.
     */
    protected void registerCacheWithTransactionManager(ThreadLocalState state) {
        getTransactionPolicy().register(cacheManager);
    }

    protected static class ThreadLocalState {
        public String id;
        public ThreadCache threadCache;
        public boolean startedTransaction = false;

        public ThreadCache getThreadCache() {
            if (threadCache == null) {
                throw new RuntimeException("This thread is not currently involved in a transaction");
            }
            return threadCache;
        }
    }

}

