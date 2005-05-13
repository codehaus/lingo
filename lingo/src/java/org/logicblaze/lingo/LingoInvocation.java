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

import org.springframework.remoting.support.RemoteInvocation;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Extends the Spring remote invocation bean to add the extra Lingo metadata
 *
 * @version $Revision$
 */
public class LingoInvocation extends RemoteInvocation {
    private MethodMetadata metadata;

    public LingoInvocation(MethodInvocation methodInvocation, MethodMetadata metadata) {
        super(methodInvocation);
        this.metadata = metadata;
    }

    public LingoInvocation(String s, Class[] classes, Object[] objects, MethodMetadata metadata) {
        super(s, classes, objects);
        this.metadata = metadata;
    }

    /**
     * Returns the metadata associated with this invocation, such as whether or not it should
     * be considered a one-way invocation, which parameters should be considered remote (listeners)
     * versus which objects should be passed by value etc.
     *
     * @return
     */
    public MethodMetadata getMetadata() {
        return metadata;
    }
}
