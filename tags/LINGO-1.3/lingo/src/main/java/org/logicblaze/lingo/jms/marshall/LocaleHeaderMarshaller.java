package org.logicblaze.lingo.jms.marshall;

import org.logicblaze.lingo.LingoInvocation;
import org.logicblaze.lingo.jms.Requestor;
import org.logicblaze.lingo.jms.RuntimeJMSException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.remoting.support.RemoteInvocationResult;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.util.Locale;

/**
 * Propagates the client locale to the server's thread of execution. A method executing on the server
 * can determine the invoking clients locale by calling LocaleContextHolder.getLocale()
 *
 * @see org.springframework.context.i18n.LocaleContextHolder
 * 
 * @author Sanjiv Jivan
 * @since 1.5
 */
public class LocaleHeaderMarshaller implements HeaderMarshaller {

    private static String LANGUAGE_HEADER = "JMSLanguage";
    private static String COUNTRY_HEADER = "JMSCountry";

    public void appendMessageHeaders(Message message, Requestor requestor, LingoInvocation invocation) throws JMSException {
        Locale locale = LocaleContextHolder.getLocale();
        message.setStringProperty(LANGUAGE_HEADER, locale.getLanguage());
        message.setStringProperty(COUNTRY_HEADER, locale.getCountry());
    }

    public void appendMessageHeaders(Message message, Session session, Object value) {
    }

    public void addResponseMessageHeaders(ObjectMessage answer, RemoteInvocationResult result, Message requestMessage) throws JMSException {
    }

    public void handleInvocationHeaders(Message message) {

        String language = null;
        String country = null;
        try {
            language = message.getStringProperty(LANGUAGE_HEADER);
            country = message.getStringProperty(COUNTRY_HEADER);
        } catch (JMSException e) {
            throw new RuntimeJMSException(e);
        }

        if (language != null) {
            country = country == null ? "" : country;
            Locale locale = new Locale(language, country);
            LocaleContextHolder.setLocale(locale);
        }
    }

    public void handleInvocationResultHeaders(Message message) {
    }

    public void handleMessageHeaders(Message message) {
    }
}
