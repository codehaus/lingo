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
import org.logicblaze.lingo.cache.impl.TransactionPolicy;

import javax.cache.Cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Revision$
 */
public class ClusteredCacheManager extends TransactionalCacheManager {
    private static final Log log = LogFactory.getLog(ClusteredCacheManager.class);

    private CommandExecutor executor;
    private Map locks = new HashMap();

    public ClusteredCacheManager(CommandExecutor executor) {
        this.executor = executor;
    }

    public ClusteredCacheManager(CommandExecutor executor, TransactionPolicy transactionPolicy) {
        super(transactionPolicy);
        this.executor = executor;
    }

    /**
     * Performs a thread local commit of all pending transactions on all the caches
     */
    public void commit() {
        Collection caches = getTransactionalCaches();
        CompositeCacheCommand command = createCommitCommand(caches);
        if (!command.isEmpty()) {
            String originator = command.getOriginator();
            Object lock = getLockForOriginator(originator);

            executor.execute(command);
            waitUntilTransactionApplied(lock, originator);

            resetLocalChanges(caches);
            handleCommitException(caches);
        }
    }

    public void execute(CacheCommand command) {
        super.execute(command);
        notifyOriginator(command.getOriginator());
    }

    // Implementation methods
    //-------------------------------------------------------------------------
    protected void notifyOriginator(String originator) {
        Object lock = null;
        synchronized (locks) {
            lock = locks.get(originator);
        }
        if (lock != null) {
            if (log.isTraceEnabled()) {
                log.trace("Notifying orginator: " + originator + " with lock: " + lock);
            }
            synchronized (lock) {
                lock.notifyAll();
            }
            // now lets remove the lock in case
            // the other thread misses the notify
            synchronized (locks) {
                locks.remove(originator);
            }
        }
        else {
            if (log.isTraceEnabled()) {
                log.trace("Igoring notification from originator from another JVM: " + originator);
            }
        }
    }

    protected Object getLockForOriginator(String originator) {
        synchronized (locks) {
            Object answer = locks.get(originator);
            if (answer == null) {
                answer = new Object();
                locks.put(originator, answer);
            }
            return answer;
        }
    }


    /**
     * Wait around up to some timeout period until the local transaction
     * has been distributed around the cluster and applied locally to the backing
     * cache
     */
    protected void waitUntilTransactionApplied(Object lock, String originator) {
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

        while (hasLockFor(originator)) {
            try {
                if (log.isTraceEnabled()) {
                    log.trace("Waiting for transaction to commit for originator: " + originator + " with lock: " + lock);
                }
                System.out.println("Waiting for transaction to commit ===>");
                synchronized (lock) {
                    // TODO we might wanna add some timeout or retries here
                    lock.wait(2000L);

                    if (log.isTraceEnabled()) {
                        log.trace("Transaction commited for originator: " + originator + " with lock: " + lock);
                    }
                    return;
                }
            }
            catch (InterruptedException e) {
                log.trace("Ignored interupted exception: " + e, e);
            }
        }
    }

    protected boolean hasLockFor(String originator) {
        synchronized (locks) {
            return locks.containsKey(originator);
        }
    }

    protected TransactionalCache createTransactionalCache(Cache backingCache, String name) {
        return new ClusteredCache(executor, backingCache, this);
    }
}
