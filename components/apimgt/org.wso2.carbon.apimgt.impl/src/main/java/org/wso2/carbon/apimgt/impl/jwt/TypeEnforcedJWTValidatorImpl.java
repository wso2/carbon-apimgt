/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

public class TypeEnforcedJWTValidatorImpl extends JWTValidatorImpl {
    
    private static final Log log = LogFactory.getLog(TypeEnforcedJWTValidatorImpl.class);
    
    @Override
    public JWTValidationInfo validateToken(SignedJWTInfo signedJWTInfo) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Validating JWT token with type enforcement");
        }
        
        // If this is a refresh token, return an error
        String tokenTypeClaim = (String) signedJWTInfo.getJwtClaimsSet().getClaim(
                APIConstants.JwtTokenConstants.TOKEN_TYPE);
        if (APIConstants.OAuthConstants.REFRESH_TOKEN.equals(tokenTypeClaim)) {
            log.warn("JWT validation failed: Refresh token provided instead of access token");
            // Invalid type header
            JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
            jwtValidationInfo.setValid(false);
            jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
            return jwtValidationInfo;
        }

        APIManagerConfiguration apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        if (apiManagerConfiguration.getTokenValidationDto().isEnforceTypeHeaderValidation()) {
            if (log.isDebugEnabled()) {
                log.debug("JWT type header validation is enforced via configuration");
            }
            // JWT type header validation is enforced via the configuration
            if (signedJWTInfo.getSignedJWT().getHeader().getType() != null
                    && APIConstants.JWT_HEADER_ACCESS_TOKEN_TYPE.equals(
                    signedJWTInfo.getSignedJWT().getHeader().getType().toString())) {
                // The type header present with the value "at+jwt"
                if (log.isDebugEnabled()) {
                    log.debug("JWT validation successful: Valid type header found with value 'at+jwt'");
                }
                return super.validateToken(signedJWTInfo);
            } else {
                // Invalid type header
                String actualType = signedJWTInfo.getSignedJWT().getHeader().getType() != null ?
                        signedJWTInfo.getSignedJWT().getHeader().getType().toString() : "null";
                log.warn("JWT validation failed: Invalid or missing type header. Expected: 'at+jwt', Found: '" 
                        + actualType + "'");
                JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
                jwtValidationInfo.setValid(false);
                jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return jwtValidationInfo;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("JWT type header validation is not enforced, proceeding with standard validation");
            }
        }
        return super.validateToken(signedJWTInfo);
    }
}
