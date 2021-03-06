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
package org.logicblaze.lingo.sca;

import org.logicblaze.lingo.MetadataStrategy;
import org.logicblaze.lingo.SimpleMetadataStrategy;
import org.osoa.sca.annotations.Callback;
import org.osoa.sca.annotations.EndSession;
import org.osoa.sca.annotations.OneWay;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * An implementation of {@link MetadataStrategy} which uses the <a
 * href="http://incubator.apache.org/tuscany/">SCA</a> annotations to configure
 * the metadata strategy.
 * 
 * @version $Revision$
 */
public class SCAMetadataStrategy extends SimpleMetadataStrategy {

    private static final long serialVersionUID = -50969123678085273L;

    @Override
    public boolean isRemoteParameter(Method method, Class parameterType, int index) {
        Annotation annotation = parameterType.getAnnotation(Callback.class);
        if (annotation != null) {
            return true;
        }
        return super.isRemoteParameter(method, parameterType, index);
    }

    @Override
    protected boolean isOneWayMethod(Method method) {
        if (hasAnnotation(method, OneWay.class)) {
            return true;
        }
        return super.isOneWayMethod(method);
    }

    @Override
    protected boolean isEndSession(Method method) {
        if (hasAnnotation(method, EndSession.class)) {
            return true;
        }
        return super.isEndSession(method);
    }

    @Override
    protected boolean isStateful(Method method) {
        // TODO
        return super.isStateful(method);
    }

    protected boolean hasAnnotation(Method method, Class type) {
        return method.getAnnotation(type) != null;
    }

}
