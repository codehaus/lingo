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

import edu.emory.mathcs.backport.java.util.concurrent.locks.Condition;
import edu.emory.mathcs.backport.java.util.concurrent.locks.Lock;
import edu.emory.mathcs.backport.java.util.concurrent.locks.ReentrantLock;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a client side {@link ConditionManager} which is used to
 * communicate with a possibly remote {@link ConditionServer}
 * 
 * @version $Revision$
 */
public class ConditionManagerImpl implements ConditionListener, ConnectionManager {

    private ConditionServer server;
    private Map map = new HashMap();

    /**
     * Gets the condition for the given ID lazily creating one if required.
     */
    public Condition getCondition(String id) {
        synchronized (map) {
            ConditionClient client = (ConditionClient) map.get(id);
            if (client == null) {
                client = createCondition(id);
                map.put(id, client);
            }
            return client;
        }
    }

    /**
     * Removes the condition of the given ID
     */
    public boolean removeCondition(String id) {
        synchronized (map) {
            return map.remove(id) != null;
        }
    }

    public void onSignal(String id) {
        synchronized (map) {
            ConditionClient client = (ConditionClient) map.get(id);
            if (client != null) {
                client.onSignal();
            }
        }
    }

    public void onSignalAll(String id) {
        ConditionClient client = (ConditionClient) map.get(id);
        if (client != null) {
            client.onSignalAll();
        }
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * Factory method to create a new condition
     */
    protected ConditionClient createCondition(String id) {
        return new ConditionClient(server, this, id, createLock(id));
    }

    /**
     * Factory method to change a lock
     */
    protected Lock createLock(String id) {
        return new ReentrantLock();
    }
}
