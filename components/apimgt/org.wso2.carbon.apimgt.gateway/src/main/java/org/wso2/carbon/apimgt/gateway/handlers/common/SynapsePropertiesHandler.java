/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.AbstractHandler;

public class SynapsePropertiesHandler extends AbstractHandler{

    public boolean handleRequest(MessageContext messageContext) {
        String httpport = System.getProperty("http.nio.port");
        String httpsport = System.getProperty("https.nio.port");
        messageContext.setProperty("http.nio.port", httpport);
        messageContext.setProperty("https.nio.port", httpsport);
        String mgtHttpsPort = System.getProperty("mgt.transport.https.port");
        messageContext.setProperty("mgtHttpsPort",mgtHttpsPort);

        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }
}
