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

package org.wso2.carbon.apimgt.keymgt.token;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.ScopesIssuer;
import org.wso2.carbon.apimgt.keymgt.handlers.ResourceConstants;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.grant.jwt.JWTBearerGrantHandler;
import org.wso2.carbon.identity.oauth2.grant.jwt.JWTConstants;
import org.wso2.carbon.identity.oauth2.model.RequestParameter;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Extends the JWTBearerGrantHanlder, in which the validateScope() method is overridden
 * to implement role based scope validation
 */
public class ExtendedJWTBearerGrantHandler extends JWTBearerGrantHandler {

    private static Log log = LogFactory.getLog(ExtendedJWTBearerGrantHandler.class);
    private IdentityProvider identityProvider = null;

    @Override public boolean validateScope(OAuthTokenReqMessageContext tokReqMsgCtx) {

        SignedJWT signedJWT = null;
        JWTClaimsSet claimsSet = null;
        String[] roles = null;
        try {
            signedJWT = getSignedJWT(tokReqMsgCtx);
        } catch (IdentityOAuth2Exception e) {
            log.error("Couldn't retrieve signed JWT", e);
        }
        claimsSet = getClaimSet(signedJWT);
        String jwtIssuer = claimsSet != null ? claimsSet.getIssuer() : null;
        String tenantDomain = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getTenantDomain();

        try {
            identityProvider = IdentityProviderManager.getInstance().getIdPByName(jwtIssuer, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            log.error("Couldn't initiate identity provider instance", e);
        }

        try {
            roles = claimsSet != null ?
                    claimsSet.getStringArrayClaim(identityProvider.getClaimConfig().getRoleClaimURI()) :
                    null;
        } catch (ParseException e) {
            log.error("Couldn't retrieve roles:", e);
        }

        List<String> updatedRoles = new ArrayList<>();
        if (roles != null) {
            for (String role : roles) {
                String updatedRoleClaimValue = getUpdatedRoleClaimValue(identityProvider, role);
                if (updatedRoleClaimValue != null) {
                    updatedRoles.add(updatedRoleClaimValue);
                } else {
                    updatedRoles.add(role);
                }
            }
        }
        AuthenticatedUser user = tokReqMsgCtx.getAuthorizedUser();
        Map<ClaimMapping, String> userAttributes = user.getUserAttributes();
        String roleClaim = identityProvider.getClaimConfig().getRoleClaimURI();
        userAttributes
                .put(ClaimMapping.build(roleClaim, roleClaim, null, false),
                        updatedRoles.toString().replace(" ", ""));
        tokReqMsgCtx.addProperty(ResourceConstants.ROLE_CLAIM, roleClaim);
        user.setUserAttributes(userAttributes);
        tokReqMsgCtx.setAuthorizedUser(user);
        return ScopesIssuer.getInstance().setScopes(tokReqMsgCtx);
    }

    /**
     * Check the retireved roles against the role mappings in the IDP and return the updated roles
     * @param identityProvider used to retrieve the role mappings
     * @param currentRoleClaimValue current roles received through the token
     * @return updated roles
     */
    private String getUpdatedRoleClaimValue(IdentityProvider identityProvider, String currentRoleClaimValue) {

        if (StringUtils.equalsIgnoreCase(IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME,
                identityProvider.getIdentityProviderName())) {
            return currentRoleClaimValue;
        }
        currentRoleClaimValue = currentRoleClaimValue.replace("\\/", "/").
                replace("[", "").replace("]", "").replace("\"", "");

        PermissionsAndRoleConfig permissionAndRoleConfig = identityProvider.getPermissionAndRoleConfig();
        if (permissionAndRoleConfig != null && ArrayUtils.isNotEmpty(permissionAndRoleConfig.getRoleMappings())) {
            String[] receivedRoles = currentRoleClaimValue.split(FrameworkUtils.getMultiAttributeSeparator());
            List<String> updatedRoleClaimValues = new ArrayList<>();
            loop:
            for (String receivedRole : receivedRoles) {
                for (RoleMapping roleMapping : permissionAndRoleConfig.getRoleMappings()) {
                    if (roleMapping.getRemoteRole().equals(receivedRole)) {
                        updatedRoleClaimValues.add(roleMapping.getLocalRole().getLocalRoleName());
                        continue loop;
                    }
                }
                if (!OAuthServerConfiguration.getInstance().isReturnOnlyMappedLocalRoles()) {
                    updatedRoleClaimValues.add(receivedRole);
                }
            }
            if (!updatedRoleClaimValues.isEmpty()) {
                return StringUtils.join(updatedRoleClaimValues, FrameworkUtils.getMultiAttributeSeparator());
            }
            return null;
        }
        if (!OAuthServerConfiguration.getInstance().isReturnOnlyMappedLocalRoles()) {
            return currentRoleClaimValue;
        }
        return null;
    }

    /**
     * Method to retrieve claims from the JWT
     * @param signedJWT JWT token
     * @return JWTClaimsSet Object
     */
    private JWTClaimsSet getClaimSet(SignedJWT signedJWT) {
        JWTClaimsSet claimsSet = null;
        try {
            claimsSet = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Error when trying to retrieve claimsSet from the JWT:", e);
        }
        return claimsSet;
    }

    /**
     * Method to parse the assertion and retrieve the signed JWT
     * @param tokReqMsgCtx request
     * @return SignedJWT object
     * @throws IdentityOAuth2Exception exception thrown due to a parsing error
     */
    private SignedJWT getSignedJWT(OAuthTokenReqMessageContext tokReqMsgCtx) throws IdentityOAuth2Exception {
        RequestParameter[] params = tokReqMsgCtx.getOauth2AccessTokenReqDTO().getRequestParameters();
        String assertion = null;
        SignedJWT signedJWT;
        for (RequestParameter param : params) {
            if (param.getKey().equals(JWTConstants.OAUTH_JWT_ASSERTION)) {
                assertion = param.getValue()[0];
                break;
            }
        }
        if (StringUtils.isEmpty(assertion)) {
            String errorMessage = "Error while retrieving assertion";
            throw new IdentityOAuth2Exception(errorMessage);
        }

        try {
            signedJWT = SignedJWT.parse(assertion);
            if (log.isDebugEnabled()) {
                log.debug(signedJWT);
            }
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the JWT.";
            throw new IdentityOAuth2Exception(errorMessage, e);
        }
        return signedJWT;
    }
}
