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
package org.logicblaze.lingo.jms;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * An exception caused by failing to process an inbound message response
 *
 * @version $Revision$
 */
public class FailedToProcessResponse extends RuntimeException {

    private static final long serialVersionUID = -6517904559060142134L;

    public FailedToProcessResponse(Message message, JMSException cause) {
        super("Failed to process response: " + message + ". Reason: " + cause, cause);
    }
}
