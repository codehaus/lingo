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

import junit.framework.TestCase;
import org.activemq.ActiveMQConnectionFactory;
import org.aopalliance.intercept.MethodInvocation;
import org.agilasoft.lingo.LingoRemoteInvocationFactory;
import org.agilasoft.lingo.MetadataStrategy;
import org.agilasoft.lingo.SimpleMetadataStrategy;
import org.agilasoft.lingo.beans.ITestBean;
import org.agilasoft.lingo.beans.TestBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.support.DefaultRemoteInvocationExecutor;
import org.springframework.remoting.support.RemoteInvocation;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import java.lang.reflect.InvocationTargetException;

/**
 * Uses the single threaded requestor
 *
 * @version $Revision$
 */
public class JmsRemotingTest extends TestCase {
    private ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
    private QueueConnection connection;
    private MetadataStrategy strategy;
    private JmsServiceExporter exporter;
    private JmsProxyFactoryBean pfb;

    public void testJmsProxyFactoryBeanAndServiceExporter() throws Throwable {
        TestBean target = new TestBean("myname", 99);
        exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setProducer(createJmsProducer());
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getQueueName());

        pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        pfb.setRequestor(createRequestor(getQueueName()));
        pfb.afterPropertiesSet();

        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        /*
        assertEquals(99, proxy.getAge());
        assertEquals(99, proxy.getAge());
        assertEquals(99, proxy.getAge());
        assertEquals(99, proxy.getAge());
        System.out.println("getting name: " + proxy.getName());
        */
        proxy.setAge(50);

        System.out.println("getting name: " + proxy.getName());
        int age = proxy.getAge();
        System.out.println("got age: " + age);

        assertEquals("myname", proxy.getName());
        assertEquals(50, proxy.getAge());

        try {
            proxy.exceptional(new IllegalStateException());
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException ex) {
            // expected
        }
        try {
            proxy.exceptional(new IllegalAccessException());
            fail("Should have thrown IllegalAccessException");
        } catch (IllegalAccessException ex) {
            // expected
        }
    }

    public void testJmsProxyFactoryBeanAndServiceExporterWithJMSException() throws Exception {
        TestBean target = new TestBean("myname", 99);
        final JmsServiceExporter exporter = new JmsServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setProducer(createJmsProducer());
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getQueueName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        pfb.setRequestor(createRequestor(getQueueName()));
        pfb.afterPropertiesSet();
        ITestBean proxy = (ITestBean) pfb.getObject();

        // lets force an exception by closing the session
        closeSession(pfb);
        try {
            proxy.setAge(50);
            fail("Should have thrown RemoteAccessException");
        } catch (RemoteAccessException ex) {
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
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getQueueName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        pfb.setRequestor(createRequestor(getQueueName()));
        pfb.setRemoteInvocationFactory(new LingoRemoteInvocationFactory(strategy) {
            public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
                RemoteInvocation invocation = super.createRemoteInvocation(methodInvocation);
                invocation.addAttribute("myKey", "myValue");
                try {
                    invocation.addAttribute("myKey", "myValue");
                    fail("Should have thrown IllegalStateException");
                } catch (IllegalStateException ex) {
                    // expected: already defined
                }
                assertNotNull(invocation.getAttributes());
                assertEquals(1, invocation.getAttributes().size());
                assertEquals("myValue", invocation.getAttributes().get("myKey"));
                assertEquals("myValue", invocation.getAttribute("myKey"));
                return invocation;
            }
        });

        pfb.afterPropertiesSet();
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
        exporter.afterPropertiesSet();
        subscribeToQueue(exporter, getQueueName());

        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("http://myurl");

        pfb.setRequestor(createRequestor(getQueueName()));
        pfb.setRemoteInvocationFactory(new LingoRemoteInvocationFactory(strategy) {
            public RemoteInvocation createRemoteInvocation(MethodInvocation methodInvocation) {
                RemoteInvocation invocation = super.createRemoteInvocation(methodInvocation);
                assertNull(invocation.getAttributes());
                assertNull(invocation.getAttribute("myKey"));
                return invocation;
            }
        });
        pfb.afterPropertiesSet();
        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
    }

    public void testJmsInvokerWithSpecialLocalMethods() throws Exception {
        String serviceUrl = "http://myurl";
        JmsProxyFactoryBean pfb = new JmsProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl(serviceUrl);

        pfb.setRequestor(createRequestor(getQueueName()));
        pfb.afterPropertiesSet();
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
        } catch (RemoteAccessException ex) {
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
        connectionFactory.stop();
        super.tearDown();
    }

    protected MetadataStrategy createMetadataStrategy() {
        return new SimpleMetadataStrategy(false);
    }


    protected void subscribeToQueue(JmsServiceExporter exporter, String queueName) throws JMSException {
        QueueSession serverSession = createQueueSession();
        Queue queue = serverSession.createQueue(queueName);
        MessageConsumer consumer = serverSession.createConsumer(queue);
        consumer.setMessageListener(exporter);
    }

    protected JmsProducer createJmsProducer() throws JMSException {
        return DefaultJmsProducer.newInstance(connectionFactory);
    }

    protected QueueSession createQueueSession() throws JMSException {
        return getConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    protected QueueConnection getConnection() throws JMSException {
        if (connection == null) {
            connection = connectionFactory.createQueueConnection();
            connection.start();
        }
        return connection;
    }

    protected Requestor createRequestor(String name) throws Exception {
        Session session = createQueueSession();
        JmsProducer producer = createJmsProducer();
        return new SingleThreadedRequestor(session, producer, session.createQueue(name));
    }

    protected void closeSession(JmsProxyFactoryBean factoryBean) throws JMSException {
        Requestor requestor = factoryBean.getRequestor();
        requestor.getSession().close();
    }

    protected String getQueueName() {
        return "test." + getClass().getName() + "." + getName();
    }
}