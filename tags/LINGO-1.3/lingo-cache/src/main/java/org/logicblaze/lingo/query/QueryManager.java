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
package org.logicblaze.lingo.query;

import java.util.List;

/**
 * Provides access to some query manager capable of querying objects in some context.
 *
 * @version $Revision$
 */
public interface QueryManager {

    /**
     * Performs a query on a given CacheManager
     *
     * @param query        the query string using the query notation of the manager's choosing.
     * @param cacheManager the collection of caches to query on
     * @return a list of results which may be empty.
     */
    public List select(String query, QueryContext cacheManager);

    Object evaluate(String query, QueryContext context) throws QueryException;
}
