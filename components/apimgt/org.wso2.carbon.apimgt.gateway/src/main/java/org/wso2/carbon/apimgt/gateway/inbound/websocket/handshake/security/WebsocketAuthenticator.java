/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.inbound.websocket.handshake.security;

import org.apache.axis2.Constants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.APIRequestInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.MsgInfoDTO;
import org.wso2.carbon.apimgt.common.gateway.dto.RequestContextDTO;
import org.wso2.carbon.apimgt.gateway.handlers.ext.contexthandler.InboundContextHandler;
import org.wso2.carbon.apimgt.gateway.handlers.security.*;
import org.wso2.carbon.apimgt.gateway.handlers.security.apikey.ApiKeyAuthenticator;
import org.wso2.carbon.apimgt.gateway.handlers.security.basicauth.BasicAuthAuthenticator;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.handshake.security.oauth.OAuthAuthenticator;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WebsocketAuthenticator {

    private static final Log log = LogFactory.getLog(WebsocketAuthenticator.class);
    protected ArrayList<Authenticator> authenticators = new ArrayList<>();
    protected boolean isAuthenticatorsInitialized = false;
    private InboundMessageContext inboundMessageContext;

    private String authorizationHeader;
    private String apiSecurity;
    private String apiLevelPolicy;

    public WebsocketAuthenticator(InboundMessageContext inboundMessageContext) {
        this.inboundMessageContext = inboundMessageContext;
        isAuthenticatorsInitialized = true;

        boolean isOAuthProtected = true;
        boolean isBasicAuthProtected = true;
        boolean isApiKeyProtected = true;
        boolean isOAuthBasicAuthMandatory = true;

        authorizationHeader = DataHolder.getInstance().getAuthorizationHeaderFromUUID(inboundMessageContext.
                getElectedAPI().getUuid());
        apiLevelPolicy = null;
        apiSecurity = DataHolder.getInstance().getApiSecurityFromUUID(inboundMessageContext.
                getElectedAPI().getUuid());

        // Set security conditions
        if (apiSecurity == null) {
            isOAuthProtected = true;
        } else {
            String[] apiSecurityLevels = apiSecurity.split(",");
            for (String apiSecurityLevel : apiSecurityLevels) {
                if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                    isOAuthProtected = true;
                } else if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_BASIC_AUTH)) {
                    isBasicAuthProtected = true;
                } else if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY)) {
                    isOAuthBasicAuthMandatory = true;
                } else if (apiSecurityLevel.trim().equalsIgnoreCase((APIConstants.API_SECURITY_API_KEY))) {
                    isApiKeyProtected = true;
                }
            }
        }
        if (!isOAuthBasicAuthMandatory) {
            isOAuthBasicAuthMandatory = true;
        }

        // Set authenticators
//        if (isMutualSSLProtected) {
//            Authenticator  authenticator = new MutualSSLAuthenticator(apiLevelPolicy, isMutualSSLMandatory, certificateInformation);
//            authenticator.init(synapseEnvironment);
//            authenticators.add(authenticator);
//        }
        if (isOAuthProtected) {
            Authenticator authenticator = new OAuthAuthenticator(authorizationHeader, isOAuthBasicAuthMandatory);
            authenticators.add(authenticator);
        }
        if (isBasicAuthProtected) {
            Authenticator authenticator = new BasicAuthAuthenticator(authorizationHeader, isOAuthBasicAuthMandatory,
                    apiLevelPolicy);
            authenticators.add(authenticator);
        }
        if (isApiKeyProtected) {
            Authenticator authenticator = new ApiKeyAuthenticator(APIConstants.API_KEY_HEADER_QUERY_PARAM,
                    apiLevelPolicy, isOAuthBasicAuthMandatory);
            authenticators.add(authenticator);
        }

        authenticators.sort(new Comparator<Authenticator>() {
            @Override
            public int compare(Authenticator o1, Authenticator o2) {
                return (o1.getPriority() - o2.getPriority());
            }
        });
    }

    /**
     * Authenticates the given request using the authenticators which have been initialized.
     *
     * @param inboundMessageContext The message to be authenticated
     * @return true if the authentication is successful (never returns false)
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    public boolean isAuthenticated(InboundMessageContext inboundMessageContext) throws APISecurityException, APIManagementException {


        boolean authenticated = false;
        AuthenticationResponse authenticationResponse;
        List<AuthenticationResponse> authResponses = new ArrayList<>();
        RequestContextDTO requestContext = generateRequestContext(inboundMessageContext);


        for (Authenticator authenticator : authenticators) {
            authenticationResponse = authenticator.authenticate(requestContext);
            if (authenticationResponse.isMandatoryAuthentication()) {
                // Update authentication status only if the authentication is a mandatory one
                authenticated = authenticationResponse.isAuthenticated();

            }
            if (!authenticationResponse.isAuthenticated()) {
                authResponses.add(authenticationResponse);
            }
            if (!authenticationResponse.isContinueToNextAuthenticator()) {
                break;
            }
        }
        if (!authenticated) {
            Pair<Integer, String> error = getError(authResponses);
            if (error.getValue() != null && error.getKey() > 0) {
                throw new APISecurityException(error.getKey(), error.getValue());
            }
            return false;
        }
        return true;
    }

    private Pair<Integer, String> getError(List<AuthenticationResponse> authResponses) {
        Pair<Integer, String> error = null;
        boolean isMissingCredentials = false;
        for (AuthenticationResponse authResponse : authResponses) {
            // get error for transport level mandatory auth failure
            if (!authResponse.isContinueToNextAuthenticator()) {
                error = Pair.of(authResponse.getErrorCode(), authResponse.getErrorMessage());
                return error;
            }
            // get error for application level mandatory auth failure
            if (authResponse.isMandatoryAuthentication() &&
                    (authResponse.getErrorCode() != APISecurityConstants.API_AUTH_MISSING_CREDENTIALS)) {
                error = Pair.of(authResponse.getErrorCode(), authResponse.getErrorMessage());
            } else {
                isMissingCredentials = true;
            }
        }
        // finally checks whether it is missing credentials
        if (error == null && isMissingCredentials) {
            error = Pair.of(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            return error;
        } else if (error == null) {
            // ideally this should not exist
            error = Pair.of(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        }
        return error;
    }

    /**
     * Generates RequestContextDTO object using InboundMessageContext.
     *
     * @param inboundMessageContext InboundMessageContext
     * @return RequestContextDTO
     */
    private RequestContextDTO generateRequestContext (InboundMessageContext inboundMessageContext) {
        RequestContextDTO requestContextDTO = new RequestContextDTO();
        MsgInfoDTO msgInfoDTO = generateMessageInfo(inboundMessageContext);
        APIRequestInfoDTO apiRequestInfoDTO = generateAPIInfoDTO(inboundMessageContext);
        requestContextDTO.setApiRequestInfo(apiRequestInfoDTO);
        requestContextDTO.setMsgInfo(msgInfoDTO);
        requestContextDTO.setOrganizationAddress(inboundMessageContext.getTenantDomain());
        requestContextDTO.setRemoteIPAddress(inboundMessageContext.getUserIP());

        InboundContextHandler inboundContextHandler = new InboundContextHandler(inboundMessageContext);
        requestContextDTO.setContextHandler(inboundContextHandler);

        return requestContextDTO;
    }


    /**
     * Populate common MsgInfoDTO properties for both Request and Response from InboundMessageContext.
     *
     * @param inboundMessageContext InboundMessageContext
     */
    private static MsgInfoDTO generateMessageInfo(InboundMessageContext inboundMessageContext) {

        MsgInfoDTO msgInfoDTO = new MsgInfoDTO();
        msgInfoDTO.setHeaders(inboundMessageContext.getRequestHeaders());
        msgInfoDTO.setResource(inboundMessageContext.getMatchingResource());
        msgInfoDTO.setElectedResource(inboundMessageContext.getMatchingResource());
        msgInfoDTO.setHttpMethod(WebSocketApiConstants.WEBSOCKET_DUMMY_HTTP_METHOD_NAME);
        msgInfoDTO.setHttpMethod((String)(inboundMessageContext.getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD)));
        return msgInfoDTO;
    }

    /**
     * Generates APIRequestInfoDTO object using InboundMessageContext.
     *
     * @param inboundMessageContext) InboundMessageContext)
     * @return APIRequestInfoDTO
     */
    private static APIRequestInfoDTO generateAPIInfoDTO(InboundMessageContext inboundMessageContext) {

        APIRequestInfoDTO apiRequestInfoDTO = new APIRequestInfoDTO();
        apiRequestInfoDTO.setContext(inboundMessageContext.getApiContext());
        apiRequestInfoDTO.setVersion(inboundMessageContext.getVersion());
        apiRequestInfoDTO.setApiType(inboundMessageContext.getElectedAPI().getApiType());
        apiRequestInfoDTO.setApiId(inboundMessageContext.getElectedAPI().getUuid());
        AuthenticationContext authenticationContext = inboundMessageContext.getAuthContext();
        if (authenticationContext != null) {
            apiRequestInfoDTO.setUsername(authenticationContext.getUsername());
            apiRequestInfoDTO.setConsumerKey(authenticationContext.getConsumerKey());
        }
        return apiRequestInfoDTO;
    }
}
