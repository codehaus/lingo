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
package org.agilasoft.lingo.jms;

import org.agilasoft.lingo.jms.MultiplexingRequestor;
import org.agilasoft.lingo.jms.Requestor;

import javax.jms.Session;

/**
 * Uses the multiplexing, multi-threaded requestor
 *
 * @version $Revision$
 */
public class JmsMultiplexingRemotingTest extends JmsRemotingTest {
    protected Requestor createRequestor(String name) throws Exception {
        Session session = createQueueSession();
        JmsProducer producer = createJmsProducer();
        return new MultiplexingRequestor(session, producer, session.createQueue(name));
    }
}
