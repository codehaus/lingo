/**
 * 
 * Copyright RAJD Consultancy Ltd
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
package org.logicblaze.lingo.jmx.remote.jms;

import java.util.Map;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.management.Notification;
import javax.management.NotificationListener;
import org.activemq.advisory.AdvisorySupport;
import org.activemq.command.ActiveMQDestination;
import org.activemq.command.ActiveMQMessage;
import org.activemq.command.RemoveInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 * @version $Revision$
 */
class ServerListenerInfo implements NotificationListener,MessageListener{
    private static final Log log=LogFactory.getLog(ServerListenerInfo.class);
    private String id;
    private Map holder;
    private Destination replyTo;
    private Session session;
    private MessageProducer producer;
    private MessageConsumer advisoryConsumer;

    ServerListenerInfo(String id,Map holder,Destination replyTo,Session session) throws JMSException{
        this.id=id;
        this.holder=holder;
        this.replyTo=replyTo;
        this.session=session;
        this.producer=session.createProducer(replyTo);
        Topic advisoryTopic=AdvisorySupport.getConsumerAdvisoryTopic((ActiveMQDestination) replyTo);
        advisoryConsumer=session.createConsumer(advisoryTopic);
        advisoryConsumer.setMessageListener(this);
    }

    /**
     * NotificationListener implementation
     * 
     * @param notification
     * @param handback
     */
    public void handleNotification(Notification notification,Object handback){
        try{
            ObjectMessage msg=session.createObjectMessage(notification);
            producer.send(msg);
        }catch(JMSException e){
            log.error("Failed to handle notification: "+notification,e);
        }
    }

    /**
     * MessageListener implementation
     * 
     * @param message
     */
    public void onMessage(Message message){
        try{
            if(message!=null&&message instanceof ObjectMessage){
                ObjectMessage objMsg=(ObjectMessage) message;
                Object obj=objMsg.getObject();
                if(obj!=null&&obj instanceof RemoveInfo){
                    close();
                }
            }else {
                if (message instanceof ActiveMQMessage ){
                    ActiveMQMessage amqMsg = (ActiveMQMessage) message;
                    if (amqMsg.getDataStructure() instanceof RemoveInfo){
                        close();
                    }
                }
            }
        }catch(JMSException e){
            log.warn("Failed to process onMessage",e);
        }
    }

    /**
     * close the info
     */
    public void close(){
        try{
            holder.remove(id);
            producer.close();
            advisoryConsumer.close();
        }catch(JMSException e){
            log.warn("problem closing",e);
        }
    }

    /**
     * @return Returns the holder.
     */
    public Map getHolder(){
        return holder;
    }

    /**
     * @param holder
     *            The holder to set.
     */
    public void setHolder(Map holder){
        this.holder=holder;
    }

    /**
     * @return Returns the id.
     */
    public String getId(){
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(String id){
        this.id=id;
    }

    /**
     * @return Returns the producer.
     */
    public MessageProducer getProducer(){
        return producer;
    }

    /**
     * @param producer
     *            The producer to set.
     */
    public void setProducer(MessageProducer producer){
        this.producer=producer;
    }

    /**
     * @return Returns the replyTo.
     */
    public Destination getReplyTo(){
        return replyTo;
    }

    /**
     * @param replyTo
     *            The replyTo to set.
     */
    public void setReplyTo(Destination replyTo){
        this.replyTo=replyTo;
    }
}