/** 
 * 
 * Copyright 2005 AgilaSoft Ltd
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
package org.agilasoft.lingo.example;

/**
 * @version $Revision$
 */
public class ExampleServiceImpl implements ExampleService {
    private String lastMethod;
    private Object[] lastArguments;

    public void someOneWayMethod(String name, int age) {
        lastMethod = "someOneWayMethod";
        lastArguments = new Object[]{name, new Integer(age)};
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
                Thread.sleep(500);
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

    // Properties
    //-------------------------------------------------------------------------
    public String getLastMethod() {
        return lastMethod;
    }

    public Object[] getLastArguments() {
        return lastArguments;
    }
}
