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

package org.wso2.carbon.apimgt.gateway.security.handlers;

import org.wso2.carbon.apimgt.gateway.exception.APIKeyMgtException;
import org.wso2.carbon.apimgt.gateway.models.TokenValidationContext;

/**
 * This is the interface to implement validating oauth2 token in API request flow
 **/

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
    default boolean validateSubscription(TokenValidationContext tokenValidationContext)
            throws APIKeyMgtException {
        //TO-DO Add the logic to call apim.core validateSubscriptions micro service
        return true;
    }

    /**
     * Validate Scopes  by oAuth2TokenValidationMessageContext
     *
     * @param tokenValidationContext
     */
    boolean validateScopes(TokenValidationContext tokenValidationContext)
            throws APIKeyMgtException;

    /**
     * generateConsumerToken  by oAuth2TokenValidationMessageContext
     * JWT token this will use to default implementation.
     *
     * @param tokenValidationContext
     */
    boolean generateConsumerToken(TokenValidationContext tokenValidationContext)
            throws APIKeyMgtException;
}
