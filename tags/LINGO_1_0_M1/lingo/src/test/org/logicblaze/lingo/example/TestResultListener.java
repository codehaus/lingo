/** 
 * 
 * Copyright 2005 LogicBlaze, Inc.
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
package org.logicblaze.lingo.example;

import java.util.ArrayList;
import java.util.List;

/**
 * @version $Revision$
 */
public class TestResultListener implements ResultListener {
    private List results = new ArrayList();
    private Object semaphore = new Object();
    private boolean stopped;
    private Exception onException;

    public synchronized void onResult(String data) {
        results.add(data);
        synchronized (semaphore) {
            semaphore.notifyAll();
        }
    }

    // lifecycle end methods
    public void stop() {
        stopped = true;
    }

    public void onException(Exception e) {
        onException = e;
    }


    public Exception getOnException() {
        return onException;
    }

    public List getResults() {
        return results;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void waitForAsyncResponses(int messageCount) {
        System.out.println("Waiting for: " + messageCount + " responses to arrive");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {
            try {
                if (hasReceivedResponses(messageCount)) {
                    break;
                }
                synchronized (semaphore) {
                    semaphore.wait(1000);
                }
            }
            catch (InterruptedException e) {
                System.out.println("Caught: " + e);
            }
        }
        long end = System.currentTimeMillis() - start;

        System.out.println("End of wait for " + end + " millis");
    }

    protected boolean hasReceivedResponse() {
        return results.isEmpty();
    }

    protected synchronized boolean hasReceivedResponses(int messageCount) {
        return results.size() >= messageCount;
    }

}
