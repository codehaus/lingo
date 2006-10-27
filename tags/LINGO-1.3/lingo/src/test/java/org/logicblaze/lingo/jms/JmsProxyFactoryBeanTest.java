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
package org.logicblaze.lingo.jms;

import org.logicblaze.lingo.jms.impl.MultiplexingRequestor;

import javax.jms.DeliveryMode;

import java.util.List;

/**
 *
 * @version $Revision$ $Date$
 * @author <a href="mailto:pavel@jehlanka.cz">Pavel Mueller</a>
 */
public class JmsProxyFactoryBeanTest extends JmsTestSupport {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JmsProxyFactoryBeanTest.class);
    }
    
    public void testJmsConfigConfiguresRequestor() throws Exception {
        JmsProxyFactoryBean factoryBean = new JmsProxyFactoryBean();
        factoryBean.setConnectionFactory(connectionFactory);
        factoryBean.setServiceInterface(List.class);
        factoryBean.setPersistentDelivery(true); // set persistent
        factoryBean.setTimeToLive(5000);
        factoryBean.afterPropertiesSet(); // init factory bean
        
        // test requestor if it's correctly configured
        Requestor requestor = factoryBean.getRequestor();
        assertTrue(requestor instanceof MultiplexingRequestor);
        MultiplexingRequestor mpxRequestor = (MultiplexingRequestor) requestor;
        
        // expected: persistent delivery
        assertEquals(DeliveryMode.PERSISTENT, mpxRequestor.getDeliveryMode());
        assertEquals(5000, mpxRequestor.getTimeToLive());
    }

}
