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

import org.agilasoft.lingo.LingoRemoteInvocationFactory;
import org.agilasoft.lingo.SimpleMetadataStrategy;
import org.agilasoft.lingo.example.ExampleService;
import org.agilasoft.lingo.example.ExampleServiceImpl;
import org.agilasoft.lingo.example.TestResultListener;
import org.agilasoft.lingo.jms.impl.MultiplexingRequestor;

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
        exporter.setProducer(createJmsProducer());
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getQueueName());

        pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ExampleService.class);
        pfb.setServiceUrl("http://myurl");
        pfb.setRemoteInvocationFactory(new LingoRemoteInvocationFactory(new SimpleMetadataStrategy(true)));
        pfb.setRequestor(createRequestor(getQueueName()));
        pfb.afterPropertiesSet();

        ExampleService proxy = (ExampleService) pfb.getObject();

        TestResultListener listener = new TestResultListener();
        proxy.asyncRequestResponse("IBM", listener);

        listener.waitForAsyncResponses(2);

        List results = listener.getResults();
        System.out.println("Found results: " + results);

        assertEquals("size of results: " + results, 2, results.size());
    }

    protected Requestor createRequestor(String name) throws Exception {
        Session session = createQueueSession();
        JmsProducer producer = createJmsProducer();
        return new MultiplexingRequestor(session, producer, session.createQueue(name));
    }
}
