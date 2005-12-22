/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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
package org.logicblaze.lingo.util.locks;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

import java.util.LinkedList;

/**
 * A server side version of a {@link Condition} which is used by a
 * {@link ConditionServer} to implement distributed conditions.
 * 
 * @version $Revision$
 */
public class ConditionController {

    private String id;
    private Lock lock;
    private LinkedList listeners = new LinkedList();
    private int signalCount;
    private int signalAllCount;

    public ConditionController(String id, Lock lock) {
        this.id = id;
        this.lock = lock;
    }

    /**
     * Returns whether or not this condition is active so that it can be cleaned
     * up in a pool.
     * 
     * @return true if this condition is active otherwise false indicating it
     *         can be deleted.
     */
    public boolean isActive() {
        if (signalCount == 0 && signalAllCount == 0) {
            lock.lock();
            try {
                return !listeners.isEmpty();
            }
            finally {
                lock.unlock();
            }
        }
        return true;
    }

    public void await(ConditionListener listener, long timeoutMillis) {
        // TODO need to timeout stuff...
        lock.lock();
        try {
            if (!pendingSignals(listener)) {
                listeners.add(listener);
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void signal() {
        lock.lock();
        try {
            if (listeners.isEmpty()) {
                // lets buffer up any missed notifies
                // in case we're failing over to a new server
                // instance and the signal() and await()
                // calls come in out of order
                signalCount++;
            }
            else {
                ConditionListener listener = (ConditionListener) listeners.remove();
                listener.onSignal(id);
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void signalAll() {
        lock.lock();
        try {
            if (listeners.isEmpty()) {
                // lets buffer up any missed notifies
                // in case we're failing over to a new server
                // instance and the signal() and await()
                // calls come in out of order
                signalAllCount++;
            }
            else {
                while (!listeners.isEmpty()) {
                    ConditionListener listener = (ConditionListener) listeners.remove();
                    listener.onSignalAll(id);
                }
            }
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Purges any inactive listeners from the Map
     */
    public void purge() {
        // TODO
    }

    /**
     * Returns true if there were pending signals
     */
    protected boolean pendingSignals(ConditionListener listener) {
        boolean answer = true;
        if (signalAllCount > 0) {
            signalAllCount--;
            listener.onSignalAll(id);
        }
        else if (signalCount > 0) {
            signalCount--;
            listener.onSignal(id);
        }
        else {
            answer = false;
        }
        return answer;
    }
}
