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
package org.logicblaze.lingo.sca;

import org.logicblaze.lingo.MetadataStrategy;
import org.logicblaze.lingo.MetadataStrategyHelper;

import java.lang.reflect.Method;

import junit.framework.TestCase;

/**
 * Base class for implementation inheritance
 * 
 * @version $Revision$
 */
public abstract class MetadataTestSupport extends TestCase {

    protected abstract Class getServiceClass();

    protected MetadataStrategy strategy;

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
        return MetadataStrategyHelper.newInstance();
    }

    protected Method findMethod(String name) throws Exception {
        return findMethod(getServiceClass(), name);
    }

}
