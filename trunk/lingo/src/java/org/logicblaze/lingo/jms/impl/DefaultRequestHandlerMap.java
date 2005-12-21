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
package org.logicblaze.lingo.jms.impl;

import edu.emory.mathcs.backport.java.util.concurrent.ScheduledExecutorService;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.jms.ReplyHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 
 * @version $Revision$
 */
public class DefaultRequestHandlerMap implements RequestHandlerMap, Runnable {

    private static final Log log = LogFactory.getLog(DefaultRequestHandlerMap.class);

    private Map map = new HashMap();
    private SortedSet index = new TreeSet();
    private ScheduledExecutorService executor;
    private long requestMapPollTimeMillis;

    public DefaultRequestHandlerMap() {
        this(new ScheduledThreadPoolExecutor(1), 1000L);
    }

    public DefaultRequestHandlerMap(long requestMapPollTimeMillis) {
        this(new ScheduledThreadPoolExecutor(1), requestMapPollTimeMillis);
    }

    public DefaultRequestHandlerMap(ScheduledExecutorService executor, long requestMapPollTimeMillis) {
        this.executor = executor;
        this.requestMapPollTimeMillis = requestMapPollTimeMillis;
        schedulePoll();
    }

    public ReplyHandler get(String correlationID) {
        Entry entry = null;
        synchronized (this) {
            entry = (Entry) map.get(correlationID);
            if (entry == null) {
                return null;
            }
            index.remove(entry);
            updateExpireTime(entry);
            index.add(entry);
        }
        return entry.getHandler();
    }

    public void put(String correlationID, ReplyHandler handler, long timeout) {
        Entry entry = new Entry(correlationID, handler, timeout);
        synchronized (this) {
            Object oldValue = map.put(correlationID, entry);
            if (oldValue != null) {
                index.remove(oldValue);
            }
            updateExpireTime(entry);
            index.add(entry);
        }
    }

    public void remove(String correlationID) {
        synchronized (this) {
            Entry entry = (Entry) map.remove(correlationID);
            if (entry != null) {
                index.remove(entry);
            }
        }
    }

    /**
     * The timer task which purges old requests and schedules another poll
     */
    public void run() {
        purgeOldRequests();
        schedulePoll();
    }

    /**
     * Purges any old entries in the map
     */
    public void purgeOldRequests() {
        log.debug("purging old requests from RequestMap");

        long now = currentTime();
        while (true) {
            Entry entry = null;
            synchronized (this) {
                if (!index.isEmpty()) {
                    entry = (Entry) index.first();
                }
            }
            if (entry == null) {
                break;
            }
            if (entry.getExpireTime() < now) {
                evict(entry.getId());
            }
            else {
                break;
            }
        }
    }

    // Properties
    // -------------------------------------------------------------------------
    public long getRequestMapPollTimeMillis() {
        return requestMapPollTimeMillis;
    }

    public void setRequestMapPollTimeMillis(long requestMapPollTimeMillis) {
        this.requestMapPollTimeMillis = requestMapPollTimeMillis;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * lets schedule each time to allow folks to change the time at runtime
     */
    protected void schedulePoll() {
        executor.schedule(this, requestMapPollTimeMillis, TimeUnit.MILLISECONDS);
    }

    protected void evict(String id) {
        if (log.isDebugEnabled()) {
            log.debug("Evicting inactive request for correlationID: " + id);
        }
        System.out.println("Evicting inactive request for correlationID: " + id);
        remove(id);
    }

    protected void updateExpireTime(Entry entry) {
        long now = currentTime();
        entry.setExpireTime(entry.getTimeout() + now);
    }

    protected long currentTime() {
        return System.currentTimeMillis();
    }

    protected static class Entry implements Comparable {
        private String id;
        private ReplyHandler handler;
        private long timeout;
        private long expireTime;

        public Entry(String id, ReplyHandler handler, long timeout) {
            this.id = id;
            this.handler = handler;
            this.timeout = timeout;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        public ReplyHandler getHandler() {
            return handler;
        }

        public void setHandler(ReplyHandler handler) {
            this.handler = handler;
        }

        public long getTimeout() {
            return timeout;
        }

        public void setTimeout(long timeout) {
            this.timeout = timeout;
        }

        public int compareTo(Object that) {
            if (this == that) {
                return 0;
            }
            if (that instanceof Entry) {
                return compareTo((Entry) that);
            }
            return 1;
        }

        public int compareTo(Entry that) {
            long diff = this.expireTime - that.expireTime;
            if (diff > 0) {
                return 1;
            }
            else if (diff < 0) {
                return -1;
            }
            return this.id.compareTo(that.id);
        }

        public String toString() {
            return "Entry for id: " + id;
        }
    }
}
