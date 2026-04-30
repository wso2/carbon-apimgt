/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.inbound.websocket.Authentication;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.utils.InboundWebsocketProcessorUtil;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Authenticator for no-auth WebSocket APIs
 */
public class NoAuthAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(NoAuthAuthenticator.class);

    @Override
    public boolean validateToken(InboundMessageContext inboundMessageContext) throws APISecurityException {
        // No token validation needed for no-auth
        return true;
    }

    @Override
    public InboundProcessorResponseDTO authenticate(InboundMessageContext inboundMessageContext) 
            throws APISecurityException {
                log.debug("Validating authentication context for no-auth WebSocket API");
                // Get client IP for throttling (similar to REST API handleNoAuthentication)
                String clientIP = inboundMessageContext.getUserIP();
                if (clientIP == null || clientIP.isEmpty()) {
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        "Unable to determine client IP for throttling in no-auth scenario");
                }
        
                // Clean up IP format if needed (remove port for IPv4)
                // IPv6 addresses are enclosed in brackets when a port is present, e.g., [2001:db8::1]:8080
                if (clientIP.contains("[") && clientIP.contains("]")) {
                    // IPv6 with port: extract address between brackets
                    clientIP = clientIP.substring(clientIP.indexOf("[") + 1, clientIP.indexOf("]"));
                } else if (clientIP.indexOf(":") > 0 && clientIP.indexOf(":") == clientIP.lastIndexOf(":")) {
                    // IPv4 with port: strip everything after the colon
                    clientIP = clientIP.substring(0, clientIP.indexOf(":"));
                }
        
                // Create a dummy AuthenticationContext object with hard coded values for Tier and KeyType
                // This is because we cannot determine the Tier nor Key Type without subscription information
                AuthenticationContext authContext = new AuthenticationContext();
                authContext.setAuthenticated(true);
                authContext.setTier(APIConstants.UNAUTHENTICATED_TIER);
                // Since we don't have details on unauthenticated tier we setting stop on quota reach true
                authContext.setStopOnQuotaReach(true);
                // Requests are throttled by the ApiKey that is set here. In an unauthenticated scenario,
                // we will use the client's IP address for throttling.
                authContext.setApiKey(clientIP);
                authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
                // This name is hardcoded as anonymous because there is no associated user token
                authContext.setUsername(APIConstants.END_USER_ANONYMOUS);
                authContext.setCallerToken(null);
                authContext.setApplicationName(null);
                authContext.setApplicationId(clientIP); // Set clientIP as application ID in unauthenticated scenario
                authContext.setConsumerKey(null);
        
                // Set additional properties for WebSocket context
                authContext.setApiName(inboundMessageContext.getApiName());
                authContext.setApiVersion(inboundMessageContext.getVersion());
        
                // Validate the authentication context and set it to inbound message context
                if (InboundWebsocketProcessorUtil.validateAuthenticationContext(authContext, inboundMessageContext)) {
                    // Create successful response - this allows the handshake to proceed normally
                    InboundProcessorResponseDTO responseDTO = new InboundProcessorResponseDTO();
                    responseDTO.setError(false);
        
                    if (log.isDebugEnabled()) {
                        log.debug("No-auth handshake successful for WebSocket API: " +
                                        inboundMessageContext.getApiContext() + ", proceeding with normal flow");
                    }
        
                    return responseDTO;
                } else {
                    log.debug("Failed to validate authentication context for no-auth WebSocket API");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                }
    }
}
