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

import javax.cache.Cache;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Implements a thread local view of a cache which implements am
 * optimistic transaction policy using the
 * {@link org.logicblaze.lingo.cache.TransactionIsolation#REPEATABLE_READ} transaction isolation level,
 * such that values looked up in the cache will remain the same during the transaction
 * which is unlike the {@link org.logicblaze.lingo.cache.TransactionIsolation#READ_COMMITTED}
 * isolation level where only committed data is read but the data can change during the transaction.
 *
 * @version $Revision$
 */
public class RepeatableReadThreadCache extends ThreadCache {
    private Map localReads;

    public RepeatableReadThreadCache(Cache behindCache) {
        super(behindCache);
        this.localReads = createMap();
    }

    public RepeatableReadThreadCache(Cache behindCache, Map localChanges, Map localRemoves, Map localReads) {
        super(behindCache, localChanges, localRemoves);
        this.localReads = localReads;
    }

    public Object remove(Object key) {
        VersionedValue entry = (VersionedValue) localReads.remove(key);
        if (entry != null) {
            localRemove(key, entry.getVersion());
            return entry.getValue();
        }
        else {
            return super.remove(key);
        }
    }

    public void resetLocalChanges() {
        super.resetLocalChanges();

        // lets always instantiate new objects to avoid
        // updating existing transaction logs
        localReads = createMap();
    }

    public TransactionalCacheCommand createTransactionCommand() {
        TransactionalCacheCommand answer = super.createTransactionCommand();
        Map readVersions = new HashMap(localReads.size());
        Set updatedKeys = answer.getUpdatedKeys();
        for (Iterator iter = localReads.entrySet().iterator(); iter.hasNext();) {
            Entry entry = (Entry) iter.next();
            Object key = entry.getKey();
            VersionedValue versionValue = (VersionedValue) entry.getValue();
            Object version = versionValue.getVersion();
            if (version != null && !updatedKeys.contains(key)) {
                readVersions.put(key, version);
            }
        }
        answer.setReadVersions(readVersions);
        return answer;
    }

    protected Object getLocalChangeValue(Object key) {
        VersionedValue entry = (VersionedValue) localReads.get(key);
        if (entry != null) {
            return entry.getValue();
        }
        else {
            Object value = super.getLocalChangeValue(key);
            if (value == null) {
                value = getDelegate().get(key);
            }
            localReads.put(key, createVersionedValue(key, value));
            return value;
        }
    }
}
