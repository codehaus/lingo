/** 
 * 
 * Copyright 2006 LogicBlaze, Inc.
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
package org.logicblaze.lingo.cache.impl;

import org.logicblaze.lingo.cache.ActiveCacheManager;
import org.springframework.transaction.support.TransactionSynchronization;

/**
 * A synchronization listener which hooks the {@link org.logicblaze.lingo.cache.ActiveCacheManager}
 * into the Spring transaction synchronization mechanism.
 * 
 * @version $Revision$
 */
public class SpringSynchronization extends TransactionSynchronizerSupport implements TransactionSynchronization {

    public SpringSynchronization(ActiveCacheManager cacheManager) {
        super(cacheManager);
    }

    public void afterCompletion(int status) {
        switch (status) {
            case STATUS_COMMITTED:
                doCommit();
                break;

            case STATUS_ROLLED_BACK:
                doRollback();
                break;
        }
    }

    public void beforeCommit(boolean readOnly) {
    }

    public void beforeCompletion() {
    }

    public void resume() {
    }

    public void suspend() {
    }
}
