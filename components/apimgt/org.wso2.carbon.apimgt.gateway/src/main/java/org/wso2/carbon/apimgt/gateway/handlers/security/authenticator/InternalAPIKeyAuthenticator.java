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

package org.wso2.carbon.apimgt.gateway.handlers.security.authenticator;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.models.OpenAPI;
import net.minidev.json.JSONObject;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.JWTTokenPayloadInfo;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.JWTUtil;
import org.wso2.carbon.apimgt.impl.utils.SigningUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.cache.Cache;

/**
 * This class used to authenticate InternalKey
 */
public class InternalAPIKeyAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(InternalAPIKeyAuthenticator.class);

    private String securityParam;
    private boolean jwtGenerationEnabled;
    private boolean isGatewayTokenCacheEnabled;
    private ExtendedJWTConfigurationDto jwtConfigurationDto;
    private AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator;
    private static volatile long ttl = -1L;

    public InternalAPIKeyAuthenticator(String securityParam) {
        this.securityParam = securityParam;
    }

    @Override
    public void init(SynapseEnvironment env) {
        jwtConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto();
        jwtGenerationEnabled = jwtConfigurationDto.isEnabled();
        isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
    }

    @Override
    public void destroy() {

    }

    @Override
    public AuthenticationResponse authenticate(MessageContext synCtx) {
        API retrievedApi = GatewayUtils.getAPI(synCtx);
        if (retrievedApi != null) {
            if (log.isDebugEnabled()) {
                log.info("Internal Key Authentication initialized");
            }

            try {
                // Extract internal from the request while removing it from the msg context.
                String internalKey = extractInternalKey(synCtx);
                if (StringUtils.isEmpty(internalKey)) {
                    return new AuthenticationResponse(false, false,
                            true, APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }
                OpenAPI openAPI = (OpenAPI) synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
                if (openAPI == null && (!APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))
                        && !APIConstants.API_TYPE_MCP.equals(synCtx.getProperty(APIConstants.API_TYPE)))) {
                    log.error("Swagger is missing in the gateway and API type is neither GraphQL nor MCP." +
                            "Therefore, Internal Key authentication cannot be performed.");
                    return new AuthenticationResponse(false, true, false,
                            APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF,
                            APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF_ERROR_MESSAGE);
                }
                JWTTokenPayloadInfo payloadInfo = null;

                String[] splitToken = internalKey.split("\\.");
                JWTClaimsSet payload;
                SignedJWT signedJWT;
                String tokenIdentifier;
                JWSHeader jwsHeader;
                String alias;
                if (splitToken.length != 3) {
                    log.error("Internal Key does not have the format {header}.{payload}.{signature} ");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }
                signedJWT = SignedJWT.parse(internalKey);
                payload = signedJWT.getJWTClaimsSet();
                tokenIdentifier = payload.getJWTID();
                jwsHeader = signedJWT.getHeader();
                if (jwsHeader != null && StringUtils.isNotEmpty(jwsHeader.getKeyID())) {
                    alias = jwsHeader.getKeyID();
                } else {
                    alias = APIUtil.getInternalApiKeyAlias();
                }
                // Check if the decoded header contains type as 'InternalKey'.
                if (!GatewayUtils.isInternalKey(payload)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalid Internal Key token type. Internal Key: " + GatewayUtils.getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid Internal Key token type.");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }

                String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
                String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
                String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                        getProperty(Constants.Configuration.HTTP_METHOD);
                String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

                String resourceCacheKey = APIUtil.getResourceInfoDTOCacheKey(apiContext, apiVersion,
                        matchingResource, httpMethod);
                VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
                verbInfoDTO.setHttpVerb(httpMethod);
                //Not doing resource level authentication
                verbInfoDTO.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                verbInfoDTO.setRequestKey(resourceCacheKey);
                verbInfoDTO.setThrottling(OpenAPIUtils.getResourceThrottlingTier(openAPI, synCtx));
                List<VerbInfoDTO> verbInfoList = new ArrayList<>();
                verbInfoList.add(verbInfoDTO);
                synCtx.setProperty(APIConstants.VERB_INFO_DTO, verbInfoList);

                String cacheKey = GatewayUtils.getAccessTokenCacheKey(tokenIdentifier, apiContext, apiVersion,
                        matchingResource, httpMethod);
                String tenantDomain = GatewayUtils.getTenantDomain();
                boolean isVerified = false;

                String cacheToken = (String) getGatewayInternalKeyCache().get(tokenIdentifier);
                if (cacheToken != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Internal Key retrieved from the Internal Key cache.");
                    }
                    if (getGatewayInternalKeyDataCache().get(cacheKey) != null) {
                        // Token is found in the key cache
                        payloadInfo = (JWTTokenPayloadInfo) getGatewayInternalKeyDataCache().get(cacheKey);
                        String accessToken = payloadInfo.getAccessToken();
                        isVerified = accessToken.equals(internalKey);
                    }
                } else if (getInvalidGatewayInternalKeyCache().get(tokenIdentifier) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Internal Key retrieved from the invalid Internal Key cache. Internal Key: " +
                                GatewayUtils.getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid Internal Key." + GatewayUtils.getMaskedToken(splitToken[0]));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }

                // Not found in cache or caching disabled
                if (!isVerified) {
                    if (log.isDebugEnabled()) {
                        log.debug("Internal Key not found in the cache.");
                    }
                    isVerified =
                            GatewayUtils.verifyTokenSignature(signedJWT, alias) && !GatewayUtils.isJwtTokenExpired(payload);
                    // Add token to tenant token cache
                    if (isVerified) {
                        getGatewayInternalKeyCache().put(tokenIdentifier, tenantDomain);
                    } else {
                        getInvalidGatewayInternalKeyCache().put(tokenIdentifier, tenantDomain);
                    }

                    if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        try {
                            // Start super tenant flow
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                            // Add token to super tenant token cache
                            if (isVerified) {
                                getGatewayInternalKeyCache().put(tokenIdentifier, tenantDomain);
                            }
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }

                    }
                }

                // If Internal Key signature is verified
                if (isVerified) {
                    if (log.isDebugEnabled()) {
                        log.debug("Internal Key signature is verified.");
                    }
                    if (payloadInfo != null) {
                        // Internal Key is found in the key cache
                        payload = payloadInfo.getPayload();
                        if (GatewayUtils.isJwtTokenExpired(payload)) {
                            getGatewayInternalKeyCache().remove(tokenIdentifier);
                            getInvalidGatewayInternalKeyCache().put(tokenIdentifier, tenantDomain);
                            log.error("Internal Key is expired");
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                        }
                    } else {
                        // Retrieve payload from InternalKey
                        if (log.isDebugEnabled()) {
                            log.debug("InternalKey payload not found in the cache.");
                        }
                        JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
                        jwtTokenPayloadInfo.setPayload(payload);
                        jwtTokenPayloadInfo.setAccessToken(internalKey);
                        getGatewayInternalKeyDataCache().put(cacheKey, jwtTokenPayloadInfo);
                    }
                    String mcpAuthClaim = payload.getStringClaim(APIMgtGatewayConstants.MCP_AUTH_CLAIM);
                    JSONObject api = null;
                    if (StringUtils.isBlank(mcpAuthClaim) || !Boolean.parseBoolean(mcpAuthClaim)) {
                        // If the MCP authenticated claim is not present or false, we can proceed with the subscription
                        // validation. This is to skip subscription validation since the MCP Server have done so already
                        api = GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload, splitToken, false);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Internal Key authentication successful.");
                    }

                    String endUserToken = null;
                    if (jwtGenerationEnabled && StringUtils.isNotBlank(mcpAuthClaim)
                            && Boolean.parseBoolean(mcpAuthClaim)) {
                        ensureJwtGeneratorInitialized();
                        JWTInfoDto jwtInfoDto = buildJWTInfoForInternalKey(payload, retrievedApi, synCtx);
                        endUserToken = generateAndRetrieveJWTToken(tokenIdentifier, jwtInfoDto);
                    }

                    AuthenticationContext authenticationContext = GatewayUtils
                            .generateAuthenticationContext(tokenIdentifier, payload, api, retrievedApi.getApiTier());

                    if (StringUtils.isNotEmpty(endUserToken)) {
                        authenticationContext.setCallerToken(endUserToken);
                    }

                    APISecurityUtils.setAuthenticationContext(synCtx, authenticationContext,
                            jwtGenerationEnabled ? getContextHeader() : null);
                    synCtx.setProperty(APIMgtGatewayConstants.END_USER_NAME, authenticationContext.getUsername());
                    if (log.isDebugEnabled()) {
                        log.debug("User is authorized to access the resource using Internal Key.");
                    }
                    return new AuthenticationResponse(true, true, false, 0, null);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Internal Key signature verification failure. Internal Key: " +
                            GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid Internal Key. Signature verification failed.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);

            } catch (APISecurityException e) {
                return new AuthenticationResponse(false, true, false, e.getErrorCode(), e.getMessage());
            } catch (ParseException e) {
                log.error("Error while parsing Internal Key", e);
                return new AuthenticationResponse(false, true, false, APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
            }
        }
        return new AuthenticationResponse(false, true, false, APISecurityConstants.API_AUTH_GENERAL_ERROR,
                APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
    }

    private void ensureJwtGeneratorInitialized() {

        if (apiMgtGatewayJWTGenerator != null && jwtConfigurationDto.getPublicCert() != null) {
            return;
        }
        String tenantDomain = GatewayUtils.getTenantDomain();
        try {
            if (jwtConfigurationDto.isTenantBasedSigningEnabled()) {
                int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
                jwtConfigurationDto.setPublicCert(SigningUtil.getPublicCertificate(tenantId));
                jwtConfigurationDto.setPrivateKey(SigningUtil.getSigningKey(tenantId));
            } else {
                jwtConfigurationDto.setPublicCert(ServiceReferenceHolder.getInstance().getPublicCert());
                jwtConfigurationDto.setPrivateKey(ServiceReferenceHolder.getInstance().getPrivateKey());
            }
            jwtConfigurationDto.setTtl(getTtl());

            apiMgtGatewayJWTGenerator =
                    ServiceReferenceHolder.getInstance().getApiMgtGatewayJWTGenerator()
                            .get(jwtConfigurationDto.getGatewayJWTGeneratorImpl());
            apiMgtGatewayJWTGenerator.setJWTConfigurationDto(jwtConfigurationDto);
        } catch (Exception e) {
            log.error("Failed initializing gateway JWT generator for InternalKey flow", e);
        }
    }

    private JWTInfoDto buildJWTInfoForInternalKey(JWTClaimsSet payload, API matchedAPI, MessageContext synCtx) {
        JWTInfoDto dto = new JWTInfoDto();

        // API meta
        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        dto.setApiContext(apiContext);
        dto.setVersion(apiVersion);
        dto.setApiName(matchedAPI != null ? matchedAPI.getName() : null);

        String dialect = jwtConfigurationDto.getConsumerDialectUri();
        if (dialect == null || dialect.isEmpty() || "/".equals(dialect)) {
            dialect = APIConstants.DEFAULT_CARBON_DIALECT + "/";
        } else if (!dialect.endsWith("/")) {
            dialect = dialect + "/";
        }

        Object subscriberObj = payload.getClaim(dialect + APIMgtGatewayConstants.SUBSCRIBER_CLAIM);
        if (subscriberObj instanceof String && StringUtils.isNotBlank((String) subscriberObj)) {
            dto.setSubscriber((String) subscriberObj);
        }

        Object applicationIdObj = payload.getClaim(dialect + APIMgtGatewayConstants.APPLICATION_ID_CLAIM);
        if (applicationIdObj instanceof String && StringUtils.isNotBlank((String) applicationIdObj)) {
            dto.setApplicationId((String) applicationIdObj);
        }

        Object appNameObj = payload.getClaim(dialect + APIMgtGatewayConstants.APPLICATION_NAME_CLAIM);
        if (appNameObj instanceof String && StringUtils.isNotBlank((String) appNameObj)) {
            dto.setApplicationName((String) appNameObj);
        }

        Object appTierObj = payload.getClaim(dialect + APIMgtGatewayConstants.APPLICATION_TIER_CLAIM);
        if (appTierObj instanceof String && StringUtils.isNotBlank((String) appTierObj)) {
            dto.setApplicationTier((String) appTierObj);
        }

        Object tierObj = payload.getClaim(dialect + APIMgtGatewayConstants.TIER_CLAIM);
        if (tierObj instanceof String && StringUtils.isNotBlank((String) tierObj)) {
            dto.setSubscriptionTier((String) tierObj);
        }

        Object appUuidObj = payload.getClaim(dialect + APIMgtGatewayConstants.APPLICATION_UUID_CLAIM);
        if (appUuidObj instanceof String && StringUtils.isNotBlank((String) appUuidObj)) {
            dto.setApplicationUUId((String) appUuidObj);
        }

        Object keyTypeObj = payload.getClaim(dialect + APIMgtGatewayConstants.KEY_TYPE_CLAIM);
        if (keyTypeObj instanceof String && StringUtils.isNotBlank((String) keyTypeObj)) {
            dto.setKeyType((String) keyTypeObj);
        }

        Object endUserObj = payload.getClaim(dialect + APIMgtGatewayConstants.END_USER_CLAIM);
        if (endUserObj instanceof String && StringUtils.isNotBlank((String) endUserObj)) {
            dto.setEndUser((String) endUserObj);
        }

        Object endUserTenantIdObj = payload.getClaim(dialect + APIMgtGatewayConstants.END_USER_TENANT_ID_CLAIM);
        if (endUserTenantIdObj instanceof Number) {
            dto.setEndUserTenantId(((Number) endUserTenantIdObj).intValue());
        }

        // Build JWTValidationInfo to carry scopes & claims (generator reads from here)
        JWTValidationInfo vi = new JWTValidationInfo();
        vi.setValid(true);
        Map<String, Object> claims = new HashMap<>();
        Set<String> exclude = new HashSet<>(APIMgtGatewayConstants.STANDARD_JWT_CLAIMS);
        exclude.addAll(Arrays.asList(
                APIMgtGatewayConstants.MCP_AUTH_CLAIM,
                APIMgtGatewayConstants.TOKEN_TYPE_CLAIM,
                APIMgtGatewayConstants.KEY_TYPE_CLAIM,
                dialect + APIMgtGatewayConstants.SUBSCRIBER_CLAIM,
                dialect + APIMgtGatewayConstants.APPLICATION_ID_CLAIM,
                dialect + APIMgtGatewayConstants.APPLICATION_NAME_CLAIM,
                dialect + APIMgtGatewayConstants.APPLICATION_TIER_CLAIM,
                dialect + APIMgtGatewayConstants.TIER_CLAIM,
                dialect + APIMgtGatewayConstants.APPLICATION_UUID_CLAIM,
                dialect + APIMgtGatewayConstants.KEY_TYPE_CLAIM,
                dialect + APIMgtGatewayConstants.END_USER_CLAIM,
                dialect + APIMgtGatewayConstants.END_USER_TENANT_ID_CLAIM
        ));
        for (Map.Entry<String, Object> e : payload.getClaims().entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if (k != null && v != null && !exclude.contains(k)) {
                claims.putIfAbsent(k, v);
            }
        }
        vi.setClaims(claims);
        long nowMs = System.currentTimeMillis();
        long expMs = (payload.getExpirationTime() != null) ?
                payload.getExpirationTime().getTime() : nowMs + (getTtl() * 1000L);
        vi.setExpiryTime(expMs);

        dto.setJwtValidationInfo(vi);
        return dto;
    }

    private String generateAndRetrieveJWTToken(String tokenSignature, JWTInfoDto jwtInfoDto) {
        String jwt = null;
        boolean valid = false;
        String cacheKey = jwtInfoDto.getApiContext() + ":" + jwtInfoDto.getVersion() + ":" + tokenSignature;

        if (isGatewayTokenCacheEnabled) {
            Object cached = CacheProvider.getGatewayJWTTokenCache().get(cacheKey);
            if (cached instanceof String) {
                jwt = (String) cached;
                long skewMs = getTimeStampSkewInSeconds() * 1000L;
                valid = JWTUtil.isJWTValid(jwt, jwtConfigurationDto.getJwtDecoding(), skewMs);
                if (!valid) {
                    CacheProvider.getGatewayJWTTokenCache().remove(cacheKey);
                    jwt = null;
                }
            }
        }
        if (jwt == null) {
            try {
                jwt = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
                if (isGatewayTokenCacheEnabled) {
                    CacheProvider.getGatewayJWTTokenCache().put(cacheKey, jwt);
                }
            } catch (Exception e) {
                log.error("Error generating backend JWT for InternalKey (MCP)", e);
                return null;
            }
        }
        return jwt;
    }

    private long getTtl() {
        if (ttl != -1) {
            return ttl;
        }
        synchronized (AbstractAPIMgtGatewayJWTGenerator.class) {
            if (ttl != -1) {
                return ttl;
            }
            APIManagerConfiguration config = org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIManagerConfiguration();

            String gwTokenCacheConfig = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
            boolean isGWTokenCacheEnabled = Boolean.parseBoolean(gwTokenCacheConfig);

            if (isGWTokenCacheEnabled) {
                String apimKeyCacheExpiry = config.getFirstProperty(APIConstants.TOKEN_CACHE_EXPIRY);

                if (apimKeyCacheExpiry != null) {
                    ttl = Long.parseLong(apimKeyCacheExpiry);
                } else {
                    ttl = Long.valueOf(900);
                }
            } else {
                String ttlValue = config.getFirstProperty(APIConstants.JWT_EXPIRY_TIME);
                if (ttlValue != null) {
                    ttl = Long.parseLong(ttlValue);
                } else {
                    //15 * 60 (convert 15 minutes to seconds)
                    ttl = Long.valueOf(900);
                }
            }
            return ttl;
        }
    }

    protected long getTimeStampSkewInSeconds() {

        return OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();
    }

    private String extractInternalKey(MessageContext mCtx) {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mCtx).getAxis2MessageContext();
        String internalKey;

        //check headers to get InternalKey
        Map headers = (Map) (axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        if (headers != null) {
            internalKey = (String) headers.get(securityParam);
            if (internalKey != null) {
                //Remove InternalKey header from the request except for MCP existing_API subtype
                API matchedAPI = GatewayUtils.getAPI(mCtx);
                if (matchedAPI != null && !APIConstants.API_TYPE_MCP.equals(matchedAPI.getApiType()) &&
                        !APIConstants.API_SUBTYPE_EXISTING_API.equals(matchedAPI.getSubtype())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Removing Internal Key header from request for API: " + matchedAPI.getName());
                    }
                    headers.remove(securityParam);
                }
                return internalKey.trim();
            }
        }
        return null;
    }



    //first level cache
    private Cache getGatewayInternalKeyCache() {

        return CacheProvider.getGatewayInternalKeyCache();
    }

    private Cache getInvalidGatewayInternalKeyCache() {

        return CacheProvider.getInvalidGatewayInternalKeyCache();
    }

    //second level cache
    private Cache getGatewayInternalKeyDataCache() {

        return CacheProvider.getGatewayInternalKeyDataCache();
    }


    @Override
    public String getChallengeString() {

        return "Internal API Key realm=\"WSO2 API Manager\"";
    }

    @Override
    public String getRequestOrigin() {

        return null;
    }

    @Override
    public int getPriority() {

        return -10;
    }

    /**
     * Get the context header defined in the config file
     *
     * @return context header
     */
    private String getContextHeader() {

        APIManagerConfiguration apimConf = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        JWTConfigurationDto jwtConfigDto = apimConf.getJwtConfigurationDto();
        return jwtConfigDto.getJwtHeader();
    }
}
