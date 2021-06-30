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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.exceptions.DataNotFoundException;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Target;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.EventCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultCategory;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.enums.FaultSubCategory;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.FaultCodeClassifier;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;

public class WebSocketAnalyticsDataProvider implements AnalyticsDataProvider {
    private static final Log log = LogFactory.getLog(WebSocketAnalyticsDataProvider.class);

    private ChannelHandlerContext ctx;

    public WebSocketAnalyticsDataProvider(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    private AuthenticationContext getAuthenticationContext()  {
        Object authContext = WebSocketUtils.getPropertyFromChannel(APISecurityUtils.API_AUTH_CONTEXT, ctx);
        if (authContext != null) {
            return (AuthenticationContext) authContext;
        }
        return null;
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

    private boolean isSuccessRequest() {
        return getAuthenticationContext() != null && getErrorCode() == -1;
    }

    private boolean isFaultRequest() {
        return getErrorCode() > -1;
    }

    private int getErrorCode() {
        Object errorCode = WebSocketUtils.getPropertyFromChannel(SynapseConstants.ERROR_CODE, ctx);
        if (errorCode != null) {
            return (int) errorCode;
        }
        return -1;
    }

    @Override
    public boolean isAnonymous() {
        AuthenticationContext authContext = getAuthenticationContext();
        return isAuthenticated() && APIConstants.END_USER_ANONYMOUS.equalsIgnoreCase(authContext.getUsername());
    }

    @Override
    public boolean isAuthenticated() {
        AuthenticationContext authContext = getAuthenticationContext();
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
        }
        return FaultCategory.OTHER;
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
        return false;
    }

    @Override
    public API getApi() {
        String apiContext = (String) WebSocketUtils.getPropertyFromChannel(RESTConstants.REST_API_CONTEXT, ctx);
        String apiVersion = (String) WebSocketUtils.getPropertyFromChannel(RESTConstants.SYNAPSE_REST_API_VERSION, ctx);
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
                throw new RuntimeException("Error occurred when getting API information", e);
            }
        }
        if (apiObj != null) {
            api.setApiId(apiObj.getUuid());
            api.setApiType(apiObj.getApiType());
            api.setApiName(apiObj.getApiName());
            api.setApiVersion(apiObj.getApiVersion());
            api.setApiCreator(apiObj.getApiProvider());
            api.setApiCreatorTenantDomain(MultitenantUtils.getTenantDomain(api.getApiCreator()));
        }
        return api;
    }

    @Override
    public Application getApplication() throws DataNotFoundException {
        AuthenticationContext authContext = getAuthenticationContext();
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
    public Operation getOperation() {
        Operation operation = new Operation();
        String method = (String) WebSocketUtils.getPropertyFromChannel(APIMgtGatewayConstants.HTTP_METHOD, ctx);
        operation.setApiMethod(method);
        String matchingResource = (String) WebSocketUtils.getPropertyFromChannel(APIConstants.API_ELECTED_RESOURCE, ctx);
        operation.setApiResourceTemplate(matchingResource);
        return operation;
    }

    @Override
    public Target getTarget() {
        Target target = new Target();

        // These properties are not applicable for WS API
        target.setResponseCacheHit(false);
        target.setTargetResponseCode(0);

        String endpointAddress = (String) WebSocketUtils.getPropertyFromChannel(
                APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS, ctx);
        target.setDestination(endpointAddress);
        return target;
    }

    @Override
    public Latencies getLatencies() {
        // Not applicable
        return new Latencies();
    }

    @Override
    public MetaInfo getMetaInfo() {
        MetaInfo metaInfo = new MetaInfo();
        metaInfo.setCorrelationId(UUID.randomUUID().toString());
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
        if (isSuccessRequest()) {
            return HttpURLConnection.HTTP_OK;
        }
        return Constants.UNKNOWN_INT_VALUE;
    }

    @Override
    public int getTargetResponseCode() {
        if (isSuccessRequest()) {
            return HttpURLConnection.HTTP_OK;
        }
        return Constants.UNKNOWN_INT_VALUE;
    }

    @Override
    public long getRequestTime() {
        Object requestStartTime = WebSocketUtils.getPropertyFromChannel(Constants.REQUEST_START_TIME_PROPERTY, ctx);
        if (requestStartTime != null) {
            return (long) requestStartTime;
        }
        return -1L;
    }

    @Override
    public Error getError(FaultCategory faultCategory) {
        int errorCode = getErrorCode();
        FaultCodeClassifier faultCodeClassifier = new WebSocketFaultCodeClassifier(ctx);
        FaultSubCategory faultSubCategory = faultCodeClassifier.getFaultSubCategory(faultCategory, errorCode);
        Error error = new Error();
        error.setErrorCode(errorCode);
        error.setErrorMessage(faultSubCategory);
        return error;
    }

    @Override
    public String getUserAgentHeader() {
        return (String) WebSocketUtils.getPropertyFromChannel(Constants.USER_AGENT_PROPERTY, ctx);
    }

    @Override
    public String getEndUserIP() {
        Object userIp = WebSocketUtils.getPropertyFromChannel(Constants.USER_IP_PROPERTY, ctx);
        if (userIp != null) {
            return (String) userIp;
        }
        return null;
    }

}
