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

import org.activemq.ActiveMQConnectionFactory;
import org.activemq.message.ActiveMQQueue;
import org.agilasoft.lingo.LingoRemoteInvocationFactory;
import org.agilasoft.lingo.MetadataStrategy;
import org.agilasoft.lingo.SimpleMetadataStrategy;
import org.agilasoft.lingo.beans.ITestBean;
import org.agilasoft.lingo.beans.TestBean;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocation;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import java.lang.reflect.InvocationTargetException;

/**
 * Uses the single threaded requestor
 *
 * @version $Revision$
 */
public class JmsRemotingTest extends JmsTestSupport {
    private MetadataStrategy strategy;

    protected JmsServiceExporter exporter;
    protected JmsProxyFactoryBean pfb;


    public void testJmsProxyFactoryBeanAndServiceExporter() throws Throwable {
        TestBean target = new TestBean("myname", 99);
        exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setProducer(createJmsProducer());
        configure(exporter);
        subscribeToQueue(exporter, getDestinationName());

        pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");
        pfb.setRequestor(createRequestor(getDestinationName()));
        configure(pfb);


        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
        proxy.setAge(50);

        System.out.println("getting name: " + proxy.getName());
        int age = proxy.getAge();
        System.out.println("got age: " + age);

        assertEquals("myname", proxy.getName());
        assertEquals(50, proxy.getAge());

        try {
            proxy.exceptional(new IllegalStateException());
            fail("Should have thrown IllegalStateException");
        }
        catch (IllegalStateException ex) {
            // expected
        }
        try {
            proxy.exceptional(new IllegalAccessException());
            fail("Should have thrown IllegalAccessException");
        }
        catch (IllegalAccessException ex) {
            // expected
        }
    }

    public void testJmsProxyFactoryBeanAndServiceExporterUsingSimpleConfiguration() throws Throwable {
        TestBean target = new TestBean("myname", 99);
        exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setConnectionFactory(connectionFactory);
        configure(exporter);
        subscribeToQueue(exporter, getDestinationName());

        pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setConnectionFactory(connectionFactory);
        pfb.setDestination(new ActiveMQQueue(getDestinationName()));
        configure(pfb);

        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
        proxy.setAge(50);

        System.out.println("getting name: " + proxy.getName());
        int age = proxy.getAge();
        System.out.println("got age: " + age);

        assertEquals("myname", proxy.getName());
        assertEquals(50, proxy.getAge());
    }

    public void testJmsProxyFactoryBeanAndServiceExporterWithOneWays() throws Throwable {
        TestBean target = new TestBean("myname", 99);
        exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setProducer(createJmsProducer());
        configure(exporter);
        subscribeToQueue(exporter, getDestinationName());

        pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");
        pfb.setRequestor(createRequestor(getDestinationName()));
        pfb.setRemoteInvocationFactory(new LingoRemoteInvocationFactory(new SimpleMetadataStrategy(true)));
        configure(pfb);

        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
        proxy.setAge(50);

        System.out.println("getting name: " + proxy.getName());
        int age = proxy.getAge();
        System.out.println("got age: " + age);

        assertEquals("myname", proxy.getName());
        assertEquals(50, proxy.getAge());

        try {
            proxy.exceptional(new IllegalStateException());
            fail("Should have thrown IllegalStateException");
        }
        catch (IllegalStateException ex) {
            // expected
        }
        try {
            proxy.exceptional(new IllegalAccessException());
            fail("Should have thrown IllegalAccessException");
        }
        catch (IllegalAccessException ex) {
            // expected
        }
    }

    public void testJmsProxyFactoryBeanAndServiceExporterWithJMSException() throws Exception {
        TestBean target = new TestBean("myname", 99);
        final JmsServiceExporter exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setProducer(createJmsProducer());
        configure(exporter);
        subscribeToQueue(exporter, getDestinationName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        pfb.setRequestor(createRequestor(getDestinationName()));
        configure(pfb);
        ITestBean proxy = (ITestBean) pfb.getObject();

        // lets force an exception by closing the session
        closeSession(pfb);
        try {
            proxy.setAge(50);
            fail("Should have thrown RemoteAccessException");
        }
        catch (RemoteAccessException ex) {
            // expected
            assertTrue(ex.getCause() instanceof JMSException);
        }
    }

    public void testJmsProxyFactoryBeanAndServiceExporterWithInvocationAttributes() throws Exception {
        TestBean target = new TestBean("myname", 99);
        final JmsServiceExporter exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setProducer(createJmsProducer());
        exporter.setRemoteInvocationExecutor(new DefaultRemoteInvocationExecutor() {
            public Object invoke(RemoteInvocation invocation, Object targetObject)
                    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
                assertNotNull(invocation.getAttributes());
                assertEquals(1, invocation.getAttributes().size());
                assertEquals("myValue", invocation.getAttributes().get("myKey"));
                assertEquals("myValue", invocation.getAttribute("myKey"));
                return super.invoke(invocation, targetObject);
            }
        });
        configure(exporter);
        subscribeToQueue(exporter, getDestinationName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");
        pfb.setRequestor(createRequestor(getDestinationName()));
        pfb.setRemoteInvocationFactory(new LingoRemoteInvocationFactory(strategy) {
            public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
                RemoteInvocation invocation = super.createRemoteInvocation(methodInvocation);
                invocation.addAttribute("myKey", "myValue");
                try {
                    invocation.addAttribute("myKey", "myValue");
                    fail("Should have thrown IllegalStateException");
                }
                catch (IllegalStateException ex) {
                    // expected: already defined
                }
                assertNotNull(invocation.getAttributes());
                assertEquals(1, invocation.getAttributes().size());
                assertEquals("myValue", invocation.getAttributes().get("myKey"));
                assertEquals("myValue", invocation.getAttribute("myKey"));
                return invocation;
            }
        });
        configure(pfb);

        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
    }

    public void testJmsProxyFactoryBeanAndServiceExporterWithCustomInvocationObject() throws Exception {
        TestBean target = new TestBean("myname", 99);
        final JmsServiceExporter exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setProducer(createJmsProducer());
        exporter.setRemoteInvocationExecutor(new DefaultRemoteInvocationExecutor() {
            public Object invoke(RemoteInvocation invocation, Object targetObject)
                    throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
                assertNull(invocation.getAttributes());
                assertNull(invocation.getAttribute("myKey"));
                return super.invoke(invocation, targetObject);
            }
        });
        configure(exporter);
        subscribeToQueue(exporter, getDestinationName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");
        pfb.setRequestor(createRequestor(getDestinationName()));
        pfb.setRemoteInvocationFactory(new LingoRemoteInvocationFactory(strategy) {
            public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
                RemoteInvocation invocation = super.createRemoteInvocation(methodInvocation);
                assertNull(invocation.getAttributes());
                assertNull(invocation.getAttribute("myKey"));
                return invocation;
            }
        });
        configure(pfb);

        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
    }

    public void testJmsInvokerWithSpecialLocalMethods() throws Exception {
        String serviceUrl = "http://myurl";
        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl(serviceUrl);
        pfb.setRequestor(createRequestor(getDestinationName()));
        configure(pfb);

        ITestBean proxy = (ITestBean) pfb.getObject();

        // shouldn't go through to remote service
        assertTrue(proxy.toString().indexOf("JMS invoker") != -1);
        assertTrue(proxy.toString().indexOf(serviceUrl) != -1);
        assertEquals(proxy.hashCode(), proxy.hashCode());
        assertTrue(proxy.equals(proxy));

        // lets force an exception by closing the session
        closeSession(pfb);
        try {
            proxy.setAge(50);
            fail("Should have thrown RemoteAccessException");
        }
        catch (RemoteAccessException ex) {
            // expected
            assertTrue(ex.getCause() instanceof JMSException);
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        strategy = createMetadataStrategy();
    }

    protected void tearDown() throws Exception {
        if (connection != null) {
            connection.close();
        }
        if (connectionFactory instanceof ActiveMQConnectionFactory) {
            ActiveMQConnectionFactory amqConnectionFactory = (ActiveMQConnectionFactory) connectionFactory;
            amqConnectionFactory.stop();
        }
        super.tearDown();
    }


    protected void configure(JmsServiceExporter exporter) throws Exception {
        exporter.afterPropertiesSet();
    }

    protected void configure(JmsProxyFactoryBean pfb) throws JMSException {
        pfb.afterPropertiesSet();
    }

    protected MetadataStrategy createMetadataStrategy() {
        return new SimpleMetadataStrategy(false);
    }


    protected void subscribeToQueue(JmsServiceExporter exporter, String queueName) throws JMSException {
        Session serverSession = createSession();
        Queue queue = serverSession.createQueue(queueName);
        MessageConsumer consumer = serverSession.createConsumer(queue);
        consumer.setMessageListener(exporter);
    }

}