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
package org.logicblaze.lingo.jmx.remote.provider.jms;


import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXServiceURL;
import org.logicblaze.lingo.jmx.remote.jms.JmsJmxConnector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

/**
 * @version $Revision$
 */
public class ClientProvider implements JMXConnectorProvider
{
    /**
     * Construct a new JMXConnector
     * @param url
     * @param environment
     * @return the nerw Connector
     * @throws IOException
     */
   public JMXConnector newJMXConnector(JMXServiceURL url, Map environment) throws IOException
   {
       String protocol = url.getProtocol();
       if (!"jms".equals(protocol)) throw new MalformedURLException("Wrong protocol " + protocol + " for provider " + this);
      JMXConnector result =  new JmsJmxConnector(environment,url);
      return result;
   }
}
