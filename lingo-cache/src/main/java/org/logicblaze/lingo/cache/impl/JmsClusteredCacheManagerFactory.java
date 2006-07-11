/**
 *
 * Copyright 2005-2006 The Apache Software Foundation
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
 */
package org.logicblaze.lingo.cache.impl;

import org.aopalliance.intercept.MethodInvocation;
import org.logicblaze.lingo.MetadataStrategy;
import org.logicblaze.lingo.cache.ClusteredCacheManagerFactory;
import org.logicblaze.lingo.cache.TransactionalCacheManager;
import org.logicblaze.lingo.jms.JmsProducerConfig;
import org.logicblaze.lingo.jms.JmsProxyFactoryBean;
import org.logicblaze.lingo.jms.JmsServiceExporter;
import org.logicblaze.lingo.jms.Requestor;
import org.logicblaze.lingo.jms.marshall.Marshaller;
import org.springframework.remoting.RemoteProxyFailureException;
import org.springframework.remoting.support.RemoteInvocationFactory;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import java.util.Map;

/**
 * 
 * @version $Revision$
 */
public class JmsClusteredCacheManagerFactory extends ClusteredCacheManagerFactory {

    private JmsProxyFactoryBean factoryBean;
    private JmsServiceExporter serviceExporter;
    private ConnectionFactory connectionFactory;
    private Destination destination;

    public TransactionalCacheManager createCacheManager(String regionName) {
        TransactionalCacheManager answer = super.createCacheManager(regionName);
        subscribeToEvents(answer);
        return answer;
    }

    protected void subscribeToEvents(TransactionalCacheManager answer) {
        JmsServiceExporter exporter = getServiceExporter();
        exporter.setServiceInterface(CommandExecutor.class);
        exporter.setService(answer);
        try {
            serviceExporter.afterPropertiesSet();
        }
        catch (Exception e) {
            throw new RemoteProxyFailureException("Failed to create JmsServiceExporter: " + e.getMessage(), e);
        }
    }

    public JmsProxyFactoryBean getFactoryBean() {
        if (factoryBean == null) {
            factoryBean = createFactoryBean();
            try {
                factoryBean.afterPropertiesSet();
            }
            catch (JMSException e) {
                throw new RemoteProxyFailureException("Failed to create JmsProxyFactoryBean: " + e.getMessage(), e);
            }
        }
        return factoryBean;
    }

    public void setFactoryBean(JmsProxyFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    public JmsServiceExporter getServiceExporter() {
        if (serviceExporter == null) {
            serviceExporter = createServiceExporter();
        }
        return serviceExporter;
    }

    public void setServiceExporter(JmsServiceExporter exporter) {
        this.serviceExporter = exporter;
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Destination getDestination() {
        if (destination == null) {
            destination = createDestination();
        }
        return destination;
    }

    /**
     * Sets the destination used to replicate cache commands to which is
     * typically a topic
     */
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    // Delegate methods
    // -------------------------------------------------------------------------
    public String getClientID() {
        return getFactoryBean().getClientID();
    }

    public int getJmsExpiration() {
        return getFactoryBean().getJmsExpiration();
    }

    public int getJmsPriority() {
        return getFactoryBean().getJmsPriority();
    }

    public String getJmsType() {
        return getFactoryBean().getJmsType();
    }

    public Marshaller getMarshaller() {
        return getFactoryBean().getMarshaller();
    }

    public Map getMessageProperties() {
        return getFactoryBean().getMessageProperties();
    }

    public MetadataStrategy getMetadataStrategy() {
        return getFactoryBean().getMetadataStrategy();
    }

    public long getMultipleResponseTimeout() {
        return getFactoryBean().getMultipleResponseTimeout();
    }

    public Object getObject() {
        return getFactoryBean().getObject();
    }

    public Class getObjectType() {
        return getFactoryBean().getObjectType();
    }

    public JmsProducerConfig getProducerConfig() {
        return getFactoryBean().getProducerConfig();
    }

    public RemoteInvocationFactory getRemoteInvocationFactory() {
        return getFactoryBean().getRemoteInvocationFactory();
    }

    public long getRemoteReferenceTimeout() {
        return getFactoryBean().getRemoteReferenceTimeout();
    }

    public Requestor getRequestor() {
        return getFactoryBean().getRequestor();
    }

    public Destination getResponseDestination() {
        return getFactoryBean().getResponseDestination();
    }

    public Class getServiceInterface() {
        return getFactoryBean().getServiceInterface();
    }

    public String getServiceUrl() {
        return getFactoryBean().getServiceUrl();
    }

    public int getTimeToLive() {
        return getFactoryBean().getTimeToLive();
    }

    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        return getFactoryBean().invoke(methodInvocation);
    }

    public boolean isMultipleResponsesExpected() {
        return getFactoryBean().isMultipleResponsesExpected();
    }

    public boolean isPersistentDelivery() {
        return getFactoryBean().isPersistentDelivery();
    }

    public boolean isSingleton() {
        return getFactoryBean().isSingleton();
    }

    public void setClientID(String clientID) {
        getFactoryBean().setClientID(clientID);
    }

    public void setCorrelationID(String correlationID) {
        getFactoryBean().setCorrelationID(correlationID);
    }

    public void setJmsExpiration(int jmsExpiration) {
        getFactoryBean().setJmsExpiration(jmsExpiration);
    }

    public void setJmsPriority(int jmsPriority) {
        getFactoryBean().setJmsPriority(jmsPriority);
    }

    public void setJmsType(String jmsType) {
        getFactoryBean().setJmsType(jmsType);
    }

    public void setMarshaller(Marshaller marshaller) {
        getFactoryBean().setMarshaller(marshaller);
    }

    public void setMessageProperties(Map messageProperties) {
        getFactoryBean().setMessageProperties(messageProperties);
    }

    public void setMetadataStrategy(MetadataStrategy metadataStrategy) {
        getFactoryBean().setMetadataStrategy(metadataStrategy);
    }

    public void setMultipleResponsesExpected(boolean multipleResponsesExpected) {
        getFactoryBean().setMultipleResponsesExpected(multipleResponsesExpected);
    }

    public void setMultipleResponseTimeout(long multipleResponseTimeout) {
        getFactoryBean().setMultipleResponseTimeout(multipleResponseTimeout);
    }

    public void setPersistentDelivery(boolean persistent) {
        getFactoryBean().setPersistentDelivery(persistent);
    }

    public void setProducerConfig(JmsProducerConfig producerConfig) {
        getFactoryBean().setProducerConfig(producerConfig);
    }

    public void setRemoteInvocationFactory(RemoteInvocationFactory arg0) {
        getFactoryBean().setRemoteInvocationFactory(arg0);
    }

    public void setRemoteReferenceTimeout(long remoteReferenceTimeout) {
        getFactoryBean().setRemoteReferenceTimeout(remoteReferenceTimeout);
    }

    public void setRequestor(Requestor requestor) {
        getFactoryBean().setRequestor(requestor);
    }

    public void setResponseDestination(Destination responseDestination) {
        getFactoryBean().setResponseDestination(responseDestination);
    }

    public void setServiceInterface(Class arg0) {
        getFactoryBean().setServiceInterface(arg0);
    }

    public void setServiceUrl(String arg0) {
        getFactoryBean().setServiceUrl(arg0);
    }

    public void setTimeToLive(int timeToLive) {
        getFactoryBean().setTimeToLive(timeToLive);
    }

    public String toString() {
        return getFactoryBean().toString();
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    protected CommandExecutor createExecutor() {
        JmsProxyFactoryBean factory = getFactoryBean();
        return (CommandExecutor) factory.getObject();
    }

    protected JmsProxyFactoryBean createFactoryBean() {
        JmsProxyFactoryBean answer = new JmsProxyFactoryBean();
        answer.setServiceInterface(CommandExecutor.class);
        answer.setConnectionFactory(getConnectionFactory());
        answer.setDestination(getDestination());
        return answer;
    }

    protected JmsServiceExporter createServiceExporter() {
        JmsServiceExporter answer = new JmsServiceExporter();
        answer.setConnectionFactory(getConnectionFactory());
        answer.setDestination(getDestination());
        return answer;
    }

    /**
     * A lazy construction hook to allow derived classes to auto-create
     * destinations
     */
    protected Destination createDestination() {
        return null;
    }

}
