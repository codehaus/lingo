package org.logicblaze.lingo.jms.marshall;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.logicblaze.lingo.ClientContextHolder;
import org.logicblaze.lingo.ClientContextImpl;
import org.logicblaze.lingo.LingoInvocation;
import org.logicblaze.lingo.jms.Requestor;
import org.logicblaze.lingo.jms.RuntimeJMSException;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

/**
 * Ageci header marshaller which propagates the clients principal as a thread local
 * variable on the server. A method executing on the server can obtain this information by calling
 * {@link ClientContextHolder#getUserName()}
 *
 * @see ClientContextHolder
 * @author Sanjiv Jivan
 * @since 1.5
 */
public class AcegiHeaderMarshaller implements HeaderMarshaller {

    private static final Log log = LogFactory.getLog(AcegiHeaderMarshaller.class);

    private static String CUSTOM_HEADER = "JMSAcegiPrincipal";

    public void appendMessageHeaders(Message message, Requestor requestor,
                                     LingoInvocation invocation) throws JMSException {
        SecurityContext sc = SecurityContextHolder.getContext();
        //SecureContext sc = (SecureContext) ContextHolder.getContext();
        Authentication auth = sc.getAuthentication();

        //if you're using a custom acegi principal, update this code accordingly
        String userName = null;
        if (auth != null) {
            Object principal = auth.getPrincipal();
            //the Acegi assigned principal is typically an instance of UserDetals
            //except for certain cases where the user programatically updates the authentication
            //token (eg users' password) by calling code the code below in which case the
            //principal will be an instance of String
            //sc.setAuthentication(new UsernamePasswordAuthenticationToken("user", "password"));
            if (principal instanceof UserDetails) {
                userName = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                userName = (String) principal;
            } else {
                throw new IllegalArgumentException("Unrecognized principal type : " + principal);
            }
        }

        //add user name info the the message header
        message.setStringProperty(CUSTOM_HEADER, userName);
    }

    public void appendMessageHeaders(Message message, Session session, Object value) {
    }

    public void addResponseMessageHeaders(ObjectMessage answer, RemoteInvocationResult result, Message requestMessage) throws JMSException {
    }

    public void handleInvocationHeaders(Message message) {
        try {
            String userName = message.getStringProperty(CUSTOM_HEADER);
            ClientContextHolder.setContext(new ClientContextImpl(userName));
        }
        catch (JMSException e) {
            throw new RuntimeJMSException(e);
        }
    }

    public void handleInvocationResultHeaders(Message message) {
    }

    public void handleMessageHeaders(Message message) {
    }
}
