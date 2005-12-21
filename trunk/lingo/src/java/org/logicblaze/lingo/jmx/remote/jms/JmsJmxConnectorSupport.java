/**
 *
 * Copyright RAJD Consultancy Ltd
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

package org.logicblaze.lingo.jmx.remote.jms;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.management.remote.JMXServiceURL;
import org.activemq.util.IntrospectionSupport;
import org.activemq.util.URISupport;

/**
 * @version $Revision$
 */
class JmsJmxConnectorSupport {
    /**
     * Default destination prefix
     */
    static final String DEFAULT_DESTINATION_PREFIX = "org.logicblaze.jms.jmx.";
    /**
     * The default destination server name
     */
    static final String MBEAN_SERVER_NAME = "*";
    /**
     * The default destination group name
     */
    static final String MBEAN_GROUP_NAME = "*";

    static URI getProviderURL(JMXServiceURL serviceURL) throws IOException {
        String protocol = serviceURL.getProtocol();
        if (!"jms".equals(protocol))
            throw new MalformedURLException("Wrong protocol " + protocol + " expecting jms ");
        try {
            String path = serviceURL.getURLPath();
            while (path.startsWith("/")) {
                path = path.substring(1);
            }
            return new URI(path);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
            throw new IOException(e.toString());
        }
    }

    static void populateProperties(Object value, URI url) throws IOException {
        String query = url.getQuery();
        if (query != null) {
            try {
                Map map = URISupport.parseQuery(query);
                if (map != null && !map.isEmpty()) {
                    IntrospectionSupport.setProperties(value, map);
                }
            }
            catch (URISyntaxException e) {
                e.printStackTrace();
                throw new IOException(e.toString());
            }

        }
    }

}