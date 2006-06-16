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
package org.logicblaze.lingo.jms.impl;

import apple.awt.CImage;
import edu.emory.mathcs.backport.java.util.concurrent.FutureTask;
import edu.emory.mathcs.backport.java.util.concurrent.ScheduledThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.jms.FailedToProcessResponse;
import org.logicblaze.lingo.jms.JmsProducerConfig;
import org.logicblaze.lingo.jms.ReplyHandler;
import org.logicblaze.lingo.jms.Requestor;
import org.logicblaze.lingo.util.DefaultTimeoutMap;
import org.logicblaze.lingo.util.TimeoutMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * A {@link org.logicblaze.lingo.jms.Requestor} which will use a single
 * producer, consumer and temporary topic for resource efficiency, but will use
 * correlation IDs on each message and response to ensure that each threads
 * requests can occur synchronously. <p/> This class can be used concurrently by
 * many different threads at the same time.
 * 
 * @version $Revision$
 */
public class MultiplexingRequestor extends SingleThreadedRequestor implements MessageListener {
    private static final Log log = LogFactory.getLog(MultiplexingRequestor.class);

    private TimeoutMap requestMap = new DefaultTimeoutMap();

    public static Requestor newInstance(ConnectionFactory connectionFactory, JmsProducerConfig config, Destination destination, Destination responseDestination)
            throws JMSException {
        Connection connection = config.createConnection(connectionFactory);
        Session session = config.createSession(connection);
        MessageProducer producer = config.createMessageProducer(session);
        return new MultiplexingRequestor(connection, session, producer, destination, responseDestination, true);
    }

    public static Requestor newInstance(ConnectionFactory connectionFactory, JmsProducerConfig config, Destination destination) throws JMSException {
        Connection connection = config.createConnection(connectionFactory);
        Session session = config.createSession(connection);
        MessageProducer producer = config.createMessageProducer(session);
        Destination responseDestination = config.createTemporaryDestination(session);
        return new MultiplexingRequestor(connection, session, producer, destination, responseDestination, true);
    }

    public MultiplexingRequestor(JmsProducerConfig config, Destination serverDestination, Destination clientDestination) throws JMSException {
        super(config, serverDestination, clientDestination);
        init();
    }

    public MultiplexingRequestor(Connection connection, Session session, MessageProducer producer, Destination serverDestination,
            Destination clientDestination, boolean ownsConnection) throws JMSException {
        super(connection, session, producer, serverDestination, clientDestination, ownsConnection);
        init();
    }

    private void init() throws JMSException {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        this.requestMap = new DefaultTimeoutMap(executor, 1000L);
        getReceiver().setMessageListener(this);
    }

    public void registerHandler(String correlationID, ReplyHandler handler, long timeout) {
        requestMap.put(correlationID, handler, timeout);
    }

    public Message request(Destination destination, Message message) throws JMSException {
        long timeout = getTimeToLive();
        return request(destination, message, timeout);
    }

    public Message request(Destination destination, Message message, long timeout) throws JMSException {
        // lets create a correlationID unless we are already given one -
        // we are already given a correaltionID if we are on the server side
        // responding to a remote object reference
        FutureTask future = null;
        String correlationID = message.getJMSCorrelationID();
        if (correlationID == null) {
            correlationID = createCorrelationID();
            message.setJMSCorrelationID(correlationID);
        }
        else {
            Object currentHandler = requestMap.get(correlationID);
            if (currentHandler instanceof AsyncReplyHandler) {
                AsyncReplyHandler handler = (AsyncReplyHandler) currentHandler;
                future = handler.newResultHandler();
            }
        }

        if (future == null) {
            FutureHandler futureHandler = new FutureHandler();
            future = futureHandler;
            requestMap.put(correlationID, futureHandler, timeout);
        }
        populateHeaders(message);
        send(destination, message);

        try {
            if (timeout < 0) {
                return (Message) future.get();
            }
            else {
                return (Message) future.get(timeout, TimeUnit.MILLISECONDS);
            }
        }
        catch (Exception e) {
            throw createJMSException(e);
        }
    }

    public void request(Destination destination, Message message, ReplyHandler handler, long timeout) throws JMSException {
        String correlationID = message.getJMSCorrelationID();
        if (correlationID == null) {
            correlationID = createCorrelationID();
            message.setJMSCorrelationID(correlationID);
        }
        synchronized (requestMap) {
            Object currentHandler = requestMap.get(correlationID);
            if (currentHandler instanceof AsyncReplyHandler) {
                AsyncReplyHandler remoteObjectHandler = (AsyncReplyHandler) currentHandler;
                remoteObjectHandler.setParent(handler);
            }
            else {
                requestMap.put(correlationID, handler, timeout);
            }
        }
        populateHeaders(message);
        send(destination, message);
    }

    /**
     * Processes inbound responses from requests
     */
    public void onMessage(Message message) {
        try {
            String correlationID = message.getJMSCorrelationID();

            // lets notify the monitor for this response
            Object handler = requestMap.get(correlationID);
            if (handler == null) {
                log.warn("Response received for unknown correlationID: " + correlationID + " request: " + message);
            }
            else if (handler instanceof ReplyHandler) {
                ReplyHandler replyHandler = (ReplyHandler) handler;
                boolean complete = replyHandler.handle(message);
                if (complete) {
                    requestMap.remove(correlationID);
                }
            }
        }
        catch (JMSException e) {
            throw new FailedToProcessResponse(message, e);
        }

    }

    // Lets ensure only one thread performs a send/receive at once
    public synchronized Message receive(long timeout) throws JMSException {
        return super.receive(timeout);
    }

    protected synchronized void doSend(Destination destination, Message message, long timeout) throws JMSException {
        super.doSend(destination, message, timeout);
    }

    // Properties
    // -------------------------------------------------------------------------
    public TimeoutMap getRequestMap() {
        return requestMap;
    }

    public void setRequestMap(TimeoutMap requests) {
        this.requestMap = requests;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected JMSException createJMSException(Exception e) {
        JMSException answer = new JMSException(e.toString());
        answer.setLinkedException(e);
        return answer;
    }

}
