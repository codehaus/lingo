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

import edu.emory.mathcs.backport.java.util.concurrent.locks.ReadWriteLock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.jms.JMSException;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation which just uses in-JVM locks.
 * 
 * @version $Revision$
 */
public class NonDeadlockLockManager implements LockManager {

    private Map locks = new HashMap();

    public ReadWriteLock getLock(String id) {
        ReadWriteLock answer = null;
        synchronized(locks) {
            answer = (ReadWriteLock) locks.get(id);
            if (answer == null) {
                answer = createLock(id);
                locks.put(id, answer);
            }
        }
        return answer ;
    }

    /**
     * Factory method to create a new read write lock
     */
    protected ReadWriteLock createLock(String id) {
        return new ReentrantReadWriteLock();
    }

    public void start() throws JMSException {
    }

    public void stop() throws JMSException {
    }

}
