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

package org.wso2.carbon.apimgt.tokenmgt.issuers;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.tokenmgt.JWTAccessTokenIssuerDTO;
import org.wso2.carbon.apimgt.tokenmgt.util.APIMTokenIssuerUtil;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OauthTokenIssuerImpl;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import java.text.ParseException;

public class APIMTokenIssuer extends OauthTokenIssuerImpl {

    private static final Log log = LogFactory.getLog(APIMTokenIssuer.class);

    @Override
    public String accessToken(OAuthTokenReqMessageContext tokReqMsgCtx) throws OAuthSystemException {

        String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        Application application;
        long start_time = 0;
        if (log.isDebugEnabled()) {
            start_time = System.nanoTime();
        }
        try {
            application = APIUtil.getApplicationByClientId(clientId);
        } catch (APIManagementException e) {
            String errorMsg = "Error occurred while retrieving application from Token client ID ";
            log.error(errorMsg + clientId, e);
            throw new OAuthSystemException(errorMsg + clientId, e);
        }
        if (log.isDebugEnabled()) {
            long end_time = System.nanoTime();
            long output = end_time - start_time;
            log.debug("Time taken to load the Application from database in milliseconds : " + output / 1000000);
        }
        if (application != null) {
            String tokenType = application.getTokenType();
            if (APIConstants.JWT.equals(tokenType)) {
                log.debug("Token type of the application is JWT.");
                // loading the stored oauth app data
                OAuthAppDO oAuthAppDO;
                try {
                    oAuthAppDO = OAuth2Util.getAppInformationByClientId(clientId);
                } catch (InvalidOAuthClientException | IdentityOAuth2Exception e) {
                    String errorMsg = "Error while retrieving oauth app information for clientId: ";
                    log.error(errorMsg + clientId, e);
                    throw new OAuthSystemException(errorMsg + clientId, e);
                }
                long validityPeriod;
                try {
                    validityPeriod = APIMTokenIssuerUtil.getAccessTokenLifeTimeInSeconds(tokReqMsgCtx, oAuthAppDO);
                } catch (IdentityOAuth2Exception e) {
                    throw new OAuthSystemException("Error while retrieving token validity period for clientId: " +
                            clientId, e);
                }

                AuthenticatedUser endUser = tokReqMsgCtx.getAuthorizedUser();
                String authCode = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getAuthorizationCode();
                String[] scopes = tokReqMsgCtx.getScope();

                JWTAccessTokenIssuerDTO jwtAccessTokenIssuerDTO = new JWTAccessTokenIssuerDTO();
                jwtAccessTokenIssuerDTO.setUser(endUser);
                jwtAccessTokenIssuerDTO.setClientId(clientId);
                jwtAccessTokenIssuerDTO.setScopeList(scopes);
                jwtAccessTokenIssuerDTO.setValidityPeriod(validityPeriod);
                jwtAccessTokenIssuerDTO.setTokenReqMessageContext(tokReqMsgCtx);
                String token = APIMTokenIssuerUtil.generateToken(jwtAccessTokenIssuerDTO, application);
                if (log.isDebugEnabled()) {
                    long end_time_2 = System.nanoTime();
                    long output = end_time_2 - start_time;
                    log.debug("Time taken to generate the JWT in milliseconds : " + output / 1000000);
                }
                return token;
            }
        }
        log.debug("Token type of the application is NOT JWT.");
        // token type is not JWT. Generate UUID token.
        return super.accessToken(tokReqMsgCtx);
    }

    @Override
    public String getAccessTokenHash(String accessToken) throws OAuthSystemException {

        if (StringUtils.isNotEmpty(accessToken) && accessToken.contains(APIConstants.DOT)) {
            try {
                JWT parse = JWTParser.parse(accessToken);
                return parse.getJWTClaimsSet().getJWTID();
            } catch (ParseException e) {
                if (log.isDebugEnabled() && IdentityUtil.isTokenLoggable(IdentityConstants.IdentityTokens.ACCESS_TOKEN)) {
                    log.debug("Error while getting JWTID from token: " + accessToken);
                }
                throw new OAuthSystemException("Error while getting access token hash", e);
            }
        } else {
            return accessToken;
        }
    }

    @Override
    public boolean renewAccessTokenPerRequest(OAuthTokenReqMessageContext tokReqMsgCtx) {

        String clientId = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getClientId();
        Application application;
        try {
            application = APIUtil.getApplicationByClientId(clientId);
            if (null != application) {
                if (APIConstants.JWT.equals(application.getTokenType())) {
                    return true;
                }
            }
        } catch (APIManagementException e) {
            log.error("Error occurred while getting Token type.", e);
        }
        return false;
    }



    //This method will be called for implicit grant
    @Override
    public String accessToken(OAuthAuthzReqMessageContext oauthAuthzMsgCtx) throws OAuthSystemException {

        String clientId = oauthAuthzMsgCtx.getAuthorizationReqDTO().getConsumerKey();
        long start_time = 0;
        if (log.isDebugEnabled()) {
            start_time = System.nanoTime();
        }
        Application application;
        try {
            application = APIUtil.getApplicationByClientId(clientId);
        } catch (APIManagementException e) {
            String errorMsg = "Error occurred while retrieving application from Token client ID ";
            log.error(errorMsg + clientId, e);
            throw new OAuthSystemException(errorMsg + clientId, e);
        }
        
        if (log.isDebugEnabled()) {
            long end_time = System.nanoTime();
            long output = end_time - start_time;
            log.debug("Time taken to load the Application from database in milliseconds : " + output / 1000000);
        }
        if (application != null) {
            String tokenType = application.getTokenType();
            if (APIConstants.JWT.equals(tokenType)) {

                // loading the stored oauth app data
                OAuthAppDO oAuthAppDO;
                try {
                    oAuthAppDO = OAuth2Util.getAppInformationByClientId(clientId);
                } catch (InvalidOAuthClientException | IdentityOAuth2Exception e) {
                    String errorMsg = "Error while retrieving oauth app information for clientId: ";
                    log.error(errorMsg + clientId, e);
                    throw new OAuthSystemException(errorMsg + clientId, e);
                }
                long validityPeriod = APIMTokenIssuerUtil.getAccessTokenLifeTimeInSeconds(oAuthAppDO);

                String[] scopeList = oauthAuthzMsgCtx.getAuthorizationReqDTO().getScopes();
                AuthenticatedUser endUser = oauthAuthzMsgCtx.getAuthorizationReqDTO().getUser();

                JWTAccessTokenIssuerDTO jwtAccessTokenIssuerDTO = new JWTAccessTokenIssuerDTO();
                jwtAccessTokenIssuerDTO.setUser(endUser);
                jwtAccessTokenIssuerDTO.setClientId(clientId);
                jwtAccessTokenIssuerDTO.setScopeList(scopeList);
                jwtAccessTokenIssuerDTO.setValidityPeriod(validityPeriod);
                jwtAccessTokenIssuerDTO.setAuthzMessageContext(oauthAuthzMsgCtx);
                String token = APIMTokenIssuerUtil.generateToken(jwtAccessTokenIssuerDTO, application);

                if (log.isDebugEnabled()) {
                    long end_time_2 = System.nanoTime();
                    long output = end_time_2 - start_time;
                    log.debug("Time taken to generate the JWT in milliseconds : " + output / 1000000);
                }
                return token;
            }
        }
        log.debug("Token type of the application is NOT JWT.");
        // token type is not JWT. Generate UUID token.
        return super.accessToken(oauthAuthzMsgCtx);
    }
}
