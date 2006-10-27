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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.cache.impl.CommandExecutor;
import org.logicblaze.lingo.cache.impl.CompositeCacheCommand;
import org.logicblaze.lingo.cache.impl.ThreadCache;
import org.logicblaze.lingo.cache.impl.TransactionPolicy;
import org.logicblaze.lingo.query.QueryContext;
import org.logicblaze.lingo.query.QueryException;
import org.logicblaze.lingo.query.QueryManager;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A {@link CacheManager} which returns {@link TransactionalCache} instances
 * using some backing {@link Cache} instances. This class also acts as the transaction
 * manager for all caches registered with it.
 * <p/>
 * You can change the transaction isolation level and transaction manager registration
 * hooks using the {@link #getTransactionPolicy()} property.
 *
 * @version $Revision$
 */
public class TransactionalCacheManager extends ActiveCacheManager implements CommandExecutor {

    private static final Log log = LogFactory.getLog(TransactionalCacheManager.class);

    private Map caches = Collections.synchronizedMap(new HashMap());
    private Map environment = new HashMap();
    private QueryManager queryManager;
    private QueryContext queryContext;

    public TransactionalCacheManager() {
    }

    public TransactionalCacheManager(TransactionPolicy transactionPolicy) {
        super(transactionPolicy);
    }

    /**
     * Shuts down this class, freeing any resources used up (such as open files or sockets).
     * One this method has been called this class should not be used again.
     */
    public void stop() throws Exception {
    }

    /**
     * Allows the query to be queried using an arbitrary query language defined via the query manager
     * returning a list of results
     */
    public List select(String query) {
        return getQueryManager().select(query, getQueryContext());
    }

    /**
     * Performs the query operation on the data set and returns the result, if any.
     */
    public Object evaluate(String query) {
        return getQueryManager().evaluate(query, getQueryContext());
    }

    /**
     * Creates a new transactional cache
     */
    public TransactionalCache createTransactionalCache(String name) throws CacheException {
        Cache backingCache = createBackingCache(name);
        return registerCache(caches, backingCache, name);
    }

    /**
     * Looks up a cache
     */
    public TransactionalCache getTransactionalCache(String name) {
        TransactionalCache answer = (TransactionalCache) caches.get(name);
        if (answer == null) {
            Cache backingCache = getBackingCache(name);
            if (backingCache != null) {
                answer = registerCache(caches, backingCache, name);
            }
        }
        return answer;
    }

    public Cache getCache(String cacheName) {
        return getTransactionalCache(cacheName);
    }

    /**
     * Performs a thread local commit of all operations on all the caches
     * used in this transaction.
     *
     * @throws TransactionException if the transaction could not be committed
     *                              due to an optimistic transaction failure or deadlock.
     */
    public void commit() throws TransactionException {
        Collection caches = getTransactionalCaches();
        CompositeCacheCommand command = createCommitCommand(caches);
        if (!command.isEmpty()) {
            command.run(this);
            resetLocalChanges(caches);
            handleCommitException(caches);
        }
    }

    /**
     * Performs rollback of all pending transactions on all the caches
     * accessed in the current thread.
     */
    public void rollback() {
        Collection caches = getTransactionalCaches();
        resetLocalChanges(caches);
    }

    // Properties
    //-------------------------------------------------------------------------

    public Map getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map environment) {
        this.environment = environment;
    }

    public QueryManager getQueryManager() {
        if (queryManager == null) {
            queryManager = createQueryManager();
        }
        return queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        this.queryManager = queryManager;
    }

    public QueryContext getQueryContext() {
        if (queryContext == null) {
            queryContext = createQueryContext();
        }
        return queryContext;
    }

    public void setQueryContext(QueryContext queryContext) {
        this.queryContext = queryContext;
    }

    /**
     * Executes the command on the cache, waiting until it has been completely
     * processed on this local cache.
     */
    public void execute(CacheCommand command) {
        command.run(this);
    }

    /**
     * Executes the command on the cache asynchronously at some point
     * in the future, without waiting for the command to be applied.
     */
    public void executeAsync(CacheCommand command) {
        command.run(this);
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected QueryManager createQueryManager() {
        // lets try use Groovy but without a static runtime dependency
        Class type = loadClass("org.logicblaze.lingo.query.groovy.GroovyQueryManager");
        if (type != null) {
            try {
                return (QueryManager) type.newInstance();
            }
            catch (Exception e) {
                throw new QueryException("Could not instantiate query manager: " + e, e);
            }
        }
        return null;
    }

    protected Class loadClass(String name) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e) {
            try {
                return getClass().getClassLoader().loadClass(name);
            }
            catch (ClassNotFoundException e1) {
                log.trace("Could not load class: " + name);
            }
        }
        return null;
    }

    protected QueryContext createQueryContext() {
        return new QueryContext() {
            public Object getValue(String name) {
                return getCache(name);
            }
        };
    }

    protected TransactionalCache registerCache(Map caches, Cache backingCache, String name) {
        TransactionalCache answer = createTransactionalCache(backingCache, name);
        answer.setName(name);
        answer.setTransactionPolicy(getTransactionPolicy());
        caches.put(name, answer);
        return answer;
    }

    protected TransactionalCache createTransactionalCache(Cache backingCache, String name) {
        return new TransactionalCache(backingCache, this);
    }

    protected Collection getTransactionalCaches() {
        return caches.values();
    }

    protected Cache getBackingCache(String name) {
        return super.getCache(name);
    }

    protected synchronized Cache createBackingCache(String name) throws CacheException {
        Cache cache = getCacheFactory().createCache(environment);
        super.registerCache(name, cache);
        return cache;
    }

    protected void resetLocalChanges(Collection caches) {
        for (Iterator iter = caches.iterator(); iter.hasNext();) {
            TransactionalCache cache = (TransactionalCache) iter.next();
            if (cache.isModifiedInTransaction()) {
                cache.resetLocalChanges();
            }
        }
    }

    protected void handleCommitException(Collection caches) {
        for (Iterator iter = caches.iterator(); iter.hasNext();) {
            TransactionalCache cache = (TransactionalCache) iter.next();
            if (cache.isModifiedInTransaction()) {
                cache.resetLocalChanges();
            }
        }

        for (Iterator iter = caches.iterator(); iter.hasNext();) {
            TransactionalCache cache = (TransactionalCache) iter.next();
            ThreadCache threadCache = cache.getThreadCache();
            threadCache.handleCommitException();
        }
    }

    protected CompositeCacheCommand createCommitCommand(Collection caches) {
        CompositeCacheCommand command = new CompositeCacheCommand();
        for (Iterator iter = caches.iterator(); iter.hasNext();) {
            TransactionalCache cache = (TransactionalCache) iter.next();
            if (cache.isModifiedInTransaction()) {
                command.addCommand(cache.createTransactionCommand());
            }
        }
        return command;
    }

}
