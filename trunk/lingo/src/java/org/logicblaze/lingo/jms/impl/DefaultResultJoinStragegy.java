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
package org.logicblaze.lingo.jms.impl;

import org.springframework.remoting.support.RemoteInvocationResult;

import java.util.Collection;
import java.util.Map;

/**
 * A default implementation of {@link ResultJoinStrategy} which will
 * add together collection results, unblock the calling thread when there is a single result
 * and let the handler timeout with whatever the default handler timeout policy is.
 * 
 * @version $Revision$
 */
public class DefaultResultJoinStragegy implements ResultJoinStrategy {

    /* (non-Javadoc)
     * @see org.logicblaze.lingo.jms.impl.ResultJoinStrategy#unblockCallerThread(org.springframework.remoting.support.RemoteInvocationResult, int)
     */
    public boolean unblockCallerThread(RemoteInvocationResult response, int responseCount) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.logicblaze.lingo.jms.impl.ResultJoinStrategy#removeHandler(org.springframework.remoting.support.RemoteInvocationResult, int)
     */
    public boolean removeHandler(RemoteInvocationResult response, int responseCount) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.logicblaze.lingo.jms.impl.ResultJoinStrategy#mergeResponses(org.springframework.remoting.support.RemoteInvocationResult, org.springframework.remoting.support.RemoteInvocationResult, int)
     */
    public RemoteInvocationResult mergeResponses(RemoteInvocationResult currentResult, RemoteInvocationResult newResult, int responseCount) {
        Object value = currentResult.getValue();
        Object newValue = newResult.getValue();
        if (value instanceof Collection) {
            Collection coll = (Collection) value;
            if (newValue instanceof Collection) {
                coll.addAll((Collection) newValue);
            }
            else {
                coll.add(newValue);
            }
        }
        else if (value instanceof Map && newValue instanceof Map) {
            Map map = (Map) value;
            map.putAll((Map) newValue);
        }
        return currentResult;
    }
}
