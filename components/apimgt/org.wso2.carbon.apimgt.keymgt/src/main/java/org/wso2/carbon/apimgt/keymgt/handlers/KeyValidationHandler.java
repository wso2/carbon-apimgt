/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.handlers;

import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;

public interface KeyValidationHandler {
    /**
     * Validate token by oAuth2TokenValidationMessageContext
     *
     * @param tokenValidationContext
     */
    boolean validateToken(TokenValidationContext tokenValidationContext)
            throws APIKeyMgtException;
    /**
     * Validate Subscriptions  by oAuth2TokenValidationMessageContext
     *
     * @param tokenValidationContext
     */
    boolean validateSubscription(TokenValidationContext tokenValidationContext)
            throws APIKeyMgtException;

    /**
     * Validate Subscriptions by API context, API version and consumer key
     *
     * @param apiContext
     * @param apiVersion
     * @param consumerKey
     */
    APIKeyValidationInfoDTO validateSubscription(String apiContext, String apiVersion, String consumerKey,String keyManager);
    /**
     * Validate Scopes  by oAuth2TokenValidationMessageContext
     *
     * @param tokenValidationContext
     */
    boolean validateScopes(TokenValidationContext tokenValidationContext)
            throws APIKeyMgtException;

    /**
     * Validate scopes bound to the resource of the API being invoked against the scopes specified
     * in the JWT token payload.
     *
     * @param apiContext        API Context
     * @param apiVersion        API Version
     * @param matchingResource  Accessed API resource
     * @param httpMethod        API resource's HTTP method
     * @param jwtValidationInfo Validated JWT Information
     * @return <code>true</code> if scope validation is successful and
     * <code>false</code> if scope validation failed
     * @throws APIKeyMgtException in case of scope validation failure
     */
    boolean validateScopes(String apiContext, String apiVersion, String matchingResource, String httpMethod,
                           JWTValidationInfo jwtValidationInfo)
            throws APIKeyMgtException;

    /**
     * generateConsumerToken  by oAuth2TokenValidationMessageContext
     * JWT token this will use to default implementation.
     * @param tokenValidationContext
     */
    boolean generateConsumerToken(TokenValidationContext tokenValidationContext)
            throws APIKeyMgtException;
}
