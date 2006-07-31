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
import edu.emory.mathcs.backport.java.util.concurrent.locks.Condition;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;

import java.util.Date;

/**
 * Implements a client side proxy to a remote {@link Condition}
 * 
 * @version $Revision$
 */
public class ConditionClient implements Condition {

    private ConditionServer server;
    private ConditionListener listener;
    private String id;
    private Lock lock;
    private Condition localCondition;
    private transient Thread signalledThread;
    private long timeout = 5000L;

    public ConditionClient(ConditionServer server, ConditionListener listener, String id, Lock lock) {
        this.server = server;
        this.listener = listener;
        this.id = id;
        this.lock = lock;
    }

    public void await() throws InterruptedException {
        lock.lock();
        try {
            while (true) {
                // oneway
                server.await(id, listener, timeout);
                if (signalledThread == Thread.currentThread()) {
                    signalledThread = null;
                    break;
                }
                localCondition.await();
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void awaitUninterruptibly() {
    }

    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        long millis = unit.convert(timeout, TimeUnit.MILLISECONDS);
        lock.lock();
        try {
            // oneway
            server.await(id, listener, millis);
            if (signalledThread == Thread.currentThread()) {
                signalledThread = null;
            }
            else {
                localCondition.await();
            }
        }
        finally {
            lock.unlock();
        }
        return false;
    }

    public boolean awaitUntil(Date arg0) throws InterruptedException {
        return false;
    }

    public void signal() {
        server.signal(id);
    }

    public void signalAll() {
        server.signalAll(id);
    }

    public void onSignal() {
        lock.lock();
        signalledThread = Thread.currentThread();
        try {
            localCondition.signal();
        }
        finally {
            lock.unlock();
        }

    }

    public void onSignalAll() {
        lock.lock();
        signalledThread = Thread.currentThread();
        try {
            localCondition.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    // Properties
    // -------------------------------------------------------------------------

    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the failover time where if a server side condition server goes down,
     * signals are re-requested again.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
