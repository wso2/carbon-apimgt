/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.security;

import org.wso2.carbon.apimgt.gateway.exception.APIKeyMgtException;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * An API consumer authenticator which authenticates user requests using
 * the OAuth protocol. This implementation uses some default token/delimiter
 * values to parse OAuth headers, but if needed these settings can be overridden
 * through the APIManagerConfiguration.
 */

public class OAuthAuthenticator implements Authenticator {

   // protected APIKeyValidator keyValidator;

   /* private String securityHeader = "Authorization";
    private String defaultAPIHeader = "WSO2_AM_API_DEFAULT_VERSION";
    private String consumerKeyHeaderSegment = "Bearer";
    private String oauthHeaderSplitter = ",";
    private String consumerKeySegmentDelimiter = " ";
    private String securityContextHeader;
    private boolean removeOAuthHeadersFromOutMessage = true;
    private boolean removeDefaultAPIHeaderFromOutMessage = true;
    private String clientDomainHeader = "referer";
    private String requestOrigin;
    */

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean authenticate(CarbonMessage carbonMessage) throws APIKeyMgtException {
        return false;
    }

    @Override
    public String getChallengeString() {
        return null;
    }

    @Override
    public String getRequestOrigin() {
        return null;
    }
}
