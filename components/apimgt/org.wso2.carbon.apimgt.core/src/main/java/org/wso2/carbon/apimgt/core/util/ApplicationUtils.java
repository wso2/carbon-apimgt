/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;

import java.util.List;

/**
 * Utility class for performing Operations related to Applications, OAuth clients.
 */
public class ApplicationUtils {

    private static final Logger log = LoggerFactory.getLogger(ApplicationUtils.class);

    /**
     * This method will parse json String and set properties in  OAuthApplicationInfo object.
     * Further it will initiate new OauthAppRequest  object and set applicationInfo object as its own property.
     *
     * @param clientName  Client Name.
     * @param callbackURL This is the call back URL of the application
     * @param grantTypes  Grant types to be supported by the OAuth Application
     * @return appRequest object of OauthAppRequest.
     */
    public static OAuthAppRequest createOauthAppRequest(String clientName, String callbackURL,
                                                        List<String> grantTypes) {
        //initiate OauthAppRequest object.
        OAuthAppRequest appRequest = new OAuthAppRequest();
        OAuthApplicationInfo authApplicationInfo = new OAuthApplicationInfo();
        authApplicationInfo.setClientName(clientName);
        authApplicationInfo.setCallbackUrl(callbackURL);
        authApplicationInfo.setGrantTypes(grantTypes);

        //set applicationInfo object
        appRequest.setOAuthApplicationInfo(authApplicationInfo);
        return appRequest;
    }

    public static AccessTokenRequest createAccessTokenRequest(OAuthApplicationInfo oAuthApplication)
            throws APIManagementException {

        AccessTokenRequest tokenRequest = new AccessTokenRequest();
        if (oAuthApplication.getClientId() != null || oAuthApplication.getClientSecret() != null) {
            tokenRequest.setClientId(oAuthApplication.getClientId());
            tokenRequest.setClientSecret(oAuthApplication.getClientSecret());
        } else {
            throw new KeyManagementException("Consumer key or Consumer Secret is missing.");
        }

        if (oAuthApplication.getParameter(KeyManagerConstants.TOKEN_SCOPES) != null) {
            String tokenScopes = (String) oAuthApplication.getParameter(KeyManagerConstants.TOKEN_SCOPES);
            tokenRequest.setScopes(tokenScopes);
            oAuthApplication.addParameter(KeyManagerConstants.OAUTH_CLIENT_TOKEN_SCOPE, tokenScopes);
        }

        tokenRequest.setGrantType(KeyManagerConstants.CLIENT_CREDENTIALS_GRANT_TYPE);

        if (oAuthApplication.getParameter(KeyManagerConstants.VALIDITY_PERIOD) != null) {
            tokenRequest.setValidityPeriod(Long.parseLong((String) oAuthApplication.getParameter(KeyManagerConstants
                    .VALIDITY_PERIOD)));
        } else {
            throw new KeyManagementException("Validity period missing for generated oAuth keys");
        }
        return tokenRequest;
    }
}
