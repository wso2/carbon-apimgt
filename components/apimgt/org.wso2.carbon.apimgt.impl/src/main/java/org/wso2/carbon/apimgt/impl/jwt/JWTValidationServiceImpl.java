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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class JWTValidationServiceImpl implements JWTValidationService {

    private static final Log log = LogFactory.getLog(JWTValidationServiceImpl.class);

    @Override
    public JWTValidationInfo validateJWTToken(SignedJWTInfo signedJWTInfo) throws APIManagementException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        String issuer = signedJWTInfo.getJwtClaimsSet().getIssuer();
        if (StringUtils.isNotEmpty(issuer)) {
            KeyManagerDto keyManagerDto = KeyManagerHolder.getKeyManagerByIssuer(tenantDomain, issuer);
            if (keyManagerDto != null && keyManagerDto.getJwtValidator() != null) {
                JWTValidationInfo validationInfo = keyManagerDto.getJwtValidator().validateToken(signedJWTInfo);
                validationInfo.setKeyManager(keyManagerDto.getName());
                // append keymanager tenant domain to username if not present
                appendTenantDomainForEndUsername(validationInfo, tenantDomain);
                return validationInfo;
            }
        }
        jwtValidationInfo.setValid(false);
        jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_GENERAL_ERROR);
        return jwtValidationInfo;
    }

    @Override
    public String getKeyManagerNameIfJwtValidatorExist(SignedJWTInfo signedJWTInfo) throws APIManagementException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        String issuer = signedJWTInfo.getJwtClaimsSet().getIssuer();
        KeyManagerDto keyManagerDto = KeyManagerHolder.getKeyManagerByIssuer(tenantDomain, issuer);
        if (keyManagerDto != null && keyManagerDto.getJwtValidator() != null) {
            return keyManagerDto.getName();
        }else{
            return null;
        }
    }

    /**
     * Append tenant domain to username of JWTValidationInfo if the tenant domain is not already present in username
     * for both cases when email as username enabled and disabled.
     *
     * @param jwtValidationInfo JWTValidationInfo
     * @param tenantDomain      Tenant Domain to append
     */
    private void appendTenantDomainForEndUsername(JWTValidationInfo jwtValidationInfo, String tenantDomain) {

        String usernameFromJWT = jwtValidationInfo.getUser();
        if (usernameFromJWT != null) {
            if ((!usernameFromJWT.contains(APIConstants.EMAIL_DOMAIN_SEPARATOR) && !MultitenantUtils.isEmailUserName())
                    || (MultitenantUtils.isEmailUserName()
                    && StringUtils.countMatches(usernameFromJWT, APIConstants.EMAIL_DOMAIN_SEPARATOR) == 1)) {
                usernameFromJWT += APIConstants.EMAIL_DOMAIN_SEPARATOR + tenantDomain;
            }
            jwtValidationInfo.setUser(usernameFromJWT);
        }
    }

}
