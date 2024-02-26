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

package org.wso2.carbon.apimgt.gateway.handlers.security;

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.common.gateway.dto.ExtensionType;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.ext.listener.ExtensionListenerUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.apikey.ApiKeyAuthenticator;
import org.wso2.carbon.apimgt.gateway.handlers.security.authenticator.MutualSSLAuthenticator;
import org.wso2.carbon.apimgt.gateway.handlers.security.authenticator.InternalAPIKeyAuthenticator;
import org.wso2.carbon.apimgt.gateway.handlers.security.basicauth.BasicAuthAuthenticator;
import org.wso2.carbon.apimgt.gateway.handlers.security.oauth.OAuthAuthenticator;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetrySpan;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryTracer;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryUtil;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authentication handler for REST APIs exposed in the API gateway. This handler will
 * drop the requests if an authentication failure occurs. But before a message is dropped
 * it looks for a special custom error handler sequence APISecurityConstants.API_AUTH_FAILURE_HANDLER
 * through which the message will be mediated when available. This is a custom extension point
 * provided to the users to handle authentication failures in a deployment specific manner.
 * Once the custom error handler has been invoked, this implementation will further try to
 * respond to the client with a 401 Unauthorized response. If this is not required, the users
 * must drop the message in their custom error handler itself.
 * <p/>
 * If no authentication errors are encountered, this will add some AuthenticationContext
 * information to the request and let it through to the next handler in the chain.
 */
public class APIAuthenticationHandler extends AbstractHandler implements ManagedLifecycle {
    private static final Log log = LogFactory.getLog(APIAuthenticationHandler.class);

    protected ArrayList<Authenticator> authenticators = new ArrayList<>();
    protected boolean isAuthenticatorsInitialized = false;
    private SynapseEnvironment synapseEnvironment;

    private String authorizationHeader;
    private String apiKeyHeader;
    private String apiSecurity;
    private String apiLevelPolicy;
    private String certificateInformation;
    private String apiUUID;
    private String apiType = String.valueOf(APIConstants.ApiTypes.API); // Default API Type
    private OpenAPI openAPI;
    private String keyManagers;
    private final String type = ExtensionType.AUTHENTICATION.toString();
    private String securityContextHeader;
    protected APIKeyValidator keyValidator;
    protected boolean isOauthParamsInitialized = false;

    public String getApiUUID() {
        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {
        this.apiUUID = apiUUID;
    }

    /**
     * To get the certificates uploaded against particular API.
     *
     * @return the certificates uploaded against particular API.
     */
    public String getCertificateInformation() {
        return certificateInformation;
    }

    /**
     * To set the certificates uploaded against particular API.
     *
     * @param certificateInformation the certificates uplaoded against the API.
     */
    public void setCertificateInformation(String certificateInformation) {
        this.certificateInformation = certificateInformation;
    }

    /**
     * To get the API level tier policy.
     *
     * @return Relevant tier policy related with API level policy.
     */
    public String getAPILevelPolicy() {
        return apiLevelPolicy;
    }

    /**
     * To set the API level tier policy.
     *
     * @param apiLevelPolicy Relevant API level tier policy related with this API.
     */
    public void setAPILevelPolicy(String apiLevelPolicy) {
        this.apiLevelPolicy = apiLevelPolicy;
    }

    /**
     * Get type of the API
     * @return API Type
     */
    public String getApiType() {
        return apiType;
    }

    /**
     * Set type of the API
     * @param apiType API Type
     */
    public void setApiType(String apiType) {
        // Since we currently support only Product APIs as the alternative, set the value to "PRODUCT_API" only if
        // the same value is provided as the type. Else the default value will remain as "API".
        if (APIConstants.ApiTypes.PRODUCT_API.name().equalsIgnoreCase(apiType)) {
            this.apiType = apiType;
        }
    }

    private boolean removeOAuthHeadersFromOutMessage = true;

    public void init(SynapseEnvironment synapseEnvironment) {
        this.synapseEnvironment = synapseEnvironment;
        if (log.isDebugEnabled()) {
            log.debug("Initializing API authentication handler instance");
        }
        initializeAuthenticators();
        if (getApiManagerConfigurationService() != null) {
            initOAuthParams();
        }
    }

    /**
     * To get the Authorization Header.
     *
     * @return Relevant the Authorization Header of the API request
     */
    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    /**
     * To set the Authorization Header.
     *
     * @param authorizationHeader the Authorization Header of the API request.
     */
    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    /**
     * To get the Api Key Header.
     *
     * @return Relevant the Api Key Header of the API request
     */
    public String getApiKeyHeader() {
        return apiKeyHeader;
    }

    /**
     * To set the Api Key Header.
     *
     * @param apiKeyHeader the Api Key Header of the API request.
     */
    public void setApiKeyHeader(String apiKeyHeader) {
        this.apiKeyHeader = apiKeyHeader;
    }

    /**
     * To get the API level security expected for the current API in gateway level.
     *
     * @return API level security related with the current API.
     */
    public String getAPISecurity() {
        return apiSecurity;
    }

    /**
     * To set the API level security of current API.
     *
     * @param apiSecurity Relevant API level security.
     */
    public void setAPISecurity(String apiSecurity) {
        this.apiSecurity = apiSecurity;
    }

    public boolean getRemoveOAuthHeadersFromOutMessage() {
        return removeOAuthHeadersFromOutMessage;
    }

    public void setRemoveOAuthHeadersFromOutMessage(boolean removeOAuthHeadersFromOutMessage) {
        this.removeOAuthHeadersFromOutMessage = removeOAuthHeadersFromOutMessage;
    }

    protected APIManagerConfigurationService getApiManagerConfigurationService() {
        return ServiceReferenceHolder.getInstance().getApiManagerConfigurationService();
    }

    public void destroy() {
        if (keyValidator != null) {
            this.keyValidator.cleanup();
        }
        if (!authenticators.isEmpty()) {
            for (Authenticator authenticator : authenticators) {
                authenticator.destroy();
            }
        } else {
            log.warn("Unable to destroy uninitialized authentication handler instance");
        }
    }

    protected void initOAuthParams() {
        setKeyValidator();
        APIManagerConfiguration config = getApiManagerConfiguration();
        String value = config.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE);
        if (value != null) {
            removeOAuthHeadersFromOutMessage = Boolean.parseBoolean(value);
        }
        JWTConfigurationDto jwtConfigurationDto = config.getJwtConfigurationDto();
        if (jwtConfigurationDto != null) {
            value = jwtConfigurationDto.getJwtHeader();
        }
        if (value != null) {
            setSecurityContextHeader(value);
        }
        isOauthParamsInitialized = true;
    }

    public void setSecurityContextHeader(String securityContextHeader) {
        this.securityContextHeader = securityContextHeader;
    }

    public String getSecurityContextHeader() {
        return securityContextHeader;
    }

    protected APIManagerConfiguration getApiManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
    }

    protected APIKeyValidator getAPIKeyValidator() {
        return this.keyValidator;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "LEST_LOST_EXCEPTION_STACK_TRACE", justification = "The exception needs to thrown for fault sequence invocation")
    protected void initializeAuthenticators() {
        isAuthenticatorsInitialized = true;

        boolean isOAuthProtected = false;
        boolean isMutualSSLProtected = false;
        boolean isBasicAuthProtected = false;
        boolean isApiKeyProtected = false;
        boolean isMutualSSLMandatory = false;
        boolean isOAuthBasicAuthMandatory = false;

        // Set security conditions
        if (apiSecurity == null) {
            isOAuthProtected = true;
        } else {
            String[] apiSecurityLevels = apiSecurity.split(",");
            for (String apiSecurityLevel : apiSecurityLevels) {
                if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.DEFAULT_API_SECURITY_OAUTH2)) {
                    isOAuthProtected = true;
                } else if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                    isMutualSSLProtected = true;
                } else if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_BASIC_AUTH)) {
                    isBasicAuthProtected = true;
                } else if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_MUTUAL_SSL_MANDATORY)) {
                    isMutualSSLMandatory = true;
                } else if (apiSecurityLevel.trim().equalsIgnoreCase(APIConstants.API_SECURITY_OAUTH_BASIC_AUTH_API_KEY_MANDATORY)) {
                    isOAuthBasicAuthMandatory = true;
                } else if (apiSecurityLevel.trim().equalsIgnoreCase((APIConstants.API_SECURITY_API_KEY))) {
                    isApiKeyProtected = true;
                }
            }
        }
        if (!isMutualSSLProtected && !isOAuthBasicAuthMandatory) {
            isOAuthBasicAuthMandatory = true;
        }
        if (!isBasicAuthProtected && !isOAuthProtected && !isMutualSSLMandatory && !isApiKeyProtected) {
            isMutualSSLMandatory = true;
        }

        // Set authenticators
        if (isMutualSSLProtected) {
            Authenticator  authenticator = new MutualSSLAuthenticator(apiLevelPolicy, isMutualSSLMandatory, certificateInformation);
            authenticator.init(synapseEnvironment);
            authenticators.add(authenticator);
        }
        if (isOAuthProtected) {
            Authenticator authenticator = new OAuthAuthenticator(authorizationHeader, isOAuthBasicAuthMandatory,
                    removeOAuthHeadersFromOutMessage);
            authenticator.init(synapseEnvironment);
            authenticators.add(authenticator);
        }
        if (isBasicAuthProtected) {
            Authenticator authenticator = new BasicAuthAuthenticator(authorizationHeader, isOAuthBasicAuthMandatory,
                    apiLevelPolicy);
            authenticator.init(synapseEnvironment);
            authenticators.add(authenticator);
        }
        if (isApiKeyProtected) {
            Authenticator authenticator = new ApiKeyAuthenticator(apiKeyHeader, apiLevelPolicy, isOAuthBasicAuthMandatory);
            authenticator.init(synapseEnvironment);
            authenticators.add(authenticator);
        }
        Authenticator authenticator = new InternalAPIKeyAuthenticator(APIMgtGatewayConstants.INTERNAL_KEY);
        authenticator.init(synapseEnvironment);
        authenticators.add(authenticator);
        authenticators.sort(new Comparator<Authenticator>() {
            @Override
            public int compare(Authenticator o1, Authenticator o2) {
                return (o1.getPriority() - o2.getPriority());
            }
        });
    }

    @MethodStats
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EXS_EXCEPTION_SOFTENING_RETURN_FALSE",
            justification = "Error is sent through payload")
    public boolean handleRequest(MessageContext messageContext) {

        TracingSpan keyTracingSpan = null;
        TelemetrySpan keySpan = null;
        if (TelemetryUtil.telemetryEnabled()) {
            TelemetrySpan responseLatencySpan =
                    (TelemetrySpan) messageContext.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);
            TelemetryTracer tracer = ServiceReferenceHolder.getInstance().getTelemetryTracer();
            keySpan = TelemetryUtil.startSpan(APIMgtGatewayConstants.KEY_VALIDATION, responseLatencySpan, tracer);
            messageContext.setProperty(APIMgtGatewayConstants.KEY_VALIDATION, keySpan);
            org.apache.axis2.context.MessageContext axis2MC =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            axis2MC.setProperty(APIMgtGatewayConstants.KEY_VALIDATION, keySpan);
        } else if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan =
                    (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);
            TracingTracer tracer = Util.getGlobalTracer();
            keyTracingSpan = Util.startSpan(APIMgtGatewayConstants.KEY_VALIDATION, responseLatencySpan, tracer);
            messageContext.setProperty(APIMgtGatewayConstants.KEY_VALIDATION, keyTracingSpan);
            org.apache.axis2.context.MessageContext axis2MC =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            axis2MC.setProperty(APIMgtGatewayConstants.KEY_VALIDATION, keyTracingSpan);
        }

        Timer.Context context = startMetricTimer();
        long startTime = System.nanoTime();
        long endTime;
        long difference;

        if (Utils.isGraphQLSubscriptionRequest(messageContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping GraphQL subscription handshake request.");
            }
            return true;
        }

        try {
            if (isAnalyticsEnabled()) {
                long currentTime = System.currentTimeMillis();
                messageContext.setProperty("api.ut.requestTime", Long.toString(currentTime));
            }

            messageContext.setProperty(APIMgtGatewayConstants.API_TYPE, apiType);
            if (ExtensionListenerUtil.preProcessRequest(messageContext, type)) {
                if (!isAuthenticatorsInitialized) {
                    initializeAuthenticators();
                }
                if (!isOauthParamsInitialized) {
                    initOAuthParams();
                }
                String authenticationScheme = getAPIKeyValidator().getResourceAuthenticationScheme(messageContext);
                if(APIConstants.AUTH_NO_AUTHENTICATION.equals(authenticationScheme)) {
                    if(log.isDebugEnabled()){
                        log.debug("Found Authentication Scheme: ".concat(authenticationScheme));
                    }
                    handleNoAuthentication(messageContext);
                    setAPIParametersToMessageContext(messageContext);
                    return ExtensionListenerUtil.postProcessRequest(messageContext, type);
                }
                try {
                    if (isAuthenticate(messageContext)) {
                        setAPIParametersToMessageContext(messageContext);
                        return ExtensionListenerUtil.postProcessRequest(messageContext, type);
                    }
                } catch (APIManagementException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Authentication of message context failed", e);
                    }
                }
            }
        } catch (APISecurityException e) {

            if (TelemetryUtil.telemetryEnabled()) {
                TelemetryUtil.setTag(keySpan, APIMgtGatewayConstants.ERROR, APIMgtGatewayConstants.KEY_SPAN_ERROR);
            } else if (Util.tracingEnabled()) {
                Util.setTag(keyTracingSpan, APIMgtGatewayConstants.ERROR, APIMgtGatewayConstants.KEY_SPAN_ERROR);
            }
            if (log.isDebugEnabled()) {
                    // We do the calculations only if the debug logs are enabled. Otherwise this would be an overhead
                    // to all the gateway calls that is happening.
                    endTime = System.nanoTime();
                    difference = (endTime - startTime) / 1000000;
                    String messageDetails = logMessageDetails(messageContext);
                    log.debug("Call to Key Manager : " + messageDetails + ", elapsedTimeInMilliseconds=" +
                            difference / 1000000);
                }

                String errorMessage = APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode());

                if (APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE.equals(errorMessage)) {
                    log.error("API authentication failure due to "
                            + APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE, e);
                } else {
                    String warnDetails = getWarnDetails(messageContext);
                    // We do not need to log known authentication failures as errors since these are not product errors.
                    // Sample Warn String (JWT): APIAuthenticationHandler API authentication failure due to The access
                    // token does not allow you to access the requested resource for requestURI=/pizzashack/1.0.0/order
                    // Sample Warn String (default): APIAuthenticationHandler API authentication failure due to The
                    // access token does not allow you to access the requested resource for appName=test
                    // userName=admin@carbon.super for requestURI=/pizzashack/1.0.0/order
                    log.warn("API authentication failure due to " + errorMessage + warnDetails);

                    if (log.isDebugEnabled()) {
                        log.debug("API authentication failed with error " + e.getErrorCode(), e);
                    }
                }

                handleAuthFailure(messageContext, e);
        } finally {
            if (TelemetryUtil.telemetryEnabled()) {
                TelemetryUtil.finishSpan(keySpan);
            } else if (Util.tracingEnabled()) {
                Util.finishSpan(keyTracingSpan);
            }
            messageContext.setProperty(APIMgtGatewayConstants.SECURITY_LATENCY,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime));
            stopMetricTimer(context);

        }
        return false;
    }

    private void handleNoAuthentication(MessageContext messageContext){

        //Using existing constant in Message context removing the additional constant in API Constants
        String clientIP = null;
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        Map<String, String> transportHeaderMap = (Map<String, String>)
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

        //Create a dummy AuthenticationContext object with hard coded values for Tier and KeyType. This is because we cannot determine the Tier nor Key Type without subscription information..
        AuthenticationContext authContext = new AuthenticationContext();
        authContext.setAuthenticated(true);
        authContext.setTier(APIConstants.UNAUTHENTICATED_TIER);
        //Since we don't have details on unauthenticated tier we setting stop on quota reach true
        authContext.setStopOnQuotaReach(true);
        //Requests are throttled by the ApiKey that is set here. In an unauthenticated scenario, we will use the client's IP address for throttling.
        authContext.setApiKey(clientIP);
        authContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
        //This name is hardcoded as anonymous because there is no associated user token
        authContext.setUsername(APIConstants.END_USER_ANONYMOUS);
        authContext.setCallerToken(null);
        authContext.setApplicationName(null);
        authContext.setApplicationId(clientIP); //Set clientIp as application ID in unauthenticated scenario
        authContext.setConsumerKey(null);
        APISecurityUtils.setAuthenticationContext(messageContext, authContext, securityContextHeader);
    }

    protected void stopMetricTimer(Timer.Context context) {
        context.stop();
    }

    protected Timer.Context startMetricTimer() {
        Timer timer = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO, MetricManager.name(
                APIConstants.METRICS_PREFIX, this.getClass().getSimpleName()));
        return timer.start();
    }

    public void setKeyValidator() {
        this.keyValidator = new APIKeyValidator();
    }

    /**
     * Authenticates the given request using the authenticators which have been initialized.
     *
     * @param messageContext The message to be authenticated
     * @return true if the authentication is successful (never returns false)
     * @throws APISecurityException If an authentication failure or some other error occurs
     */
    protected boolean isAuthenticate(MessageContext messageContext) throws APISecurityException, APIManagementException {
        boolean authenticated = false;
        AuthenticationResponse authenticationResponse;
        List<AuthenticationResponse> authResponses = new ArrayList<>();
        AuthenticationResponse optionalAuthentication = null;
        boolean mandatoryAuthFailed = false;

        for (Authenticator authenticator : authenticators) {
            authenticationResponse = authenticator.authenticate(messageContext);
            if (optionalAuthentication == null &&
                    (authenticationResponse.getErrorCode() != APISecurityConstants.API_AUTH_MISSING_CREDENTIALS
                            || authenticationResponse.isAuthenticated())) {
                optionalAuthentication = authenticationResponse;
            }
            if (authenticationResponse.isMandatoryAuthentication()) {
                // Update authentication status only if the authentication is a mandatory one
                authenticated = authenticationResponse.isAuthenticated();
                mandatoryAuthFailed = !(authenticationResponse.isAuthenticated());
            }
            if (!authenticationResponse.isAuthenticated()) {
                authResponses.add(authenticationResponse);
            }
            if (!authenticationResponse.isContinueToNextAuthenticator()) {
                break;
            }
        }
        if (!authenticated) {
            if (mandatoryAuthFailed || optionalAuthentication == null) {
                Pair<Integer, String> error = getError(authResponses);
                throw new APISecurityException(error.getKey(), error.getValue());
            } else if (!(optionalAuthentication.isAuthenticated())) {
                throw new APISecurityException(optionalAuthentication.getErrorCode()
                        , optionalAuthentication.getErrorMessage());
            }
        }
        return true;
    }

    private Pair<Integer, String> getError(List<AuthenticationResponse> authResponses) {
        Pair<Integer, String> error = null;
        boolean isMissingCredentials = false;
        for (AuthenticationResponse authResponse : authResponses) {
            // get error for transport level mandatory auth failure
            if (!authResponse.isContinueToNextAuthenticator()) {
                error = Pair.of(authResponse.getErrorCode(), authResponse.getErrorMessage());
                return error;
            }
            // get error for application level mandatory auth failure
            if (authResponse.isMandatoryAuthentication() &&
                    (authResponse.getErrorCode() != APISecurityConstants.API_AUTH_MISSING_CREDENTIALS)) {
                error = Pair.of(authResponse.getErrorCode(), authResponse.getErrorMessage());
            } else {
                isMissingCredentials = true;
            }
        }
        // finally checks whether it is missing credentials
        if (error == null && isMissingCredentials) {
            error = Pair.of(APISecurityConstants.API_AUTH_MISSING_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE);
            return error;
        } else if (error == null) {
            // ideally this should not exist
            error = Pair.of(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    APISecurityConstants.API_AUTH_GENERAL_ERROR_MESSAGE);
        }
        return error;
    }

    protected String getAuthenticatorsChallengeString() {
        StringBuilder challengeString = new StringBuilder();
        if (authenticators != null) {
            for (Authenticator authenticator : authenticators) {
                if (Boolean.parseBoolean(System.getProperty("removeInternalAPIKeyRealm")) && authenticator instanceof InternalAPIKeyAuthenticator) {
                    continue;
                }
                challengeString.append(authenticator.getChallengeString()).append(", ");
            }
        }
        return challengeString.toString().trim();
    }

    protected boolean isAnalyticsEnabled() {
        return APIUtil.isAnalyticsEnabled();
    }

    @MethodStats
    public boolean handleResponse(MessageContext messageContext) {

        if (ExtensionListenerUtil.preProcessResponse(messageContext, type)) {
            return ExtensionListenerUtil.postProcessResponse(messageContext, type);
        }
        return false;

    }

    private void handleAuthFailure(MessageContext messageContext, APISecurityException e) {
        messageContext.setProperty(SynapseConstants.ERROR_CODE, e.getErrorCode());
        messageContext.setProperty(SynapseConstants.ERROR_MESSAGE,
                APISecurityConstants.getAuthenticationFailureMessage(e.getErrorCode()));
        messageContext.setProperty(SynapseConstants.ERROR_EXCEPTION, e);

        Mediator sequence = messageContext.getSequence(APISecurityConstants.API_AUTH_FAILURE_HANDLER);

        //Setting error description which will be available to the handler
        String errorDetail = APISecurityConstants.getFailureMessageDetailDescription(e.getErrorCode(), e.getMessage());
        // if custom auth header is configured, the error message should specify its name instead of default value
        if (e.getErrorCode() == APISecurityConstants.API_AUTH_MISSING_CREDENTIALS) {
            errorDetail =
                    APISecurityConstants.getFailureMessageDetailDescription(e.getErrorCode(), e.getMessage()) + "'"
                            + authorizationHeader + " : Bearer ACCESS_TOKEN' or '" + authorizationHeader +
                            " : Basic ACCESS_TOKEN' or '" + apiKeyHeader + " : API_KEY'";
        }
        messageContext.setProperty(SynapseConstants.ERROR_DETAIL, errorDetail);

        // By default we send a 401 response back
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        // This property need to be set to avoid sending the content in pass-through pipe (request message)
        // as the response.
        axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
        try {
            RelayUtils.consumeAndDiscardMessage(axis2MC);
        } catch (AxisFault axisFault) {
            //In case of an error it is logged and the process is continued because we're setting a fault message in the payload.
            log.error("Error occurred while consuming and discarding the message", axisFault);
        }
        axis2MC.setProperty(Constants.Configuration.MESSAGE_TYPE, "application/soap+xml");
        int status;
        if (e.getErrorCode() == APISecurityConstants.API_AUTH_GENERAL_ERROR ||
                e.getErrorCode() == APISecurityConstants.API_AUTH_MISSING_OPEN_API_DEF) {
            status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        } else if (e.getErrorCode() == APISecurityConstants.API_AUTH_INCORRECT_API_RESOURCE ||
                e.getErrorCode() == APISecurityConstants.API_AUTH_FORBIDDEN ||
                e.getErrorCode() == APISecurityConstants.INVALID_SCOPE) {
            status = HttpStatus.SC_FORBIDDEN;
        } else {
            status = HttpStatus.SC_UNAUTHORIZED;
            Map<String, String> headers =
                    (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (headers != null) {
                headers.put(HttpHeaders.WWW_AUTHENTICATE, getAuthenticatorsChallengeString() +
                        " error=\"invalid_token\"" +
                        ", error_description=\"The provided token is invalid\"");
                axis2MC.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
            }
        }

        messageContext.setProperty(APIMgtGatewayConstants.HTTP_RESPONSE_STATUS_CODE, status);

        // Invoke the custom error handler specified by the user
        if (sequence != null && !sequence.mediate(messageContext)) {
            // If needed user should be able to prevent the rest of the fault handling
            // logic from getting executed
            return;
        }

        sendFault(messageContext, status);
    }

    protected void sendFault(MessageContext messageContext, int status) {
        Utils.sendFault(messageContext, status);
    }

    private String logMessageDetails(MessageContext messageContext) {
        //TODO: Hardcoded const should be moved to a common place which is visible to org.wso2.carbon.apimgt.gateway.handlers
        String applicationName = (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME);
        String endUserName = (String) messageContext.getProperty(APIMgtGatewayConstants.END_USER_NAME);
        Date incomingReqTime = null;
        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String logMessage = "API call failed reason=API_authentication_failure"; //"app-name=" + applicationName + " " + "user-name=" + endUserName;
        String logID = axisMC.getOptions().getMessageId();
        if (applicationName != null) {
            logMessage = " belonging to appName=" + applicationName;
        }
        if (endUserName != null) {
            logMessage = logMessage + " userName=" + endUserName;
        }
        if (logID != null) {
            logMessage = logMessage + " transactionId=" + logID;
        }
        String userAgent = (String) ((TreeMap) axisMC.getProperty(org.apache.axis2.context.MessageContext
                .TRANSPORT_HEADERS)).get(APIConstants.USER_AGENT);
        if (userAgent != null) {
            logMessage = logMessage + " with userAgent=" + userAgent;
        }
        String accessToken = (String) ((TreeMap) axisMC.getProperty(org.apache.axis2.context.MessageContext
                .TRANSPORT_HEADERS)).get(APIMgtGatewayConstants.AUTHORIZATION);
        if (accessToken != null) {
            logMessage = logMessage + " with accessToken=" + accessToken;
        }
        String requestURI = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        if (requestURI != null) {
            logMessage = logMessage + " for requestURI=" + requestURI;
        }
        String requestReceivedTime = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(APIMgtGatewayConstants.REQUEST_RECEIVED_TIME);
        if (requestReceivedTime != null) {
            long reqIncomingTimestamp = Long.parseLong(requestReceivedTime);
            incomingReqTime = new Date(reqIncomingTimestamp);
            logMessage = logMessage + " at time=" + incomingReqTime;
        }

        String remoteIP = (String) axisMC.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        if (remoteIP != null) {
            logMessage = logMessage + " from clientIP=" + remoteIP;
        }
        return logMessage;
    }

    private String getWarnDetails(MessageContext messageContext) {
        // Get application information to be logged in API authentication failure (only requestURI for JWT tokens)
        String applicationName = (String) messageContext.getProperty(APIMgtGatewayConstants.APPLICATION_NAME);
        String endUserName = (String) messageContext.getProperty(APIMgtGatewayConstants.END_USER_NAME);
        String requestURI = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String warnDetails = "";
        if (applicationName != null) {
            warnDetails = " for appName=" + applicationName;
        }
        if (endUserName != null) {
            warnDetails = warnDetails + " userName=" + endUserName;
        }
        if (requestURI != null) {
            warnDetails = warnDetails + " for requestURI=" + requestURI;
        }
        return warnDetails;
    }

    protected void setAPIParametersToMessageContext(MessageContext messageContext) {

        AuthenticationContext authContext = getAuthenticationContext(messageContext);
        org.apache.axis2.context.MessageContext axis2MsgContext =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        String consumerKey = "";
        String username = "";
        String applicationName = "";
        String applicationId = "";
        if (authContext != null) {
            consumerKey = authContext.getConsumerKey();
            username = authContext.getUsername();
            applicationName = authContext.getApplicationName();
            applicationId = authContext.getApplicationId();
        }

        String context = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String version = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);

        String apiPublisher = (String) messageContext.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
        //if publisher is null,extract the publisher from the api_version
        if (apiPublisher == null) {
            apiPublisher = GatewayUtils.getApiProviderFromContextAndVersion(messageContext);
        }

        String api = GatewayUtils.getAPINameFromContextAndVersion(messageContext);
        String resource = extractResource(messageContext);
        String method = (String) (axis2MsgContext.getProperty(
                Constants.Configuration.HTTP_METHOD));
        String hostName = APIUtil.getHostAddress();

        messageContext.setProperty(APIMgtGatewayConstants.CONSUMER_KEY, consumerKey);
        messageContext.setProperty(APIMgtGatewayConstants.USER_ID, username);
        messageContext.setProperty(APIMgtGatewayConstants.CONTEXT, context);
        messageContext.setProperty(APIMgtGatewayConstants.API_VERSION, version);
        messageContext.setProperty(APIMgtGatewayConstants.API, api);
        messageContext.setProperty(APIMgtGatewayConstants.VERSION, version);
        messageContext.setProperty(APIMgtGatewayConstants.RESOURCE, resource);
        messageContext.setProperty(APIMgtGatewayConstants.HTTP_METHOD, method);
        messageContext.setProperty(APIMgtGatewayConstants.HOST_NAME, hostName);
        messageContext.setProperty(APIMgtGatewayConstants.API_PUBLISHER, apiPublisher);
        messageContext.setProperty(APIMgtGatewayConstants.APPLICATION_NAME, applicationName);
        messageContext.setProperty(APIMgtGatewayConstants.APPLICATION_ID, applicationId);
    }

    protected AuthenticationContext getAuthenticationContext(MessageContext messageContext) {
        return APISecurityUtils.getAuthenticationContext(messageContext);
    }

    private String extractResource(MessageContext mc) {
        String resource = "/";
        Pattern pattern = Pattern.compile(APIMgtGatewayConstants.RESOURCE_PATTERN);
        Matcher matcher = pattern.matcher((String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH));
        if (matcher.find()) {
            resource = matcher.group(1);
        }
        return resource;
    }

    public String getKeyManagers() {

        return keyManagers;
    }

    public void setKeyManagers(String keyManagers) {
        this.keyManagers = keyManagers;
    }
}
