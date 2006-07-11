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

import javax.cache.Cache;

/**
 * An exception thrown if a commit() fails due to optimistic transaction failures.
 *
 * @version $Revision$
 */
public class OptimisticTransactionException extends TransactionException {
    /**
     * 
     */
    private static final long serialVersionUID = -7074114353375834944L;
    private Cache cache;
    private Object key;
    private Object updateVersion;
    private Object currentVersion;

    public OptimisticTransactionException(Cache cache, Object key, Object updateVersion, Object currentVersion) {
        super("Failed to update key: " + key + " at version: " + currentVersion + " from update version: " + updateVersion);
        this.cache = cache;
        this.key = key;
        this.updateVersion = updateVersion;
        this.currentVersion = currentVersion;
    }

    public Cache getCache() {
        return cache;
    }

    public Object getKey() {
        return key;
    }

    /**
     * Returns the version at which a change was applied
     */
    public Object getUpdateVersion() {
        return updateVersion;
    }

    /**
     * Returns the current version of the entry
     */
    public Object getCurrentVersion() {
        return currentVersion;
    }
}
