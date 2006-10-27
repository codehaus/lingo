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

import javax.cache.Cache;
import javax.cache.CacheEntry;

/**
 * A collection of helper methods for working with JCache
 *
 * @version $Revision$
 */
public class JCacheHelper {

    /**
     * Returns the version object for the given key or null if there is no version available
     */
    public static Object getEntryVersion(Cache cache, Object key) {
        CacheEntry entry = cache.getCacheEntry(key);
        if (entry != null) {
            return new Long(entry.getVersion());
        }
        return null;
    }
}
