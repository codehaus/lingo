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
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
/**
 * @version $Revision$
 */
public class  MBeanJmsServerConnectionImpl extends  MBeanServerConnectionDelegate implements MBeanJmsServerConnection{
    private static final Log log=LogFactory.getLog(MBeanJmsServerConnectionImpl.class);
    private Session jmsSession;
    private Map notificationListeners = new ConcurrentHashMap();
    /**
     * Construct this thing
     * @param connection
     * @param jmsConnection 
     * @throws JMSException 
     */
    public MBeanJmsServerConnectionImpl(MBeanServerConnection connection, Connection jmsConnection) throws JMSException{
        super(connection);
        this.jmsSession =jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }
    /**
     * Add a Notification listener
     * @param listenerId
     * @param name
     * @param replyToDestination
     */
    public void addNotificationListener(String listenerId, ObjectName name, Destination replyToDestination){
        try{
            ServerListenerInfo info = new ServerListenerInfo(listenerId,notificationListeners,replyToDestination,jmsSession);
            notificationListeners.put(listenerId,info);
            connection.addNotificationListener(name, info,null,null);
        }catch(Exception e){
            log.error("Failed to addNotificationListener ",e);
        }
        
    }
    
    /**
     * Remove a Notification listener
     * @param listenerId
     */
    public void removeNotificationListener(String listenerId){
        ServerListenerInfo info = (ServerListenerInfo) notificationListeners.remove(listenerId);
        if (info != null){
            info.close();
        }
    }
    
}