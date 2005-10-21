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
package org.logicblaze.lingo;

import org.logicblaze.lingo.util.LRUCache;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Provides caching of metadata for performance.
 * 
 * @version $Revision$
 */
public abstract class CachingMetadataStrategySupport implements MetadataStrategy {

    private Map cache;
    private int cacheSize = 5000;

    public MethodMetadata getMethodMetadata(Method method) {
        MethodMetadata answer = (MethodMetadata) getCache().get(method);
        if (answer == null) {
            answer = createMethodMetadata(method);
            getCache().put(method, answer);
        }
        return answer;
    }

    // Properties
    // -------------------------------------------------------------------------
    public Map getCache() {
        if (cache == null) {
            cache = createCache();
        }
        return cache;
    }

    public void setCache(Map cache) {
        this.cache = cache;
    }

    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    // Implementation methods
    // -------------------------------------------------------------------------

    /**
     * Factory method to create the metadata object for the given method
     */
    protected abstract MethodMetadata createMethodMetadata(Method method);

    protected Map createCache() {
        return new LRUCache(getCacheSize());
    }
}
