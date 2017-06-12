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
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;

/**
 * Utility class for performing Operations related to Applications, OAuth clients.
 */
public class ApplicationUtils {

    private static final Logger log = LoggerFactory.getLogger(ApplicationUtils.class);
    
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
