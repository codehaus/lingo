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
package org.logicblaze.lingo;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory of remote invocation instances which includes the extra Lingo
 * metadata.
 * 
 * @version $Revision$
 */
public class LingoRemoteInvocationFactory implements RemoteInvocationFactory {
    private static final MethodMetadata DEFAULT_METHOD_METADATA = new MethodMetadata(false);

    private MetadataStrategy metadataStrategy;
    private Map cache = new HashMap();

    public LingoRemoteInvocationFactory(MetadataStrategy metadataStrategy) {
        this.metadataStrategy = metadataStrategy;
    }

    public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
        MethodMetadata metadata = getMethodMetadata(methodInvocation);
        return new LingoInvocation(methodInvocation, metadata);
    }

    public MetadataStrategy getMetadataStrategy() {
        return metadataStrategy;
    }

    public void setMetadataStrategy(MetadataStrategy metadataStrategy) {
        this.metadataStrategy = metadataStrategy;
    }

    protected synchronized MethodMetadata getMethodMetadata(MethodInvocation methodInvocation) {
        Method method = methodInvocation.getMethod();
        MethodMetadata answer = (MethodMetadata) cache.get(method);
        if (answer == null) {
            if (metadataStrategy == null) {
                // TODO we could use a singleton here
                answer = DEFAULT_METHOD_METADATA;
            }
            else {
                answer = metadataStrategy.getMethodMetadata(method);
            }
            cache.put(method, answer);
        }
        return answer;
    }

}
