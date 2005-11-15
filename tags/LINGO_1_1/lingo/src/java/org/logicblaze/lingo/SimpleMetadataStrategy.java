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
package org.logicblaze.lingo;

import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.EventListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A simple metadata strategy which uses POJO naming conventions.
 *
 * By default all method invocations are synchronous to avoid surprising users of Spring Remoting. However
 * if you set the {@link #setOneWayForVoidMethods(boolean)} value to true then all void methods which do
 * not throw checked exceptions become asynchronous one way methods.
 *
 * <p/>
 * Also any object which implements the {@link Remote} interface or the {@link EventListener}
 * are assumed to be remote and so a remote proxy is used to allow remote notifications and asynchronous
 * messaging.
 *
 * @version $Revision$
 */
public class SimpleMetadataStrategy implements MetadataStrategy {
    private static final long serialVersionUID = 3314789109318386510L;
    
    private boolean oneWayForVoidMethods;
    private Set remoteTypes;

    public SimpleMetadataStrategy() {
    }

    public SimpleMetadataStrategy(boolean oneWayForVoidMethods) {
        this.oneWayForVoidMethods = oneWayForVoidMethods;
    }

    public MethodMetadata getMethodMetadata(Method method) {
        boolean oneway = false;
        if (oneWayForVoidMethods) {
            oneway = method.getReturnType().equals(void.class) && method.getExceptionTypes().length == 0;
        }
        boolean[] remoteParams = null;
        Class[] parameterTypes = method.getParameterTypes();
        int size = parameterTypes.length;
        if (size > 0) {
            remoteParams = new boolean[size];
            for (int i = 0; i < size; i++) {
                remoteParams[i] = isRemoteParameter(method, parameterTypes[i], i);
            }
        }
        return new MethodMetadata(oneway, remoteParams);
    }

    public boolean isOneWayForVoidMethods() {
        return oneWayForVoidMethods;
    }

    public void setOneWayForVoidMethods(boolean oneWayForVoidMethods) {
        this.oneWayForVoidMethods = oneWayForVoidMethods;
    }

    public Set getRemoteTypes() {
        if (remoteTypes == null) {
            remoteTypes = new HashSet();
            populateDefaultRemoteTypes(remoteTypes);
        }
        return remoteTypes;
    }


    public void setRemoteTypes(Set remoteTypes) {
        this.remoteTypes = remoteTypes;
    }

    public boolean isRemoteParameter(Method method, Class parameterType, int index) {
        for (Iterator iter = getRemoteTypes().iterator(); iter.hasNext();) {
            Class type = (Class) iter.next();
            if (type.isAssignableFrom(parameterType)) {
                return true;
            }
        }
        return false;
    }

    protected void populateDefaultRemoteTypes(Set remoteTypes) {
        remoteTypes.add(Remote.class);
        remoteTypes.add(EventListener.class);
    }
}
