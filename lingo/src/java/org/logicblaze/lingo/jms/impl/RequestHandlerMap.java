/**
 * 
 * Copyright 2005 LogicBlaze, Inc. http://www.logicblaze.com
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

import org.logicblaze.lingo.jms.ReplyHandler;

/**
 * Represents a thread safe map of collectionIDs to reply handlers where
 * handlers which are not accessed within the timeout period are discarded.
 * 
 * This interface also implements the {@link Runnable} interface so that it can
 * be polled to clean up any expired entries.
 * 
 * @version $Revision$
 */
public interface RequestHandlerMap extends Runnable {

    ReplyHandler get(String correlationID);

    void put(String correlationID, ReplyHandler handler, long timeout);

    void remove(String correlationID);

}
