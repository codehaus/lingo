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

import org.logicblaze.lingo.LingoRemoteInvocationFactory;
import org.logicblaze.lingo.SimpleMetadataStrategy;
import org.logicblaze.lingo.example.ExampleService;
import org.logicblaze.lingo.example.ExampleServiceImpl;
import org.logicblaze.lingo.example.TestResultListener;
import org.logicblaze.lingo.jms.impl.MultiplexingRequestor;

import javax.jms.Destination;
import javax.jms.Session;

import java.util.List;

/**
 * Uses the multiplexing, multi-threaded requestor
 *
 * @version $Revision$
 */
public class JmsMultiplexingRemotingTest extends JmsRemotingTest {

    public void testJmsProxyFactoryBeanAndAsyncServiceExporter() throws Throwable {
        ExampleService target = new ExampleServiceImpl();
        exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ExampleService.class);
        exporter.setService(target);
        configure(exporter);
        subscribeToQueue(exporter, getDestinationName());

        pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ExampleService.class);
        pfb.setServiceUrl("http://myurl");
        pfb.setRemoteInvocationFactory(new LingoRemoteInvocationFactory(new SimpleMetadataStrategy(true)));
        Requestor requestor = createRequestor(getDestinationName());
        pfb.setRequestor(requestor);
        configure(pfb);
        
        ExampleService proxy = (ExampleService) pfb.getObject();

        TestResultListener listener = new TestResultListener();
        proxy.asyncRequestResponse("IBM", listener);

        listener.waitForAsyncResponses(2);

        List results = listener.getResults();
        System.out.println("Found results: " + results);

        assertEquals("size of results: " + results, 2, results.size());
    }

    protected Requestor createRequestor(String name) throws Exception {
        Session session = createSession();
        JmsProducer producer = createJmsProducer();
        return new MultiplexingRequestor(connection, session, producer.getMessageProducer(), session.createQueue(name), null, false);
    }
}
