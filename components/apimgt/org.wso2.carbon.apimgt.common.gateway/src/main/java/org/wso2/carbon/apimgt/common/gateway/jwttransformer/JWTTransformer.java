/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.gateway.jwttransformer;

import com.nimbusds.jwt.JWTClaimsSet;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;

import java.util.List;

/**
 * This Class will be used to transform JWT claims to local claims.
 */
public interface JWTTransformer {

    /**
     * This method used to retrieve ConsumerKey From JWT.
     * @param jwtClaimsSet retrieved JwtClaimSet
     * @return consumerKey of JWT
     */
    public String getTransformedConsumerKey(JWTClaimsSet jwtClaimsSet) throws JWTGeneratorException;

    /**
     * This method used to retrieve Scopes From JWT.
     * @param jwtClaimsSet retrieved JwtClaimSet
     * @return scopes of JWT
     */
    public List<String> getTransformedScopes(JWTClaimsSet jwtClaimsSet) throws JWTGeneratorException;


    /**
     * This method used to transform JWT claimset from given JWT into required format.
     *
     * @param jwtClaimsSet jwtClaimSet from given JWT
     * @return transformed JWT Claims.
     */
    public JWTClaimsSet transform(JWTClaimsSet jwtClaimsSet) throws JWTGeneratorException;

    /**
     * This method returns issuer name which used the implementation to transform JWT.
     *
     * @return issuer url.
     */
    public String getIssuer();

    public void loadConfiguration(TokenIssuerDto tokenIssuerConfiguration);

    /**
     * This method used to retrieve whether authorized user type of the JWT token is Application or not. The default
     * implementation returns null.
     *
     * @param jwtClaimsSet jwtClaimSet from given JWT
     * @return transformed JWT Claims
     * @throws JWTGeneratorException if an error occurs while retrieving whether token type is Application
     */
    default Boolean getTransformedIsAppTokenType(JWTClaimsSet jwtClaimsSet) throws JWTGeneratorException {

        return false;
    }
}

