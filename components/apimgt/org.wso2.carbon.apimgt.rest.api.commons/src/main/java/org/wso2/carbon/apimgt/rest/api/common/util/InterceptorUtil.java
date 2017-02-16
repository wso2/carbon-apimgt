/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.common.util;

import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;

/**
 * Utility class for REST API Interceptors.
 */
public class InterceptorUtil {

    public static AccessTokenInfo getValidatedTokenResponse(String accessToken) throws APIMgtSecurityException {
        try {
            KeyManager loginKeyManager = KeyManagerHolder.getAMLoginKeyManagerInstance();
            AccessTokenInfo accessTokenInfo = loginKeyManager.getTokenMetaData(accessToken);
            return accessTokenInfo;

        } catch (KeyManagementException e) {
            throw new APIMgtSecurityException("Error while validating access token", ExceptionCodes.AUTH_GENERAL_ERROR);
        }
    }
}
