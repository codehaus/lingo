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

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link Requestor} which will use a single producer, consumer
 * and temporary topic for resource efficiency, but will use correlation
 * IDs on each message and response to ensure that each threads requests
 * can occur synchronously.
 * <p/>
 * This class can be used concurrently by many different threads at the same time.
 *
 * @version $Revision$
 */
public class MultiplexingRequestor extends SingleThreadedRequestor implements MessageListener {
    private static final Log log = LogFactory.getLog(MultiplexingRequestor.class);

    private Map requests = new HashMap();

    public MultiplexingRequestor(Session session, JmsProducer producer, Destination destination) throws JMSException {
        super(session, producer, destination);
        getReceiver().setMessageListener(this);
    }

    public void registerHandler(String correlationID, ReplyHandler handler) {
        synchronized (this) {
            requests.put(correlationID, handler);
        }
    }

    public Message request(Destination destination, Message message) throws JMSException {
        long timeout = getMaximumTimeout();
        return request(destination, message, timeout);
    }

    public Message request(Destination destination, Message message, long timeout) throws JMSException {
        // lets create a correlationID
        String correlationID = createCorrelationID();
        FutureResult future = new FutureResultHandler();
        synchronized (this) {
            requests.put(correlationID, future);
        }
        message.setJMSCorrelationID(correlationID);
        oneWay(destination, message);

        try {
            if (timeout < 0) {
                return (Message) future.get();
            }
            else if (timeout == 0) {
                return (Message) future.peek();
            }
            else {
                return (Message) future.timedGet(timeout);
            }
        }
        catch (Exception e) {
            throw createJMSException(e);
        }
    }

    /**
     * Processes inbound responses from requests
     */
    public void onMessage(Message message) {
        try {
            String correlationID = message.getJMSCorrelationID();

            // lets notify the monitor for this response
            ReplyHandler handler = null;
            synchronized (this) {
                handler = (ReplyHandler) requests.get(correlationID);
            }
            if (handler == null) {
                log.warn("Response received for unknown request: " + message);
            }
            else {
                boolean complete = handler.handle(message);
                if (complete) {
                    synchronized (this) {
                        requests.remove(correlationID);
                    }
                }
            }
        }
        catch (JMSException e) {
            throw new FailedToProcessResponse(message, e);
        }

    }

    // Implementation methods
    //-------------------------------------------------------------------------

    protected JMSException createJMSException(Exception e) {
        JMSException answer = new JMSException(e.toString());
        answer.setLinkedException(e);
        return answer;
    }


}
