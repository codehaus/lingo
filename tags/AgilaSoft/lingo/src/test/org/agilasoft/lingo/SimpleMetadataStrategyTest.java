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
package org.agilasoft.lingo;

import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @version $Revision$
 */
public class SimpleMetadataStrategyTest extends TestCase {
    protected MetadataStrategy strategy;

    public void testExampleService_someOneWayMethod() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod(ExampleService.class, "someOneWayMethod"));
        assertEquals("oneway", true, metadata.isOneWay());
        assertEquals("param 0 remote", false, metadata.isRemoteParameter(0));
        assertEquals("param 1 remote", false, metadata.isRemoteParameter(1));
    }

    public void testExampleService_regularRPC() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod(ExampleService.class, "regularRPC"));
        assertEquals("oneway", false, metadata.isOneWay());
        assertEquals("param 0 remote", false, metadata.isRemoteParameter(0));
    }

    public void testExampleService_anotherRPC() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod(ExampleService.class, "anotherRPC"));
        assertEquals("oneway", false, metadata.isOneWay());
    }

    public void testExampleService_asyncRequestResponse() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod(ExampleService.class, "asyncRequestResponse"));
        assertEquals("oneway", true, metadata.isOneWay());
        assertEquals("param 0 remote", false, metadata.isRemoteParameter(0));
        assertEquals("param 1 remote", true, metadata.isRemoteParameter(1));
    }

    protected Method findMethod(Class type, String name) throws Exception {
        Method[] methods = type.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals(name)) {
                return method;
            }
        }
        fail("No such method called: " + name + " on type: " + type.getName());
        return null;
    }

    protected void setUp() throws Exception {
        strategy = createMetadataStrategy();
    }

    protected MetadataStrategy createMetadataStrategy() {
        return new SimpleMetadataStrategy(true);
    }
}
