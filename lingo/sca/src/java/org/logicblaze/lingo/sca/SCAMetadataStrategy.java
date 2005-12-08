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

import org.logicblaze.lingo.CachingMetadataStrategySupport;
import org.logicblaze.lingo.MetadataStrategy;
import org.logicblaze.lingo.MethodMetadata;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.OneWay;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * An implementation of {@link MetadataStrategy} which uses
 * the <a href="http://incubator.apache.org/tuscany/">SCA</a> 
 * annotations to configure the metadata strategy.
 * 
 * @version $Revision$
 */
public class SCAMetadataStrategy extends CachingMetadataStrategySupport {

    private static final long serialVersionUID = 1146367535939318426L;

    protected MethodMetadata createMethodMetadata(Method method) {
        Annotation annotation = method.getAnnotation(OneWay.class);
        boolean oneway = annotation != null;
        
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
        Annotation annotation = parameterType.getAnnotation(Callback.class);
        return annotation != null;
    }
}
