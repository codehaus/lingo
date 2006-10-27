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

/**
 * A Strategy pattern describing how to join multiple results together, to decide when it is safe to unblock the calling
 * client thread and when a request can be considered to be complete.
 * 
 * @version $Revision$
 */
public interface ResultJoinStrategy {

    /**
     * Returns true if the calling thread should be unblocked after calling the method and so returning the current value of the 
     * invocation result; future responses could still mutate the result object if required. The default implementation
     * will return true after a single response is returned. 
     */
    public abstract boolean unblockCallerThread(RemoteInvocationResult response, int responseCount);

    /**
     * Returns true if there have been sufficient responses to remove the handler from the system.
     * Typically handlers should expire after some inactivity timeout.
     */
    public abstract boolean removeHandler(RemoteInvocationResult response, int responseCount);

    /**
     * Merges the new response with the previous response object. The default implementation just merges collections together
     * otherwise the first response wins.
     */
    public abstract RemoteInvocationResult mergeResponses(RemoteInvocationResult currentResult, RemoteInvocationResult newResult, int responseCount);

    /**
     * After we timeout in the ResultJoinHandler waiting for responses this method is called to
     * see if we should stop processing and return the results we have (or null if none).  It
     * passes in the current result and the approximate amount of time in milliseconds we
     * have waited so far.  Returns true to unblock and return the result otherwise false to
     * continue waiting.
     */
    public abstract boolean unblockAfterTimeout(RemoteInvocationResult currentResult, long waitSoFarMillis); 
}