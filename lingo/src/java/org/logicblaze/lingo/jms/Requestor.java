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

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Represents a JMS based requestor which is capable of performing various
 * Message Exchange Patterns such as one-way, synchronous request-response,
 * receive etc.
 * 
 * @version $Revision$
 */
public interface Requestor {

    /**
     * Sends a one way message, not waiting for the response.
     * 
     * @param destination
     *            the server side destination
     * @param message
     *            the message to send
     */
    void send(Destination destination, Message message) throws JMSException;

    /**
     * Sends a message to the given destination in a way that can be implemented
     * in JMS 1.0.2b as well as using the JMS 1.1 send() method on
     * {@link MessageProducer}
     * 
     * @throws JMSException
     *             if the message could not be sent
     */
    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException;

    /**
     * Sends a request and waits for a reply. The temporary queue is used for
     * the <CODE>JMSReplyTo</CODE> destination, and only one reply per request
     * is expected.
     * 
     * @param destination
     *            the server side destination
     * @param message
     *            the message to send
     * @return the reply message
     * @throws javax.jms.JMSException
     *             if the JMS provider fails to complete the request due to some
     *             internal error.
     */
    Message request(Destination destination, Message message) throws JMSException;

    /**
     * Sends a request and waits for a reply up to a maximum timeout. The
     * temporary queue is used for the <CODE>JMSReplyTo</CODE> destination,
     * and only one reply per request is expected.
     * 
     * @param destination
     *            the server side destination
     * @param message
     *            the message to send
     * @return the reply message
     * @throws javax.jms.JMSException
     *             if the JMS provider fails to complete the request due to some
     *             internal error.
     */
    Message request(Destination destination, Message message, long timeout) throws JMSException;

    /**
     * Receives a message waiting for a maximum timeout if the timeout value is >
     * 0 ir waiting forever if the timeout is < 0 or not waiting at all if the
     * timeout is zero
     */
    Message receive(long timeout) throws JMSException;

    /**
     * Sends a request and provides a handler for all responses until the
     * request is considered dead (or it is timed out).
     */
    void request(Destination destination, Message requestMessage, ReplyHandler handler, long timeout) throws JMSException;

    /**
     * Returns the underying producer
     */
    public MessageProducer getMessageProducer();

    /**
     * Provides access to the underlying Connection this requestor is using
     */
    Connection getConnection();

    /**
     * Provides access to the underlying JMS session so that you can create
     * messages.
     */
    Session getSession();

    /**
     * Closes the <CODE>Requestor</CODE> and its session. <p/>
     * <P>
     * Since a provider may allocate some resources on behalf of a <CODE>Requestor</CODE>
     * outside the Java virtual machine, clients should close them when they are
     * not needed. Relying on garbage collection to eventually reclaim these
     * resources may not be timely enough. <p/>
     * <P>
     * Note that this method closes the <CODE>Session</CODE> object passed to
     * the <CODE>Requestor</CODE> constructor.
     * 
     * @throws javax.jms.JMSException
     *             if the JMS provider fails to close the <CODE>Requestor</CODE>
     *             due to some internal error.
     */
    void close() throws JMSException;

    /**
     * Creates a new correlation ID. Note that because the correlationID is used
     * on a per-temporary destination basis, it does not need to be unique
     * across more than one destination. So a simple counter will suffice.
     * 
     * @return
     */
    String createCorrelationID();
}
