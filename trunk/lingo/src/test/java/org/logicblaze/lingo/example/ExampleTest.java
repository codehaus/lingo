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

import org.logicblaze.lingo.SpringTestSupport;

/**
 * A simple test case which tests the use of Lingo from Spring using the Spring XML deployment descriptor
 *
 * @version $Revision$
 */
public class ExampleTest extends SpringTestSupport {

    public void TODO_testAsyncRequestResponse() throws Exception {
        // START SNIPPET: async

        // lets lookup the client in Spring
        // (we could be using DI here instead)
        ExampleService service = (ExampleService) getBean("client");

        // async request-response
        TestResultListener listener = new TestResultListener();
        service.asyncRequestResponse("IBM", listener);

        // the server side will now invoke the listener
        // objects's methods asynchronously
        // END SNIPPET: async

        listener.waitForAsyncResponses(2);

        System.out.println("Found results: " + listener.getResults());
    }

    public void testSyncRequestResponse() throws Exception {
        // START SNIPPET: simple

        // lets lookup the client in Spring
        // (we could be using DI here instead)
        ExampleService service = (ExampleService) getBean("client");

        // regular synchronous request-response
        int i = service.regularRPC("Foo");
        System.out.println("Found result: " + i);
    }

    public void testOneWayMethodCall() throws Exception {
        ExampleServiceImpl serverImpl = (ExampleServiceImpl) getBean("serverImpl");

        callOneWayMethod();

        serverImpl.assertOneWayCalled();
    }

    protected void callOneWayMethod() {
        ExampleService service = (ExampleService) getBean("client");

        long start = System.currentTimeMillis();
        service.someOneWayMethod("James", 35);
        logTime("Method invocation took", System.currentTimeMillis() - start);
        System.out.println("### client side method invoked");
    }

    protected void logTime(String text, long millis) {
        System.out.println(text + " took: " + (millis) + " milli(s) to complete");
    }

    protected void setUp() throws Exception {
        super.setUp();

        // lets force the creation of the server side
        getBean("server");
    }

    protected String getApplicationContextXml() {
        return "org/logicblaze/lingo/example/spring.xml";
    }
}
