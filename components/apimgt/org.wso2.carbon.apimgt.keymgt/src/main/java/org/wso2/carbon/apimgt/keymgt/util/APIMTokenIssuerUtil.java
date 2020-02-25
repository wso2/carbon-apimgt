/*
 *Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APISubscriptionInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.impl.dto.JwtTokenInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscribedApiDTO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.JWTAccessTokenIssuerDTO;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.apimgt.keymgt.token.APIMJWTGenerator;
import org.wso2.carbon.apimgt.keymgt.token.TokenGenerator;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.handlers.grant.AuthorizationGrantHandler;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class APIMTokenIssuerUtil {

    private static final Log log = LogFactory.getLog(APIMTokenIssuerUtil.class);

    public static JwtTokenInfoDTO getJwtTokenInfoDTO(Application application, OAuthTokenReqMessageContext tokReqMsgCtx)
            throws APIManagementException {

        String tenantDomain = tokReqMsgCtx.getAuthorizedUser().getTenantDomain();
        String userName = tokReqMsgCtx.getAuthorizedUser().toFullQualifiedUsername();
        String applicationName = application.getName();

        String appOwner = application.getOwner();
        APISubscriptionInfoDTO[] apis = ApiMgtDAO.getInstance()
                .getSubscribedAPIsForAnApp(appOwner, applicationName);

        JwtTokenInfoDTO jwtTokenInfoDTO = new JwtTokenInfoDTO();
        jwtTokenInfoDTO.setSubscriber("sub");
        jwtTokenInfoDTO.setEndUserName(userName);
        jwtTokenInfoDTO.setContentAware(true);

        Set<String> subscriptionTiers = new HashSet<>();
        List<SubscribedApiDTO> subscribedApiDTOList = new ArrayList<SubscribedApiDTO>();
        for (APISubscriptionInfoDTO api : apis) {
            subscriptionTiers.add(api.getSubscriptionTier());

            SubscribedApiDTO subscribedApiDTO = new SubscribedApiDTO();
            subscribedApiDTO.setName(api.getApiName());
            subscribedApiDTO.setContext(api.getContext());
            subscribedApiDTO.setVersion(api.getVersion());
            subscribedApiDTO.setPublisher(api.getProviderId());
            subscribedApiDTO.setSubscriptionTier(api.getSubscriptionTier());
            subscribedApiDTO.setSubscriberTenantDomain(tenantDomain);
            subscribedApiDTOList.add(subscribedApiDTO);
        }
        jwtTokenInfoDTO.setSubscribedApiDTOList(subscribedApiDTOList);

        if (subscriptionTiers.size() > 0) {
            SubscriptionPolicy[] subscriptionPolicies = ApiMgtDAO.getInstance()
                    .getSubscriptionPolicies(subscriptionTiers.toArray(new String[0]), APIUtil.getTenantId(appOwner));

            Map<String, SubscriptionPolicyDTO> subscriptionPolicyDTOList = new HashMap<>();
            for (SubscriptionPolicy subscriptionPolicy : subscriptionPolicies) {
                SubscriptionPolicyDTO subscriptionPolicyDTO = new SubscriptionPolicyDTO();
                subscriptionPolicyDTO.setSpikeArrestLimit(subscriptionPolicy.getRateLimitCount());
                subscriptionPolicyDTO.setSpikeArrestUnit(subscriptionPolicy.getRateLimitTimeUnit());
                subscriptionPolicyDTO.setStopOnQuotaReach(subscriptionPolicy.isStopOnQuotaReach());
                subscriptionPolicyDTOList.put(subscriptionPolicy.getPolicyName(), subscriptionPolicyDTO);
            }
            jwtTokenInfoDTO.setSubscriptionPolicyDTOList(subscriptionPolicyDTOList);
        }
        return jwtTokenInfoDTO;
    }

    /**
     * Get token validity period for the Self contained JWT Access Token.
     *
     * @param tokenReqMessageContext message context of the token request
     * @param oAuthAppDO oauth app DBO.
     * @return expiry time in seconds
     * @throws IdentityOAuth2Exception
     */
    public static long getAccessTokenLifeTimeInSeconds(OAuthTokenReqMessageContext tokenReqMessageContext,
                                                   OAuthAppDO oAuthAppDO) throws IdentityOAuth2Exception {

        long lifetimeInSeconds;
        boolean isUserAccessTokenType =
                isUserAccessTokenType(tokenReqMessageContext.getOauth2AccessTokenReqDTO().getGrantType());

        if (isUserAccessTokenType) {
            lifetimeInSeconds = oAuthAppDO.getUserAccessTokenExpiryTime();
            if (log.isDebugEnabled()) {
                log.debug("User Access Token Life time set to : " + lifetimeInSeconds + " sec.");
            }
        } else {
            if (tokenReqMessageContext.getValidityPeriod() == -1) { // the token request does not specify the validity period explicitly
                lifetimeInSeconds = oAuthAppDO.getApplicationAccessTokenExpiryTime();
            } else { // set the expiry time sent in the request
                lifetimeInSeconds = getSecondsTillExpiry(tokenReqMessageContext.getValidityPeriod());
            }
            if (log.isDebugEnabled()) {
                log.debug("Application Access Token Life time set to : " + lifetimeInSeconds + " sec.");
            }
        }

        if (lifetimeInSeconds == 0) {
            if (isUserAccessTokenType) {
                lifetimeInSeconds =
                        OAuthServerConfiguration.getInstance().getUserAccessTokenValidityPeriodInSeconds();
                if (log.isDebugEnabled()) {
                    log.debug("User access token time was 0ms. Setting default user access token lifetime : "
                            + lifetimeInSeconds + " sec.");
                }
            } else {
                lifetimeInSeconds =
                        OAuthServerConfiguration.getInstance().getApplicationAccessTokenValidityPeriodInSeconds();
                if (log.isDebugEnabled()) {
                    log.debug("Application access token time was 0ms. Setting default Application access token " +
                            "lifetime : " + lifetimeInSeconds + " sec.");
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("JWT Self Signed Access Token Life time set to : " + lifetimeInSeconds + " sec.");
        }
        return lifetimeInSeconds;
    }

    /**
     * Utility method to be used to generate JWT access token for all grant types.
     * @param jwtAccessTokenIssuerDTO DTO with information needed to populate some claims in JWT.
     * @param application application object to fetch some information needed for JWT.
     * @return JWT access token
     * @throws OAuthSystemException
     */
    public static String generateToken(JWTAccessTokenIssuerDTO jwtAccessTokenIssuerDTO, Application application)
            throws OAuthSystemException {

        String accessToken;
        String clientId = jwtAccessTokenIssuerDTO.getClientId();
        try {
            OAuthAppDO oAuthAppDO = OAuth2Util.getAppInformationByClientId(clientId);
            String[] audience = oAuthAppDO.getAudiences();
            List<String> audienceList = Arrays.asList(audience);
            StringBuilder scopeString = new StringBuilder();
            String[] scopeList = jwtAccessTokenIssuerDTO.getScopeList();
            for (String scope : scopeList) {
                scopeString.append(scope).append(" ");
            }

            ApplicationDTO applicationDTO = new ApplicationDTO();
            applicationDTO.setId(application.getId());
            applicationDTO.setName(application.getName());
            applicationDTO.setTier(application.getTier());
            applicationDTO.setOwner(application.getOwner());

            AuthenticatedUser endUser = jwtAccessTokenIssuerDTO.getUser();
            JwtTokenInfoDTO jwtTokenInfoDTO = APIUtil.getJwtTokenInfoDTO(application,
                    endUser.toFullQualifiedUsername(),
                    endUser.getTenantDomain());
            jwtTokenInfoDTO.setScopes(scopeString.toString().trim());
            jwtTokenInfoDTO.setAudience(audienceList);
            jwtTokenInfoDTO.setExpirationTime(jwtAccessTokenIssuerDTO.getValidityPeriod());
            jwtTokenInfoDTO.setApplication(applicationDTO);
            jwtTokenInfoDTO.setKeyType(application.getKeyType());
            jwtTokenInfoDTO.setConsumerKey(clientId);

            APIMJWTGenerator apimjwtGenerator = new APIMJWTGenerator();
            accessToken = apimjwtGenerator.generateJWT(jwtTokenInfoDTO);

        } catch (InvalidOAuthClientException | IdentityOAuth2Exception | APIManagementException e) {
            log.error("Error occurred while getting JWT Token client ID : " + clientId + " when getting oAuth App " +
                    "information", e);
            throw new OAuthSystemException("Error occurred while getting JWT Token client ID : " + clientId, e);
        }
        return accessToken;
    }

    /**
     * Get token validity period for the Self contained JWT Access Token. (For implicit grant)
     *
     * @param oAuthAppDO Oauth app DBO
     * @return expiry time in seconds.
     */
    public static long getAccessTokenLifeTimeInSeconds(OAuthAppDO oAuthAppDO) {

        long lifetimeInSeconds = oAuthAppDO.getUserAccessTokenExpiryTime();
        if (lifetimeInSeconds == 0) {
            lifetimeInSeconds = OAuthServerConfiguration.getInstance()
                    .getUserAccessTokenValidityPeriodInSeconds();
            if (log.isDebugEnabled()) {
                log.debug("User access token time was 0ms. Setting default user access token lifetime : "
                        + lifetimeInSeconds + " sec.");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("JWT Self Signed Access Token Life time set to : " + lifetimeInSeconds + " sec.");
        }
        return lifetimeInSeconds;
    }

    private static boolean isUserAccessTokenType(String grantType) throws IdentityOAuth2Exception {

        AuthorizationGrantHandler grantHandler =
                OAuthServerConfiguration.getInstance().getSupportedGrantTypes().get(grantType);
        return grantHandler.isOfTypeApplicationUser();
    }

    private static long getSecondsTillExpiry(long validityPeriod) {

        if (validityPeriod < 0) {
            // a non-expiring token request, set the expiration to a large value
            return Integer.MAX_VALUE;
        } else {
            return validityPeriod;
        }
    }
}
