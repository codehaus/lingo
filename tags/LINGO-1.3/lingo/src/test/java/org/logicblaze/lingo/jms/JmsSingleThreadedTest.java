package org.logicblaze.lingo.jms;

import org.logicblaze.lingo.jms.impl.SingleThreadedRequestor;

import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * @author yuri
 * @version $Revision$
 */
public class JmsSingleThreadedTest extends JmsTestSupport {
    public JmsSingleThreadedTest() {
    }

    public void testTimeout() throws Exception {
        SingleThreadedRequestor requestor = (SingleThreadedRequestor) createRequestor(getDestinationName());

        Session session = createSession();
        MessageConsumer receiver = session.createConsumer(session.createQueue(getDestinationName()));

        // clear old messages
        while (receiver.receive(1) != null) {
            ;
        }

        requestor.send(null, session.createTextMessage("bonson"), 1);
        Thread.sleep(50);
        assertNull(receiver.receive(1));

        requestor.send(null, session.createTextMessage("bonson2"), -1);
        TextMessage message = (TextMessage) receiver.receive(1000);
        assertNotNull(message);
        assertEquals("bonson2", message.getText());
    }

    public void testTimeouUsingPermanentQueue() throws Exception {
        SingleThreadedRequestor requestor = (SingleThreadedRequestor) createRequestor(getDestinationName(), getDestinationName() + ".ClientInbound");

        Session session = createSession();
        MessageConsumer receiver = session.createConsumer(session.createQueue(getDestinationName()));

        // clear old messages
        while (receiver.receive(1) != null) {
            ;
        }

        requestor.send(null, session.createTextMessage("bonson"), 1);
        Thread.sleep(50);
        assertNull(receiver.receive(1));

        requestor.send(null, session.createTextMessage("bonson2"), -1);
        TextMessage message = (TextMessage) receiver.receive(1000);
        assertNotNull(message);
        assertEquals("bonson2", message.getText());
    }
}
