/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.oauth;

import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.handlers.security.Authenticator;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.JWTValidator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.metrics.manager.Level;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * An API consumer authenticator which authenticates user requests using
 * the OAuth protocol. This implementation uses some default token/delimiter
 * values to parse OAuth headers, but if needed these settings can be overridden
 * through the APIManagerConfiguration.
 */
public class OAuthAuthenticator implements Authenticator {

    private static final Log log = LogFactory.getLog(OAuthAuthenticator.class);
    private List<String> keyManagerList;

    protected APIKeyValidator keyValidator;
    protected JWTValidator jwtValidator;

    private String securityHeader = HttpHeaders.AUTHORIZATION;
    private String defaultAPIHeader="WSO2_AM_API_DEFAULT_VERSION";
    private String consumerKeyHeaderSegment = "Bearer";
    private String oauthHeaderSplitter = ",";
    private String consumerKeySegmentDelimiter = " ";
    private String securityContextHeader;
    private boolean removeOAuthHeadersFromOutMessage=true;
    private boolean removeDefaultAPIHeaderFromOutMessage=true;
    private String clientDomainHeader = "referer";
    private String requestOrigin;
    private String remainingAuthHeader;
    private boolean isMandatory;
    private String apiLevelPolicy;
    public OAuthAuthenticator() {
    }

    public OAuthAuthenticator(String authorizationHeader, boolean isMandatory, boolean removeOAuthHeader,
                              String apiLevelPolicy,List<String> keyManagerList) {
        this.securityHeader = authorizationHeader;
        this.removeOAuthHeadersFromOutMessage = removeOAuthHeader;
        this.isMandatory = isMandatory;
        this.apiLevelPolicy = apiLevelPolicy;
        this.keyManagerList = keyManagerList;
    }

    public void init(SynapseEnvironment env) {
        this.keyValidator = new APIKeyValidator(env.getSynapseConfiguration().getAxisConfiguration());
        this.jwtValidator = new JWTValidator(apiLevelPolicy, this.keyValidator);
        initOAuthParams();
    }

    public void destroy() {
        if (keyValidator != null) {
            this.keyValidator.cleanup();
        }
    }

    @MethodStats
    public AuthenticationResponse authenticate(MessageContext synCtx) {
        boolean isJwtToken = false;
        OpenAPI openAPI = null;
        String apiKey = null;
        boolean defaultVersionInvoked = false;
        TracingSpan getClientDomainSpan = null;
        TracingSpan authenticationSchemeSpan = null;
        TracingSpan keyInfo = null;
        Map headers = (Map) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            requestOrigin = (String) headers.get("Origin");

            apiKey = extractCustomerKeyFromAuthHeader(headers);
            if (log.isDebugEnabled()) {
                log.debug(apiKey != null ? "Received Token ".concat(apiKey) : "No valid Authorization header found");
            }
            //Check if client invoked the default version API (accessing API without version).
            defaultVersionInvoked = headers.containsKey(defaultAPIHeader);
        }


        if(log.isDebugEnabled()){
            log.debug("Default Version API invoked");
        }

        if(removeOAuthHeadersFromOutMessage){
            //Remove authorization headers sent for authentication at the gateway and pass others to the backend
            if (StringUtils.isNotBlank(remainingAuthHeader)) {
                if(log.isDebugEnabled()){
                    log.debug("Removing OAuth key from Authorization header");
                }
                headers.put(securityHeader, remainingAuthHeader);
                remainingAuthHeader = "";
            } else {
                if(log.isDebugEnabled()){
                    log.debug("Removing Authorization header from headers");
                }
                headers.remove(securityHeader);
            }

        }
        if(removeDefaultAPIHeaderFromOutMessage){
            headers.remove(defaultAPIHeader);
        }

        String apiContext = (String) synCtx.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpMethod = (String)((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        String matchingResource = (String) synCtx.getProperty(APIConstants.API_ELECTED_RESOURCE);
        SignedJWT signedJWT = null;

        if (Util.tracingEnabled()) {
            TracingSpan keySpan = (TracingSpan) synCtx.getProperty(APIMgtGatewayConstants.KEY_VALIDATION);
            TracingTracer tracer = Util.getGlobalTracer();
            getClientDomainSpan = Util.startSpan(APIMgtGatewayConstants.GET_CLIENT_DOMAIN, keySpan, tracer);
        }
        String clientDomain = getClientDomain(synCtx);
        if (Util.tracingEnabled()) {
            Util.finishSpan(getClientDomainSpan);
        }
        if(log.isDebugEnabled() && null != clientDomain) {
            log.debug("Received Client Domain ".concat(clientDomain));
        }
        //If the matching resource does not require authentication
        Timer timer = getTimer(MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "GET_RESOURCE_AUTH"));
        Timer.Context context = timer.start();
        org.apache.axis2.context.MessageContext axis2MessageCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        org.apache.axis2.context.MessageContext.setCurrentMessageContext(axis2MessageCtx);

        if (Util.tracingEnabled()) {
            TracingSpan keySpan = (TracingSpan) synCtx.getProperty(APIMgtGatewayConstants.KEY_VALIDATION);
            TracingTracer tracer = Util.getGlobalTracer();
            authenticationSchemeSpan =
                    Util.startSpan(APIMgtGatewayConstants.GET_RESOURCE_AUTHENTICATION_SCHEME, keySpan, tracer);
        }
        String authenticationScheme;
        try {
            //Initial guess of a JWT token using the presence of a DOT.
            if (StringUtils.isNotEmpty(apiKey) && apiKey.contains(APIConstants.DOT)) {
                try {
                    // Check if the header part is decoded
                    Base64.getUrlDecoder().decode(apiKey.split("\\.")[0]);
                    if (StringUtils.countMatches(apiKey, APIConstants.DOT) != 2) {
                        log.debug("Invalid JWT token. The expected token format is <header.payload.signature>");
                        throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                                "Invalid JWT token");
                    }
                    signedJWT = SignedJWT.parse(apiKey);
                    String keyManager =
                            ServiceReferenceHolder.getInstance().getJwtValidationService().getKeyManagerNameIfJwtValidatorExist(signedJWT);
                    if (StringUtils.isNotEmpty(keyManager)){
                        if (keyManagerList.contains(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS) ||
                                keyManagerList.contains(keyManager)) {
                            isJwtToken = true;
                        }else{
                            return new AuthenticationResponse(false, isMandatory, true,
                                    APISecurityConstants.API_INVALID_KEY_MANAGER,
                                    APISecurityConstants.API_KEY_MANAGER_NOT_AVAILABLE_MESSAGE);
                        }
                    }
                } catch ( ParseException  e) {
                    log.debug("Not a JWT token. Failed to decode the token header.", e);
                } catch (APIManagementException e) {
                    log.error("error while check validation of JWt", e);
                    return new AuthenticationResponse(false, isMandatory, true,
                            APISecurityConstants.API_AUTH_GENERAL_ERROR,APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
                }
            }
            // Find the resource authentication scheme based on the token type
            if (isJwtToken) {
                // If a JWT token
                openAPI = (OpenAPI) synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
                if (openAPI == null && !APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
                    log.error("Swagger is missing in the gateway. " +
                            "Therefore, JWT authentication cannot be performed.");
                    return new AuthenticationResponse(false, isMandatory, true,
                            APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF,
                            "JWT authentication cannot be performed.");
                }
                List<VerbInfoDTO> verbInfoList;

                if (APIConstants.GRAPHQL_API.equals(synCtx.getProperty(APIConstants.API_TYPE))) {
                    HashMap<String, Boolean> operationAuthSchemeMappingList =
                            (HashMap<String, Boolean>) synCtx.getProperty(APIConstants.OPERATION_AUTH_SCHEME_MAPPING);
                    HashMap<String, String> operationThrottlingMappingList =
                            (HashMap<String, String>) synCtx.getProperty(APIConstants.OPERATION_THROTTLING_MAPPING);

                    String[] operationList = matchingResource.split(",");
                    verbInfoList = new ArrayList<>(1);
                    authenticationScheme = APIConstants.AUTH_NO_AUTHENTICATION;
                    for (String operation: operationList) {
                        boolean operationAuthSchemeEnabled = operationAuthSchemeMappingList.get(operation);
                        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
                        if (operationAuthSchemeEnabled) {
                            verbInfoDTO.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
                            authenticationScheme = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                        } else {
                            verbInfoDTO.setAuthType(APIConstants.AUTH_NO_AUTHENTICATION);
                        }
                        verbInfoDTO.setHttpVerb(httpMethod);
                        verbInfoDTO.setThrottling(operationThrottlingMappingList.get(operation));
                        verbInfoDTO.setRequestKey(apiContext + "/" + apiVersion + operation + ":" + httpMethod);
                        verbInfoList.add(verbInfoDTO);
                    }
                } else {
                    authenticationScheme = OpenAPIUtils.getResourceAuthenticationScheme(openAPI, synCtx);
                    verbInfoList = new ArrayList<>(1);
                    VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
                    verbInfoDTO.setHttpVerb(httpMethod);
                    verbInfoDTO.setAuthType(authenticationScheme);
                    verbInfoDTO.setThrottling(OpenAPIUtils.getResourceThrottlingTier(openAPI, synCtx));
                    verbInfoDTO.setContentAware(OpenAPIUtils.isContentAwareTierAvailable(openAPI, synCtx));
                    verbInfoDTO.setRequestKey(apiContext + "/" + apiVersion + matchingResource + ":" + httpMethod);
                    verbInfoList.add(verbInfoDTO);
                }

                synCtx.setProperty(APIConstants.VERB_INFO_DTO, verbInfoList);
            } else {
                // If an OAuth token
                authenticationScheme = getAPIKeyValidator().getResourceAuthenticationScheme(synCtx);
            }
        } catch (APISecurityException ex) {
            return new AuthenticationResponse(false, isMandatory, true, ex.getErrorCode(), ex.getMessage());
        }
        if (Util.tracingEnabled()) {
            Util.finishSpan(authenticationSchemeSpan);
        }
        context.stop();
        APIKeyValidationInfoDTO info;
        if(APIConstants.AUTH_NO_AUTHENTICATION.equals(authenticationScheme)){

            if(log.isDebugEnabled()){
                log.debug("Found Authentication Scheme: ".concat(authenticationScheme));
            }

            //using existing constant in Message context removing the additinal constant in API Constants
            String clientIP = null;
            org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) synCtx).
                    getAxis2MessageContext();
            TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>)
                                                         axis2MessageContext.getProperty
                                                                 (org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            if (transportHeaderMap != null) {
                clientIP = transportHeaderMap.get(APIMgtGatewayConstants.X_FORWARDED_FOR);
            }

            //Setting IP of the client
            if (clientIP != null && !clientIP.isEmpty()) {
                if (clientIP.indexOf(",") > 0) {
                    clientIP = clientIP.substring(0, clientIP.indexOf(","));
                }
            } else {
                clientIP = (String) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
            }

            //Create a dummy AuthenticationContext object with hard coded values for
            // Tier and KeyType. This is because we cannot determine the Tier nor Key
            // Type without subscription information..
            AuthenticationContext authContext = new AuthenticationContext();
            authContext.setAuthenticated(true);
            authContext.setTier(APIConstants.UNAUTHENTICATED_TIER);
            authContext.setStopOnQuotaReach(true);//Since we don't have details on unauthenticated tier we setting stop on quota reach true
            //Requests are throttled by the ApiKey that is set here. In an unauthenticated scenario,
            //we will use the client's IP address for throttling.
            authContext.setApiKey(clientIP);
            authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
            //This name is hardcoded as anonymous because there is no associated user token
            authContext.setUsername(APIConstants.END_USER_ANONYMOUS);
            authContext.setCallerToken(null);
            authContext.setApplicationName(null);
            authContext.setApplicationId(clientIP); //Set clientIp as application ID in unauthenticated scenario
            authContext.setConsumerKey(null);
            APISecurityUtils.setAuthenticationContext(synCtx, authContext, securityContextHeader);
            return new AuthenticationResponse(true, isMandatory, false, 0, null);
        } else if (APIConstants.NO_MATCHING_AUTH_SCHEME.equals(authenticationScheme)) {
            info = new APIKeyValidationInfoDTO();
            info.setAuthorized(false);
            info.setValidationStatus(900906);
        } else if (apiKey == null || apiContext == null || apiVersion == null) {
            if(log.isDebugEnabled()){
                if(apiKey == null){
                    log.debug("OAuth headers not found");
                }
                else if(apiContext == null){
                    log.debug("Couldn't find API Context");
                }
                else if(apiVersion == null){
                    log.debug("Could not find api version");
                }
            }
            return new AuthenticationResponse(false, isMandatory, true,
                    APISecurityConstants.API_AUTH_MISSING_CREDENTIALS, "Required OAuth credentials not provided");
        } else {
            //Start JWT token validation
            if (isJwtToken) {
                try {
                    AuthenticationContext authenticationContext = jwtValidator.authenticate(signedJWT, synCtx, openAPI);
                    APISecurityUtils.setAuthenticationContext(synCtx, authenticationContext, securityContextHeader);
                    log.debug("User is authorized using JWT token to access the resource.");
                    return new AuthenticationResponse(true, isMandatory, false, 0, null);

                } catch (APISecurityException ex) {
                    return new AuthenticationResponse(false, isMandatory, true,
                            ex.getErrorCode(), ex.getMessage());
                }
            }

            if(log.isDebugEnabled()){
                log.debug("Matching resource is: ".concat(matchingResource));
            }

            timer = getTimer(MetricManager.name(
                    APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), "GET_KEY_VALIDATION_INFO"));
            context = timer.start();

            if (Util.tracingEnabled()) {
                TracingSpan keySpan = (TracingSpan) synCtx.getProperty(APIMgtGatewayConstants.KEY_VALIDATION);
                TracingTracer tracer = Util.getGlobalTracer();
                keyInfo = Util.startSpan(APIMgtGatewayConstants.GET_KEY_VALIDATION_INFO, keySpan, tracer);
            }
            try {
                info = getAPIKeyValidator().getKeyValidationInfo(apiContext, apiKey, apiVersion, authenticationScheme, clientDomain,
                        matchingResource, httpMethod, defaultVersionInvoked,keyManagerList);
            } catch (APISecurityException ex) {
                return new AuthenticationResponse(false, isMandatory, true, ex.getErrorCode(), ex.getMessage());
            }
            if (Util.tracingEnabled()) {
                Util.finishSpan(keyInfo);
            }
            context.stop();
            synCtx.setProperty(APIMgtGatewayConstants.APPLICATION_NAME, info.getApplicationName());
            synCtx.setProperty(APIMgtGatewayConstants.END_USER_NAME, info.getEndUserName());
            synCtx.setProperty(APIMgtGatewayConstants.SCOPES, info.getScopes() == null ? null : info.getScopes()
                                                                                                    .toString());
        }

        if (info.isAuthorized()) {
            AuthenticationContext authContext = new AuthenticationContext();
            authContext.setAuthenticated(true);
            authContext.setTier(info.getTier());
            authContext.setApiKey(apiKey);
            authContext.setKeyType(info.getType());
            if (info.getEndUserName() != null) {
                authContext.setUsername(info.getEndUserName());
            } else {
                authContext.setUsername(APIConstants.END_USER_ANONYMOUS);
            }
            authContext.setCallerToken(info.getEndUserToken());
            authContext.setApplicationId(info.getApplicationId());
            authContext.setApplicationName(info.getApplicationName());
            authContext.setApplicationTier(info.getApplicationTier());
            authContext.setSubscriber(info.getSubscriber());
            authContext.setConsumerKey(info.getConsumerKey());
            authContext.setApiTier(info.getApiTier());
            authContext.setThrottlingDataList(info.getThrottlingDataList());
            authContext.setSubscriberTenantDomain(info.getSubscriberTenantDomain());
            authContext.setSpikeArrestLimit(info.getSpikeArrestLimit());
            authContext.setSpikeArrestUnit(info.getSpikeArrestUnit());
            authContext.setStopOnQuotaReach(info.isStopOnQuotaReach());
            authContext.setIsContentAware(info.isContentAware());
            APISecurityUtils.setAuthenticationContext(synCtx, authContext, securityContextHeader);
            if (info.getProductName() != null && info.getProductProvider() != null) {
                authContext.setProductName(info.getProductName());
                authContext.setProductProvider(info.getProductProvider());
            }

            /* Synapse properties required for BAM Mediator*/
            //String tenantDomain = MultitenantUtils.getTenantDomain(info.getApiPublisher());
            synCtx.setProperty("api.ut.apiPublisher", info.getApiPublisher());
            synCtx.setProperty("API_NAME", info.getApiName());

            if(log.isDebugEnabled()){
                log.debug("User is authorized to access the Resource");
            }
            return new AuthenticationResponse(true, isMandatory, false, 0, null);
        } else {
            if(log.isDebugEnabled()){
                log.debug("User is NOT authorized to access the Resource");
            }
            return new AuthenticationResponse(false, isMandatory, true, info.getValidationStatus(),
                    "Access failure for API: " + apiContext +
                    ", version: "+ apiVersion + " status: (" + info.getValidationStatus() +
                    ") - " + APISecurityConstants.getAuthenticationFailureMessage(info.getValidationStatus()));
        }
    }

    /**
     * Extracts the customer API key from the OAuth Authentication header. If the required
     * security header is present in the provided map, it will be removed from the map
     * after processing.
     *
     * @param headersMap Map of HTTP headers
     * @return extracted customer key value or null if the required header is not present
     */
    public String extractCustomerKeyFromAuthHeader(Map headersMap) {

        //From 1.0.7 version of this component onwards remove the OAuth authorization header from
        // the message is configurable. So we dont need to remove headers at this point.
        String authHeader = (String) headersMap.get(securityHeader);
        if (authHeader == null) {
            if (log.isDebugEnabled()) {
                log.debug("OAuth2 Authentication: Expected authorization header with the name '"
                        .concat(securityHeader).concat("' was not found."));
            }
            return null;
        }

        ArrayList<String> remainingAuthHeaders = new ArrayList<>();
        String consumerKey = null;
        boolean consumerkeyFound = false;
        String[] headers = authHeader.split(oauthHeaderSplitter);
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                String[] elements = headers[i].split(consumerKeySegmentDelimiter);
                if (elements != null && elements.length > 1) {
                    int j = 0;
                    boolean isConsumerKeyHeaderAvailable = false;
                    for (String element : elements) {
                        if (!"".equals(element.trim())) {
                            if (consumerKeyHeaderSegment.equals(elements[j].trim())) {
                                isConsumerKeyHeaderAvailable = true;
                            } else if (isConsumerKeyHeaderAvailable) {
                                consumerKey = removeLeadingAndTrailing(elements[j].trim());
                                consumerkeyFound = true;
                            }
                        }
                        j++;
                    }
                }
                if (!consumerkeyFound) {
                    remainingAuthHeaders.add(headers[i]);
                } else {
                    consumerkeyFound = false;
                }
            }
        }
        remainingAuthHeader = String.join(oauthHeaderSplitter, remainingAuthHeaders);
        return consumerKey;
    }

    private String removeLeadingAndTrailing(String base) {
        String result = base;

        if (base.startsWith("\"") || base.endsWith("\"")) {
            result = base.replace("\"", "");
        }
        return result.trim();
    }

    protected void initOAuthParams() {
        APIManagerConfiguration config = getApiManagerConfiguration();
        String value = config.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE);
        if (value != null) {
            removeOAuthHeadersFromOutMessage = Boolean.parseBoolean(value);
        }
        JWTConfigurationDto jwtConfigurationDto = config.getJwtConfigurationDto();
        value = jwtConfigurationDto.getJwtHeader();
        if (value != null) {
            setSecurityContextHeader(value);
        }
    }

    protected APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    public String getChallengeString() {
        return "OAuth2 realm=\"WSO2 API Manager\"";
    }

    private String getClientDomain(MessageContext synCtx) {
        String clientDomainHeaderValue = null;
        Map headers = (Map) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers != null) {
            clientDomainHeaderValue = (String) headers.get(clientDomainHeader);
        }
        return clientDomainHeaderValue;
    }

	public String getRequestOrigin() {
		return requestOrigin;
	}

    public String getSecurityHeader() {
        return securityHeader;
    }

    public void setSecurityHeader(String securityHeader) {
        this.securityHeader = securityHeader;
    }

    public String getDefaultAPIHeader() {
        return defaultAPIHeader;
    }

    public void setDefaultAPIHeader(String defaultAPIHeader) {
        this.defaultAPIHeader = defaultAPIHeader;
    }

    public String getConsumerKeyHeaderSegment() {
        return consumerKeyHeaderSegment;
    }

    public void setConsumerKeyHeaderSegment(String consumerKeyHeaderSegment) {
        this.consumerKeyHeaderSegment = consumerKeyHeaderSegment;
    }

    public String getOauthHeaderSplitter() {
        return oauthHeaderSplitter;
    }

    public void setOauthHeaderSplitter(String oauthHeaderSplitter) {
        this.oauthHeaderSplitter = oauthHeaderSplitter;
    }

    public String getConsumerKeySegmentDelimiter() {
        return consumerKeySegmentDelimiter;
    }

    public void setConsumerKeySegmentDelimiter(String consumerKeySegmentDelimiter) {
        this.consumerKeySegmentDelimiter = consumerKeySegmentDelimiter;
    }

    public String getSecurityContextHeader() {
        return securityContextHeader;
    }

    public void setSecurityContextHeader(String securityContextHeader) {
        this.securityContextHeader = securityContextHeader;
    }

    public boolean isRemoveOAuthHeadersFromOutMessage() {
        return removeOAuthHeadersFromOutMessage;
    }

    public void setRemoveOAuthHeadersFromOutMessage(boolean removeOAuthHeadersFromOutMessage) {
        this.removeOAuthHeadersFromOutMessage = removeOAuthHeadersFromOutMessage;
    }

    public String getClientDomainHeader() {
        return clientDomainHeader;
    }

    public void setClientDomainHeader(String clientDomainHeader) {
        this.clientDomainHeader = clientDomainHeader;
    }

    public boolean isRemoveDefaultAPIHeaderFromOutMessage() {
        return removeDefaultAPIHeaderFromOutMessage;
    }

    public void setRemoveDefaultAPIHeaderFromOutMessage(boolean removeDefaultAPIHeaderFromOutMessage) {
        this.removeDefaultAPIHeaderFromOutMessage = removeDefaultAPIHeaderFromOutMessage;
    }

    public void setRequestOrigin(String requestOrigin) {
        this.requestOrigin = requestOrigin;
    }

    protected Timer getTimer(String name) {
        return MetricManager.timer(Level.INFO, name);
    }
    protected APIKeyValidator getAPIKeyValidator() {
        return this.keyValidator;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
