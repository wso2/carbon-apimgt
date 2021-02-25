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

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
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
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.dto.JWTTokenPayloadInfo;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.cache.Cache;

/**
 * This class used to authenticate InternalKey
 */
public class InternalAPIKeyAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(InternalAPIKeyAuthenticator.class);

    private String securityParam;

    public InternalAPIKeyAuthenticator(String securityParam) {
        this.securityParam = securityParam;
    }

    @Override
    public void init(SynapseEnvironment env) {
        // Nothing to do in init phase.
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
                String apiKey = extractApiKey(synCtx);
                if (StringUtils.isEmpty(apiKey)){
                    return new AuthenticationResponse(false, false,
                            true, APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }
                JWTTokenPayloadInfo payloadInfo = null;

                String[] splitToken = apiKey.split("\\.");
                JWTClaimsSet payload;
                SignedJWT signedJWT;
                String tokenIdentifier;
                if (splitToken.length != 3) {
                    log.error("Test Key does not have the format {header}.{payload}.{signature} ");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }
                try {
                    signedJWT = SignedJWT.parse(apiKey);
                    payload = signedJWT.getJWTClaimsSet();
                    tokenIdentifier = payload.getJWTID();
                } catch (IllegalArgumentException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalid Api Key. Api Key: " + GatewayUtils.getMaskedToken(splitToken[0]), e);
                    }
                    log.error("Invalid JWT token. Failed to decode the Api Key header.");
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
                }
                // Check if the decoded header contains type as 'InternalKey'.
                if (!GatewayUtils.isInternalKey(payload)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalid Internal Key token type. Internal API Key: " + GatewayUtils.getMaskedToken(splitToken[0]));
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

                OpenAPI openAPI = (OpenAPI) synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
                if (openAPI == null && !APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
                    log.error("Swagger is missing in the gateway. " +
                            "Therefore, Api Key authentication cannot be performed.");
                    return new AuthenticationResponse(false, true, true,
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

                String cacheToken = (String) getGatewayApiKeyCache().get(tokenIdentifier);
                if (cacheToken != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key retrieved from the Api Key cache.");
                    }
                    if (getGatewayApiKeyDataCache().get(cacheKey) != null) {
                        // Token is found in the key cache
                        payloadInfo = (JWTTokenPayloadInfo) getGatewayApiKeyDataCache().get(cacheKey);
                        String rawPayload = payloadInfo.getRawPayload();
                        isVerified = rawPayload.equals(splitToken[1]);
                    }
                } else if (getInvalidGatewayApiKeyCache().get(tokenIdentifier) != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key retrieved from the invalid Api Key cache. Internal Key: " +
                                GatewayUtils.getMaskedToken(splitToken[0]));
                    }
                    log.error("Invalid Internal Key." + GatewayUtils.getMaskedToken(splitToken[0]));
                    throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                            APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                }

                // Not found in cache or caching disabled
                if (!isVerified) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key not found in the cache.");
                    }
                    try {
                        isVerified = GatewayUtils.verifyTokenSignature(signedJWT,APIUtil.getInternalApiKeyAlias());
                    } catch (APISecurityException e) {
                        if (e.getErrorCode() == APISecurityConstants.API_AUTH_INVALID_CREDENTIALS) {
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                        } else {
                            throw e;
                        }
                    }
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

                // If Internal Key signature is verified
                if (isVerified) {
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key signature is verified.");
                    }
                    if (payloadInfo != null) {
                        // Api Key is found in the key cache
                        payload = payloadInfo.getPayload();
                        if (isJwtTokenExpired(payload)) {
                            getGatewayApiKeyCache().remove(tokenIdentifier);
                            getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                            log.error("Api Key is expired");
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                        }
                    } else {
                        // Retrieve payload from ApiKey
                        if (log.isDebugEnabled()) {
                            log.debug("ApiKey payload not found in the cache.");
                        }
                        if (isJwtTokenExpired(payload)) {
                            getGatewayApiKeyCache().remove(tokenIdentifier);
                            getInvalidGatewayApiKeyCache().put(tokenIdentifier, tenantDomain);
                            log.error("Api Key is expired");
                            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
                        }
                        JWTTokenPayloadInfo jwtTokenPayloadInfo = new JWTTokenPayloadInfo();
                        jwtTokenPayloadInfo.setPayload(payload);
                        jwtTokenPayloadInfo.setRawPayload(splitToken[1]);
                        getGatewayApiKeyDataCache().put(cacheKey, jwtTokenPayloadInfo);
                    }
                    JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion,
                            payload, splitToken, false);
                    if (log.isDebugEnabled()) {
                        log.debug("Api Key authentication successful.");
                    }


                    AuthenticationContext authenticationContext = GatewayUtils
                            .generateAuthenticationContext(tokenIdentifier, payload, api, retrievedApi.getStatus());
                    APISecurityUtils.setAuthenticationContext(synCtx, authenticationContext);
                    if (log.isDebugEnabled()) {
                        log.debug("User is authorized to access the resource using Api Key.");
                    }
                    return new AuthenticationResponse(true, true, false, 0, null);
                }

                if (log.isDebugEnabled()) {
                    log.debug("Api Key signature verification failure. Api Key: " +
                            GatewayUtils.getMaskedToken(splitToken[0]));
                }
                log.error("Invalid Api Key. Signature verification failed.");
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);

            } catch (APISecurityException e) {
                return new AuthenticationResponse(false, true, false, e.getErrorCode(), e.getMessage());
            } catch (ParseException e) {
                log.error("Error while parsing API Key", e);
                return new AuthenticationResponse(false, true, true, APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
            }
        }
        return new AuthenticationResponse(false, true, false, APISecurityConstants.API_AUTH_GENERAL_ERROR,
                APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
    }

    private String extractApiKey(MessageContext mCtx) {

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
        return null;
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

        return -10;
    }
}
