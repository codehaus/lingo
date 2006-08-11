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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.cache.CacheCommand;
import org.logicblaze.lingo.cache.OptimisticTransactionException;
import org.logicblaze.lingo.cache.TransactionException;
import org.logicblaze.lingo.cache.TransactionalCache;
import org.logicblaze.lingo.cache.TransactionalCacheManager;

import javax.cache.Cache;
import javax.cache.CacheEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * <p><code>TransactionalCacheCommand</code> is the default
 * implementation of a CacheCommand which can include adding
 * new items, updating items and removing items.
 *
 * @author James Strachan
 * @version 1.7
 */
public class TransactionalCacheCommand extends CacheCommand {
    /**
     * 
     */
    private static final long serialVersionUID = -6503977955225621356L;

    private static final Log log = LogFactory.getLog(TransactionalCache.class);

    private String name;
    private Map changes;
    private Map removalVersions;
    private Map readVersions;
    private boolean clear;

    public TransactionalCacheCommand(String name, Map changes, Map removalKeys, boolean clear, String originator) {
        super(originator);
        this.name = name;
        this.changes = changes;
        this.removalVersions = removalKeys;
        this.clear = clear;
    }


    public Set getUpdatedKeys() {
        Set answer = new HashSet();
        answer.addAll(changes.keySet());
        answer.addAll(removalVersions.keySet());
        return answer;
    }

    /**
     * Returns a local, temporary  snapshot of the cache
     */
    public Map getSnapshot(Cache cache) {
        Map answer = null;
        if (clear) {
            answer = new HashMap();
        }
        else {
            answer = new HashMap(cache);

            for (Iterator iter = removalVersions.keySet().iterator(); iter.hasNext();) {
                answer.remove(iter.next());
            }
        }

        for (Iterator iter = changes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            VersionedValue change = (VersionedValue) entry.getValue();
            answer.put(key, change.getValue());
        }
        return answer;
    }

    public void run(TransactionalCacheManager cacheManager) {
        TransactionalCache transactionCache = cacheManager.getTransactionalCache(name);
        if (transactionCache != null) {
            runOnCache(transactionCache);
        }
        else {
            log.warn("Discarding command for unknown cache: " + name);
        }
    }

    public void runOnCache(TransactionalCache transactionCache) {
        // lets get the thread cache of the originator of the transaction
        ThreadCache threadCache = transactionCache.getThreadCache(getOriginator());
        if (threadCache == null) {
            // we are not the JVM which initiated the transaction to silently discard
            // any transaction failures
            try {
                updateBackingCache(transactionCache.getBackingCache());
            }
            catch (TransactionException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Discarding optimistic transaction failure: " + e, e);
                }
                //System.out.println("Discarding optimistic transaction failure: " + e + " as no cache could be found for: " + originator + " on transactionCache: " + transactionCache.hashCode());
            }
            finally {
                if (threadCache != null) {
                    threadCache.resetLocalChanges();
                }
            }
        }
        else {
            runOnCache(threadCache);
        }
    }

    public void runOnCache(ThreadCache threadCache) {
        try {
            updateBackingCache(threadCache.getDelegate());
            threadCache.onCommitException(null);
        }
        catch (TransactionException e) {
            threadCache.onCommitException(e);
        }
        finally {
            threadCache.resetLocalChanges();
        }
    }

    public void updateBackingCache(Cache cache) {
        synchronized (cache) {
            checkForConcurrencyFailure(cache);
            if (clear) {
                cache.clear();
            }
            else {
                for (Iterator iter = removalVersions.keySet().iterator(); iter.hasNext();) {
                    cache.remove(iter.next());
                }
            }
            applyChanges(cache);
        }
    }

    public Map getReadVersions() {
        return readVersions;
    }

    public void setReadVersions(Map readVersions) {
        this.readVersions = readVersions;
    }

    protected void checkForConcurrencyFailure(Cache cache) {
        for (Iterator iter = changes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();

            VersionedValue change = (VersionedValue) entry.getValue();
            Object updateVersion = change.getVersion();
            Object version = JCacheHelper.getEntryVersion(cache, key);

            if (log.isDebugEnabled()) {
                log.debug("key: " + key + " update version: " + updateVersion + " on current version: " + version + " on cache: " + System.identityHashCode(cache));
            }

            if (!versionsCompatible(updateVersion, version)) {
                if (log.isDebugEnabled()) {
                    log.debug("Incompatible change: version: " + version + " not compatible with: " + updateVersion);
                }
                throw new OptimisticTransactionException(cache, key, updateVersion, version);
            }
        }
        for (Iterator iter = removalVersions.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object updateVersion = entry.getKey();
            Object version = JCacheHelper.getEntryVersion(cache, key);

            if (log.isDebugEnabled()) {
                log.debug("key: " + key + " remove version: " + updateVersion + " on current version: " + version + " on cache: " + System.identityHashCode(cache));
            }

            if (!versionsCompatible(updateVersion, version)) {
                if (log.isDebugEnabled()) {
                    log.debug("Incompatible remove: version: " + version + " not compatible with: " + updateVersion);
                }
                throw new OptimisticTransactionException(cache, key, updateVersion, version);
            }
        }
        if (readVersions != null) {
            for (Iterator iter = readVersions.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object updateVersion = entry.getKey();
                Object version = JCacheHelper.getEntryVersion(cache, key);

                if (log.isDebugEnabled()) {
                    log.debug("key: " + key + " read version: " + updateVersion + " on current version: " + version + " on cache: " + System.identityHashCode(cache));
                }

                if (!versionsCompatible(updateVersion, version)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Incompatible read: version: " + version + " not compatible with: " + updateVersion);
                    }
                    throw new OptimisticTransactionException(cache, key, updateVersion, version);
                }
            }
        }
    }


    /**
     * Returns true if the given version of the entry is compatible with this
     * change
     */
    protected boolean versionsCompatible(Object updateVersion, Object currentVersion) {
        return currentVersion == updateVersion || (currentVersion != null && currentVersion.equals(updateVersion));
    }

    protected void applyChanges(Cache cache) {
        for (Iterator iter = changes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            VersionedValue change = (VersionedValue) entry.getValue();

            // TODO should put do this by default?
            CacheEntry cacheEntry = cache.getCacheEntry(key);
            if (cacheEntry != null) {
                cacheEntry.setValue(change.getValue());
            }
            else {
                cache.put(key, change.getValue());
            }

            //System.out.println("Entry: " + key + " has value " + cache.get(key) + " with version: " + JCacheHelper.getEntryVersion(cache, key) + " for cache: " + System.identityHashCode(cache));
        }
    }

}
