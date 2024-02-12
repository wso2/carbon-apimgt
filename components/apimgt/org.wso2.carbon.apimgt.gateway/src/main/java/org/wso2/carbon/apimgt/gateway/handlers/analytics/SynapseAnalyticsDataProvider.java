/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.CorrelationConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.subscription.URLMapping;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsCustomDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.exceptions.DataNotFoundException;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Target;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.URITemplate;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.EventCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategory;
import org.wso2.carbon.apimgt.common.gateway.constants.JWTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS;
import static org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.UNKNOWN_VALUE;

public class SynapseAnalyticsDataProvider implements AnalyticsDataProvider {

    private static final Log log = LogFactory.getLog(SynapseAnalyticsDataProvider.class);
    private MessageContext messageContext;
    private AnalyticsCustomDataProvider analyticsCustomDataProvider;
    private Boolean buildResponseMessage = null;

    public SynapseAnalyticsDataProvider(MessageContext messageContext) {

        this.messageContext = messageContext;
    }

    public SynapseAnalyticsDataProvider(MessageContext messageContext,
                                        AnalyticsCustomDataProvider analyticsCustomDataProvider) {

        this.messageContext = messageContext;
        this.analyticsCustomDataProvider = analyticsCustomDataProvider;
    }

    public static String sortGraphQLOperations(String apiResourceTemplates) {

        if (apiResourceTemplates == null || !apiResourceTemplates.contains(",")) {
            return apiResourceTemplates;
        }
        String[] list = apiResourceTemplates.split(",");
        // sorting alphabetical order
        Arrays.sort(list);
        return String.join(",", list);
    }

    @Override
    public EventCategory getEventCategory() {

        if (isSuccessRequest()) {
            return EventCategory.SUCCESS;
        } else if (isFaultRequest()) {
            return EventCategory.FAULT;
        } else {
            return EventCategory.INVALID;
        }
    }

    @Override
    public boolean isAnonymous() {

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        return isAuthenticated() && APIConstants.END_USER_ANONYMOUS.equalsIgnoreCase(authContext.getUsername());
    }

    @Override
    public boolean isAuthenticated() {

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        return authContext != null && authContext.isAuthenticated();
    }

    @Override
    public FaultCategory getFaultType() {

        if (isAuthFaultRequest()) {
            return FaultCategory.AUTH;
        } else if (isThrottledFaultRequest()) {
            return FaultCategory.THROTTLED;
        } else if (isTargetFaultRequest()) {
            return FaultCategory.TARGET_CONNECTIVITY;
        } else {
            return FaultCategory.OTHER;
        }
    }

    @Override
    public API getApi() throws DataNotFoundException {

        String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(apiContext);
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        org.wso2.carbon.apimgt.keymgt.model.entity.API apiObj = store.getApiByContextAndVersion(apiContext, apiVersion);
        API api = new API();
        if (apiObj == null) {
            try {
                apiObj = new SubscriptionDataLoaderImpl().getApi(apiContext, apiVersion);
            } catch (DataLoadingException e) {
                log.error("Error occurred when getting api.", e);
                throw new DataNotFoundException("Error occurred when getting API information", e);
            }
        }

        if (apiObj != null) {
            api.setApiId(apiObj.getUuid());
            api.setApiType(apiObj.getApiType());
            api.setApiName(apiObj.getApiName());
            api.setApiVersion(apiObj.getApiVersion());
            api.setApiCreator(apiObj.getApiProvider());
            api.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(api.getApiCreator()));
            List<URITemplate> uriTemplates = new ArrayList<>();
            for (URLMapping uriTemplate : apiObj.getUrlMappings()) {
                org.wso2.carbon.apimgt.common.analytics.publishers.dto.URITemplate uriTemplateObj
                        = new org.wso2.carbon.apimgt.common.analytics.publishers.dto.URITemplate();
                if (uriTemplate.getHttpMethod() != null && uriTemplate.getHttpMethod()
                        .equals(messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD))
                        && uriTemplate.getUrlPattern() != null && uriTemplate.getUrlPattern()
                        .equals(messageContext.getProperty("API_ELECTED_RESOURCE"))) {
                    uriTemplateObj.setResourceURI(uriTemplate.getUrlPattern());
                    uriTemplateObj.setHttpVerb(uriTemplate.getHttpMethod());
                    uriTemplateObj.setAuthScheme(uriTemplate.getAuthScheme());
                    List<org.wso2.carbon.apimgt.common.analytics.publishers.dto.OperationPolicy> operationPolicies
                            = new ArrayList<>();
                    for (OperationPolicy operationPolicy : uriTemplate.getOperationPolicies()) {
                        org.wso2.carbon.apimgt.common.analytics.publishers.dto.OperationPolicy operationPolicyObj
                                = new org.wso2.carbon.apimgt.common.analytics.publishers.dto.OperationPolicy();
                        operationPolicyObj.setPolicyVersion(operationPolicy.getPolicyVersion());
                        operationPolicyObj.setPolicyName(operationPolicy.getPolicyName());
                        operationPolicyObj.setPolicyId(operationPolicy.getPolicyId());
                        operationPolicyObj.setDirection(operationPolicy.getDirection());
                        operationPolicyObj.setOrder(operationPolicy.getOrder());
                        operationPolicies.add(operationPolicyObj);
                    }
                    uriTemplateObj.setOperationPolicies(operationPolicies);
                    uriTemplates.add(uriTemplateObj);
                    break;
                }
            }
            List<org.wso2.carbon.apimgt.common.analytics.publishers.dto.OperationPolicy> apiPolicyList =
                    new ArrayList<>();
            for (OperationPolicy apiPolicy : apiObj.getApiPolicies()) {
                org.wso2.carbon.apimgt.common.analytics.publishers.dto.OperationPolicy operationPolicyObj
                        = new org.wso2.carbon.apimgt.common.analytics.publishers.dto.OperationPolicy();
                operationPolicyObj.setPolicyVersion(apiPolicy.getPolicyVersion());
                operationPolicyObj.setPolicyName(apiPolicy.getPolicyName());
                operationPolicyObj.setPolicyId(apiPolicy.getPolicyId());
                operationPolicyObj.setDirection(apiPolicy.getDirection());
                operationPolicyObj.setOrder(apiPolicy.getOrder());
                apiPolicyList.add(operationPolicyObj);
            }
            api.setUriTemplates(uriTemplates);
            api.setApiPolicies(apiPolicyList);
        }
        return api;
    }

    @Override
    public Application getApplication() throws DataNotFoundException {

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(messageContext);
        if (authContext == null) {
            throw new DataNotFoundException("Error occurred when getting Application information");
        }
        Application application = new Application();
        application.setApplicationId(authContext.getApplicationUUID());
        application.setApplicationName(authContext.getApplicationName());
        application.setApplicationOwner(authContext.getSubscriber());
        application.setKeyType(authContext.getKeyType());
        return application;
    }

    @Override
    public Operation getOperation() throws DataNotFoundException {

        String httpMethod = (String) messageContext.getProperty(APIMgtGatewayConstants.HTTP_METHOD);
        String apiResourceTemplate = (String) messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE);
        Operation operation = new Operation();
        operation.setApiMethod(httpMethod);
        if (APIConstants.GRAPHQL_API.equalsIgnoreCase(getApi().getApiType())) {
            String orderedOperations = sortGraphQLOperations(apiResourceTemplate);
            operation.setApiResourceTemplate(orderedOperations);
        } else {
            operation.setApiResourceTemplate(apiResourceTemplate);
        }
        return operation;
    }

    @Override
    public Target getTarget() {

        Target target = new Target();

        String endpointAddress = (String) messageContext.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        if (endpointAddress == null) {
            endpointAddress = APIMgtGatewayConstants.DUMMY_ENDPOINT_ADDRESS;
        }
        int targetResponseCode = getTargetResponseCode();
        target.setResponseCacheHit(isCacheHit());
        target.setDestination(endpointAddress);
        target.setTargetResponseCode(targetResponseCode);
        return target;
    }

    @Override
    public Latencies getLatencies() {

        long backendLatency = getBackendLatency();
        long responseLatency = getResponseLatency();
        long requestMediationLatency = getRequestMediationLatency();
        long responseMediationLatency = getResponseMediationLatency();

        Latencies latencies = new Latencies();
        latencies.setResponseLatency(responseLatency);
        latencies.setBackendLatency(backendLatency);
        latencies.setRequestMediationLatency(requestMediationLatency);
        latencies.setResponseMediationLatency(responseMediationLatency);
        return latencies;
    }

    private boolean isCacheHit() {

        Object cacheHitObject = messageContext.getProperty(Constants.REQUEST_CACHE_HIT);
        boolean isCacheHit = false;
        if (cacheHitObject instanceof String) {
            isCacheHit = Boolean.parseBoolean((String) cacheHitObject);
        } else if (cacheHitObject instanceof Boolean) {
            isCacheHit = (boolean) cacheHitObject;
        }
        return isCacheHit;
    }

    @Override
    public MetaInfo getMetaInfo() {

        MetaInfo metaInfo = new MetaInfo();
        Object correlationId = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(CorrelationConstants.CORRELATION_ID);
        if (correlationId instanceof String) {
            metaInfo.setCorrelationId((String) correlationId);
        }
        metaInfo.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
        Map<String, String> configMap = ServiceReferenceHolder.getInstance().getApiManagerConfigurationService()
                .getAPIAnalyticsConfiguration().getReporterProperties();
        String region;
        if (System.getProperties().containsKey(Constants.REGION_ID_PROP)) {
            region = System.getProperty(Constants.REGION_ID_PROP);
        } else if (configMap != null && configMap.containsKey(Constants.REGION_ID_PROP)) {
            region = configMap.get(Constants.REGION_ID_PROP);
        } else {
            region = Constants.DEFAULT_REGION_ID;
        }
        metaInfo.setRegionId(region);
        return metaInfo;
    }

    @Override
    public int getProxyResponseCode() {

        Object clientResponseCodeObj = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(SynapseConstants.HTTP_SC);
        if (clientResponseCodeObj == null) {
            return HttpStatus.SC_OK;
        }
        int proxyResponseCode;
        if (clientResponseCodeObj instanceof Integer) {
            proxyResponseCode = (int) clientResponseCodeObj;
        } else {
            proxyResponseCode = Integer.parseInt((String) clientResponseCodeObj);
        }
        return proxyResponseCode;
    }

    @Override
    public int getTargetResponseCode() {

        Object responseCodeObject = messageContext.getProperty(Constants.BACKEND_RESPONSE_CODE);
        if (responseCodeObject == null) {
            responseCodeObject = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                    .getProperty(SynapseConstants.HTTP_SC);
        }
        if (responseCodeObject instanceof Integer) {
            return (int) responseCodeObject;
        } else if (responseCodeObject instanceof String) {
            return Integer.parseInt((String) responseCodeObject);
        }
        return 0;
    }

    @Override
    public long getRequestTime() {

        return (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
    }

    @Override
    public Error getError(FaultCategory faultCategory) {

        int errorCode = (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
        FaultCodeClassifier faultCodeClassifier = new FaultCodeClassifier(messageContext);
        FaultSubCategory faultSubCategory = faultCodeClassifier.getFaultSubCategory(faultCategory, errorCode);
        Error error = new Error();
        error.setErrorCode(errorCode);
        error.setErrorMessage(faultSubCategory);
        return error;
    }

    @Override
    public String getUserAgentHeader() {

        return (String) messageContext.getProperty(Constants.USER_AGENT_PROPERTY);
    }

    @Override
    public String getUserName() {

        if (messageContext.getPropertyKeySet().contains(APIMgtGatewayConstants.END_USER_NAME)) {
            return (String) messageContext.getProperty(APIMgtGatewayConstants.END_USER_NAME);
        }
        if (messageContext.getPropertyKeySet().contains(APIMgtGatewayConstants.USER_ID)) {
            return (String) messageContext.getProperty(APIMgtGatewayConstants.USER_ID);
        }
        return null;
    }

    @Override
    public String getEndUserIP() {

        if (messageContext.getPropertyKeySet().contains(Constants.USER_IP_PROPERTY)) {
            return (String) messageContext.getProperty(Constants.USER_IP_PROPERTY);
        }
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> customProperties;

        if (analyticsCustomDataProvider != null) {
            customProperties = analyticsCustomDataProvider.getCustomProperties(messageContext);
        } else {
            customProperties = new HashMap<>();
        }
        customProperties.put(Constants.API_USER_NAME_KEY, getUserName());
        customProperties.put(Constants.API_CONTEXT_KEY, getApiContext());
        customProperties.put(Constants.RESPONSE_SIZE, getResponseSize());
        customProperties.put(Constants.RESPONSE_CONTENT_TYPE, getResponseContentType());
        return customProperties;
    }

    private String getApiContext() {

        if (messageContext.getPropertyKeySet().contains(JWTConstants.REST_API_CONTEXT)) {
            return (String) messageContext.getProperty(JWTConstants.REST_API_CONTEXT);
        }
        return null;
    }

    private boolean isSuccessRequest() {

        return !messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE)
                && APISecurityUtils.getAuthenticationContext(messageContext) != null;
    }

    private boolean isFaultRequest() {

        return messageContext.getPropertyKeySet().contains(SynapseConstants.ERROR_CODE);
    }

    private boolean isAuthFaultRequest() {

        int errorCode = getErrorCode();
        return errorCode >= Constants.ERROR_CODE_RANGES.AUTH_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.AUTH_FAILURE__END;
    }

    private boolean isThrottledFaultRequest() {

        int errorCode = getErrorCode();
        return errorCode >= Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE__END;
    }

    private boolean isTargetFaultRequest() {

        int errorCode = getErrorCode();
        return (errorCode >= Constants.ERROR_CODE_RANGES.TARGET_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.TARGET_FAILURE__END)
                || errorCode == Constants.ENDPOINT_SUSPENDED_ERROR_CODE;
    }

    private int getErrorCode() {

        return (int) messageContext.getProperty(SynapseConstants.ERROR_CODE);
    }

    public long getBackendLatency() {

        if (isCacheHit()) {
            return 0L;
        }
        Object backendStartTimeObj = messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY);
        long backendStartTime = backendStartTimeObj == null ? 0L : (long) backendStartTimeObj;
        Object backendEndTimeObj = messageContext.getProperty(Constants.BACKEND_END_TIME_PROPERTY);
        long backendEndTime = backendEndTimeObj == null ? 0L : (long) backendEndTimeObj;
        if (backendStartTime == 0L || backendEndTime == 0L) {
            return 0L;
        } else {
            return backendEndTime - backendStartTime;
        }
    }

    public long getResponseLatency() {

        long requestInTime = (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
        return System.currentTimeMillis() - requestInTime;
    }

    public long getRequestMediationLatency() {

        long requestInTime = (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
        if (isCacheHit()) {
            return System.currentTimeMillis() - requestInTime;
        }
        Object backendStartTimeObj = messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY);
        long backendStartTime = backendStartTimeObj == null ? 0L : (long) backendStartTimeObj;
        if (backendStartTime == 0L) {
            return System.currentTimeMillis() - requestInTime;
        } else {
            return backendStartTime - requestInTime;
        }
    }

    public long getResponseMediationLatency() {

        if (isCacheHit()) {
            return 0;
        }
        Object backendEndTimeObj = messageContext.getProperty(Constants.BACKEND_END_TIME_PROPERTY);
        long backendEndTime = backendEndTimeObj == null ? 0L : (long) backendEndTimeObj;
        if (backendEndTime == 0L) {
            return 0L;
        } else {
            return System.currentTimeMillis() - backendEndTime;
        }
    }

    public int getResponseSize() {
        int responseSize = 0;
        if (buildResponseMessage == null) {
            Map<String,String> configs = APIManagerConfiguration.getAnalyticsProperties();
            if (configs.containsKey(Constants.BUILD_RESPONSE_MESSAGE_CONFIG)) {
                buildResponseMessage = Boolean.parseBoolean(configs.get(Constants.BUILD_RESPONSE_MESSAGE_CONFIG));
            } else {
                buildResponseMessage = false;
            }
        }
        Map headers = (Map) messageContext.getProperty(TRANSPORT_HEADERS);
        if (headers != null  && headers.get(HttpHeaders.CONTENT_LENGTH) != null) {
            responseSize = Integer.parseInt(headers.get(HttpHeaders.CONTENT_LENGTH).toString());
        }
        if (responseSize == 0 && buildResponseMessage) {
            try {
                RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
            } catch (IOException ex) {
                //In case of an exception, it won't be propagated up,and set response size to 0
                log.error("Error occurred while building the message to" +
                        " calculate the response body size", ex);
            } catch (XMLStreamException ex) {
                log.error("Error occurred while building the message to calculate the response" +
                        " body size", ex);
            }

            SOAPEnvelope env = messageContext.getEnvelope();
            if (env != null) {
                SOAPBody soapbody = env.getBody();
                if (soapbody != null) {
                    byte[] size = soapbody.toString().getBytes(Charset.defaultCharset());
                    responseSize =  size.length;
                }
            }
        }
        return responseSize;
    }

    public String getResponseContentType() {
        Map headers = (Map) messageContext.getProperty(TRANSPORT_HEADERS);
        if (headers != null && headers.get(HttpHeaders.CONTENT_TYPE) != null) {
            return headers.get(HttpHeaders.CONTENT_TYPE).toString();
        }
        return UNKNOWN_VALUE;
    }
}
