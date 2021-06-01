/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.apikey;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.JWTInfoDto;
import org.wso2.carbon.apimgt.gateway.dto.JWTTokenPayloadInfo;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.generator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import javax.cache.Cache;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ApiKeyAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(ApiKeyAuthenticator.class);
    private AbstractAPIMgtGatewayJWTGenerator apiMgtGatewayJWTGenerator;
    private JWTConfigurationDto jwtConfigurationDto;
    private boolean jwtGenerationEnabled;
    private boolean isGatewayTokenCacheEnabled;
    private static boolean gatewayApiKeyCacheInit = false;
    private static boolean gatewayApiKeyKeyCacheInit = false;
    private static boolean gatewayInvalidApiKeyCacheInit = false;
    private String contextHeader = null;
    private String securityParam;
    private String apiLevelPolicy;
    private boolean isMandatory;

    public ApiKeyAuthenticator(String authorizationHeader, String apiLevelPolicy, boolean isApiKeyMandatory) {
        this.securityParam = authorizationHeader;
        this.apiLevelPolicy = apiLevelPolicy;
        this.isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
        this.isMandatory = isApiKeyMandatory;
        this.jwtConfigurationDto =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getJwtConfigurationDto();
        this.jwtGenerationEnabled  = jwtConfigurationDto.isEnabled();
        this.apiMgtGatewayJWTGenerator =
                ServiceReferenceHolder.getInstance().getApiMgtGatewayJWTGenerator()
                        .get(jwtConfigurationDto.getGatewayJWTGeneratorImpl());
    }

    @Override
    public void init(SynapseEnvironment env) {
        initParams();
    }

    @Override
    public void destroy() {

    }

    @Override
    public AuthenticationResponse authenticate(MessageContext synCtx) {
        if (log.isDebugEnabled()) {
            log.info("ApiKey Authentication initialized");
        }

        try {
            // Extract apikey from the request while removing it from the msg context.
            String apiKey = extractApiKey(synCtx);
            JWTTokenPayloadInfo payloadInfo = null;

            String splitToken[] = apiKey.split("\\.");
            JWSHeader decodedHeader;
            JWTClaimsSet payload = null;
            SignedJWT signedJWT = null;
            String certAlias, tokenIdentifier;
            if (splitToken.length != 3) {
                log.error("Api Key does not have the format {header}.{payload}.{signature} ");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }
            try {
                decodedHeader = JWSHeader.parse(new Base64URL(splitToken[0]));
                signedJWT = SignedJWT.parse(apiKey);
                tokenIdentifier = signedJWT.getJWTClaimsSet().getJWTID();
            } catch (IllegalArgumentException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid Api Key. Api Key: " + GatewayUtils.getMaskedToken(splitToken[0]), e);
                }
                log.error("Invalid JWT token. Failed to decode the Api Key header.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE , e);
            }
            // Check if the decoded header contains type as 'JWT'.
            if (!decodedHeader.getType().equals(JOSEObjectType.JWT)) {
                if (log.isDebugEnabled()) {
                    log.debug("Invalid Api Key token type. Api Key: " + GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid Api Key token type.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }

            if (decodedHeader.getKeyID() == null) {
                if (log.isDebugEnabled()){
                    log.debug("Invalid Api Key. Could not find alias in header. Api Key: " +
                            GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid Api Key. Could not find alias in header");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            } else {
                certAlias = decodedHeader.getKeyID();
            }

            String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String httpMethod = (String)((Axis2MessageContext) synCtx).getAxis2MessageContext().
                    getProperty(Constants.Configuration.HTTP_METHOD);
            String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);

            OpenAPI openAPI = (OpenAPI) synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
            if (openAPI == null && !APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
                log.error("Swagger is missing in the gateway. " +
                        "Therefore, Api Key authentication cannot be performed.");
                return new AuthenticationResponse(false, isMandatory, true,
                        APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF,
                        APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF_ERROR_MESSAGE);
            }
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

            // Validate from cache
            if (isGatewayTokenCacheEnabled) {
                String cacheToken = (String) getGatewayApiKeyCache().get(tokenIdentifier);
                if (cacheToken != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key retrieved from the Api Key cache.");
                    }
                    if (getGatewayApiKeyDataCache().get(cacheKey) != null) {
                        // Token is found in the key cache
                        payloadInfo = (JWTTokenPayloadInfo) getGatewayApiKeyDataCache().get(cacheKey);
                        String rawPayload = payloadInfo.getRawPayload();
                        if (!rawPayload.equals(splitToken[1])) {
                            isVerified = false;
                        } else {
                            isVerified = true;
                        }
                    }
                } else if (getInvalidGatewayApiKeyCache().get(tokenIdentifier) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key retrieved from the invalid Api Key cache. Api Key: " +
                                GatewayUtils.getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid Api Key." + GatewayUtils.getMaskedToken(splitToken[0]));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                } else if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenIdentifier)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                                getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid API Key. " + GatewayUtils.getMaskedToken(splitToken[0]));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            "Invalid API Key");
                }
            } else {
                if (RevokedJWTDataHolder.isJWTTokenSignatureExistsInRevokedMap(tokenIdentifier)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Token retrieved from the revoked jwt token map. Token: " + GatewayUtils.
                                getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid JWT token. " + GatewayUtils.getMaskedToken(splitToken[0]));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            "Invalid JWT token");
                }
            }

            // Not found in cache or caching disabled
            if (!isVerified) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key not found in the cache.");
                }
                try {
                    signedJWT = (SignedJWT) JWTParser.parse(apiKey);
                    payload = signedJWT.getJWTClaimsSet();
                } catch (JSONException | IllegalArgumentException | ParseException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalid Api Key. Api Key: " + GatewayUtils.getMaskedToken(splitToken[0]), e);
                    }
                    log.error("Invalid JWT token. Failed to decode the Api Key body.");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
                }
                try {
                    isVerified = GatewayUtils.verifyTokenSignature(signedJWT, certAlias);
                } catch (APISecurityException e) {
                    if (e.getErrorCode() == APISecurityConstants.API_AUTH_INVALID_CREDENTIALS) {
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                    } else {
                        throw e;
                    }
                }
                if (isGatewayTokenCacheEnabled) {
                    // Add token to tenant token cache
                    if (isVerified) {
                        getGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                    } else {
                        getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                    }

                    if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        try {
                            // Start super tenant flow
                            PrivilegedCarbonContext.startTenantFlow();
                            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                                    .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                            // Add token to super tenant token cache
                            if (isVerified) {
                                getGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                            } else {
                                getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                            }
                        } finally {
                            PrivilegedCarbonContext.endTenantFlow();
                        }

                    }
                }
            }

            // If Api Key signature is verified
            if (isVerified) {
                if (log.isDebugEnabled()) {
                    log.debug("Api Key signature is verified.");
                }
                if (isGatewayTokenCacheEnabled && payloadInfo != null) {
                    // Api Key is found in the key cache
                    payload = payloadInfo.getPayload();
                    if (isJwtTokenExpired(payload)) {
                        getGatewayApiKeyCache().remove(tokenIdentifier);
                        getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                        log.error("Api Key is expired");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                    }
                    validateAPIKeyRestrictions(payload, synCtx);
                } else {
                    // Retrieve payload from ApiKey
                    if (log.isDebugEnabled()) {
                        log.debug("ApiKey payload not found in the cache.");
                    }
                    if (payload == null) {
                        try {
                            signedJWT = (SignedJWT) JWTParser.parse(apiKey);
                            payload = signedJWT.getJWTClaimsSet();
                        } catch (JSONException | IllegalArgumentException|ParseException e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Invalid ApiKey. ApiKey: " + GatewayUtils.getMaskedToken(splitToken[0]));
                            }
                            log.error("Invalid Api Key. Failed to decode the Api Key body.");
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
                        }
                    }
                    if (isJwtTokenExpired(payload)) {
                        if (isGatewayTokenCacheEnabled) {
                            getGatewayApiKeyCache().remove(tokenIdentifier);
                            getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                        }
                        log.error("Api Key is expired");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                    }
                    validateAPIKeyRestrictions(payload, synCtx);
                    if (isGatewayTokenCacheEnabled) {
                        JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
                        jwtTokenPayloadInfo.setPayload(payload);
                        jwtTokenPayloadInfo.setRawPayload(splitToken[1]);
                        getGatewayApiKeyDataCache().put(cacheKey, jwtTokenPayloadInfo);
                    }
                }
                net.minidev.json.JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload, splitToken, false);
                if (log.isDebugEnabled()) {
                    log.debug("Api Key authentication successful.");
                }

                String endUserToken = null;
                String contextHeader = null;
                if (jwtGenerationEnabled) {
                    SignedJWTInfo signedJWTInfo = new SignedJWTInfo(apiKey, signedJWT, payload);
                    JWTValidationInfo jwtValidationInfo = getJwtValidationInfo(signedJWTInfo);
                    JWTInfoDto jwtInfoDto = GatewayUtils.generateJWTInfoDto(api, jwtValidationInfo, null, synCtx);
                    endUserToken = generateAndRetrieveBackendJWTToken(tokenIdentifier, jwtInfoDto);
                    contextHeader = getContextHeader();
                }

                AuthenticationContext authenticationContext;
                authenticationContext = GatewayUtils
                        .generateAuthenticationContext(tokenIdentifier, payload, api, getApiLevelPolicy(), endUserToken, synCtx);
                APISecurityUtils.setAuthenticationContext(synCtx, authenticationContext, contextHeader);

                if (log.isDebugEnabled()) {
                    log.debug("User is authorized to access the resource using Api Key.");
                }
                return new AuthenticationResponse(true, isMandatory, false, 0, null);
            }

            if (log.isDebugEnabled()) {
                log.debug("Api Key signature verification failure. Api Key: " +
                        GatewayUtils.getMaskedToken(splitToken[0]));
            }
            log.error("Invalid Api Key. Signature verification failed.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);

        } catch (APISecurityException e) {
            return new AuthenticationResponse(false, isMandatory, true, e.getErrorCode(), e.getMessage());
        } catch (ParseException e) {
            log.error("Error while parsing API Key", e);
            return new AuthenticationResponse(false, isMandatory, true, APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        }
    }

    private void validateAPIKeyRestrictions(JWTClaimsSet payload, MessageContext synCtx) throws APISecurityException {
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).
                getAxis2MessageContext();

        String permittedIPList = null;
        if (payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_IP) != null) {
            permittedIPList = (String) payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_IP);
        }

        if (StringUtils.isNotEmpty(permittedIPList)) {
            // Validate client IP against permitted IPs
            String clientIP = GatewayUtils.getIp(axis2MessageContext);

            if (StringUtils.isNotEmpty(clientIP)) {
                for (String restrictedIP : permittedIPList.split(",")) {
                    if (APIUtil.isIpInNetwork(clientIP, restrictedIP.trim())) {
                        // Client IP is allowed
                        return;
                    }
                }
                if (log.isDebugEnabled()) {
                    String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
                    String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

                    if (StringUtils.isNotEmpty(clientIP)) {
                        log.debug("Invocations to API: " + apiContext + ":" + apiVersion +
                                " is not permitted for client with IP: " + clientIP);
                    }
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                        "Access forbidden for the invocations");
            }

        }

        String permittedRefererList = null;
        if (payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_REFERER) != null) {
            permittedRefererList = (String) payload.getClaim(APIConstants.JwtTokenConstants.PERMITTED_REFERER);
        }

        if (StringUtils.isNotEmpty(permittedRefererList)) {
            // Validate http referer against the permitted referrers
            Map<String, String> transportHeaderMap = (Map<String, String>)
                    axis2MessageContext.getProperty
                            (org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (transportHeaderMap != null) {
                String referer = transportHeaderMap.get("Referer");
                if (StringUtils.isNotEmpty(referer)) {
                    for (String restrictedReferer : permittedRefererList.split(",")) {
                        String restrictedRefererRegExp = restrictedReferer.trim()
                                .replace("*", "[^ ]*");
                        if (referer.matches(restrictedRefererRegExp)) {
                            // Referer is allowed
                            return;
                        }
                    }
                    if (log.isDebugEnabled()) {
                        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
                        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

                        if (StringUtils.isNotEmpty(referer)) {
                            log.debug("Invocations to API: " + apiContext + ":" + apiVersion +
                                    " is not permitted for referer: " + referer);
                        }
                    }
                    throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                            "Access forbidden for the invocations");
                } else {
                    throw new APISecurityException(APISecurityConstants.API_AUTH_FORBIDDEN,
                            "Access forbidden for the invocations");
                }
            }
        }
    }

    private String generateAndRetrieveBackendJWTToken(String tokenSignature, JWTInfoDto jwtInfoDto)
            throws APISecurityException {

        String endUserToken = null;
        boolean valid = false;
        String jwtTokenCacheKey =
                jwtInfoDto.getApicontext().concat(":").concat(jwtInfoDto.getVersion()).concat(":").concat(tokenSignature);
        if (isGatewayTokenCacheEnabled) {
            Object token = getGatewayApiKeyCache().get(jwtTokenCacheKey);
            if (token != null) {
                endUserToken = (String) token;
                String[] splitToken = ((String) token).split("\\.");
                JSONObject payload = new JSONObject(new String(Base64.getUrlDecoder().decode(splitToken[1])));
                long exp = payload.getLong("exp");
                long timestampSkew = OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds() * 1000;
                valid = (exp - System.currentTimeMillis() > timestampSkew);
            }
            if (StringUtils.isEmpty(endUserToken) || !valid) {
                try {
                    endUserToken = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
                    getGatewayApiKeyCache().put(jwtTokenCacheKey, endUserToken);
                } catch (APIManagementException e) {
                    log.error("Error while Generating Backend JWT", e);
                    throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
                }
            }
        } else {
            try {
                endUserToken = apiMgtGatewayJWTGenerator.generateToken(jwtInfoDto);
            } catch (APIManagementException e) {
                log.error("Error while Generating Backend JWT", e);
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
            }
        }
        return endUserToken;
    }

    private String extractApiKey(MessageContext mCtx) throws APISecurityException {

        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mCtx).getAxis2MessageContext();
        String apiKey;

        //check headers to get apikey
        Map headers = (Map) (axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS));
        if (headers != null) {
            apiKey = (String) headers.get(securityParam);
            if (apiKey != null) {
                //Remove apikey header from the request
                headers.remove(securityParam);
                return apiKey.trim();
            }
        }
        //check query params to get apikey
        try {
            apiKey = new SynapseXPath("$url:apikey").stringValueOf(mCtx);
            if (StringUtils.isNotBlank(apiKey)) {
                String rest_url_postfix = (String) axis2MC.getProperty(NhttpConstants.REST_URL_POSTFIX);
                rest_url_postfix = removeApiKeyFromQueryParameters(rest_url_postfix, URLEncoder.encode(apiKey));
                axis2MC.setProperty(NhttpConstants.REST_URL_POSTFIX, rest_url_postfix);
                return apiKey.trim();
            } else {
                if (log.isDebugEnabled()){
                    log.debug("Api Key Authentication failed: Header or Query parameter with the name '"
                            .concat(securityParam).concat("' was not found."));
                }
                throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                        APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
            }
        } catch (JaxenException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving apikey from the request query params.", e);
            }
            throw new APISecurityException(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    APISecurityConstants.API_AUTH_MISSING_CREDENTIALS_MESSAGE);
        }
    }

    private String removeApiKeyFromQueryParameters(String queryParam, String apiKey) {
        queryParam = queryParam.replace("?apikey=" + apiKey, "?");
        queryParam = queryParam.replace("&apikey=" + apiKey, "");
        queryParam = queryParam.replace("?&", "?");
        if (queryParam.lastIndexOf("?") == (queryParam.length() - 1)) {
            queryParam = queryParam.replace("?", "");
        }
        return queryParam;

    }

    /**
     * Check whether the jwt token is expired or not.
     *
     * @param payload The payload of the JWT token
     * @return returns true if the JWT token is expired
     */
    private static boolean isJwtTokenExpired(JWTClaimsSet payload) {

        int timestampSkew = (int) OAuthServerConfiguration.getInstance().getTimeStampSkewInSeconds();

        DefaultJWTClaimsVerifier jwtClaimsSetVerifier = new DefaultJWTClaimsVerifier();
        jwtClaimsSetVerifier.setMaxClockSkew(timestampSkew);
        try {
            jwtClaimsSetVerifier.verify(payload);
            if (log.isDebugEnabled()) {
                log.debug("Token is not expired. User: " + payload.getSubject());
            }
        } catch (BadJWTException e) {
            if ("Expired JWT".equals(e.getMessage())) {
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Token is not expired. User: " + payload.getSubject());
        }
        return false;
    }

    private JWTValidationInfo getJwtValidationInfo(SignedJWTInfo signedJWTInfo) {
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setClaims(signedJWTInfo.getJwtClaimsSet().getClaims());
        jwtValidationInfo.setUser(signedJWTInfo.getJwtClaimsSet().getSubject());
        return jwtValidationInfo;
    }

    private byte[] decode(String payload) throws IllegalArgumentException {
        return java.util.Base64.getUrlDecoder().decode(payload.getBytes(StandardCharsets.UTF_8));
    }

    protected void initParams () {
        APIManagerConfiguration apimConf = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        JWTConfigurationDto jwtConfigDto = apimConf.getJwtConfigurationDto();
        String header = jwtConfigDto.getJwtHeader();
        if (header != null) {
            setContextHeader(header);
        }
    }

    public String getContextHeader() {
        return contextHeader;
    }

    public void setContextHeader(String contextHeader) {
        this.contextHeader = contextHeader;
    }

    //first level cache
    private Cache getGatewayApiKeyCache() {
        return CacheProvider.getGatewayApiKeyCache();
    }

    private Cache getInvalidGatewayApiKeyCache() {
        return CacheProvider.getInvalidGatewayApiKeyCache();
    }

    //second level cache
    private Cache getGatewayApiKeyDataCache() {
        return CacheProvider.getGatewayApiKeyDataCache();
    }

    private String getApiLevelPolicy() {
        return apiLevelPolicy;
    }

    @Override
    public String getChallengeString() {
        return null;
    }

    @Override
    public String getRequestOrigin() {
        return null;
    }

    @Override
    public int getPriority() {
        return 30;
    }
}
