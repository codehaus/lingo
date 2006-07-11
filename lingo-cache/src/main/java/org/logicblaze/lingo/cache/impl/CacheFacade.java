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
import javax.cache.CacheException;
import javax.cache.CacheListener;
import javax.cache.CacheStatistics;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Delegates all operations to a delegate cache.
 *
 * @version $Revision$
 */
public abstract class CacheFacade implements Cache {

    protected abstract Cache getDelegate();

    public void addListener(CacheListener listener) {
        getDelegate().addListener(listener);
    }

    public void clear() {
        getDelegate().clear();
    }

    public boolean containsKey(Object key) {
        return getDelegate().containsKey(key);
    }


    public boolean containsValue(Object value) {
        return getDelegate().containsValue(value);
    }

    public Set entrySet() {
        return getDelegate().entrySet();
    }

    public void evict() {
        getDelegate().evict();
    }

    public Object get(Object key) {
        return getDelegate().get(key);
    }

    public Map getAll(Collection keys) throws CacheException {
        return getDelegate().getAll(keys);
    }

    public CacheEntry getCacheEntry(Object key) {
        return getDelegate().getCacheEntry(key);
    }

    public CacheStatistics getCacheStatistics() {
        return getDelegate().getCacheStatistics();
    }

    public boolean isEmpty() {
        return getDelegate().isEmpty();
    }

    public Set keySet() {
        return getDelegate().keySet();
    }

    public void load(Object key) throws CacheException {
        getDelegate().load(key);
    }

    public void loadAll(Collection keys) throws CacheException {
        getDelegate().loadAll(keys);
    }

    public Object peek(Object key) {
        return getDelegate().peek(key);
    }

    public Object put(Object key, Object value) {
        return getDelegate().put(key, value);
    }

    public void putAll(Map t) {
        getDelegate().putAll(t);
    }

    public Object remove(Object key) {
        return getDelegate().remove(key);
    }

    public void removeListener(CacheListener listener) {
        getDelegate().removeListener(listener);
    }

    public int size() {
        return getDelegate().size();
    }

    public Collection values() {
        return getDelegate().values();
    }
}
