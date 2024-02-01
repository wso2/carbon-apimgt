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

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.ApiKeyAuthenticatorUtils;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiKeyAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(ApiKeyAuthenticator.class);

    private String securityParam;
    private String apiLevelPolicy;
    private boolean isMandatory;

    public ApiKeyAuthenticator(String authorizationHeader, String apiLevelPolicy, boolean isApiKeyMandatory) {
        this.securityParam = authorizationHeader;
        this.apiLevelPolicy = apiLevelPolicy;
        this.isMandatory = isApiKeyMandatory;
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

        log.debug("ApiKey Authentication initialized");
        try {
            // Extract apikey from the request while removing it from the msg context.
            String apiKey = extractApiKey(synCtx);
            String[] splitToken = apiKey.split("\\.");
            ApiKeyAuthenticatorUtils.validateAPIKeyFormat(splitToken);
            SignedJWT signedJWT = (SignedJWT) JWTParser.parse(apiKey);
            JWSHeader decodedHeader = signedJWT.getHeader();
            JWTClaimsSet payload = signedJWT.getJWTClaimsSet();

            if (!ApiKeyAuthenticatorUtils.isAPIKey(splitToken, decodedHeader, payload)) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                        APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            }

            String certAlias = decodedHeader.getKeyID();
            String tokenIdentifier = payload.getJWTID();
            String tenantDomain = GatewayUtils.getTenantDomain();
            String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
            String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
            String httpMethod = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
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
            boolean isGatewayTokenCacheEnabled = GatewayUtils.isGatewayTokenCacheEnabled();
            boolean isVerified = ApiKeyAuthenticatorUtils.verifyAPIKeySignatureFromTokenCache(isGatewayTokenCacheEnabled,
                    tokenIdentifier, cacheKey, apiKey, splitToken[0]);
            if (!isVerified) {
                // Not found in cache or caching disabled
                isVerified = ApiKeyAuthenticatorUtils.verifyAPIKeySignature(signedJWT, certAlias);
            }
            ApiKeyAuthenticatorUtils.addTokenToTokenCache(isGatewayTokenCacheEnabled, tokenIdentifier, isVerified,
                    tenantDomain);
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).
                    getAxis2MessageContext();
            Map<String, String> transportHeaderMap = (Map<String, String>)
                    axis2MessageContext.getProperty
                            (org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            String referer = null;
            if (transportHeaderMap != null) {
                referer = transportHeaderMap.get(APIMgtGatewayConstants.REFERER);
            }

            // If Api Key signature is verified
            if (isVerified) {
                ExtendedJWTConfigurationDto jwtConfigurationDto = ServiceReferenceHolder.getInstance().
                        getAPIManagerConfiguration().getJwtConfigurationDto();
                boolean jwtGenerationEnabled = false;
                if (jwtConfigurationDto != null) {
                    jwtGenerationEnabled = jwtConfigurationDto.isEnabled();
                }
                ApiKeyAuthenticatorUtils.overridePayloadFromDataCache(isGatewayTokenCacheEnabled, cacheKey, payload);
                ApiKeyAuthenticatorUtils.checkTokenExpired(isGatewayTokenCacheEnabled, cacheKey, tokenIdentifier, apiKey,
                        tenantDomain, payload);
                ApiKeyAuthenticatorUtils.validateAPIKeyRestrictions(payload, GatewayUtils.getIp(axis2MessageContext),
                        apiContext, apiVersion, referer);
                net.minidev.json.JSONObject api = GatewayUtils.validateAPISubscription(apiContext, apiVersion, payload,
                        splitToken[0]);
                String endUserToken = ApiKeyAuthenticatorUtils.getEndUserToken(api, jwtConfigurationDto, apiKey,
                        signedJWT, payload, tokenIdentifier, isGatewayTokenCacheEnabled);
                AuthenticationContext authenticationContext = GatewayUtils.generateAuthenticationContext(tokenIdentifier,
                        payload, api, apiLevelPolicy, endUserToken, synCtx);
                APISecurityUtils.setAuthenticationContext(synCtx, authenticationContext,
                        jwtGenerationEnabled ? getContextHeader() : null);
                synCtx.setProperty(APIMgtGatewayConstants.END_USER_NAME, authenticationContext.getUsername());
                log.debug("User is authorized to access the resource using Api Key.");
                return new AuthenticationResponse(true, isMandatory, false,
                        0, null);
            }
            if (log.isDebugEnabled()) {
                log.debug("Api Key signature verification failure. Api Key: " +
                        GatewayUtils.getMaskedToken(splitToken[0]));
            }
            log.error("Invalid Api Key. Signature verification failed.");
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
        } catch (APISecurityException e) {
            return new AuthenticationResponse(false, isMandatory, true,
                    e.getErrorCode(), e.getMessage());
        } catch (ParseException e) {
            log.error("Error while parsing API Key", e);
            return new AuthenticationResponse(false, isMandatory, true,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        } catch (APIManagementException e) {
            log.error("Error while setting public cert/private key for backend jwt generation", e);
            return new AuthenticationResponse(false, isMandatory, true,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR, APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        }
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
                if (log.isDebugEnabled()) {
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

    private String getContextHeader() {
        APIManagerConfiguration apimConf = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        JWTConfigurationDto jwtConfigDto = apimConf.getJwtConfigurationDto();
        return jwtConfigDto.getJwtHeader();
    }

    @Override
    public String getChallengeString() {
        return "API Key realm=\"WSO2 API Manager\"";
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
