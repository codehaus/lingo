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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;

import javax.jms.JMSException;
import javax.jms.Message;
import java.lang.reflect.InvocationTargetException;

/**
 * @version $Revision$
 */
public class AsyncReplyHandler extends RemoteInvocationBasedExporter implements ReplyHandler {
    private static final Log log = LogFactory.getLog(AsyncReplyHandler.class);

    private Object pojo;
    private RemoteInvocationReader invocationReader = new RemoteInvocationReader();

    public AsyncReplyHandler(Object pojo) {
        this.pojo = pojo;
    }

    public boolean handle(Message message) throws JMSException {
        RemoteInvocation invocation = invocationReader.readRemoteInvocation(message);
        try {
            invoke(invocation, pojo);
        }
        catch (NoSuchMethodException e) {
            onException(invocation, e);
        }
        catch (IllegalAccessException e) {
            onException(invocation, e);
        }
        catch (InvocationTargetException e) {
            onException(invocation, e);
        }
        return false;
    }


    protected void onException(RemoteInvocation invocation, Exception e) {
        log.error("Failed to invoke: " + invocation + " on: " + pojo + ". Reason: " + e, e);
    }
}
