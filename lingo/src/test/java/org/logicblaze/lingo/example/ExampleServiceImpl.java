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

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.ClientContextHolder;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Set;
import java.util.Locale;

/**
 * @version $Revision$
 */
public class ExampleServiceImpl extends Assert implements ExampleService {
    private static final transient Log log = LogFactory.getLog(ExampleServiceImpl.class);

    private static volatile String lastMethod;
    private Object[] lastArguments;
    private int delay = 1000;
    private Set cheese;

    public synchronized void someOneWayMethod(String name, int age) {
        System.out.println("#### starting server side method for: " + name + " with age: " + age + " on instance: " + this);

        // TODO bad test case but lets simulate slow server with a sleep

        System.out.println("####Êsleeping for: " + delay + " millis to simulate slow server");

        try {
            Thread.sleep(delay);
        }
        catch (InterruptedException e) {
            log.error("Caught: " + e, e);
        }
        lastMethod = "someOneWayMethod";
        lastArguments = new Object[]{name, new Integer(age)};

        System.out.println("#### completed server side method for: " + name + " with age: " + age);
    }

    public int regularRPC(String name) {
        lastMethod = "regularRPC";
        lastArguments = new Object[]{name};
        return 55;
    }

    public void anotherRPC() throws Exception {
        lastMethod = "anotherRPC";
        lastArguments = new Object[0];
    }

    public void asyncRequestResponse(String stock, ResultListener listener) {
        lastMethod = "asyncRequestResponse";
        lastArguments = new Object[]{stock, listener};

        // lets invoke the listener with some responses
        try {
            System.out.println("##### inside asyncRequestResponse() and about to call onResult()");
            System.out.println("Remote proxy is: " + listener);

            listener.onResult("Price for " + stock + " is 10");
            try {
                Thread.sleep(delay);
            }
            catch (InterruptedException e) {
                // ignore
            }
            listener.onResult("Price for " + stock + " is 11");
            listener.stop();
        }
        catch (Exception e) {
            System.out.println("#### error: " + e);
            e.printStackTrace();
        }
    }

    public String whoAmI() {
        return ClientContextHolder.getUserName();
    }

    public Locale whereAmI() {
        return LocaleContextHolder.getLocale();
    }

    // Properties
    //-------------------------------------------------------------------------
    public String getLastMethod() {
        return lastMethod;
    }

    public Object[] getLastArguments() {
        return lastArguments;
    }

    public void assertOneWayCalled() {
        assertNotNull("lastMethod should not be null if we have been invoked on instance: " + this, lastMethod);
    }

    public void assertOneWayNotCompletedYet() {
        assertEquals("lastMethod should be null as we should not have waited for the method to complete for instance: " + this, null, lastMethod);
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    
    public Set getCheese() {
        return cheese;
    }

    public void setCheese(Set cheese) {
        this.cheese = cheese;
    }

    public void clear() {
        lastMethod = null;
    }
}
