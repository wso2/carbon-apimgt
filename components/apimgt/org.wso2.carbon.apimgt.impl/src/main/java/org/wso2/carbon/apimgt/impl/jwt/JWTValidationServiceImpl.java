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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.dto.KeyManagerDto;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.context.CarbonContext;

import java.text.ParseException;

public class JWTValidationServiceImpl implements JWTValidationService {

    private static final Log log = LogFactory.getLog(JWTValidationServiceImpl.class);

    @Override
    public JWTValidationInfo validateJWTToken(SignedJWT signedJWT) throws APIManagementException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        try {
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();
            if (StringUtils.isNotEmpty(issuer)) {
                KeyManagerDto keyManagerDto = KeyManagerHolder.getKeyManagerByIssuer(tenantDomain, issuer);
                if (keyManagerDto != null && keyManagerDto.getJwtValidator() != null) {
                    JWTValidationInfo validationInfo = keyManagerDto.getJwtValidator().validateToken(signedJWT);
                    validationInfo.setKeyManager(keyManagerDto.getName());
                    return validationInfo;
                }
            }
            jwtValidationInfo.setValid(false);
            jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_GENERAL_ERROR);
            return jwtValidationInfo;
        } catch (ParseException e) {
            log.error("Error while parsing JWT Token", e);
            jwtValidationInfo.setValid(false);
            jwtValidationInfo.setValidationCode(APIConstants.KeyValidationStatus.API_AUTH_GENERAL_ERROR);
            return jwtValidationInfo;
        }
    }

    @Override
    public String getKeyManagerNameIfJwtValidatorExist(SignedJWT signedJWT) throws APIManagementException {
        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        try {
            String issuer = signedJWT.getJWTClaimsSet().getIssuer();
            KeyManagerDto keyManagerDto = KeyManagerHolder.getKeyManagerByIssuer(tenantDomain, issuer);
            if (keyManagerDto != null && keyManagerDto.getJwtValidator() != null) {
                return keyManagerDto.getName();
            }else{
                return null;
            }
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing JWT", e);
        }
    }

}
