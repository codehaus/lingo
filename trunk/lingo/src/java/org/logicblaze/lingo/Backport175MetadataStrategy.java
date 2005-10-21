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
package org.logicblaze.lingo;

import org.codehaus.backport175.reader.Annotations;
import org.logicblaze.lingo.annotations.Async;

import java.lang.reflect.Method;

/**
 * An implementation of {@link MetadataStrategy} which uses
 * the <a href="http://backport175.codehaus.org/">Backport175</a> 
 * library for working with annotations on Java 1.x and 5 platforms.
 * 
 * @version $Revision$
 */
public class Backport175MetadataStrategy extends CachingMetadataStrategySupport {

    private static final long serialVersionUID = 3266417144621024889L;

    // TODO delegate to simple strategy until we can use parameter based annotations
    private SimpleMetadataStrategy delegate = new SimpleMetadataStrategy();
    
    protected MethodMetadata createMethodMetadata(Method method) {
        boolean oneway = Annotations.isAnnotationPresent(Async.class, method);
        
        if (oneway) {
            System.out.println("#### found Async annotation!!!");
        }
        
        boolean[] remoteParams = null;
        Class[] parameterTypes = method.getParameterTypes();
        int size = parameterTypes.length;
        if (size > 0) {
            remoteParams = new boolean[size];
            for (int i = 0; i < size; i++) {
                remoteParams[i] = isRemoteParameter(method, parameterTypes[i], i);
            }
        }
        return new MethodMetadata(oneway, remoteParams);
   }

    protected boolean isRemoteParameter(Method method, Class parameterType, int index) {
        return delegate.isRemoteParameter(method, parameterType, index);
    }
}
