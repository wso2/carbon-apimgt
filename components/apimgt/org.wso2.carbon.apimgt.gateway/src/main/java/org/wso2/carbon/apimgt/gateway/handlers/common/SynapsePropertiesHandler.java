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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

public class SynapsePropertiesHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(SynapsePropertiesHandler.class);
    private static APIManagerConfiguration config = null;
    private static boolean iskmReverseProxyEnabled = false;

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
                log.debug("Host Header : " + hostHeader);
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
            headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        }

        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        if (config == null) {
            // Retrieve the ISKMReverseProxyEnabled property value from api manager configurations.
            // This value indicates whether the IS Authentication endpoint has been reverse proxied through
            // the Gateway.
            config = ServiceReferenceHolder.getInstance().getApiManagerConfigurationService()
                    .getAPIManagerConfiguration();
            iskmReverseProxyEnabled = Boolean.parseBoolean(
                    config.getFirstProperty(APIConstants.AUTH_MANAGER + APIConstants.IS_KM_REVERSE_PROXY_ENABLED));
        }
        // Modify location header only if ISKMReverseProxyEnabled property is set to true
        if (iskmReverseProxyEnabled) {
            // The logic is related if the API context is "/authorize", "/commonauth" or "/oidc" while status code is 302
            log.debug("Km revers proxy enabled for " + messageContext.getProperty(RESTConstants.REST_API_CONTEXT));
            if (messageContext.getProperty(RESTConstants.REST_API_CONTEXT)
                    .equals(APIMgtGatewayConstants.AUTHORIZE_CONTEXT) || messageContext
                    .getProperty(RESTConstants.REST_API_CONTEXT).equals(APIMgtGatewayConstants.COMMON_AUTH_CONTEXT)
                    || messageContext.getProperty(RESTConstants.REST_API_CONTEXT)
                    .equals(APIMgtGatewayConstants.OIDC_CONTEXT)) {
                if (302 == (Integer) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                        .getProperty(SynapseConstants.HTTP_SC)) {
                    // Retrieve the transport headers in the response and identify the location header
                    TreeMap<String, String> headers = (TreeMap) ((Axis2MessageContext) messageContext)
                            .getAxis2MessageContext().getProperty(APIMgtGatewayConstants.TRANSPORT_HEADERS);
                    try {
                        URI locationURI = new URI(headers.get(APIMgtGatewayConstants.LOCATION));
                        log.debug("Location URI before rewrite " + locationURI.toString());
                        String hostHeader = (String) messageContext.getProperty(APIMgtGatewayConstants.HOST_HEADER);
                        String kmHostName = (String) messageContext.getProperty("keyManager.hostname");
                        String kmHostPort = (String) messageContext.getProperty("keyManager.port");
                        String locationURIString = locationURI.toString();
                        if (locationURI.getPort() == -1) {
                            locationURIString = locationURIString.replaceFirst(kmHostName, hostHeader);
                        } else {
                            locationURIString = locationURIString.replaceFirst(kmHostName + ":" + kmHostPort,
                                    hostHeader);
                        }
                        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                                .setProperty("PRE_LOCATION_HEADER", locationURI);
                        log.debug("Location URI after rewrite KM host to GW " + locationURIString);
                        if (messageContext.getProperty(RESTConstants.REST_API_CONTEXT)
                                .equals(APIMgtGatewayConstants.COMMON_AUTH_CONTEXT)) {
                            // Commonauth endpoints return location header /oauth2/authorize. Since GW has /authorize we have to
                            // omit the /oauth2 portion from the location header value
                            locationURIString = locationURIString.replaceFirst(APIMgtGatewayConstants.OAUTH2_CONTEXT, "");
                        }
                        // Inserting modified headers to the message context
                        log.debug("Location URI after rewrite removing /oauth2 " + locationURIString);
                        headers.put(APIMgtGatewayConstants.LOCATION, locationURIString);
                        ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                                setProperty(APIMgtGatewayConstants.TRANSPORT_HEADERS, headers);
                    } catch (URISyntaxException e) {
                        log.error("Unable to cast location URL", e);
                    }
                }
            }
        }
        return true;
    }
}
