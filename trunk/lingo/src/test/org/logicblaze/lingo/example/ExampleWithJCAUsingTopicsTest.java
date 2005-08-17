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

import junit.framework.TestCase;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * A simple test case which tests the use of Lingo from Spring using the Spring XML deployment descriptor
 *
 * @version $Revision$
 */
public class ExampleWithJCAUsingTopicsTest extends ExampleTest {

    protected String getApplicationContextXml() {
        return "org/logicblaze/lingo/example/spring-with-jca-topics.xml";
    }
}
