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

import org.apache.axis2.Constants;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;

public class SynapsePropertiesHandler extends AbstractHandler {

    public boolean handleRequest(MessageContext messageContext) {
        String httpport = System.getProperty("http.nio.port");
        String httpsport = System.getProperty("https.nio.port");
        messageContext.setProperty("http.nio.port", httpport);
        messageContext.setProperty("https.nio.port", httpsport);
        String mgtHttpsPort = System.getProperty(APIConstants.KEYMANAGER_PORT);
        messageContext.setProperty("keyManager.port", mgtHttpsPort);
        String keyManagerHost = System.getProperty(APIConstants.KEYMANAGER_HOSTNAME);
        messageContext.setProperty("keyManager.hostname", keyManagerHost);

        String httpMethod = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map headers =
                (Map) (axis2MsgContext).getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        boolean isContentTypeNotSet = false;
        if (headers != null) {
            isContentTypeNotSet = headers.get("Content-Type") == null || headers.get("Content-Type").equals("");
            if (headers.get(APIMgtGatewayConstants.HOST) != null || !("")
                    .equals(headers.get(APIMgtGatewayConstants.HOST))) {
                // Derive the outward facing host and port from host header
                String hostHeader = (String) headers.get(APIMgtGatewayConstants.HOST);
                // Set it as a message context property to retrieve in HandleResponse method
                messageContext.setProperty(APIMgtGatewayConstants.HOST_HEADER, hostHeader);
            }
        }
        if (isContentTypeNotSet && (httpMethod.equals(Constants.Configuration.HTTP_METHOD_POST) ||
                httpMethod.equals(Constants.Configuration.HTTP_METHOD_PUT))) {
            // Need to set both the property and the header for this to work.
            // Simply setting the header will not work. It'll make synapse assume the ContentType property
            // to be default 'application/octet-stream'. Which causes a HTTP 415 response
            ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    setProperty("ContentType", "application/x-www-form-urlencoded");
            headers.put(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
        }

        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }
}
