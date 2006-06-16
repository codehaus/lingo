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

import edu.emory.mathcs.backport.java.util.concurrent.ScheduledExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

import org.logicblaze.lingo.util.DefaultTimeoutMap;
import org.logicblaze.lingo.util.ScheduledTask;
import org.logicblaze.lingo.util.TimeoutMap;
import org.logicblaze.lingo.util.TimeoutMapEntry;

/**
 * A server side implementation of ConditionServer.
 * 
 * @version $Revision$
 */
public class ConditionServerImpl implements ConditionServer {

    private TimeoutMap map = new DefaultTimeoutMap() {
        protected boolean isValidForEviction(TimeoutMapEntry entry) {
            ConditionController condition = (ConditionController) entry.getValue();
            return !condition.isActive();
        }
    };
    private ScheduledTask schedule;
    private final long inactivityTimeout;

    public ConditionServerImpl(ScheduledExecutorService executor, long inactivityTimeout) {
        this.inactivityTimeout = inactivityTimeout;
        this.schedule = new ScheduledTask(map, executor, inactivityTimeout);
    }

    public void await(String id, ConditionListener listener, long timeoutMillis) {
        ConditionController condition = getCondition(id);
        condition.await(listener, timeoutMillis);
    }

    public void signal(String id) {
        ConditionController condition = getCondition(id);
        condition.signal();
    }

    public void signalAll(String id) {
        ConditionController condition = getCondition(id);
        condition.signalAll();
    }

    public void purge() {
        Object[] keys = map.getKeys();
        for (int i = 0; i < keys.length; i++) {
            String id = (String) keys[i];
            ConditionController condition = (ConditionController) map.get(id);
            if (condition != null) {
                condition.purge();
            }
        }

        // now lets purge any dead controllers
        map.purge();
    }

    public void stop() {
        schedule.stop();
    }

    protected ConditionController getCondition(String id) {
        synchronized (map) {
            ConditionController answer = (ConditionController) map.get(id);
            if (answer == null) {
                answer = createCondition(id);
                map.put(id, answer, inactivityTimeout);
            }
            return answer;
        }
    }

    protected ConditionController createCondition(String id) {
        return new ConditionController(id, createLock(id));
    }

    /**
     * Factory method to change a lock
     */
    protected Lock createLock(String id) {
        return new ReentrantLock();
    }
}
