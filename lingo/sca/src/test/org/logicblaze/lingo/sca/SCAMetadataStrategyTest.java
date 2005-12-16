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
package org.logicblaze.lingo.sca;

import org.logicblaze.lingo.MethodMetadata;

/**
 * @version $Revision$
 */
public class SCAMetadataStrategyTest extends MetadataTestSupport {
    public void testExampleService_someOneWayMethod() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod("someOneWayMethod"));
        assertEquals("oneway", true, metadata.isOneWay());
        assertEquals("param 0 remote", false, metadata.isRemoteParameter(0));
        assertEquals("param 1 remote", false, metadata.isRemoteParameter(1));
    }

    public void testExampleService_regularRPC() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod("regularRPC"));
        assertEquals("oneway", false, metadata.isOneWay());
        assertEquals("param 0 remote", false, metadata.isRemoteParameter(0));
    }

    public void testExampleService_anotherRPC() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod("anotherRPC"));
        assertEquals("oneway", false, metadata.isOneWay());
    }

    public void testExampleService_asyncRequestResponse() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod("asyncRequestResponse"));
        assertEquals("oneway", true, metadata.isOneWay());
        assertEquals("param 0 remote", false, metadata.isRemoteParameter(0));
        assertEquals("param 1 remote", true, metadata.isRemoteParameter(1));
    }

    protected Class getServiceClass() {
        return ScaServiceImpl.class;
    }
}
