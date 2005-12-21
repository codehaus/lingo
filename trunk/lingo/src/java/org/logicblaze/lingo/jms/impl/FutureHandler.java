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
package org.logicblaze.lingo.jms.impl;

import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import edu.emory.mathcs.backport.java.util.concurrent.Callable;
import edu.emory.mathcs.backport.java.util.concurrent.FutureTask;

import org.logicblaze.lingo.jms.ReplyHandler;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * A {@link FutureResult} which implements
 * {@link org.logicblaze.lingo.jms.ReplyHandler} so that it can be used as a
 * handler for a correlation ID
 * 
 * @version $Revision$
 */
public class FutureHandler extends FutureTask implements ReplyHandler {

    private static final Callable EMPTY_CALLABLE = new Callable() {
        public Object call() throws Exception {
            return null;
        }
    };

    public FutureHandler() {
        super(EMPTY_CALLABLE);
    }

    public synchronized void set(Object result) {
        super.set(result);
    }

    public boolean handle(Message message) throws JMSException {
        set(message);
        return true;
    }
}
