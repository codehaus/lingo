/**
 * 
 * Copyright 2005 LogicBlaze, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 */
package org.logicblaze.lingo.jmx.remote;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.monitor.GaugeMonitor;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import junit.framework.TestCase;
import org.activemq.broker.BrokerFactory;
import org.activemq.broker.BrokerService;

/**
 
 */
public class JmxRemoteTest extends TestCase{
    private MBeanServer server;
    private BrokerService broker;
    private JMXConnectorServer connectorServer;
    private JMXConnector connector;

    protected void setUp() throws Exception{
        broker=BrokerFactory.createBroker(new URI("broker:(tcp://localhost:6000)/localhost?persistent=false"));
        broker.start();
        server=MBeanServerFactory.createMBeanServer();
        //register a DynamicService from mx4j
        SimpleService service = new SimpleService();
        // start the connector server
        //JMXServiceURL url=new JMXServiceURL("service:jmx:jms:///vm://localhost");
        JMXServiceURL url=new JMXServiceURL("service:jmx:jms:///tcp://localhost:6000");
        // Create and start the RMIConnectorServer
        Map env=new HashMap();
        env.put("jmx.remote.protocol.provider.pkgs","org.logicblaze.lingo.jmx.remote.provider");
        connectorServer=JMXConnectorServerFactory.newJMXConnectorServer(url,env,server);
        connectorServer.start();
        // Connect a JSR 160 JMXConnector to the server side
        connector=JMXConnectorFactory.connect(url,env);
        
    }

    protected void tearDown() throws Exception{
        connector.close();
        connectorServer.stop();
        broker.stop();
    }

    public void testSimpleRemoteJmx() throws Exception{
        // Retrieve an MBeanServerConnection that represent the MBeanServer the remote
        // connector server is bound to
        MBeanServerConnection connection=connector.getMBeanServerConnection();
        ObjectName queryName=new ObjectName("*:*");
        java.util.Set names=connection.queryNames(queryName,null);
        for(Iterator iter=names.iterator();iter.hasNext();){
            ObjectName name=(ObjectName) iter.next();
            MBeanInfo beanInfo=connection.getMBeanInfo(name);
            System.out.println("bean info = "+beanInfo.getDescription());
            System.out.println("attrs = " + beanInfo.getAttributes());
        }
    }
    
    public void testNotificationsJmx() throws Exception{
        SimpleService serviceMBean=new SimpleService();
        ObjectName serviceName=new ObjectName("examples","mbean","simple");
        server.registerMBean(serviceMBean,serviceName);
        // Now let's register a Monitor
        // We would like to know if we have peaks in activity, so we can use JMX's
        // GaugeMonitor
        GaugeMonitor monitorMBean=new GaugeMonitor();
        ObjectName monitorName=new ObjectName("examples","monitor","gauge");
        server.registerMBean(monitorMBean,monitorName);
        // Setup the monitor: we want to be notified if we have too many clients or too less
        monitorMBean.setThresholds(new Integer(8),new Integer(4));
        // Setup the monitor: we want to know if a threshold is exceeded
        monitorMBean.setNotifyHigh(true);
        monitorMBean.setNotifyLow(true);
        // Setup the monitor: we're interested in absolute values of the number of clients
        monitorMBean.setDifferenceMode(false);
        // Setup the monitor: link to the service MBean
        monitorMBean.addObservedObject(serviceName);
        monitorMBean.setObservedAttribute("SimpleCounter");
        // Setup the monitor: a short granularity period
        monitorMBean.setGranularityPeriod(50L);
        // Setup the monitor: register a listener
        MBeanServerConnection connection=connector.getMBeanServerConnection();
        connection.addNotificationListener(monitorName, new NotificationListener(){
            public void handleNotification(Notification notification,Object handback){
                System.out.println("Notification = " + notification);
            }}, null, null);
        Thread.sleep(5000);
       
    }
    
    
}