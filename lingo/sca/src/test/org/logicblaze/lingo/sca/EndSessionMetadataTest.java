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

import org.logicblaze.lingo.MethodMetadata;

/**
 *
 * @version $Revision$
 */
public class EndSessionMetadataTest extends MetadataTestSupport {

    public void testEndSession() throws Exception {
        MethodMetadata metadata = strategy.getMethodMetadata(findMethod("stop"));
        assertEquals("isEndSession()", true, metadata.isEndSession());
        assertEquals("isOneWay()", false, metadata.isOneWay());
    }

    @Override
    protected Class getServiceClass() {
        return ResultListener.class;
    }
}
