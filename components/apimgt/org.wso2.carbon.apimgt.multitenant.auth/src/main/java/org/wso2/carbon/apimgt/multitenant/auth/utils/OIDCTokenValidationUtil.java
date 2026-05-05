/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.multitenant.auth.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.util.JWTSignatureValidationUtils;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.List;
import java.util.Objects;

/**
 * Utility class for validating OIDC JWT tokens (audience, issuer, signature).
 */
public final class OIDCTokenValidationUtil {

    private static final Log LOG = LogFactory.getLog(OIDCTokenValidationUtil.class);

    private OIDCTokenValidationUtil() {
    }

    /**
     * Get the issuer claim from the JWT claims set.
     *
     * @param claimsSet The JWT claims set.
     * @return The issuer value.
     */
    public static String getIssuer(JWTClaimsSet claimsSet) {

        return claimsSet.getIssuer();
    }

    /**
     * Validate that the audience claim contains the expected token endpoint alias.
     *
     * @param audienceList List of audience values from the JWT.
     * @param idp          The identity provider.
     * @param tenantDomain The tenant domain.
     * @throws AuthenticationFailedException If none of the audience values match the token endpoint alias.
     */
    public static void validateAudience(List<String> audienceList, IdentityProvider idp, String tenantDomain)
            throws AuthenticationFailedException {

        if (LOG.isDebugEnabled()) {  
            LOG.debug("Validating audience claim for IDP: " + idp.getIdentityProviderName() + 
                      " in tenant domain: " + tenantDomain);  
        }

        String tokenEndPointAlias = getTokenEndpointAlias(idp, tenantDomain);

        if (StringUtils.isBlank(tokenEndPointAlias) || audienceList == null || audienceList.isEmpty()) {
            throw new AuthenticationFailedException(
                    OIDCErrorConstants.ErrorMessages.JWT_TOKEN_AUD_CLAIM_VALIDATION_FAILED.getCode(),
                    String.format(
                            OIDCErrorConstants.ErrorMessages.JWT_TOKEN_AUD_CLAIM_VALIDATION_FAILED.getMessage(),
                            tokenEndPointAlias));
        }

        for (String audience : audienceList) {
            if (Objects.equals(tokenEndPointAlias, audience)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(tokenEndPointAlias + " of IDP was found in the list of audiences.");
                }
                return;
            }
        }

        throw new AuthenticationFailedException(
                OIDCErrorConstants.ErrorMessages.JWT_TOKEN_AUD_CLAIM_VALIDATION_FAILED.getCode(),
                String.format(
                        OIDCErrorConstants.ErrorMessages.JWT_TOKEN_AUD_CLAIM_VALIDATION_FAILED.getMessage(),
                        tokenEndPointAlias));
    }

    /**
     * Validate the JWT signature against the identity provider's certificate.
     *
     * @param signedJWT        The signed JWT to validate.
     * @param identityProvider The identity provider whose certificate is used for verification.
     * @throws AuthenticationFailedException If the signature validation fails.
     * @throws JOSEException                If there is a JOSE processing error.
     * @throws IdentityOAuth2Exception       If there is an OAuth2 error during validation.
     */
    public static void validateSignature(SignedJWT signedJWT, IdentityProvider identityProvider)
            throws AuthenticationFailedException, JOSEException, IdentityOAuth2Exception {

        if (!JWTSignatureValidationUtils.validateSignature(signedJWT, identityProvider)) {
            throw new AuthenticationFailedException(
                    OIDCErrorConstants.ErrorMessages.JWT_TOKEN_SIGNATURE_VALIDATION_FAILED.getCode(),
                    OIDCErrorConstants.ErrorMessages.JWT_TOKEN_SIGNATURE_VALIDATION_FAILED.getMessage());
        }
    }

    /**
     * Validate that the issuer claim is present and non-blank.
     *
     * @param claimsSet The JWT claims set.
     * @throws AuthenticationFailedException If the issuer claim is blank or missing.
     */
    public static void validateIssuerClaim(JWTClaimsSet claimsSet) throws AuthenticationFailedException {

        if (StringUtils.isBlank(getIssuer(claimsSet))) {
            throw new AuthenticationFailedException(
                    OIDCErrorConstants.ErrorMessages.JWT_TOKEN_ISS_CLAIM_VALIDATION_FAILED.getCode(),
                    OIDCErrorConstants.ErrorMessages.JWT_TOKEN_ISS_CLAIM_VALIDATION_FAILED.getMessage());
        }
    }

    /**
     * Resolve the token endpoint alias for the given identity provider.
     * For resident IDPs, this is the OAuth2 token URL; for federated IDPs, it is the IDP alias.
     *
     * @param identityProvider The identity provider.
     * @param tenantDomain     The tenant domain.
     * @return The token endpoint alias, or {@code null} if not resolvable.
     */
    private static String getTokenEndpointAlias(IdentityProvider identityProvider, String tenantDomain) {

        if (!IdentityApplicationConstants.RESIDENT_IDP_RESERVED_NAME.equals(
                identityProvider.getIdentityProviderName())) {
            String alias = identityProvider.getAlias();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Token End Point Alias of the Federated IDP: " + alias);
            }
            return alias;
        }

        // Resident IDP: resolve from federated authenticator config.
        try {
            identityProvider = IdentityProviderManager.getInstance().getResidentIdP(tenantDomain);
        } catch (IdentityProviderManagementException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error while getting Resident IDP: " + e.getMessage());
            }
            return null;
        }

        FederatedAuthenticatorConfig oauthConfig =
                IdentityApplicationManagementUtil.getFederatedAuthenticator(
                        identityProvider.getFederatedAuthenticatorConfigs(),
                        IdentityApplicationConstants.Authenticator.OIDC.NAME);

        if (oauthConfig == null) {
            return null;
        }

        Property oauthTokenURL = IdentityApplicationManagementUtil.getProperty(
                oauthConfig.getProperties(),
                IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL);

        if (oauthTokenURL != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Token End Point Alias of Resident IDP: " + oauthTokenURL.getValue());
            }
            return oauthTokenURL.getValue();
        }
        return null;
    }
}
