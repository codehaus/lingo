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
package org.agilasoft.lingo.jms.marshall;

import org.agilasoft.lingo.LingoInvocation;
import org.agilasoft.lingo.MethodMetadata;
import org.agilasoft.lingo.jms.JmsTestSupport;
import org.agilasoft.lingo.jms.Requestor;

import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * @version $Revision$
 */
public class XStreamMarshallerTest extends JmsTestSupport {
    Marshaller marshaller = new XStreamMarshaller();

    public void testMarshall() throws Exception {
        Requestor requestor = createRequestor(getDestinationName());

        LingoInvocation invocation = new LingoInvocation("foo", new Class[0], new Object[0], new MethodMetadata(false));
        Message message = marshaller.createRequestMessage(requestor, invocation);

        assertTrue("Should have created a text message: " + message, message instanceof TextMessage);

        TextMessage textMessage = (TextMessage) message;
        String text = textMessage.getText();
        assertTrue("Should have created a valid string", text != null && text.length() > 0);

        System.out.println("XML is: ");
        System.out.println(text);
    }
}
