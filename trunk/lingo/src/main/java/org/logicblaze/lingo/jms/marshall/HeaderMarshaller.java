package org.logicblaze.lingo.jms.marshall;

import org.logicblaze.lingo.LingoInvocation;
import org.logicblaze.lingo.jms.Requestor;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 * Strategy for custom header marshalling.
 *
 * @see DefaultMarshaller
 * @author Sanjiv Jivan
 * @since 1.5
 */
public interface HeaderMarshaller {
    /**
     * A strategy method for derived classes to allow them a plugin point to
     * perform custom header processing. This method is called when a message is being sent
     */
    void appendMessageHeaders(Message message, Requestor requestor, LingoInvocation invocation) throws JMSException;

    void appendMessageHeaders(Message message, Session session, Object value);

    /**
     * A strategy for derived classes to allow them to plug in custom header
     * processing for responses
     */
    void addResponseMessageHeaders(ObjectMessage answer, RemoteInvocationResult result, Message requestMessage) throws JMSException;

    /**
     * A strategy method to allow derived classes to process the headers in a
     * special way. This method is called when a message is received
     */
    void handleInvocationHeaders(Message message);

    /**
     * A strategy method to allow derived classes to process the headers in a
     * special way
     */
    void handleInvocationResultHeaders(Message message);

    /**
     * A strategy method to allow derived classes to process the headers in a
     * special way
     */
    void handleMessageHeaders(Message message);
}
