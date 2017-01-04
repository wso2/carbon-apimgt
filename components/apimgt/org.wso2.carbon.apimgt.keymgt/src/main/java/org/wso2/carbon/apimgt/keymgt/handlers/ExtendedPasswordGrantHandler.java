/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.issuers.ScopesIssuingHandler;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.identity.application.common.cache.BaseCache;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.ResponseHeader;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.PasswordGrantHandler;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.config.RealmConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import javax.xml.namespace.QName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtendedPasswordGrantHandler extends PasswordGrantHandler {

    private static Log log = LogFactory.getLog(ExtendedPasswordGrantHandler.class);

    private static final String CONFIG_ELEM_OAUTH = "OAuth";

    // Claims that are set as response headers of access token response
    private static final String REQUIRED_CLAIM_URIS = "RequiredRespHeaderClaimUris";
    private BaseCache<String, Claim[]> userClaimsCache;

    // Primary/Secondary Login configuration
    private static final String CLAIM_URI = "ClaimUri";
    private static final String LOGIN_CONFIG = "LoginConfig";
    private static final String USERID_LOGIN = "UserIdLogin";
    private static final String EMAIL_LOGIN = "EmailLogin";
    private static final String PRIMARY_LOGIN = "primary";

    private Map<String,Map<String,String>> loginConfiguration = new ConcurrentHashMap<String, Map<String,String>>();

    private List<String> requiredHeaderClaimUris = new ArrayList<String>();

    public void init() throws IdentityOAuth2Exception {

        super.init();

        IdentityConfigParser configParser;
        configParser = IdentityConfigParser.getInstance();
        OMElement oauthElem = configParser.getConfigElement(CONFIG_ELEM_OAUTH);

        // Get the required claim uris that needs to be included in the response.
        parseRequiredHeaderClaimUris(oauthElem.getFirstChildWithName(getQNameWithIdentityNS(REQUIRED_CLAIM_URIS)));

        // read login config
        parseLoginConfig(oauthElem);

        userClaimsCache = new BaseCache<String, Claim[]>("UserClaimsCache");
        if (log.isDebugEnabled()) {
            log.debug("Successfully created UserClaimsCache under " + OAuthConstants.OAUTH_CACHE_MANAGER);
        }
    }

    @Override
    public boolean validateGrant(OAuthTokenReqMessageContext tokReqMsgCtx)
            throws IdentityOAuth2Exception {

        OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = tokReqMsgCtx.getOauth2AccessTokenReqDTO();
        String username = oAuth2AccessTokenReqDTO.getResourceOwnerUsername();
        String loginUserName = getLoginUserName(username);
        tokReqMsgCtx.getOauth2AccessTokenReqDTO().setResourceOwnerUsername(loginUserName);

        boolean isValidated = super.validateGrant(tokReqMsgCtx);

        if(isValidated){

            int tenantId;
            tenantId = IdentityTenantUtil.getTenantIdOfUser(username);

            RealmService realmService = APIKeyMgtDataHolder.getRealmService();
            UserStoreManager userStoreManager;
            try {
                userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            } catch (UserStoreException e) {
                log.error("Error when getting the tenant's UserStoreManager", e);
                return false;
            }

            List<ResponseHeader> respHeaders = new ArrayList<ResponseHeader>();

            if (oAuth2AccessTokenReqDTO.getResourceOwnerUsername() != null) {

                try {

                    if (requiredHeaderClaimUris != null && !requiredHeaderClaimUris.isEmpty()) {
                        // Get user's claim values from the default profile.
                        String userStoreDomain = tokReqMsgCtx.getAuthorizedUser().getUserStoreDomain();

                        String endUsernameWithDomain = UserCoreUtil.addDomainToName
                                (oAuth2AccessTokenReqDTO.getResourceOwnerUsername(),
                                        userStoreDomain);

                        Claim[] mapClaimValues = getUserClaimValues(endUsernameWithDomain,userStoreManager);

                        if(mapClaimValues != null && mapClaimValues.length > 0){
                            ResponseHeader header;
                            for (String claimUri : requiredHeaderClaimUris) {
                                for (Claim claim : mapClaimValues) {
                                    if (claimUri.equals(claim.getClaimUri())) {
                                        header = new ResponseHeader();
                                        header.setKey(claim.getDisplayTag());
                                        header.setValue(claim.getValue());
                                        respHeaders.add(header);
                                        break;
                                    }
                                }
                            }
                        }
                        else if(log.isDebugEnabled()){
                           log.debug("No claim values for user : "+endUsernameWithDomain);
                        }

                    }
                } catch (Exception e) {
                    throw new IdentityOAuth2Exception(e.getMessage(), e);
                }
            }

            tokReqMsgCtx.addProperty("RESPONSE_HEADERS", respHeaders.toArray(
                    new ResponseHeader[respHeaders.size()]));
        }

        return isValidated;
    }

    @Override
    public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx){
        return ScopesIssuingHandler.getInstance().setScopes(tokReqMsgCtx);
    }

    private String getLoginUserName(String userID) {
        String loginUserName = userID;
        if (isSecondaryLogin(userID)) {
            loginUserName = getPrimaryFromSecondary(userID);
        }
        return loginUserName;
    }

    /**
     * Identify whether the logged in user used his Primary Login name or
     * Secondary login name
     *
     * @param userId - The username used to login.
     * @return <code>true</code> if secondary login name is used,
     *         <code>false</code> if primary login name has been used
     */
    private boolean isSecondaryLogin(String userId) {

        if (loginConfiguration.get(EMAIL_LOGIN) != null) {
            Map<String, String> emailConf = loginConfiguration.get(EMAIL_LOGIN);
            if ("true".equalsIgnoreCase(emailConf.get(PRIMARY_LOGIN))) {
                return !isUserLoggedInEmail(userId);
            }
            else if ("false".equalsIgnoreCase(emailConf.get(PRIMARY_LOGIN))) {
                return isUserLoggedInEmail(userId);
            }
        }
        else if (loginConfiguration.get(USERID_LOGIN) != null) {
            Map<String, String> userIdConf = loginConfiguration.get(USERID_LOGIN);
            if ("true".equalsIgnoreCase(userIdConf.get(PRIMARY_LOGIN))) {
                return isUserLoggedInEmail(userId);
            }
            else if ("false".equalsIgnoreCase(userIdConf.get(PRIMARY_LOGIN))) {
                return !isUserLoggedInEmail(userId);
            }
        }
        return false;
    }

    /**
     * Identify whether the logged in user used his ordinal username or email
     *
     * @param userId - username used to login.
     * @return - <code>true</code> if userId contains '@'. <code>false</code> otherwise
     */
    private boolean isUserLoggedInEmail(String userId) {
        return userId.contains("@");
    }

    /**
     * Get the primaryLogin name using secondary login name. Primary secondary
     * Configuration is provided in the identitiy.xml. In the userstore, it is
     * users responsibility TO MAINTAIN THE SECONDARY LOGIN NAME AS UNIQUE for
     * each and every users. If it is not unique, we will pick the very first
     * entry from the userlist.
     *
     * @param login - username used to login.
     * @return -
     */
    private String getPrimaryFromSecondary(String login) {

        String claimURI, username = null;
        if (isUserLoggedInEmail(login)) {
            Map<String, String> emailConf = loginConfiguration.get(EMAIL_LOGIN);
            claimURI = emailConf.get(CLAIM_URI);
        } else {
            Map<String, String> userIdConf = loginConfiguration.get(USERID_LOGIN);
            claimURI = userIdConf.get(CLAIM_URI);
        }

        try {
            RealmService realmSvc = APIKeyMgtDataHolder.getRealmService();
            RealmConfiguration config = new RealmConfiguration();
            UserRealm realm = realmSvc.getUserRealm(config);
            org.wso2.carbon.user.core.UserStoreManager storeManager = realm.getUserStoreManager();
            String[] user = storeManager.getUserList(claimURI, login, null);
            if (user.length > 0) {
                username = user[0];
            }
        } catch (UserStoreException e) {
            log.error("Error while retrieving the primaryLogin name using secondary login name : " + login, e);
        }
        return username;
    }

    private Claim[] getUserClaimValues(String authorizedUser, UserStoreManager userStoreManager)
            throws
            UserStoreException {
        Claim[] userClaims = userClaimsCache.getValueFromCache(authorizedUser);
        if(userClaims != null){
            return userClaims;
        }else{
            if(log.isDebugEnabled()){
                log.debug("Cache miss for user claims. Username :" + authorizedUser);
            }
            userClaims = userStoreManager.getUserClaimValues(
                    authorizedUser, null);
            userClaimsCache.addToCache(authorizedUser,userClaims);
            return userClaims;
        }
    }

    // Read the required claim configuration from identity.xml
    private void parseRequiredHeaderClaimUris(OMElement requiredClaimUrisElem) {
        if (requiredClaimUrisElem == null) {
            return;
        }

        Iterator claimUris = requiredClaimUrisElem.getChildrenWithLocalName(CLAIM_URI);
        if (claimUris != null) {
            while (claimUris.hasNext()) {
                OMElement claimUri = (OMElement) claimUris.next();
                if (claimUri != null) {
                    requiredHeaderClaimUris.add(claimUri.getText());
                }
            }
        }
    }

    /**
     * Read the primary/secondary login configuration
     * <OAuth>
     *	....
     *	<LoginConfig>
     *		<UserIdLogin  primary="true">
     *			<ClaimUri></ClaimUri>
     *		</UserIdLogin>
     *		<EmailLogin  primary="false">
     *			<ClaimUri>http://wso2.org/claims/emailaddress</ClaimUri>
     *		</EmailLogin>
     *	</LoginConfig>
     *	.....
     *   </OAuth>
     * @param oauthConfigElem - The '<LoginConfig>' xml configuration element in the api-manager.xml
     */
    private void parseLoginConfig(OMElement oauthConfigElem) {
        OMElement loginConfigElem =  oauthConfigElem.getFirstChildWithName(getQNameWithIdentityNS(LOGIN_CONFIG));
        if (loginConfigElem != null) {
            if (log.isDebugEnabled()) {
                log.debug("Login configuration is set ");
            }
            // Primary/Secondary supported login mechanisms
            OMElement emailConfigElem = loginConfigElem.getFirstChildWithName(getQNameWithIdentityNS(EMAIL_LOGIN));

            OMElement userIdConfigElem =  loginConfigElem.getFirstChildWithName(getQNameWithIdentityNS(USERID_LOGIN));

            Map<String, String> emailConf = new HashMap<String, String>(2);
            emailConf.put(PRIMARY_LOGIN,
                    emailConfigElem.getAttributeValue(new QName(PRIMARY_LOGIN)));
            emailConf.put(CLAIM_URI,
                    emailConfigElem.getFirstChildWithName(getQNameWithIdentityNS(CLAIM_URI))
                            .getText());

            Map<String, String> userIdConf = new HashMap<String, String>(2);
            userIdConf.put(PRIMARY_LOGIN,
                    userIdConfigElem.getAttributeValue(new QName(PRIMARY_LOGIN)));
            userIdConf.put(CLAIM_URI,
                    userIdConfigElem.getFirstChildWithName(getQNameWithIdentityNS(CLAIM_URI))
                            .getText());

            loginConfiguration.put(EMAIL_LOGIN, emailConf);
            loginConfiguration.put(USERID_LOGIN, userIdConf);
        }
    }

    private QName getQNameWithIdentityNS(String localPart) {
        return new QName(IdentityCoreConstants.IDENTITY_DEFAULT_NAMESPACE, localPart);
    }
}
