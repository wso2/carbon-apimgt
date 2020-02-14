/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.bean.context.MessageContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.data.publisher.application.authentication.AbstractAuthenticationDataPublisher;
import org.wso2.carbon.identity.data.publisher.application.authentication.model.AuthenticationData;
import org.wso2.carbon.identity.data.publisher.application.authentication.model.SessionData;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.dao.OAuthTokenPersistenceFactory;
import org.wso2.carbon.identity.oauth2.model.AccessTokenDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static org.wso2.carbon.identity.oauth.OAuthUtil.handleError;

/**
 * In this implementation, when a user logout, all the access tokens related to 'admin_store' or 'admin_publisher'
 * applications for that particular user will be revoked.
 */
public class SessionDataPublisherImpl extends AbstractAuthenticationDataPublisher {

    public static final Log log = LogFactory.getLog(SessionDataPublisherImpl.class);
    private static final String handlerName = "APIMSessionDataPublisherImpl";
    private static final String DEVPORTAL_CLIENT_APP_NAME = "admin_apim_devportal";
    private static final String DEVPORTAL_CLIENT_APP_NAME_OLD = "admin_store";
    private static final String PUBLISHER_CLIENT_APP_NAME_OLD = "admin_publisher";
    private static final String PUBLISHER_CLIENT_APP_NAME = "admin_apim_publisher";

    @Override public void doPublishAuthenticationStepSuccess(AuthenticationData authenticationData) {

    }

    @Override public void doPublishAuthenticationStepFailure(AuthenticationData authenticationData) {

    }

    @Override public void doPublishAuthenticationSuccess(AuthenticationData authenticationData) {

    }

    @Override public void doPublishAuthenticationFailure(AuthenticationData authenticationData) {

    }

    @Override public void doPublishSessionCreation(SessionData sessionData) {

    }

    @Override public void doPublishSessionUpdate(SessionData sessionData) {

    }

    @Override public void doPublishSessionTermination(SessionData sessionData) {

    }

    @Override public String getName() {
        return handlerName;
    }

    @Override public boolean isEnabled(MessageContext messageContext) {
        return true;
    }

    /**
     * Overridden method which implements the access token revocation
     * @param request termination request
     * @param context termination context
     * @param sessionContext termination sessionContext
     * @param params termination params
     */
    @Override public void publishSessionTermination(HttpServletRequest request, AuthenticationContext context,
            SessionContext sessionContext, Map<String, Object> params) {

        OAuthConsumerAppDTO[] appDTOs = new OAuthConsumerAppDTO[0];
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) params.get("user");
        String username = authenticatedUser.getUserName();
        String tenantDomain = authenticatedUser.getTenantDomain();
        String userStoreDomain = authenticatedUser.getUserStoreDomain();
        AuthenticatedUser federatedUser;

        if (authenticatedUser.isFederatedUser()) {
            try {
                federatedUser = buildAuthenticatedUser(authenticatedUser);
                authenticatedUser = federatedUser;
            } catch (IdentityOAuth2Exception e) {
                log.error("Error thrown while building authenticated user in logout flow for user " + authenticatedUser
                        .getUserName(), e);
            }
        }
        try {
            appDTOs = getAppsAuthorizedByUser(authenticatedUser);
            if (appDTOs.length > 0) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "The user: " + authenticatedUser.getUserName() + " has " + appDTOs.length + " OAuth apps");
                }
            }
        } catch (IdentityOAuthAdminException e) {
            log.error("Error while retrieving applications authorized for the user " + authenticatedUser.getUserName(),
                    e);
        }

        for (OAuthConsumerAppDTO appDTO : appDTOs) {
            if (StringUtils.equalsIgnoreCase(DEVPORTAL_CLIENT_APP_NAME_OLD, appDTO.getApplicationName()) ||
                    StringUtils.equalsIgnoreCase(DEVPORTAL_CLIENT_APP_NAME, appDTO.getApplicationName()) ||
                    StringUtils.equalsIgnoreCase(PUBLISHER_CLIENT_APP_NAME_OLD, appDTO.getApplicationName()) ||
                    StringUtils.equalsIgnoreCase(PUBLISHER_CLIENT_APP_NAME, appDTO.getApplicationName())) {
                Set<AccessTokenDO> accessTokenDOs = null;
                try {
                    // Retrieve all ACTIVE or EXPIRED access tokens for particular client authorized by this user
                    accessTokenDOs = OAuthTokenPersistenceFactory.getInstance().getAccessTokenDAO()
                            .getAccessTokens(appDTO.getOauthConsumerKey(), authenticatedUser,
                                    authenticatedUser.getUserStoreDomain(), true);
                } catch (IdentityOAuth2Exception e) {
                    log.error("Error while retrieving access tokens for the application " + appDTO.getApplicationName()
                            + "and the for user " + authenticatedUser.getUserName(), e);
                }
                AuthenticatedUser authzUser;
                if (accessTokenDOs != null) {
                    for (AccessTokenDO accessTokenDO : accessTokenDOs) {
                        //Clear cache with AccessTokenDO
                        authzUser = accessTokenDO.getAuthzUser();
                        OAuthUtil.clearOAuthCache(accessTokenDO.getConsumerKey(), authzUser,
                                OAuth2Util.buildScopeString(accessTokenDO.getScope()),"NONE");
                        OAuthUtil.clearOAuthCache(accessTokenDO.getConsumerKey(), authzUser,
                                OAuth2Util.buildScopeString(accessTokenDO.getScope()));
                        OAuthUtil.clearOAuthCache(accessTokenDO.getConsumerKey(), authzUser);
                        OAuthUtil.clearOAuthCache(accessTokenDO.getAccessToken());
                        AccessTokenDO scopedToken = null;
                        try {
                            // Retrieve latest access token for particular client, user and scope combination if
                            // its ACTIVE or EXPIRED.
                            scopedToken = OAuthTokenPersistenceFactory.getInstance().getAccessTokenDAO()
                                    .getLatestAccessToken(appDTO.getOauthConsumerKey(), authenticatedUser,
                                            userStoreDomain, OAuth2Util.buildScopeString(accessTokenDO.getScope()),
                                            true);
                        } catch (IdentityOAuth2Exception e) {
                            log.error("Error while retrieving scoped access tokens for the application " + appDTO
                                    .getApplicationName() + "and the for user " + authenticatedUser.getUserName(), e);
                        }
                        if (scopedToken != null) {
                            //Revoking token from database
                            try {
                                OAuthTokenPersistenceFactory.getInstance().getAccessTokenDAO()
                                        .revokeAccessTokens(new String[] { scopedToken.getAccessToken() });

                            } catch (IdentityOAuth2Exception e) {
                                log.error("Error while revoking access tokens related for the application " + appDTO
                                                .getApplicationName() + "and the for user " + authenticatedUser.getUserName(),
                                        e);
                            }
                            //Revoking the oauth consent from database.
                            try {
                                OAuthTokenPersistenceFactory.getInstance().getTokenManagementDAO()
                                        .revokeOAuthConsentByApplicationAndUser(
                                                authzUser.getAuthenticatedSubjectIdentifier(), tenantDomain, username);
                            } catch (IdentityOAuth2Exception e) {
                                log.error("Error while revoking access tokens related for the application " + appDTO
                                                .getApplicationName() + "and the for user " + authenticatedUser.getUserName(),
                                        e);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Method to retrieve applications authorized for user
     * @param authenticatedUser authenticated user info
     * @return array of authorized applications
     * @throws IdentityOAuthAdminException exception
     */
    private OAuthConsumerAppDTO[] getAppsAuthorizedByUser(AuthenticatedUser authenticatedUser)
            throws IdentityOAuthAdminException {

        OAuthAppDAO appDAO = new OAuthAppDAO();
        String tenantAwareusername = authenticatedUser.getUserName();
        String tenantDomain = authenticatedUser.getTenantDomain();
        String username = UserCoreUtil.addTenantDomainToEntry(tenantAwareusername, tenantDomain);
        String userStoreDomain = authenticatedUser.getUserStoreDomain();
        Set<String> clientIds;
        try {
            clientIds = OAuthTokenPersistenceFactory.getInstance().getTokenManagementDAO()
                    .getAllTimeAuthorizedClientIds(authenticatedUser);
        } catch (IdentityOAuth2Exception e) {
            throw handleError("Error occurred while retrieving apps authorized by User ID : " + username, e);
        }
        Set<OAuthConsumerAppDTO> appDTOs = new HashSet<>();
        for (String clientId : clientIds) {
            Set<AccessTokenDO> accessTokenDOs;
            try {
                accessTokenDOs = OAuthTokenPersistenceFactory.getInstance().getAccessTokenDAO()
                        .getAccessTokens(clientId, authenticatedUser, userStoreDomain, true);
            } catch (IdentityOAuth2Exception e) {
                throw handleError(
                        "Error occurred while retrieving access tokens issued for " + "Client ID : " + clientId
                                + ", User ID : " + username, e);
            }
            if (!accessTokenDOs.isEmpty()) {
                Set<String> distinctClientUserScopeCombo = new HashSet<>();
                for (AccessTokenDO accessTokenDO : accessTokenDOs) {
                    AccessTokenDO scopedToken;
                    String scopeString = OAuth2Util.buildScopeString(accessTokenDO.getScope());
                    try {
                        scopedToken = OAuthTokenPersistenceFactory.getInstance().
                                getAccessTokenDAO()
                                .getLatestAccessToken(clientId, authenticatedUser, userStoreDomain, scopeString, true);
                        if (scopedToken != null && !distinctClientUserScopeCombo.contains(clientId + ":" + username)) {
                            OAuthAppDO appDO;
                            try {
                                appDO = appDAO.getAppInformation(scopedToken.getConsumerKey());
                                appDTOs.add(buildConsumerAppDTO(appDO));
                                if (log.isDebugEnabled()) {
                                    log.debug("Found App: " + appDO.getApplicationName() + " for user: " + username);
                                }
                            } catch (InvalidOAuthClientException e) {
                                String errorMsg = "Invalid Client ID : " + scopedToken.getConsumerKey();
                                log.error(errorMsg, e);
                                throw new IdentityOAuthAdminException(errorMsg);
                            } catch (IdentityOAuth2Exception e) {
                                String errorMsg =
                                        "Error occurred while retrieving app information " + "for Client ID : "
                                                + scopedToken.getConsumerKey();
                                log.error(errorMsg, e);
                                throw new IdentityOAuthAdminException(errorMsg);
                            }
                            distinctClientUserScopeCombo.add(clientId + ":" + username);
                        }
                    } catch (IdentityOAuth2Exception e) {
                        String errorMsg =
                                "Error occurred while retrieving latest access token issued for Client ID :" + " "
                                        + clientId + ", User ID : " + username + " and Scope : " + scopeString;
                        throw handleError(errorMsg, e);
                    }
                }
            }
        }
        return appDTOs.toArray(new OAuthConsumerAppDTO[0]);
    }

    /**
     * Method to build a OAuthConsumerAppDTO type object
     * @param appDO required param
     * @return OAuthConsumerAppDTO type object
     */
    private OAuthConsumerAppDTO buildConsumerAppDTO(OAuthAppDO appDO) {

        OAuthConsumerAppDTO dto = new OAuthConsumerAppDTO();
        dto.setApplicationName(appDO.getApplicationName());
        dto.setCallbackUrl(appDO.getCallbackUrl());
        dto.setOauthConsumerKey(appDO.getOauthConsumerKey());
        dto.setOauthConsumerSecret(appDO.getOauthConsumerSecret());
        dto.setOAuthVersion(appDO.getOauthVersion());
        dto.setGrantTypes(appDO.getGrantTypes());
        dto.setScopeValidators(appDO.getScopeValidators());
        dto.setUsername(appDO.getAppOwner().toFullQualifiedUsername());
        dto.setState(appDO.getState());
        dto.setPkceMandatory(appDO.isPkceMandatory());
        dto.setPkceSupportPlain(appDO.isPkceSupportPlain());
        dto.setUserAccessTokenExpiryTime(appDO.getUserAccessTokenExpiryTime());
        dto.setApplicationAccessTokenExpiryTime(appDO.getApplicationAccessTokenExpiryTime());
        dto.setRefreshTokenExpiryTime(appDO.getRefreshTokenExpiryTime());
        dto.setIdTokenExpiryTime(appDO.getIdTokenExpiryTime());
        dto.setAudiences(appDO.getAudiences());
        dto.setRequestObjectSignatureValidationEnabled(appDO.isRequestObjectSignatureValidationEnabled());
        dto.setIdTokenEncryptionEnabled(appDO.isIdTokenEncryptionEnabled());
        dto.setIdTokenEncryptionAlgorithm(appDO.getIdTokenEncryptionAlgorithm());
        dto.setIdTokenEncryptionMethod(appDO.getIdTokenEncryptionMethod());
        dto.setBackChannelLogoutUrl(appDO.getBackChannelLogoutUrl());
        dto.setTokenType(appDO.getTokenType());
        dto.setBypassClientCredentials(appDO.isBypassClientCredentials());
        return dto;
    }

    /**
     * Method to build a AuthenticatedUser type object
     * @param authenticatedUser required param
     * @return AuthenticatedUser type object
     * @throws IdentityOAuth2Exception exception
     */
    private AuthenticatedUser buildAuthenticatedUser(AuthenticatedUser authenticatedUser)
            throws IdentityOAuth2Exception {

        AuthenticatedUser user = new AuthenticatedUser();
        String tenantAwareusername = authenticatedUser.getUserName();
        String tenantDomain = authenticatedUser.getTenantDomain();
        user.setUserName(UserCoreUtil.removeDomainFromName(tenantAwareusername));
        user.setTenantDomain(tenantDomain);
        user.setUserStoreDomain(IdentityUtil.extractDomainFromName(tenantAwareusername));
        user.setFederatedUser(true);
        user.setUserStoreDomain(OAuth2Util.getUserStoreForFederatedUser(authenticatedUser));
        return user;
    }
}
