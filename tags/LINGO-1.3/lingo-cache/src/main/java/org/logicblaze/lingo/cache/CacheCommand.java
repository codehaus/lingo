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
package org.logicblaze.lingo.cache;

import java.io.Serializable;

/**
 * Represents a command which operates on a cache manager
 *
 * @version $Revision$
 */
public abstract class CacheCommand implements Serializable {
    private String originator;

    public CacheCommand() {
    }

    public CacheCommand(String originator) {
        this.originator = originator;
    }

    /**
     * Actually performs the command on the cache manager
     */
    public abstract void run(TransactionalCacheManager cacheManager);

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }
}
