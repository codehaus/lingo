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
package org.logicblaze.lingo.lock;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Condition;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @version $Revision$
 */
public class DeadlockingWriteLock implements Lock {
    private ReentrantLock delegate;

    public void lock() {
        boolean locked = delegate.tryLock();
        if (!locked) {
            // lets check if there are folks waiting on the current owner
        }
    }

    public void lockInterruptibly() throws InterruptedException {
        delegate.lockInterruptibly();
    }

    public Condition newCondition() {
        return delegate.newCondition();
    }

    public boolean tryLock() {
        return delegate.tryLock();
    }

    public boolean tryLock(long arg0, TimeUnit arg1) throws InterruptedException {
        return delegate.tryLock(arg0, arg1);
    }

    public void unlock() {
        delegate.unlock();
    }
}
