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
import org.logicblaze.lingo.jms.ReplyHandler;

import javax.jms.Message;

/**
 * A {@link FutureResult} which implements {@link org.logicblaze.lingo.jms.ReplyHandler} so that it can be used as a handler
 * for a correlation ID
 *
 * @version $Revision$
 */
public class FutureResultHandler extends FutureResult implements ReplyHandler {

    public boolean handle(Message message) {
        set(message);
        return true;
    }
}
