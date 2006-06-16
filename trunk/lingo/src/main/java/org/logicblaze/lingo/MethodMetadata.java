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

import java.io.Serializable;

/**
 * Represents the Message Exchange Pattern characteristics of a specific method
 * invocation.
 * 
 * @version $Revision$
 */
public class MethodMetadata implements Serializable {
    private static final long serialVersionUID = 7969481427004071349L;

    private boolean oneWay;
    private boolean stateful;
    private boolean endSession;
    private boolean[] remoteParameters;

    public MethodMetadata(boolean oneWay) {
        this(oneWay, null);
    }

    public MethodMetadata(boolean oneWay, boolean[] remoteParameters) {
        this.oneWay = oneWay;
        this.remoteParameters = remoteParameters;
    }

    public MethodMetadata(boolean oneWay, boolean[] remoteParameters, boolean stateful, boolean endSession) {
        this.oneWay = oneWay;
        this.remoteParameters = remoteParameters;
        this.stateful = stateful;
        this.endSession = endSession;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    /**
     * Returns true if the parameter at the given index is remote method
     */
    public boolean isRemoteParameter(int i) {
        return remoteParameters != null && remoteParameters[i];
    }

    /**
     * Should sticky load balancing be used to refer to a remote stateful
     * service
     */
    public boolean isStateful() {
        return stateful;
    }

    /**
     * Returns whether or not this method ends the session if used on a callback
     * object. e.g. does this method terminate the use of a callback object
     */
    public boolean isEndSession() {
        return endSession;
    }

}
