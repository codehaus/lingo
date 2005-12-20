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

import org.aopalliance.intercept.MethodInvocation;
import org.logicblaze.lingo.util.LRUCache;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Provides caching of metadata for performance.
 * 
 * @version $Revision$
 */
public class CachingMetadataStrategy implements MetadataStrategy {

    private static final long serialVersionUID = -6008790663804471523L;
    
    private MetadataStrategy proxy;
    private Map cache;
    private int cacheSize = 5000;

    public CachingMetadataStrategy(MetadataStrategy proxy) {
        this.proxy = proxy;
    }

    public CachingMetadataStrategy(MetadataStrategy proxy, Map cache) {
        this.proxy = proxy;
        this.cache = cache;
    }

    public MethodMetadata getMethodMetadata(Method method) {
        MethodMetadata answer = (MethodMetadata) getCache().get(method);
        if (answer == null) {
            answer = proxy.getMethodMetadata(method);
            getCache().put(method, answer);
        }
        return answer;
    }

    public ResultJoinStrategy getResultJoinStrategy(MethodInvocation methodInvocation, MethodMetadata metadata) {
        return proxy.getResultJoinStrategy(methodInvocation, metadata);
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
    protected Map createCache() {
        return new LRUCache(getCacheSize());
    }
}
