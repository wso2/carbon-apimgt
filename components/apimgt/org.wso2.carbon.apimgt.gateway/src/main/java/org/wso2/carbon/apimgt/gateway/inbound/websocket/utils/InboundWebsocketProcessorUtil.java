/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

import org.apache.axiom.util.UIDGenerator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.api.API;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;
import org.wso2.carbon.apimgt.gateway.handlers.DataPublisherUtil;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.JWTValidator;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiException;
import org.wso2.carbon.apimgt.gateway.handlers.throttling.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.GraphQLProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.APIMgtGoogleAnalyticsUtils;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ResourceInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsData;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The util class to handle inbound websocket processor execution.
 */
public class InboundWebsocketProcessorUtil {

    private static final Log log = LogFactory.getLog(InboundWebsocketProcessorUtil.class);

    /**
     * Validates AuthenticationContext and set APIKeyValidationInfoDTO to InboundMessageContext.
     *
     * @param authenticationContext Validated AuthenticationContext
     * @param inboundMessageContext InboundMessageContext
     * @return true if authenticated
     */
    public static boolean validateAuthenticationContext(AuthenticationContext authenticationContext,
                                                        InboundMessageContext inboundMessageContext) {

        if (authenticationContext == null || !authenticationContext.isAuthenticated()) {
            return false;
        }
        // The information given by the AuthenticationContext is set to an APIKeyValidationInfoDTO object
        // so to feed information analytics and throttle data publishing
        APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();
        info.setAuthorized(authenticationContext.isAuthenticated());
        info.setApplicationTier(authenticationContext.getApplicationTier());
        info.setTier(authenticationContext.getTier());
        info.setSubscriberTenantDomain(authenticationContext.getSubscriberTenantDomain());
        info.setSubscriber(authenticationContext.getSubscriber());
        info.setStopOnQuotaReach(authenticationContext.isStopOnQuotaReach());
        info.setApiName(authenticationContext.getApiName());
        info.setApplicationId(authenticationContext.getApplicationId());
        info.setType(authenticationContext.getKeyType());
        info.setApiPublisher(authenticationContext.getApiPublisher());
        info.setApplicationName(authenticationContext.getApplicationName());
        info.setConsumerKey(authenticationContext.getConsumerKey());
        info.setEndUserName(authenticationContext.getUsername());
        info.setApiTier(authenticationContext.getApiTier());
        info.setGraphQLMaxDepth(authenticationContext.getGraphQLMaxDepth());
        info.setGraphQLMaxComplexity(authenticationContext.getGraphQLMaxComplexity());
        info.setEndUserToken(authenticationContext.getCallerToken());

        inboundMessageContext.setKeyType(info.getType());
        inboundMessageContext.setInfoDTO(info);
        inboundMessageContext.setAuthContext(authenticationContext);
        inboundMessageContext.setInfoDTO(info);
        inboundMessageContext.setToken(info.getEndUserToken());
        return authenticationContext.isAuthenticated();
    }

    /**
     * Validate scopes of JWT token for incoming GraphQL subscription messages.
     *
     * @param matchingResource      Invoking GraphQL subscription operation
     * @param inboundMessageContext InboundMessageContext
     * @return true if authorized
     * @throws APIManagementException if an internal error occurs
     * @throws APISecurityException   if authorization fails
     */
    public static boolean authorizeGraphQLSubscriptionEvents(String matchingResource,
                                                             InboundMessageContext inboundMessageContext)
            throws APIManagementException, APISecurityException {

        JWTValidator jwtValidator = new JWTValidator(new APIKeyValidator(), inboundMessageContext.getTenantDomain());
        jwtValidator.validateScopesForGraphQLSubscriptions(inboundMessageContext.getApiContext(),
                inboundMessageContext.getVersion(), matchingResource, inboundMessageContext.getSignedJWTInfo(),
                inboundMessageContext.getAuthContext());
        return true;
    }

    /**
     * Finds matching VerbInfoDTO for the subscription operation.
     *
     * @param operation             subscription operation name
     * @param inboundMessageContext InboundMessageContext
     * @return VerbInfoDTO
     */
    public static VerbInfoDTO findMatchingVerb(String operation, InboundMessageContext inboundMessageContext) {
        String resourceCacheKey;
        VerbInfoDTO verbInfoDTO = null;
        if (inboundMessageContext.getResourcesMap() != null) {
            ResourceInfoDTO resourceInfoDTO = inboundMessageContext.getResourcesMap().get(operation);
            Set<VerbInfoDTO> verbDTOList = resourceInfoDTO.getHttpVerbs();
            for (VerbInfoDTO verb : verbDTOList) {
                if (verb.getHttpVerb().equals(GraphQLConstants.SubscriptionConstants.HTTP_METHOD_NAME)) {
                    if (isResourcePathMatching(operation, resourceInfoDTO)) {
                        verbInfoDTO = verb;
                        resourceCacheKey = APIUtil.getResourceInfoDTOCacheKey(
                                inboundMessageContext.getApiContext(), inboundMessageContext.getVersion(),
                                operation, GraphQLConstants.SubscriptionConstants.HTTP_METHOD_NAME);
                        verb.setRequestKey(resourceCacheKey);
                        break;
                    }
                }
            }
        }
        return verbInfoDTO;
    }

    /**
     * Check if resource path matches.
     *
     * @param resourceString  Resource string
     * @param resourceInfoDTO ResourceInfoDTO
     * @return true if matches
     */
    private static boolean isResourcePathMatching(String resourceString, ResourceInfoDTO resourceInfoDTO) {
        String resource = resourceString.trim();
        String urlPattern = resourceInfoDTO.getUrlPattern().trim();
        return resource.equalsIgnoreCase(urlPattern);
    }

    /**
     * Checks if the request is throttled for GraphQL subscriptions.
     *
     * @param msgSize               Websocket msg size
     * @param verbInfoDTO           VerbInfoDTO for invoking operation.
     * @param inboundMessageContext InboundMessageContext
     * @param operationId           Operation ID
     * @return InboundProcessorResponseDTO
     */
    public static InboundProcessorResponseDTO doThrottleForGraphQL(int msgSize, VerbInfoDTO verbInfoDTO,
                                                                   InboundMessageContext inboundMessageContext,
                                                                   String operationId) {

        GraphQLProcessorResponseDTO responseDTO = new GraphQLProcessorResponseDTO();
        responseDTO.setId(operationId);
        return InboundWebsocketProcessorUtil.doThrottle(msgSize, verbInfoDTO, inboundMessageContext, responseDTO);
    }

    /**
     * Checks if the request is throttled.
     *
     * @param msgSize               Websocket msg size
     * @param verbInfoDTO           VerbInfoDTO for invoking operation. Pass null for websocket API throttling.
     * @param inboundMessageContext InboundMessageContext
     * @return false if throttled
     */
    public static InboundProcessorResponseDTO doThrottle(int msgSize, VerbInfoDTO verbInfoDTO,
                                                         InboundMessageContext inboundMessageContext,
                                                         InboundProcessorResponseDTO responseDTO) {

        APIKeyValidationInfoDTO infoDTO = inboundMessageContext.getInfoDTO();
        String applicationLevelTier = infoDTO.getApplicationTier();
        String apiLevelTier = infoDTO.getApiTier() == null && verbInfoDTO == null ? APIConstants.UNLIMITED_TIER
                : infoDTO.getApiTier();
        String subscriptionLevelTier = infoDTO.getTier();
        String resourceLevelTier;
        String authorizedUser;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(infoDTO.getSubscriberTenantDomain())) {
            authorizedUser = infoDTO.getSubscriber() + "@" + infoDTO.getSubscriberTenantDomain();
        } else {
            authorizedUser = infoDTO.getSubscriber();
        }
        String apiName = infoDTO.getApiName();
        String apiVersion = inboundMessageContext.getVersion();
        String appTenant = infoDTO.getSubscriberTenantDomain();
        String apiTenant = inboundMessageContext.getTenantDomain();
        String appId = infoDTO.getApplicationId();
        String applicationLevelThrottleKey = appId + ":" + authorizedUser;
        String apiLevelThrottleKey = inboundMessageContext.getApiContext() + ":" + apiVersion;
        String resourceLevelThrottleKey;
        //If API level throttle policy is present then it will apply and no resource level policy will apply for it
        if (StringUtils.isNotEmpty(apiLevelTier) && verbInfoDTO == null) {
            resourceLevelThrottleKey = apiLevelThrottleKey;
            resourceLevelTier = apiLevelTier;
        } else {
            resourceLevelThrottleKey = verbInfoDTO.getRequestKey();
            resourceLevelTier = verbInfoDTO.getThrottling();
        }
        String subscriptionLevelThrottleKey = appId + ":" + inboundMessageContext.getApiContext() + ":" + apiVersion;
        String messageId = UIDGenerator.generateURNString();
        String remoteIP = inboundMessageContext.getUserIP();
        if (log.isDebugEnabled()) {
            log.debug("Remote IP address : " + remoteIP);
        }
        if (remoteIP.indexOf(":") > 0) {
            remoteIP = remoteIP.substring(1, remoteIP.indexOf(":"));
        }
        JSONObject jsonObMap = new JSONObject();
        Utils.setRemoteIp(jsonObMap, remoteIP);
        jsonObMap.put(APIThrottleConstants.MESSAGE_SIZE, msgSize);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    inboundMessageContext.getTenantDomain(), true);
            boolean isThrottled = WebsocketUtil.isThrottled(resourceLevelThrottleKey, subscriptionLevelThrottleKey,
                    applicationLevelThrottleKey);
            if (isThrottled) {
                responseDTO.setError(true);
                responseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR);
                responseDTO.setErrorMessage(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        Object[] objects =
                new Object[]{messageId, applicationLevelThrottleKey, applicationLevelTier, apiLevelThrottleKey,
                        apiLevelTier, subscriptionLevelThrottleKey, subscriptionLevelTier, resourceLevelThrottleKey,
                        resourceLevelTier, authorizedUser, inboundMessageContext.getApiContext(), apiVersion,
                        appTenant, apiTenant, appId, apiName, jsonObMap.toString()};
        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(
                "org.wso2.throttle.request.stream:1.0.0", System.currentTimeMillis(), null, null, objects);
        if (ServiceReferenceHolder.getInstance().getThrottleDataPublisher() == null) {
            log.error("Cannot publish events to traffic manager because ThrottleDataPublisher "
                    + "has not been initialised");
        }
        ServiceReferenceHolder.getInstance().getThrottleDataPublisher().getDataPublisher().tryPublish(event);
        return responseDTO;
    }

    /**
     * Set tenant domain to InboundMessageContext.
     *
     * @param inboundMessageContext InboundMessageContext
     */
    public static void setTenantDomainToContext(InboundMessageContext inboundMessageContext) {

        String tenantDomain;
        if (inboundMessageContext.getRequestPath().contains("/t/")) {
            tenantDomain = MultitenantUtils.getTenantDomainFromUrl(inboundMessageContext.getRequestPath());
        } else {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        inboundMessageContext.setTenantDomain(tenantDomain);
    }

    /**
     * Get the matching API for the request and set API information to InboundMessageContext.
     *
     * @param inboundMessageContext InboundMessageContext
     * @param synCtx                Synapse MessageContext
     * @return API
     */
    public static API getApi(MessageContext synCtx, InboundMessageContext inboundMessageContext) {

        String requestPath = inboundMessageContext.getFullRequestPath();
        TreeMap<String, org.wso2.carbon.apimgt.keymgt.model.entity.API> selectedAPIS =
                Utils.getSelectedAPIList(requestPath, inboundMessageContext.getTenantDomain());
        if (selectedAPIS.size() > 0) {
            String selectedPath = selectedAPIS.firstKey();
            org.wso2.carbon.apimgt.keymgt.model.entity.API selectedAPI = selectedAPIS.get(selectedPath);
            API api = synCtx.getEnvironment().getSynapseConfiguration()
                    .getAPI(GatewayUtils.getQualifiedApiName(selectedAPI.getApiName(),
                            selectedAPI.getApiVersion()));
            inboundMessageContext.setVersion(selectedAPI.getApiVersion());
            inboundMessageContext.setApiName(selectedAPI.getApiName());
            inboundMessageContext.setElectedRoute(selectedPath);
            inboundMessageContext.setElectedAPI(selectedAPI);
            return api;
        }
        return null;
    }

    /**
     * Remove token query parameter from full request path in InboundMessageContext.
     *
     * @param parameters            Query parameters
     * @param inboundMessageContext InboundMessageContext
     */
    public static void removeTokenFromQuery(Map<String, List<String>> parameters,
                                            InboundMessageContext inboundMessageContext, String tokenType) {

        String fullRequestPath = inboundMessageContext.getFullRequestPath();
        StringBuilder queryBuilder = new StringBuilder(fullRequestPath.substring(0, fullRequestPath.indexOf('?') + 1));

        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            if (!tokenType.equals(entry.getKey())) {
                queryBuilder.append(entry.getKey()).append('=').append(entry.getValue().get(0)).append('&');
            }
        }

        // remove trailing '?' or '&' from the built string
        fullRequestPath = queryBuilder.substring(0, queryBuilder.length() - 1);
        inboundMessageContext.setFullRequestPath(fullRequestPath);
    }

    /**
     * Authenticate inbound websocket request handshake.
     *
     * @param inboundMessageContext InboundMessageContext
     * @return whether authenticated or not
     * @throws APISecurityException   if authentication fails
     */
    public static boolean isAuthenticated(InboundMessageContext inboundMessageContext) throws APISecurityException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                    inboundMessageContext.getTenantDomain(), true);
            if (inboundMessageContext.getAuthenticator().validateToken(inboundMessageContext)) {
                return !authenticate(inboundMessageContext).isError();
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return false;
    }

    /**
     * Authenticate token during inbound websocket request (frame) execution, without validating the scopes.
     *
     * @param inboundMessageContext InboundMessageContext
     * @return InboundProcessorResponseDTO
     */
    public static InboundProcessorResponseDTO authenticateToken(InboundMessageContext inboundMessageContext)
            throws APISecurityException {
        return authenticate(inboundMessageContext);
    }

    /**
     * Authenticate token during handshake and inbound websocket request (frame) execution.
     *
     * @param inboundMessageContext InboundMessageContext
     * @return InboundProcessorResponseDTO
     */
    public static InboundProcessorResponseDTO authenticate(InboundMessageContext inboundMessageContext)
            throws APISecurityException {
        return inboundMessageContext.getAuthenticator().authenticate(inboundMessageContext);
    }

    /**
     * Publish Google Analytics data.
     *
     * @param inboundMessageContext InboundMessageContext
     * @param remoteAddress         Remote IP address
     * @throws WebSocketApiException if an  error occurs
     */
    public static void publishGoogleAnalyticsData(InboundMessageContext inboundMessageContext, String remoteAddress)
            throws WebSocketApiException {
        // publish Google Analytics data
        GoogleAnalyticsData.DataBuilder gaData;
        try {
            gaData = new GoogleAnalyticsData.DataBuilder(null, null, null, null)
                    .setDocumentPath(inboundMessageContext.getFullRequestPath())
                    .setDocumentHostName(DataPublisherUtil.getHostAddress())
                    .setSessionControl("end").setCacheBuster(APIMgtGoogleAnalyticsUtils.getCacheBusterId())
                    .setIPOverride(remoteAddress);
            APIMgtGoogleAnalyticsUtils gaUtils = new APIMgtGoogleAnalyticsUtils();
            gaUtils.init(inboundMessageContext.getTenantDomain());
            gaUtils.publishGATrackingData(gaData, inboundMessageContext.getRequestHeaders().get(HttpHeaders.USER_AGENT),
                    inboundMessageContext.getRequestHeaders().get(HttpHeaders.AUTHORIZATION));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new WebSocketApiException("Error while publishing google analytics data for API "
                    + inboundMessageContext.getApiContext());
        }
    }

    /**
     * Get handshake error DTO for error code and message. The closeConnection parameter is false.
     *
     * @param errorCode    Error code
     * @param errorMessage Error message
     * @return InboundProcessorResponseDTO
     */
    public static InboundProcessorResponseDTO getHandshakeErrorDTO(int errorCode, String errorMessage) {

        InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();
        inboundProcessorResponseDTO.setError(true);
        inboundProcessorResponseDTO.setErrorCode(errorCode);
        inboundProcessorResponseDTO.setErrorMessage(errorMessage);
        return inboundProcessorResponseDTO;
    }

    /**
     * Get error frame DTO for error code and message closeConnection parameters.
     *
     * @param errorCode       Error code
     * @param errorMessage    Error message
     * @param closeConnection Whether to close connection after throwing the error frame
     * @return InboundProcessorResponseDTO
     */
    public static InboundProcessorResponseDTO getFrameErrorDTO(int errorCode, String errorMessage,
                                                               boolean closeConnection) {

        InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();
        inboundProcessorResponseDTO.setError(true);
        inboundProcessorResponseDTO.setErrorCode(errorCode);
        inboundProcessorResponseDTO.setErrorMessage(errorMessage);
        inboundProcessorResponseDTO.setCloseConnection(closeConnection);
        return inboundProcessorResponseDTO;
    }

    /**
     * Get GraphQL subscription error frame DTO for error code and message closeConnection parameters.
     *
     * @param errorCode       Error code
     * @param errorMessage    Error message
     * @param closeConnection Whether to close connection after throwing the error frame
     * @param operationId     Operation ID
     * @return InboundProcessorResponseDTO
     */
    public static GraphQLProcessorResponseDTO getGraphQLFrameErrorDTO(int errorCode, String errorMessage,
                                                                      boolean closeConnection, String operationId) {

        GraphQLProcessorResponseDTO graphQLProcessorResponseDTO = new GraphQLProcessorResponseDTO();
        graphQLProcessorResponseDTO.setError(true);
        graphQLProcessorResponseDTO.setErrorCode(errorCode);
        graphQLProcessorResponseDTO.setErrorMessage(errorMessage);
        graphQLProcessorResponseDTO.setCloseConnection(closeConnection);
        graphQLProcessorResponseDTO.setId(operationId);
        return graphQLProcessorResponseDTO;
    }

    /**
     * Get bad request (error code 4010) error frame DTO for error message. The closeConnection parameter is false.
     *
     * @param errorMessage Error message
     * @return InboundProcessorResponseDTO
     */
    public static InboundProcessorResponseDTO getBadRequestFrameErrorDTO(String errorMessage) {

        InboundProcessorResponseDTO inboundProcessorResponseDTO = new InboundProcessorResponseDTO();
        inboundProcessorResponseDTO.setError(true);
        inboundProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST);
        inboundProcessorResponseDTO.setErrorMessage(errorMessage);
        return inboundProcessorResponseDTO;
    }

    /**
     * Get bad request (error code 4010) error frame DTO for GraphQL subscriptions. The closeConnection parameter is
     * false.
     *
     * @param errorMessage Error message
     * @param operationId  Operation ID
     * @return InboundProcessorResponseDTO
     */
    public static InboundProcessorResponseDTO getBadRequestGraphQLFrameErrorDTO(String errorMessage,
                                                                                String operationId) {

        GraphQLProcessorResponseDTO inboundProcessorResponseDTO = new GraphQLProcessorResponseDTO();
        inboundProcessorResponseDTO.setError(true);
        inboundProcessorResponseDTO.setErrorCode(WebSocketApiConstants.FrameErrorConstants.BAD_REQUEST);
        inboundProcessorResponseDTO.setErrorMessage(errorMessage);
        inboundProcessorResponseDTO.setId(operationId);
        return inboundProcessorResponseDTO;
    }

    /**
     * Validates scopes for subscription operations.
     *
     * @param inboundMessageContext InboundMessageContext
     * @param subscriptionOperation Subscription operation
     * @param operationId           GraphQL message Id
     * @return InboundProcessorResponseDTO
     */
    public static InboundProcessorResponseDTO validateScopes(InboundMessageContext inboundMessageContext,
                                                             String subscriptionOperation, String operationId) {

        InboundProcessorResponseDTO responseDTO = new GraphQLProcessorResponseDTO();
        if (inboundMessageContext.isJWTToken()) { // scope validation not supported for Opaque tokens with gql subs
            // validate scopes based on subscription payload
            try {
                if (!InboundWebsocketProcessorUtil.authorizeGraphQLSubscriptionEvents(subscriptionOperation,
                        inboundMessageContext)) {
                    String errorMessage = WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR_MESSAGE
                            + StringUtils.SPACE + subscriptionOperation;
                    log.error(errorMessage);
                    responseDTO = InboundWebsocketProcessorUtil.getGraphQLFrameErrorDTO(
                            WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR, errorMessage, false,
                            operationId);
                }
            } catch (APIManagementException e) {
                log.error(WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_MESSAGE, e);
                responseDTO = InboundWebsocketProcessorUtil.getFrameErrorDTO(
                        WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_ERROR,
                        WebSocketApiConstants.FrameErrorConstants.API_AUTH_GENERAL_MESSAGE, true);
            } catch (APISecurityException e) {
                log.error(WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR_MESSAGE, e);
                responseDTO = InboundWebsocketProcessorUtil.getGraphQLFrameErrorDTO(
                        WebSocketApiConstants.FrameErrorConstants.RESOURCE_FORBIDDEN_ERROR, e.getMessage(), false,
                        operationId);
            }
        }
        return responseDTO;
    }

    /**
     * Checks if the authenticator is enabled for the given authentication type.
     *
     * @param authenticationType The String containing the Authentication type (eg: api_key, oauth2)
     * @param inboundMessageContext  The InboundMessageContext object containing the request details
     * @return true if the authenticator is enabled for the given authentication type
     */
    public static boolean isAuthenticatorEnabled(String authenticationType, InboundMessageContext inboundMessageContext) {
        if (!Utils.getSecuritySchemeOfWebSocketAPI(inboundMessageContext.getApiContext(), inboundMessageContext.
                getVersion(), inboundMessageContext.getTenantDomain()).contains(authenticationType)) {
            if (log.isDebugEnabled()) {
                log.debug("Authentication has not been enabled for the Authenticator type: " + authenticationType);
            }
            return false;
        }
        return true;
    }
}
