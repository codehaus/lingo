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

import javax.cache.Cache;

/**
 * A clustered cache which replicates its state across other members of a cluster
 *
 * @version $Revision$
 */
public class ClusteredCache extends TransactionalCache {
    private CommandExecutor executor;

    public ClusteredCache(CommandExecutor executor, Cache behindCache, TransactionalCacheManager manager) {
        super(behindCache, manager);
        this.executor = executor;

    }

    public void commit() {
        // we need to send the command to the executor
        // and wait for a response
        CacheCommand command = createTransactionCommand();
        executor.execute(command);
        waitUntilTransactionApplied();
    }

    /**
     * Wait around up to some timeout period until the local transaction
     * has been distributed around the cluster and applied locally to the backing
     * cache
     */
    protected void waitUntilTransactionApplied() {
        // One strategy could be to not wait at all
        // and just use the local transaction view.
        // To do this we'd need to be able to snapshot the local changes
        // to a savepoint
        // so that a rollback after a commit() would only rollback the new changes
        // since the last savepoint. The savepoints can be discarded when the transaction
        // is eventually applied to the backing cache
        //
        // however we'd never be able to fail a transaction with
        // an optimistic transaction failure - but it would be super fast :)
    }
}
