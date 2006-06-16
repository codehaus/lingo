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

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;

import javax.jms.JMSException;

/**
 * Factory bean for JMS proxies. Behaves like the proxied service when
 * used as bean reference, exposing the specified service interface.
 * <p/>
 * <p>The service URL must be an JMS URL exposing a JMS service.
 * For details, see JmsClientInterceptor docs.
 *
 * @author James Strachan
 * @see JmsClientInterceptor
 * @see JmsServiceExporter
 */
public class JmsProxyFactoryBean extends JmsClientInterceptor implements FactoryBean {

    private Object serviceProxy;

    public void afterPropertiesSet() throws JMSException {
        super.afterPropertiesSet();
        Class serviceInterface = getServiceInterface();
        this.serviceProxy = ProxyFactory.getProxy(serviceInterface, this);
    }

    public Object getObject() {
        return this.serviceProxy;
    }

    public Class getObjectType() {
        return (this.serviceProxy != null) ? this.serviceProxy.getClass() : getServiceInterface();
    }

    public boolean isSingleton() {
        return true;
    }

}
