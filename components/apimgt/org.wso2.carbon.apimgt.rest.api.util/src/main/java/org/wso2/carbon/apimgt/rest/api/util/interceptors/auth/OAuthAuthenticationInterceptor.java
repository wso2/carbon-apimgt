/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.util.interceptors.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.security.AuthenticationException;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.MethodStats;
import org.wso2.carbon.apimgt.rest.api.common.RestAPIAuthenticationManager;
import org.wso2.carbon.apimgt.rest.api.common.RestAPIAuthenticator;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.AbstractOAuthAuthenticator;
import org.wso2.carbon.apimgt.rest.api.util.impl.OAuthJwtAuthenticatorImpl;
import org.wso2.carbon.apimgt.rest.api.util.impl.OAuthOpaqueAuthenticatorImpl;
import org.wso2.carbon.apimgt.rest.api.util.utils.JWTAuthenticationUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.*;
import java.util.regex.Pattern;

/**
 * This class will validate incoming requests with OAUth authenticator headers
 * You can place this handler name in your web application if you need OAuth
 * based authentication.
 */
public class OAuthAuthenticationInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(OAuthAuthenticationInterceptor.class);
    private static final String OAUTH_AUTHENTICATOR = "OAuth";
    private static final String REGEX_BEARER_PATTERN = "Bearer\\s";
    private static final Pattern PATTERN = Pattern.compile(REGEX_BEARER_PATTERN);
    private Map<String, AbstractOAuthAuthenticator> authenticatorMap = new HashMap<>();

    {
        authenticatorMap.put(RestApiConstants.JWT_AUTHENTICATION, new OAuthJwtAuthenticatorImpl());
        authenticatorMap.put(RestApiConstants.OPAQUE_AUTHENTICATION, new OAuthOpaqueAuthenticatorImpl());
    }

    public OAuthAuthenticationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }

    @Override
    @MethodStats
    public void handleMessage(Message inMessage) {

        //by-passes the interceptor if user calls an anonymous api
        if (RestApiUtil.checkIfAnonymousAPI(inMessage)) {
            return;
        }

        HashMap<String, Object> authContext = JWTAuthenticationUtils.addToJWTAuthenticationContext(inMessage);
        RestAPIAuthenticator authenticator = RestAPIAuthenticationManager.getAuthenticator(authContext);

        if (authenticator != null) {
            try {
                String authenticationType = authenticator.getAuthenticationType();
                inMessage.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, authenticator.getAuthenticationType());
                String basePath = (String) inMessage.get(RestApiConstants.BASE_PATH);
                String version = (String) inMessage.get(RestApiConstants.API_VERSION);
                authContext.put(RestApiConstants.URI_TEMPLATES, RestApiUtil.getURITemplatesForBasePath(basePath + version));
                authContext.put(RestApiConstants.ORG_ID, RestApiUtil.resolveOrganization(inMessage));
                if (authenticator.authenticate(authContext)) {
                    inMessage = JWTAuthenticationUtils.addToMessageContext(inMessage, authContext);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Request has been Authenticated , authentication type : "+ authenticationType);
                    }
                } else {
                    logger.error("Failed to Authenticate , authentication type : "+ authenticationType);
                    throw new AuthenticationException("Unauthenticated request");
                }
            } catch (APIManagementException e) {
                logger.error("Authentication Failure " +  e.getMessage());
                return;
            }
        }

        // Following logic will be moved to separate class in near future
        if (authenticator == null) {
             String accessToken = RestApiUtil.extractOAuthAccessTokenFromMessage(inMessage,
                    RestApiConstants.REGEX_BEARER_PATTERN, RestApiConstants.AUTH_HEADER_NAME);
            //add masked token to the Message
            inMessage.put(RestApiConstants.MASKED_TOKEN, APIUtil.getMaskedToken(accessToken));

            if (accessToken == null) {
                return;
            }

            if (accessToken.contains(RestApiConstants.DOT)) {
                inMessage.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, RestApiConstants.JWT_AUTHENTICATION);
            } else {
                inMessage.put(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME, RestApiConstants.OPAQUE_AUTHENTICATION);
            }

            try {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Authenticating request with : "
                            + inMessage.get(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME)) + "Authentication");
                }
                AbstractOAuthAuthenticator abstractOAuthAuthenticator = authenticatorMap
                        .get(inMessage.get(RestApiConstants.REQUEST_AUTHENTICATION_SCHEME));
                logger.debug("Selected Authenticator for the token validation " + abstractOAuthAuthenticator);
                if (abstractOAuthAuthenticator.authenticate(inMessage)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("User logged into Web app using OAuth Authentication");
                    }
                } else {
                    throw new AuthenticationException("Unauthenticated request");
                }
            } catch (APIManagementException e) {
                logger.error("Error while authenticating incoming request to API Manager REST API", e);
            }
        }
    }
}
