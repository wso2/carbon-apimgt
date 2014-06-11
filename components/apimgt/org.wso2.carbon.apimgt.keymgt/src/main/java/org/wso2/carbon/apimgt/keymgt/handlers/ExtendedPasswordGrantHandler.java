package org.wso2.carbon.apimgt.keymgt.handlers;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.cache.BaseCache;
import org.wso2.carbon.identity.oauth.common.OAuthConstants;
import org.wso2.carbon.identity.oauth.internal.OAuthComponentServiceHolder;
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

import javax.cache.Cache;
import javax.cache.Caching;
import javax.xml.namespace.QName;
import java.util.*;
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

        IdentityConfigParser configParser = null;
        try {
            configParser = IdentityConfigParser.getInstance();
            OMElement oauthElem = configParser.getConfigElement(CONFIG_ELEM_OAUTH);

            // Get the required claim uris that needs to be included in the response.
            parseRequiredHeaderClaimUris(oauthElem.getFirstChildWithName(getQNameWithIdentityNS(REQUIRED_CLAIM_URIS)));

            // read login config
            parseLoginConfig(oauthElem);

            userClaimsCache = new BaseCache<String, Claim[]>("UserClaimsCache");
            if(userClaimsCache != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully created UserClaimsCache under "+ OAuthConstants.OAUTH_CACHE_MANAGER);
                }
            } else {
                log.error("Error while creating UserClaimsCache");
            }

        } catch (ServerConfigurationException e) {
            log.error("Error when reading the OAuth Configurations. " +
                    "OAuth related functionality might be affected.", e);
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

            try {
                tenantId = IdentityUtil.getTenantIdOFUser(username);
            } catch (IdentityException e) {
                throw new IdentityOAuth2Exception(e.getMessage(), e);
            }

            RealmService realmService = OAuthComponentServiceHolder.getRealmService();
            UserStoreManager userStoreManager = null;
            try {
                userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            } catch (UserStoreException e) {
                log.error("Error when getting the tenant's UserStoreManager", e);
                return false;
            }

            List<ResponseHeader> respHeaders = new ArrayList<ResponseHeader>();

            if (oAuth2AccessTokenReqDTO.getResourceOwnerUsername() != null) {

                try {

                    if (requiredHeaderClaimUris != null && requiredHeaderClaimUris.size() > 0) {
                        // Get user's claim values from the default profile.
                        Claim[] mapClaimValues = getUserClaimValues(oAuth2AccessTokenReqDTO, userStoreManager);
                        ResponseHeader header;
                        for (Iterator<String> iterator = requiredHeaderClaimUris.iterator(); iterator.hasNext(); ) {

                            String claimUri = iterator.next();

                            for (int j = 0; j < mapClaimValues.length; j++) {
                                Claim claim = mapClaimValues[j];
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
        ScopesIssuer scopesIssuer = new ScopesIssuer();
        return scopesIssuer.setScopes(tokReqMsgCtx);
    }

    /**
     * identify the login username is primary or secondary
     *
     * @param userID
     * @return
     */
    private String getLoginUserName(String userID) {
        String loginUserName = userID;
        if (isSecondaryLogin(userID)) {
            loginUserName = getPrimaryFromSecondary(userID);
        }
        return loginUserName;
    }

    /**
     * Identify whether the loggedin user used his Primary Login name or
     * Secondary login name
     *
     * @param userId
     * @return <code>true</code> if secondary login name is used,
     *         <code>false</code> if primary login name has been used
     */
    private boolean isSecondaryLogin(String userId) {

        if (loginConfiguration.get(EMAIL_LOGIN) != null) {
            Map<String, String> emailConf = loginConfiguration.get(EMAIL_LOGIN);
            if ("true".equalsIgnoreCase(emailConf.get(PRIMARY_LOGIN))) {
                if (isUserLoggedInEmail(userId)) {
                    return false;
                } else {
                    return true;
                }
            }
            if ("false".equalsIgnoreCase(emailConf.get(PRIMARY_LOGIN))) {
                if (isUserLoggedInEmail(userId)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        if (loginConfiguration.get(USERID_LOGIN) != null) {
            Map<String, String> userIdConf = loginConfiguration.get(USERID_LOGIN);
            if ("true".equalsIgnoreCase(userIdConf.get(PRIMARY_LOGIN))) {
                if (isUserLoggedInEmail(userId)) {
                    return true;
                } else {
                    return false;
                }
            }
            if ("false".equalsIgnoreCase(userIdConf.get(PRIMARY_LOGIN))) {
                if (isUserLoggedInEmail(userId)) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Identify whether the loggedin user used his ordinal username or email
     *
     * @param userId
     * @return
     */
    private boolean isUserLoggedInEmail(String userId) {

        if (userId.contains("@")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the primaryLogin name using secondary login name. Primary secondary
     * Configuration is provided in the identitiy.xml. In the userstore, it is
     * users responsibility TO MAINTAIN THE SECONDARY LOGIN NAME AS UNIQUE for
     * each and every users. If it is not unique, we will pick the very first
     * entry from the userlist.
     *
     * @param login
     * @return
     */
    private String getPrimaryFromSecondary(String login) {

        String claimURI = null, username = null;
        if (isUserLoggedInEmail(login)) {
            Map<String, String> emailConf = loginConfiguration.get(EMAIL_LOGIN);
            claimURI = emailConf.get(CLAIM_URI);
        } else {
            Map<String, String> userIdConf = loginConfiguration.get(USERID_LOGIN);
            claimURI = userIdConf.get(CLAIM_URI);
        }

        try {
            RealmService realmSvc = OAuthComponentServiceHolder.getRealmService();
            RealmConfiguration config = new RealmConfiguration();
            UserRealm realm = realmSvc.getUserRealm(config);
            org.wso2.carbon.user.core.UserStoreManager storeManager = realm.getUserStoreManager();
            String user[] = storeManager.getUserList(claimURI, login, null);
            if (user.length > 0) {
                username = user[0].toString();
            }
        } catch (UserStoreException e) {
            log.error("Error while retrieving the primaryLogin name using secondary login name : " + login, e);
        }
        return username;
    }

    private Claim[] getUserClaimValues(OAuth2AccessTokenReqDTO tokenReqDTO, UserStoreManager userStoreManager) throws UserStoreException {
        Claim[] userClaims = userClaimsCache.getValueFromCache(tokenReqDTO.getResourceOwnerUsername());
        if(userClaims != null){
            return userClaims;
        }else{
            if(log.isDebugEnabled()){
                log.debug("Cache miss for user claims. Username :" + tokenReqDTO.getResourceOwnerUsername());
            }
            userClaims = userStoreManager.getUserClaimValues(
                    tokenReqDTO.getResourceOwnerUsername(), null);
            userClaimsCache.addToCache(tokenReqDTO.getResourceOwnerUsername(),userClaims);
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
            for (; claimUris.hasNext();) {
                OMElement claimUri = ((OMElement) claimUris.next());
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
     * @param oauthConfigElem
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
        return new QName(IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE, localPart);
    }
}
