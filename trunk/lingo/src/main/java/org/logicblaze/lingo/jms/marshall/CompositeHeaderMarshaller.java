package org.logicblaze.lingo.jms.marshall;

import edu.emory.mathcs.backport.java.util.Collections;
import org.logicblaze.lingo.LingoInvocation;
import org.logicblaze.lingo.jms.Requestor;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * Composite Header Marshaller which allows you to plugin multiple HeaderMarshaller strategies.
 *
 * Example :
 * <pre>
 * CompositeHeaderMarshaller headerMarshaller = new CompositeHeaderMarshaller();
 * headerMarshaller.add(new AcegiHeaderMarshaller());
 * headerMarshaller.add(new LocaleHeaderMarshaller());
 * DefaultMarshaller marshaller = new DefualtMarshaller();
 * marshaller.setHeaderMarshaller(headerMarshaller);
 * </pre>
 *
 * @author Sanjiv Jivan
 * @since 1.5
 */
public class CompositeHeaderMarshaller implements HeaderMarshaller {

    private List headerMarshallers = Collections.synchronizedList(new ArrayList());

    public List getHeaderMarshallers() {
        return headerMarshallers;
    }

    public void setHeaderMarshallers(List headerMarshallers) {
        this.headerMarshallers = headerMarshallers;
    }

    public void addHeaderMarshaller(HeaderMarshaller headerMarshaller) {
        if(!headerMarshallers.contains(headerMarshaller)) {
            headerMarshallers.add(headerMarshaller);
        }
    }

    public void removeHeaderMarshaller(HeaderMarshaller headerMarshaller) {
        headerMarshallers.remove(headerMarshaller);
    }

    public void appendMessageHeaders(Message message, Requestor requestor, LingoInvocation invocation) throws JMSException {
        for (int i = 0; i < headerMarshallers.size(); i++) {
            HeaderMarshaller header = (HeaderMarshaller) headerMarshallers.get(i);
            header.appendMessageHeaders(message, requestor, invocation);
        }
    }

    public void appendMessageHeaders(Message message, Session session, Object value) {
        for (int i = 0; i < headerMarshallers.size(); i++) {
            HeaderMarshaller header = (HeaderMarshaller) headerMarshallers.get(i);
            header.appendMessageHeaders(message, session, value);
        }
    }

    public void addResponseMessageHeaders(ObjectMessage answer, RemoteInvocationResult result, Message requestMessage) throws JMSException {
        for (int i = 0; i < headerMarshallers.size(); i++) {
            HeaderMarshaller header = (HeaderMarshaller) headerMarshallers.get(i);
            header.addResponseMessageHeaders(answer, result, requestMessage);
        }
    }

    public void handleInvocationHeaders(Message message) {
        for (int i = 0; i < headerMarshallers.size(); i++) {
            HeaderMarshaller header = (HeaderMarshaller) headerMarshallers.get(i);
            header.handleInvocationHeaders(message);
        }
    }

    public void handleInvocationResultHeaders(Message message) {
        for (int i = 0; i < headerMarshallers.size(); i++) {
            HeaderMarshaller header = (HeaderMarshaller) headerMarshallers.get(i);
            header.handleInvocationResultHeaders(message);
        }
    }

    public void handleMessageHeaders(Message message) {
        for (int i = 0; i < headerMarshallers.size(); i++) {
            HeaderMarshaller header = (HeaderMarshaller) headerMarshallers.get(i);
            header.handleMessageHeaders(message);
        }
    }
}

