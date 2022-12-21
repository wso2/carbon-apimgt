/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import com.nimbusds.jwt.JWTClaimsSet;
import org.wso2.carbon.apimgt.common.gateway.constants.APIConstants;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.exception.CommonGatewayException;
import org.wso2.carbon.apimgt.common.gateway.exception.JWTGeneratorException;

/**
 * Default JWTValidator Implementation.
 */
public class JWTValidatorImpl extends CommonJWTValidatorImpl implements JWTValidator {

    @Override
    public JWTValidationInfo validateToken(SignedJWTInfo signedJWTInfo) throws CommonGatewayException {

        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        boolean state;
        try {
            state = validateSignature(signedJWTInfo.getSignedJWT());
            if (state) {
                JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
                state = isValidCertificateBoundAccessToken(signedJWTInfo);
                if (state) {
                    state = validateTokenExpiry(jwtClaimsSet);
                    if (state) {
                        jwtValidationInfo.setConsumerKey(getConsumerKey(jwtClaimsSet));
                        jwtValidationInfo.setScopes(getScopes(jwtClaimsSet));
                        jwtValidationInfo.setAppToken(getIsAppToken(jwtClaimsSet));
                        JWTClaimsSet transformedJWTClaimSet = transformJWTClaims(jwtClaimsSet);
                        createJWTValidationInfoFromJWT(jwtValidationInfo, transformedJWTClaimSet);
                        jwtValidationInfo.setRawPayload(signedJWTInfo.getToken());
                        return jwtValidationInfo;
                    } else {
                        jwtValidationInfo.setValid(false);
                        jwtValidationInfo.setValidationCode(
                                APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                        return jwtValidationInfo;
                    }
                } else {
                    jwtValidationInfo.setValid(false);
                    jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                    return jwtValidationInfo;
                }
            } else {
                jwtValidationInfo.setValid(false);
                jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return jwtValidationInfo;
            }
        } catch (JWTGeneratorException e) {
            throw new CommonGatewayException("Error while parsing JWT", e);
        }
    }

    @Override
    public void loadValidatorConfiguration(JWTValidatorConfiguration jwtValidatorConfiguration) {
        loadConfiguration(jwtValidatorConfiguration);
    }
}
