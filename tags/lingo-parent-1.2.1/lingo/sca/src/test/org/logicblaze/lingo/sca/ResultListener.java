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
package org.logicblaze.lingo.sca;

import org.osoa.sca.annotations.EndSession;

import java.util.EventListener;

/**
 * An example asynchronous notification listener which can be passed as a
 * parameter across lingo
 * 
 * @version $Revision$
 */
public interface ResultListener extends EventListener {
    public void onResult(String data);

    // lifecycle end methods

    @EndSession
    public void stop();

    @EndSession
    public void onException(Exception e);
}
