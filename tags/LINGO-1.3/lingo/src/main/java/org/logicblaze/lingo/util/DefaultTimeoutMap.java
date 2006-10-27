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
package org.logicblaze.lingo.util;

import edu.emory.mathcs.backport.java.util.concurrent.ScheduledExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @version $Revision$
 */
public class DefaultTimeoutMap implements TimeoutMap, Runnable {

    private static final Log log = LogFactory.getLog(DefaultTimeoutMap.class);

    private Map map = new HashMap();
    private SortedSet index = new TreeSet();
    private ScheduledExecutorService executor;
    private long purgePollTime;

    public DefaultTimeoutMap() {
        this(null, 1000L);
    }

    public DefaultTimeoutMap(ScheduledExecutorService executor, long requestMapPollTimeMillis) {
        this.executor = executor;
        this.purgePollTime = requestMapPollTimeMillis;
        schedulePoll();
    }

    public Object get(Object key) {
        TimeoutMapEntry entry = null;
        synchronized (map) {
            entry = (TimeoutMapEntry) map.get(key);
            if (entry == null) {
                return null;
            }
            index.remove(entry);
            updateExpireTime(entry);
            index.add(entry);
        }
        return entry.getValue();
    }

    public void put(Object key, Object value, long timeoutMillis) {
        TimeoutMapEntry entry = new TimeoutMapEntry(key, value, timeoutMillis);
        synchronized (map) {
            Object oldValue = map.put(key, entry);
            if (oldValue != null) {
                index.remove(oldValue);
            }
            updateExpireTime(entry);
            index.add(entry);
        }
    }

    public void remove(Object id) {
        synchronized (map) {
            TimeoutMapEntry entry = (TimeoutMapEntry) map.remove(id);
            if (entry != null) {
                index.remove(entry);
            }
        }
    }

    /**
     * Returns a copy of the keys in the map
     */
    public Object[] getKeys() {
        Object[] keys = null;
        synchronized (map) {
            Set keySet = map.keySet();
            keys = new String[keySet.size()];
            keySet.toArray(keys);
        }
        return keys;
    }

    /**
     * The timer task which purges old requests and schedules another poll
     */
    public void run() {
        purge();
        schedulePoll();
    }

    /**
     * Purges any old entries from the map
     */
    public void purge() {
        long now = currentTime();
        synchronized (map) {
            for (Iterator iter = index.iterator(); iter.hasNext();) {
                TimeoutMapEntry entry = (TimeoutMapEntry) iter.next();
                if (entry == null) {
                    break;
                }
                if (entry.getExpireTime() < now) {
                    if (isValidForEviction(entry)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Evicting inactive request for correlationID: " + entry);
                        }
                        map.remove(entry.getKey());
                        iter.remove();
                    }
                }
                else {
                    break;
                }
            }
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public long getPurgePollTime() {
        return purgePollTime;
    }

    /**
     * Sets the next purge poll time in milliseconds
     */
    public void setPurgePollTime(long purgePollTime) {
        this.purgePollTime = purgePollTime;
    }

    public ScheduledExecutorService getExecutor() {
        return executor;
    }

    /**
     * Sets the executor used to schedule purge events of inactive requests
     */
    public void setExecutor(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * lets schedule each time to allow folks to change the time at runtime
     */
    protected void schedulePoll() {
        if (executor != null) {
            executor.schedule(this, purgePollTime, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * A hook to allow derivations to avoid evicting the current entry
     * 
     * @param entry
     * @return
     */
    protected boolean isValidForEviction(TimeoutMapEntry entry) {
        return true;
    }

    protected void updateExpireTime(TimeoutMapEntry entry) {
        long now = currentTime();
        entry.setExpireTime(entry.getTimeout() + now);
    }

    protected long currentTime() {
        return System.currentTimeMillis();
    }
}
