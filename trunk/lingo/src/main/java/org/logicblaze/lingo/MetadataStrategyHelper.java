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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A helper class for working with {@link MetadataStrategy} instances
 * 
 * @version $Revision$
 */
public class MetadataStrategyHelper {

    private static final Log log = LogFactory.getLog(MetadataStrategyHelper.class);
    private static boolean initialised;
    private static Class type;

    /**
     * Creates a new default instance of MetadataStrategy. On Java 5 if you have
     * the <a href="http://lingo.codehaus.org/SCA+Support">SCA Annotations</a>
     * on the classpath then this will use the SCAMetadataStrategy by default;
     * otherwise the {@link SimpleMetadataStrategy} is used.
     * 
     * @return a newly created instance
     */
    public static MetadataStrategy newInstance() {
        synchronized (MetadataStrategy.class) {
            if (!initialised) {
                type = findMetadataStrategyClass();
                initialised = true;
            }
        }
        
        if (type != null) {
            try {
                return (MetadataStrategy) type.newInstance();
            }
            catch (Exception e) {
                log.warn("Could not create instance of: " + type.getName() + ". Reason: " + e, e);
            }
        }
        return new SimpleMetadataStrategy();
    }

    protected static Class findMetadataStrategyClass() {
        return findClass("org.logicblaze.lingo.sca.SCAMetadataStrategy");
    }

    protected static Class findClass(String name) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        }
        catch (ClassNotFoundException e) {
            try {
                return MetadataStrategy.class.getClassLoader().loadClass(name);
            }
            catch (ClassNotFoundException e1) {
                log.debug("Failed to find class: " + name + " on classpath. Reason: " + e, e);
                return null;
            }
        }
    }
}
