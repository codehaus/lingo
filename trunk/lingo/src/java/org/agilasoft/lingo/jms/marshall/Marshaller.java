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
package org.agilasoft.lingo.jms.marshall;

import org.agilasoft.lingo.LingoInvocation;
import org.agilasoft.lingo.jms.Requestor;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @version $Revision$
 */
public interface Marshaller {
    /**
     * Creates the request message
     *
     * @param requestor
     * @param invocation the remote invocation to send
     * @throws javax.jms.JMSException if the message could not be created
     */
    Message createRequestMessage(Requestor requestor, LingoInvocation invocation) throws JMSException;

    /**
     * Extracts the invocation result from the response message
     *
     * @param message the response message
     * @return the invocation result
     * @throws javax.jms.JMSException is thrown if a JMS exception occurs
     */
    RemoteInvocationResult extractInvocationResult(Message message) throws JMSException;

    /**
     * Read a RemoteInvocation from the given JMS message
     *
     * @param message current JMS message
     * @return the RemoteInvocation object
     */
    RemoteInvocation readRemoteInvocation(Message message) throws JMSException;
}
