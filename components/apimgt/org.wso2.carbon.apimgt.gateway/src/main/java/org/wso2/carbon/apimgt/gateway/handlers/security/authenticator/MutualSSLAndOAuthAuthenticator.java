/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.authenticator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.opensaml.xml.signature.P;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.handlers.security.oauth.OAuthAuthenticator;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Authenticator responsible for handling Mutual SSL and OAuth secured APIs.
 */
public class MutualSSLAndOAuthAuthenticator implements Authenticator {
    private static final Log log = LogFactory.getLog(MutualSSLAndOAuthAuthenticator.class);
    private volatile Authenticator oAuthAuthenticator;
    private volatile Authenticator mutualSSLAuthenticator;
    private String apiLevelPolicy;
    private String authorizationHeader;
    private String certificateInformation;
    private boolean removeOAuthHeadersFromOutMessage;

    /**
     * Initialized the authenticator with required parameter.
     *
     * @param authorizationHeader    Relevant authorization header.
     * @param removeOAuthHeader      indicate whether to remove OAuth header.
     * @param apiLevelPolicy         API level tier policy
     * @param certificateInformation Relevant certificate information.
     */
    public MutualSSLAndOAuthAuthenticator(String authorizationHeader, boolean removeOAuthHeader,
            String apiLevelPolicy, String certificateInformation) {
        this.authorizationHeader = authorizationHeader;
        this.removeOAuthHeadersFromOutMessage = removeOAuthHeader;
        this.apiLevelPolicy = apiLevelPolicy;
        this.certificateInformation = certificateInformation;
    }

    @Override
    public void init(SynapseEnvironment env) {
        getAuthenticator(APIConstants.DEFAULT_API_SECURITY_OAUTH2).init(env);
        getAuthenticator(APIConstants.API_SECURITY_MUTUAL_SSL).init(env);
    }

    /**
     * To get the relevant authenticator with the authenticator name.
     *
     * @param authenticatorName Name of authenticator.
     * @return relevant authenticator name.
     */
    private Authenticator getAuthenticator(String authenticatorName) {
        Authenticator authenticator;
        if (authenticatorName.equalsIgnoreCase(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
            if (oAuthAuthenticator == null) {
                oAuthAuthenticator = new OAuthAuthenticator(authorizationHeader, removeOAuthHeadersFromOutMessage);
            }
            authenticator = oAuthAuthenticator;
        } else {
            if (mutualSSLAuthenticator == null) {
                mutualSSLAuthenticator = new MutualSSLAuthenticator(apiLevelPolicy, certificateInformation);
            }
            authenticator = mutualSSLAuthenticator;
        }
        return authenticator;
    }

    @Override
    public void destroy() {
        if (oAuthAuthenticator != null) {
            oAuthAuthenticator.destroy();
        } else {
            log.warn("Unable to destroy uninitialized OAuth2 authentication handler instance");
        }
        if (mutualSSLAuthenticator != null) {
            oAuthAuthenticator.destroy();
        } else {
            log.warn("Unable to destroy uninitialized Mutual SSL authentication handler instance");
        }
    }

    @Override
    public boolean authenticate(MessageContext synCtx) throws APISecurityException {
        boolean isAuthenticated;
        String errorMessage = "";
        try {
            isAuthenticated = mutualSSLAuthenticator.authenticate(synCtx);
        } catch (APISecurityException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Mutual SSL Authentication based authentication failed. Trying with Oauth2 Authenticator",
                        ex);
            }
            log.warn("Mutual SSL Authentication based authentication failed  " + ex.getMessage());
            isAuthenticated = false;
            errorMessage = ex.getMessage() + " and ";
        }
        try {
            if (!isAuthenticated) {
                isAuthenticated = oAuthAuthenticator.authenticate(synCtx);
            }
        } catch (APISecurityException ex) {
            if (ex.getErrorCode() == APISecurityConstants.API_AUTH_MISSING_CREDENTIALS) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                        errorMessage + ex.getMessage());
            } else {
                throw ex;
            }
        }
        return isAuthenticated;
    }

    @Override
    public String getChallengeString() {
        return mutualSSLAuthenticator.getChallengeString() + " " + oAuthAuthenticator.getChallengeString();
    }

    @Override
    public String getRequestOrigin() {
        String requestOrigin = mutualSSLAuthenticator.getRequestOrigin();

        if (StringUtils.isEmpty(requestOrigin)) {
            requestOrigin = oAuthAuthenticator.getRequestOrigin();
        }
        return requestOrigin;
    }
}
