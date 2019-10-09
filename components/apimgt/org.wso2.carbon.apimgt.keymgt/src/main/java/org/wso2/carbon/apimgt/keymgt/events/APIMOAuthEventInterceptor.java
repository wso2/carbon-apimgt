/*
 *Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.apimgt.keymgt.events;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.ExpiredJWTCleaner;
import org.wso2.carbon.apimgt.keymgt.token.TokenRevocationNotifier;
import org.wso2.carbon.identity.oauth.event.AbstractOAuthEventInterceptor;
import org.wso2.carbon.identity.oauth2.ResponseHeader;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationRequestDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuthRevocationResponseDTO;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.model.RefreshTokenValidationDataDO;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

/**
 * This class provides an implementation of OAuthEventInterceptor interface in which
 * onPostTokenRevocationByClient method is overridden to handle token revocation feature logic
 */
public class APIMOAuthEventInterceptor extends AbstractOAuthEventInterceptor {

    private static final Log log = LogFactory.getLog(APIMOAuthEventInterceptor.class);
    private TokenRevocationNotifier tokenRevocationNotifier;
    private boolean realtimeNotifierEnabled;
    private boolean persistentNotifierEnabled;
    private Properties realtimeNotifierProperties;
    private Properties persistentNotifierProperties;
    private static final String REVOKED_ACCESS_TOKEN = "RevokedAccessToken";

    /**
     * Default Constructor
     */
    public APIMOAuthEventInterceptor() {

        log.debug("Initializing OAuth interceptor");
        realtimeNotifierProperties = APIManagerConfiguration.getRealtimeTokenRevocationNotifierProperties();
        persistentNotifierProperties = APIManagerConfiguration.getPersistentTokenRevocationNotifiersProperties();

        realtimeNotifierEnabled = realtimeNotifierProperties != null;
        persistentNotifierEnabled = persistentNotifierProperties != null;

        String className = APIManagerConfiguration.getTokenRevocationClassName();
        try {
            tokenRevocationNotifier = (TokenRevocationNotifier) Class.forName(className).getConstructor()
                    .newInstance();
            log.debug("Oauth interceptor initialized");
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
                | ClassNotFoundException e) {
            log.error("Oauth interceptor object creation error", e);
        }
    }

    /**
     * Overridden method to handle the post processing of token revocation
     * Called after revoking a token by oauth client
     *
     * @param revokeRequestDTO requested revoke request object
     * @param revokeResponseDTO requested revoke response object
     * @param accessTokenDO requested access token object
     * @param refreshTokenDO requested refresh token object
     * @param params requested params Map<String,Object>
     */
    @Override
    public void onPostTokenRevocationByClient(OAuthRevocationRequestDTO revokeRequestDTO,
            OAuthRevocationResponseDTO revokeResponseDTO, AccessTokenDO accessTokenDO,
            RefreshTokenValidationDataDO refreshTokenDO, Map<String, Object> params) {

        // If the response header contains RevokedAccessToken header, it implies the token revocation was a success.
        ResponseHeader[] responseHeaders = revokeResponseDTO.getResponseHeaders();
        boolean isRevokedAccessTokenHeaderExists = false;
        if(responseHeaders != null) {
            for (ResponseHeader responseHeader : responseHeaders) {
                if (responseHeader.getKey().equals(REVOKED_ACCESS_TOKEN) && responseHeader.getValue() != null){
                    isRevokedAccessTokenHeaderExists = true; // indicates a successful revocation
                    break;
                }
            }
        }

        if(isRevokedAccessTokenHeaderExists) {
            if (realtimeNotifierEnabled) {
                log.debug("Realtime message sending is enabled");
                tokenRevocationNotifier.sendMessageOnRealtime(revokeRequestDTO.getToken(), realtimeNotifierProperties);
            } else {
                log.debug("Realtime message sending isn't enabled or configured properly");
            }
            if (persistentNotifierEnabled) {
                log.debug("Persistent message sending is enabled");
                tokenRevocationNotifier.sendMessageToPersistentStorage(revokeRequestDTO.getToken(), persistentNotifierProperties);
            } else {
                log.debug("Persistent message sending isn't enabled or configured properly");
            }
            String revokedToken = revokeRequestDTO.getToken();
            // Persist only if the token is JWT
            if (revokedToken.contains(APIConstants.DOT)) {
                Long expiryTime = APIUtil.getExpiryifJWT(revokedToken);
                persistRevokedJWTSignature(revokedToken, expiryTime);
            }
        }

    }

    /**
     * Overridden method to handle the post processing of token revocation
     *
     * @param revokeRequestDTO requested revoke request object
     * @param revokeRespDTO requested revoke request object
     * @param accessTokenDO requested Access token object
     * @param params requested params Map<String,Object>
     */
    @Override
    public void onPostTokenRevocationByResourceOwner(
            org.wso2.carbon.identity.oauth.dto.OAuthRevocationRequestDTO revokeRequestDTO,
            org.wso2.carbon.identity.oauth.dto.OAuthRevocationResponseDTO revokeRespDTO, AccessTokenDO accessTokenDO,
            Map<String, Object> params) {

        if(accessTokenDO != null) { // if accessTokenDO is not null, it implies the revocation was a success
            if (realtimeNotifierEnabled) {
                log.debug("Realtime message sending is enabled");
                tokenRevocationNotifier.sendMessageOnRealtime(accessTokenDO.getTokenId(), realtimeNotifierProperties);
            } else {
                log.debug("Realtime message sending isn't enabled or configured properly");
            }
            if (persistentNotifierEnabled) {
                log.debug("Persistent message sending is enabled");
                tokenRevocationNotifier.sendMessageToPersistentStorage(accessTokenDO.getTokenId(), persistentNotifierProperties);
            } else {
                log.debug("Persistent message sending isn't enabled or configured properly");
            }
            String revokedToken = accessTokenDO.getTokenId();
            if (revokedToken.contains(APIConstants.DOT)) {
                Long expiryTime = APIUtil.getExpiryifJWT(revokedToken);
                // Persist revoked JWT token to database.
                persistRevokedJWTSignature(revokedToken, expiryTime);
            }
        }

    }

    /**
     * Overridden method in which default implementation is to check the identity.xml for registered event listener
     *
     * @return boolean
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    private void persistRevokedJWTSignature(String token, Long expiryTime) {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        try {
            String tokenSignature = APIUtil.getSignatureIfJWT(token);
            apiMgtDAO.addRevokedJWTSignature(tokenSignature, expiryTime);

            // Cleanup expired revoked tokens from db.
            Runnable expiredJWTCleaner = new ExpiredJWTCleaner();
            Thread cleanupThread = new Thread(expiredJWTCleaner);
            cleanupThread.start();
        } catch (APIManagementException e) {
            log.error("Unable to add revoked JWT signature to the database");
        }
    }
}
