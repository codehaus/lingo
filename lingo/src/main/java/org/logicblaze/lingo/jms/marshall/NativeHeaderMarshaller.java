package org.logicblaze.lingo.jms.marshall;

import org.logicblaze.lingo.jms.Requestor;
import org.logicblaze.lingo.LingoInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.Message;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.ObjectMessage;

/**
 * Required header marshalling that is internal to Lingo.
 *
 * @author James Strachan
 * @author Sanjiv Jivan
 * @since 1.5
 */
public class NativeHeaderMarshaller implements HeaderMarshaller {

    private String stickySessionID;

    /**
     * A strategy method for derived classes to allow them a plugin point to
     * perform custom header processing
     */
    public void appendMessageHeaders(Message message, Requestor requestor, LingoInvocation invocation) throws JMSException {
        if (invocation.getMetadata().isStateful()) {
            message.setStringProperty("JMSXGroupID", getStickySessionID());
        }
    }

    public void appendMessageHeaders(Message message, Session session, Object value) {
    }

    protected String getStickySessionID() {
        if (stickySessionID == null) {
            stickySessionID = "hey";
            // TODO
            throw new RuntimeException("TODO: Not Implemented Yet!");
        }
        return stickySessionID;
    }

    /**
     * A strategy for derived classes to allow them to plug in custom header
     * processing for responses
     */
    public void addResponseMessageHeaders(ObjectMessage answer, RemoteInvocationResult result, Message requestMessage) throws JMSException {
    }

    /**
     * A strategy method to allow derived classes to process the headers in a
     * special way
     */
    public void handleInvocationHeaders(Message message) {
    }

    /**
     * A strategy method to allow derived classes to process the headers in a
     * special way
     */
    public void handleInvocationResultHeaders(Message message) {
    }

    /**
     * A strategy method to allow derived classes to process the headers in a
     * special way
     */
    public void handleMessageHeaders(Message message) {
    }
}
