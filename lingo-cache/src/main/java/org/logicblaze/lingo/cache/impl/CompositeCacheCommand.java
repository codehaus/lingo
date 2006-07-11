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

import org.logicblaze.lingo.cache.CacheCommand;
import org.logicblaze.lingo.cache.TransactionalCacheManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Revision$
 */
public class CompositeCacheCommand extends CacheCommand {
    /**
     * 
     */
    private static final long serialVersionUID = 999535944385968291L;
    private List list = new ArrayList();

    public void addCommand(CacheCommand command) {
        list.add(command);
        setOriginator(command.getOriginator());
    }

    public void removeCommand(CacheCommand command) {
        list.remove(command);
    }

    public void run(TransactionalCacheManager cacheManager) {
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            CacheCommand command = (CacheCommand) iter.next();
            command.run(cacheManager);
        }
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}

