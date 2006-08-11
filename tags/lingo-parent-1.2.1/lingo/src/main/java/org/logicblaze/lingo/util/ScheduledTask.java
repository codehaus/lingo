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

/**
 * A simple POJO which is useful for wiring together tasks in an IoC type way.
 * 
 * @version $Revision$
 */
public class ScheduledTask implements Runnable {

    private static final Log log = LogFactory.getLog(ScheduledTask.class);

    private ScheduledExecutorService executor;
    private Runnable task;
    private long timeoutMillis;

    public ScheduledTask(Runnable task, ScheduledExecutorService executor, long millis) {
        this.task = task;
        this.executor = executor;
        this.timeoutMillis = millis;
        scheduleTask();
    }

    /**
     * Performs the task and then schedules another execution of the task.
     */
    public void run() {
        try {
            task.run();
        }
        catch (RuntimeException e) {
            log.warn("Caught exception while running task: " + task + ". Detail: " + e, e);
        }
        scheduleTask();
    }

    public void stop() {
        executor = null;
    }

    // Properties
    // -------------------------------------------------------------------------
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    // Implemetation methods
    // -------------------------------------------------------------------------
    protected void scheduleTask() {
        if (executor != null) {
            executor.schedule(this, timeoutMillis, TimeUnit.MILLISECONDS);
        }
    }

}
