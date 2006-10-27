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

import org.springframework.remoting.support.RemoteInvocationResult;

import java.util.Collection;
import java.util.Map;

/**
 * A default implementation of {@link ResultJoinStrategy} which will add
 * together collection results, unblock the calling thread when there is a
 * single result and let the handler timeout with whatever the default handler
 * timeout policy is.
 * 
 * @version $Revision$
 */
public class DefaultResultJoinStrategy implements ResultJoinStrategy {

    private int minimumResults = 1;
    private int maximumResults = 0;

    /*
     * (non-Javadoc)
     * 
     * @see org.logicblaze.lingo.jms.impl.ResultJoinStrategy#unblockCallerThread(org.springframework.remoting.support.RemoteInvocationResult,
     *      int)
     */
    public boolean unblockCallerThread(RemoteInvocationResult response, int responseCount) {
        return responseCount >= minimumResults;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.logicblaze.lingo.jms.impl.ResultJoinStrategy#removeHandler(org.springframework.remoting.support.RemoteInvocationResult,
     *      int)
     */
    public boolean removeHandler(RemoteInvocationResult response, int responseCount) {
        if (maximumResults > 0) {
            return responseCount >= maximumResults;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.logicblaze.lingo.jms.impl.ResultJoinStrategy#mergeResponses(org.springframework.remoting.support.RemoteInvocationResult,
     *      org.springframework.remoting.support.RemoteInvocationResult, int)
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
    
    /*
     * (non-Javadoc)
     * 
     * @see org.logicblaze.lingo.jms.impl.ResultJoinStrategy#unblockAfterTimeout(RemoteInvocationResult currentResult, long waitSoFarMillis)
     */
	public boolean unblockAfterTimeout(RemoteInvocationResult currentResult,
			long waitSoFarMillis) {
		if (currentResult == null) {
			// if we don't have a result then continue waiting
			return false;
		} else if (waitSoFarMillis > 2000) {
			// we've exceeded the default timeout
			return true;
		} else {
			// not timed out yet
			return false;
		}
	}
	
    public int getMaximumResults() {
        return maximumResults;
    }

    public void setMaximumResults(int maximumResults) {
        this.maximumResults = maximumResults;
    }

    public int getMinimumResults() {
        return minimumResults;
    }

    public void setMinimumResults(int minimiumResults) {
        this.minimumResults = minimiumResults;
    }

}
