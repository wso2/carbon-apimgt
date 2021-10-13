/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.util.impl;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.RESTAPICacheConfiguration;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.MethodStats;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.AbstractOAuthAuthenticator;
import org.wso2.carbon.apimgt.rest.api.util.jwt.JWTUtil;
import java.text.ParseException;

/**
 * This class is for Authenticate API requests that coming with X-JWT-Assertion header. X-JWT-Assertion header transporting
 * the backend JWT.Validating the JWT against the token issuer is not required since the token coming from well known party
 * Un-authorize access are denied by network policies.
 * */

public class BackendJWTAuthenticationImpl extends AbstractOAuthAuthenticator {

    private static final Log log = LogFactory.getLog(BackendJWTAuthenticationImpl.class);
    private boolean isRESTApiTokenCacheEnabled;

    @Override
    public boolean authenticate(Message message) throws APIManagementException {
        RESTAPICacheConfiguration cacheConfiguration = APIUtil.getRESTAPICacheConfig();
        isRESTApiTokenCacheEnabled = cacheConfiguration.isTokenCacheEnabled();
        String accessToken = (String) message.get(RestApiConstants.JWT_TOKEN);

        if ((accessToken != null && StringUtils.countMatches(accessToken, APIConstants.DOT) != 2)) {
            log.error("Invalid JWT token. The expected token format is <header.payload.signature>");
            return false;
        }
        try {
            SignedJWTInfo signedJWTInfo = null;
            String token = (String) message.get(RestApiConstants.JWT_TOKEN);
            if (accessToken != null) {
                signedJWTInfo = getSignedJwt(accessToken);
            }

            if (signedJWTInfo != null) {
                String jwtTokenIdentifier = getJWTTokenIdentifier(signedJWTInfo);

                JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
                jwtValidationInfo.setValid(true);
                if (isRESTApiTokenCacheEnabled) {
                    getRESTAPITokenCache().put(jwtTokenIdentifier, jwtValidationInfo);
                }
                //Validating scopes
                return JWTUtil.handleScopeValidation(message, signedJWTInfo, token);
            } else {
                log.error("Invalid Signed JWT :" + signedJWTInfo);
                return false;
            }

        } catch (ParseException e) {
            log.error("Not a JWT token. Failed to decode the token. Reason: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get signed jwt info.
     *
     * @param accessToken JWT token
     * @return SignedJWTInfo : Signed token info
     */
    @MethodStats
    private SignedJWTInfo getSignedJwt(String accessToken) throws ParseException {

        SignedJWT signedJWT = SignedJWT.parse(accessToken);
        JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
        return new SignedJWTInfo(accessToken, signedJWT, jwtClaimsSet);
    }

    /**
     * Get jti information.
     *
     * @param signedJWTInfo
     * @return String : jti
     */
    private String getJWTTokenIdentifier(SignedJWTInfo signedJWTInfo) {

        JWTClaimsSet jwtClaimsSet = signedJWTInfo.getJwtClaimsSet();
        String jwtID = jwtClaimsSet.getJWTID();
        if (StringUtils.isNotEmpty(jwtID)) {
            return jwtID;
        }
        return signedJWTInfo.getSignedJWT().getSignature().toString();
    }
}
