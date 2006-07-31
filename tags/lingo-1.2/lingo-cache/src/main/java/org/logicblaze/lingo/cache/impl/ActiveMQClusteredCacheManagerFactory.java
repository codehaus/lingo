/**
 * 
 * Copyright 2004 Protique Ltd
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
package org.logicblaze.lingo.cache.impl;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.logicblaze.lingo.cache.ClusteredCacheManagerFactory;

import javax.jms.Destination;

/**
 * An implementation of the {@link ClusteredCacheManagerFactory} which uses the
 * ActiveMQ and Lingo for the remoting.
 * 
 * @version $Revision$
 */
public class ActiveMQClusteredCacheManagerFactory extends JmsClusteredCacheManagerFactory {

    public static final String DEFAULT_URL = "peer://lingo";

    private String defaultTopic = "org.logicblaze.lingo.cache";

    public ActiveMQClusteredCacheManagerFactory() {
        this(DEFAULT_URL);
    }

    public ActiveMQClusteredCacheManagerFactory(String brokerURL) {
        this(new ActiveMQConnectionFactory(brokerURL));
    }

    public ActiveMQClusteredCacheManagerFactory(ActiveMQConnectionFactory connectionFactory) {
        setConnectionFactory(connectionFactory);
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public void setDefaultTopic(String defaultTopic) {
        this.defaultTopic = defaultTopic;
    }

    protected Destination createDestination() {
        return new ActiveMQTopic(defaultTopic);
    }

}
