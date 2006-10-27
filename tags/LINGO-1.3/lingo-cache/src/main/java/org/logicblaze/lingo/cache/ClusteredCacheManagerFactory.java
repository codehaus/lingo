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

import org.logicblaze.lingo.cache.impl.CommandExecutor;

/**
 * A Factory class for creating new {@link ClusteredCacheManager} instances.
 * 
 * @version $Revision$
 */
public class ClusteredCacheManagerFactory extends TransactionalCacheManagerFactory {
    private CommandExecutor executor;

    public ClusteredCacheManagerFactory() {
    }

    public ClusteredCacheManagerFactory(CommandExecutor executor) {
        this.executor = executor;
    }

    public CommandExecutor getExecutor() {
        if (executor == null) {
            executor = createExecutor();
        }
        return executor;
    }

    public void setExecutor(CommandExecutor executor) {
        this.executor = executor;
    }

    public TransactionalCacheManager createCacheManager(String regionName) {
        // TODO should we create a new executor for each cache?
        CommandExecutor value = getExecutor();
        if (value == null) {
            throw new IllegalArgumentException("An 'executor' property must be configured!");
        }
        return new ClusteredCacheManager(value);
    }

    /**
     * Provide a hook for derived classes to lazily create an executor
     */
    protected CommandExecutor createExecutor() {
        return null;
    }

}
