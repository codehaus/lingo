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
import org.logicblaze.lingo.cache.TransactionException;

import javax.cache.Cache;
import javax.cache.CacheEntry;
import javax.cache.CacheException;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This class is only used by one thread at any point in time and
 * provides a thread local view of a transactional cache which implements
 * the <i>READ_COMMITTED</i> isolation level in that there are no dirty reads
 * and only committed data is read but that the data can change, during a transaction
 * as concurrent transactions in other processes and threads commit.
 *
 * @version $Revision$
 */
public class ThreadCache extends CacheFacade {
    private static final Log log = LogFactory.getLog(ThreadCache.class);

    private Cache backingCache;
    private Map localChanges;
    private Map localRemoves;
    private boolean clear;
    private String name;
    private String ID;
    private TransactionException commitException;

    public ThreadCache(Cache behindCache) {
        init(behindCache, createMap(), createMap());
    }

    public ThreadCache(Cache behindCache, Map localChanges, Map localRemoves) {
        init(behindCache, localChanges, localRemoves);
    }

    private void init(Cache behindCache, Map localChanges, Map localRemoves) {
        if (behindCache == null) {
            throw new IllegalArgumentException("A NULL cache is not allowed");
        }
        this.backingCache = behindCache;
        this.localChanges = localChanges;
        this.localRemoves = localRemoves;
    }

    public String toString() {
        Map map = (clear) ? new IdentityHashMap() : new IdentityHashMap(backingCache);
        for (Iterator iter = localRemoves.keySet().iterator(); iter.hasNext();) {
            map.remove(iter.next());
        }
        for (Iterator iter = localChanges.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            Object key = entry.getKey();
            VersionedValue change = (VersionedValue) entry.getValue();
            map.put(key, change.getValue());
        }
        return map.toString();
    }

    /**
     * Returns the unique ID of this cache
     */
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Returns whether or not this cache has been modified in the transaction
     */
    public boolean isModifiedInTransaction() {
        return clear || !localChanges.isEmpty() || !localRemoves.isEmpty();
    }

    public TransactionalCacheCommand createTransactionCommand() {
        if (!clear && localChanges.isEmpty() && localRemoves.isEmpty()) {
            return null;
        }
        return new TransactionalCacheCommand(getName(), localChanges, localRemoves, clear, ID);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * Throws any runtime exceptions which occurred on a background
     * thread due to the current transaction commit.
     */
    public void handleCommitException() {
        RuntimeException e = commitException;
        commitException = null;
        if (e != null) {
            // create nested exception to preserve all stack traces
            throw new TransactionException(e);
        }
    }

    /**
     * Passes in a runtime exception from the cache updating thread
     * if an exception should be passed to the caller thread
     */
    public void onCommitException(TransactionException e) {
        this.commitException = e;
    }

    /**
     * Called by the transaction manager after a commit or rollback
     */
    public void resetLocalChanges() {
        // lets always instantiate new objects to avoid
        // updating existing transaction logs
        localChanges = createMap();
        localRemoves = createMap();
        clear = false;
    }

    public void clear() {
        resetLocalChanges();
        clear = true;
    }

    public boolean containsKey(Object key) {
        if (clear) {
            return localChangesContainsKey(key);
        }
        return localChangesContainsKey(key) || (super.containsKey(key) && !localRemoves.containsKey(key));
    }

    public boolean containsValue(Object value) {
        if (clear) {
            return localChangesContainsValue(value);
        }
        else {
            if (localChangesContainsValue(value)) {
                return true;
            }
            else {
                for (Iterator iter = getDelegate().entrySet().iterator(); iter.hasNext();) {
                    Entry entry = (Entry) iter.next();
                    if (entry.getValue().equals(value) && !localRemoves.containsKey(entry.getKey())) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    public Object get(Object key) {
        if (clear) {
            return getLocalChangeValue(key);
        }
        else {
            Object answer = getLocalChangeValue(key);
            if (answer == null && !localRemoves.containsKey(key)) {
                answer = super.get(key);
            }
            return answer;
        }
    }

    public Map getAll(Collection keys) throws CacheException {
        Map answer = createMap();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            Object key = iter.next();
            answer.put(key, get(key));
        }
        return answer;
    }


    public CacheEntry getCacheEntry(Object key) {
        if (clear || localRemoves.containsKey(key)) {
            return null;
        }
        // lets return the old entry if we've done updates to the data
        return super.getCacheEntry(key);
    }

    public boolean isEmpty() {
        if (clear) {
            return localChanges.isEmpty();
        }
        else {
            return localChanges.isEmpty() && (super.isEmpty() || super.size() == localRemoves.size());
        }
    }

    public Object peek(Object key) {
        if (clear) {
            return getLocalChangeValue(key);
        }
        else {
            Object answer = getLocalChangeValue(key);
            if (answer == null && !localRemoves.containsKey(key)) {
                answer = super.peek(key);
            }
            return answer;
        }
    }

    public int size() {
        if (clear) {
            return localChanges.size();
        }
        else {
            // TODO lets guess though this could be wrong
            // as it does not take into account updates
            return localChanges.size() + super.size() - localRemoves.size();
        }
    }

    public void putAll(Map map) {
        for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            localChangesPut(entry.getKey(), entry.getValue());
        }
    }

    public Object put(Object key, Object value) {
        Object answer = getLocalChangeValue(key);
        if (answer == null) {
            answer = getDelegate().get(key);
        }
        localChangesPut(key, value);
        return answer;
    }


    public Object remove(Object key) {
        if (localRemoves.containsKey(key)) {
            return null;
        }
        else {
            Object version = getVersion(key);
            localRemove(key, version);
            return getDelegate().get(key);
        }
    }

    public Set entrySet() {
        // TODO
        throw createUnsupportedException();
    }

    public Set keySet() {
        // TODO
        throw createUnsupportedException();
    }

    public Collection values() {
        // TODO
        throw createUnsupportedException();
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected Cache getDelegate() {
        return backingCache;
    }

    protected void localChangesPut(Object key, Object value) {
        localChanges.put(key, createVersionedValue(key, value));
    }

    protected boolean localChangesContainsKey(Object key) {
        return localChanges.containsKey(key);
    }

    protected boolean localChangesContainsValue(Object value) {
        for (Iterator iter = localChanges.values().iterator(); iter.hasNext();) {
            VersionedValue change = (VersionedValue) iter.next();
            if (value.equals(change.getValue())) {
                return true;
            }
        }
        return false;
    }

    protected void localRemove(Object key, Object version) {
        localRemoves.put(key, version);
    }

    protected Object getLocalChangeValue(Object key) {
        VersionedValue change = (VersionedValue) localChanges.get(key);
        return (change != null) ? change.getValue() : null;
    }

    protected VersionedValue createVersionedValue(Object key, Object value) {
        Object version = getVersion(key);
        return new VersionedValue(value, version);
    }

    protected Object getVersion(Object key) {
        return JCacheHelper.getEntryVersion(this, key);
    }

    protected Map createMap() {
        // lets maintain order so changes are made in the correct order
        return new IdentityHashMap();
    }

    protected UnsupportedOperationException createUnsupportedException() {
        return new UnsupportedOperationException("This operation is not yet supported for the TransactionalCache");
    }
}

