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


/**
 * A simple test case which tests the use of Lingo from Spring using the Spring XML deployment descriptor
 *
 * @version $Revision$
 */
public class ExampleWithDurableTopicTest extends ExampleTest {

    protected void setUp() throws Exception {
        super.setUp();
        
        // With ActiveMQ activation of topic subscriptions is not synchronous, so if we send a message immediately
        // the subscription may not yet be active - so lets sleep a little to ensure the subscription is active
        
        Thread.sleep(1000);
    }
    
    protected String getApplicationContextXml() {
        return "org/logicblaze/lingo/example/spring-with-durable-topic.xml";
    }
}
