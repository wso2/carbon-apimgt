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

package org.wso2.carbon.apimgt.common.gateway.jwt;

import com.nimbusds.jwt.SignedJWT;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.exception.CommonGatewayException;

/**
 * This interface used to validate JWT In Self Contain Manner
 */
public interface JWTValidator {

    /**
     * This method used to validate JWT token
     * @param jwtToken {@link SignedJWT} token
     * @return JWTValidationInfo for validated Token
     * @throws CommonGatewayException if an exception occurs during the validation process
     */
    JWTValidationInfo validateToken(SignedJWTInfo jwtToken) throws CommonGatewayException;

    /**
     * This method used to load JWTValidator related configurations
     *
     * @param tokenIssuerConfigurations {@link TokenIssuerDto} object
     */
    @Deprecated
    void loadTokenIssuerConfiguration(TokenIssuerDto tokenIssuerConfigurations);

    /**
     * This method is used to load the configuration for JWT Validator.
     *
     * @param jwtValidatorConfiguration {@link JWTValidatorConfiguration} object
     */
    default void loadValidatorConfiguration(JWTValidatorConfiguration jwtValidatorConfiguration) {
        // TODO: (VirajSalaka) implement body
    }
}
