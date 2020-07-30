/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.jwt;

import com.nimbusds.jwt.SignedJWT;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.dto.TokenIssuerDto;

/**
 * This interface used to validate JWT In Self Contain Manner
 */
public interface JWTValidator {

    /**
     * This method used to validate JWT token
     * @param jwtToken {@link SignedJWT} token
     * @return JWTValidationInfo for validated Token
     * @throws APIManagementException
     */
    JWTValidationInfo validateToken(SignedJWTInfo jwtToken) throws APIManagementException;

    /**
     * This method used to load JWTValidator related configurations
     *
     * @param tokenIssuerConfigurations
     */
    void loadTokenIssuerConfiguration(TokenIssuerDto tokenIssuerConfigurations);
 }
