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

/**
 *
 * @version $Revision$
 */
public class SCAMetadataStrategyWithNoAnnotationsTest extends SCAMetadataStrategyTest {

    @Override
    protected Class getServiceClass() {
        return ExampleServiceImpl.class;
    }

    @Override
    protected MetadataStrategy createMetadataStrategy() {
        MetadataStrategy metadataStrategy = MetadataStrategyHelper.newInstance();
        assertTrue("Should be an instance of SCAMetadataStrategy", metadataStrategy instanceof SCAMetadataStrategy);
        SCAMetadataStrategy strategy = (SCAMetadataStrategy) metadataStrategy;
        strategy.setOneWayForVoidMethods(true);
        return metadataStrategy;
    }

    
}
