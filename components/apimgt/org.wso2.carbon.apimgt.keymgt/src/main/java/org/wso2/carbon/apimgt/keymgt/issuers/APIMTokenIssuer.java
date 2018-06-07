/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.keymgt.issuers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.token.APIMJWTGenerator;
import org.wso2.carbon.apimgt.keymgt.util.APIMTokenIssuerUtil;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.JWTTokenIssuer;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.util.Arrays;
import java.util.List;

public class APIMTokenIssuer extends JWTTokenIssuer {

    private static final Log log = LogFactory.getLog(APIMTokenIssuer.class);

    public APIMTokenIssuer() throws IdentityOAuth2Exception {
    }

    @Override
    public String accessToken(OAuthTokenReqMessageContext tokReqMsgCtx) throws OAuthSystemException {

        String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        try {
            long start_time = 0;
            if (log.isDebugEnabled()) {
                start_time = System.nanoTime();
            }
            Application application = APIUtil.getApplicationByClientId(clientId);
            if (log.isDebugEnabled()) {
                long end_time = System.nanoTime();
                long output = end_time - start_time;
                log.debug("Time taken to load the Application from database in milliseconds : " + output / 1000000);
            }
            if (application != null) {
                String tokenType = application.getTokenType();
                if (APIConstants.JWT.equals(tokenType)) {
                    OAuthAppDO oAuthAppDO = OAuth2Util.getAppInformationByClientId(clientId);
                    String[] audience = oAuthAppDO.getAudiences();
                    List<String> audienceList = Arrays.asList(audience);
                    String[] scopes = tokReqMsgCtx.getScope();
                    StringBuilder scopeString = new StringBuilder();
                    for (String scope : scopes) {
                        scopeString.append(scope).append(" ");
                    }

                    ApplicationDTO applicationDTO = new ApplicationDTO();
                    applicationDTO.setId(application.getId());
                    applicationDTO.setName(application.getName());
                    applicationDTO.setTier(application.getTier());

                    JwtTokenInfoDTO jwtTokenInfoDTO = APIMTokenIssuerUtil.getJwtTokenInfoDTO(application);
                    jwtTokenInfoDTO.setScopes(scopeString.toString().trim());
                    jwtTokenInfoDTO.setAudience(audienceList);
                    jwtTokenInfoDTO.setExpirationTime(tokReqMsgCtx.getValidityPeriod());
                    jwtTokenInfoDTO.setApplication(applicationDTO);
                    jwtTokenInfoDTO.setKeyType(application.getKeyType());
                    APIMJWTGenerator apimjwtGenerator = new APIMJWTGenerator();
                    String accessToken = apimjwtGenerator.generateJWT(jwtTokenInfoDTO);
                    if (log.isDebugEnabled()) {
                        long end_time_2 = System.nanoTime();
                        long output = end_time_2 - start_time;
                        log.debug("Time taken to generate the JWG in milliseconds : " + output / 1000000);
                    }
                    return accessToken;
                }
            }

        } catch (APIManagementException e) {
            log.error("Error occurred while getting JWT Token client ID : " + clientId, e);
            throw new OAuthSystemException("Error occurred while getting JWT Token client ID : " + clientId, e);
        } catch (InvalidOAuthClientException e) {
            log.error("Error occurred while getting JWT Token client ID : " + clientId + " when getting oAuth App " +
                    "information", e);
            throw new OAuthSystemException("Error occurred while getting JWT Token client ID : " + clientId, e);
        } catch (IdentityOAuth2Exception e) {
            log.error("Error occurred while getting JWT Token client ID : " + clientId + " when getting oAuth App " +
                    "information", e);
            throw new OAuthSystemException("Error occurred while getting JWT Token client ID : " + clientId, e);
        }
        return OAuthServerConfiguration.getInstance().getOAuthTokenGenerator().accessToken();
    }

    @Override
    public String getAccessTokenHash(String accessToken) throws OAuthSystemException {
        if (StringUtils.isNotEmpty(accessToken) && accessToken.contains(APIConstants.DOT)) {
            return super.getAccessTokenHash(accessToken);
        } else {
            return accessToken;
        }
    }
}
