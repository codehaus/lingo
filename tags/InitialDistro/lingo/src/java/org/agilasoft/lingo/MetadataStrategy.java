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

import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * Represents the metadata strategy used to decide which methods
 * are one-way and which methods have parameters which should not
 * be serialised etc.
 *
 * @version $Revision$
 */
public interface MetadataStrategy extends Serializable {

    /**
     * Generates the method specific metadata for the given method invocation
     */
    public MethodMetadata getMethodMetadata(Method method);
}
