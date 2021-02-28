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
import org.wso2.carbon.apimgt.common.gateway.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.*;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.UUID;

public class WebSocketAnalyticsDataProvider implements AnalyticsDataProvider {
    private static final Log log = LogFactory.getLog(WebSocketAnalyticsDataProvider.class);

    ChannelHandlerContext ctx;

    public WebSocketAnalyticsDataProvider(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    private Object getPropertyFromCtx(String key) {
        Object prop = ctx.channel().attr(WebSocketUtils.WSO2_PROPERTIES).get();
        if (prop != null) {
            Map<String, Object> properties = (Map<String, Object>) prop;
            return properties.get(key);
        }
        return null;
    }

    private AuthenticationContext getAuthenticationContext()  {
        Object authContext = getPropertyFromCtx(APISecurityUtils.API_AUTH_CONTEXT);
        if (authContext != null) {
            return (AuthenticationContext) authContext;
        }
        return null;
    }

    @Override
    public boolean isSuccessRequest() {
        return getAuthenticationContext() != null && getErrorCode() == -1;
    }

    @Override
    public boolean isFaultRequest() {
        return getErrorCode() > -1;
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
    public boolean isAuthFaultRequest() {
        int errorCode = getErrorCode();
        return errorCode >= Constants.ERROR_CODE_RANGES.AUTH_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.AUTH_FAILURE__END;
    }

    @Override
    public boolean isThrottledFaultRequest() {
        int errorCode = getErrorCode();
        return errorCode >= Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE_START
                && errorCode < Constants.ERROR_CODE_RANGES.THROTTLED_FAILURE__END;
    }

    @Override
    public boolean isTargetFaultRequest() {
        return false;
    }

    @Override
    public boolean isResourceNotFound() {
        return getErrorCode() == Constants.RESOURCE_NOT_FOUND_ERROR_CODE;
    }

    @Override
    public boolean isMethodNotAllowed() {
        return false;
    }

    @Override
    public API getApi() {
        String apiContext = (String) getPropertyFromCtx(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) getPropertyFromCtx(RESTConstants.SYNAPSE_REST_API_VERSION);
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
        if(apiObj != null) {
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
    public Application getApplication() {
        AuthenticationContext authContext = getAuthenticationContext();
        if (authContext != null) {
            Application application = new Application();
            application.setApplicationId(authContext.getApplicationUUID());
            application.setApplicationName(authContext.getApplicationName());
            application.setApplicationOwner(authContext.getSubscriber());
            application.setKeyType(authContext.getKeyType());
            return application;
        }
        return null;
    }

    @Override
    public Operation getOperation() {
        Operation operation = new Operation();
        String method = (String) getPropertyFromCtx(APIMgtGatewayConstants.HTTP_METHOD);
        operation.setApiMethod(method);
        String matchingResource = (String) getPropertyFromCtx(APIConstants.API_ELECTED_RESOURCE);
        operation.setApiResourceTemplate(matchingResource);
        return operation;
    }

    @Override
    public Target getTarget() {
        Target target = new Target();

        // These properties are not applicable for WS API
        target.setResponseCacheHit(false);
        target.setTargetResponseCode(0);

        String endpointAddress = (String) getPropertyFromCtx(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS);
        target.setDestination(endpointAddress);
        return target;
    }

    @Override
    public Latencies getLatencies() {
        // Not applicable for WS API
        return new Latencies();
    }

    @Override
    public MetaInfo getMetaInfo() {
        MetaInfo metaInfo = new MetaInfo();
        metaInfo.setCorrelationId(UUID.randomUUID().toString());
        metaInfo.setDeploymentId(Constants.DEPLOYMENT_ID);
        metaInfo.setGatewayType(APIMgtGatewayConstants.GATEWAY_TYPE);
        metaInfo.setRegionId(Constants.REGION_ID);
        return metaInfo;
    }

    @Override
    public int getProxyResponseCode() {
        return 0;
    }

    @Override
    public int getTargetResponseCode() {
        return 0;
    }

    @Override
    public long getRequestTime() {
        Object requestStartTime = getPropertyFromCtx(Constants.REQUEST_START_TIME_PROPERTY);
        if (requestStartTime != null) {
            return (long) requestStartTime;
        }
        return -1L;
    }

    @Override
    public Error getError() {
        int errorCode = getErrorCode();
        Object errorMessage = getPropertyFromCtx(SynapseConstants.ERROR_MESSAGE);
        if (errorCode > -1 && errorMessage != null) {
            Error error = new Error();
            error.setErrorCode(errorCode);
            error.setErrorMessage((String) errorMessage);
            return error;
        }
        return null;
    }

    @Override
    public String getUserAgentHeader() {
        return (String) getPropertyFromCtx(Constants.USER_AGENT_PROPERTY);
    }

    private int getErrorCode() {
        Object errorCode = getPropertyFromCtx(SynapseConstants.ERROR_CODE);
        if (errorCode != null) {
            return (int) errorCode;
        }
        return -1;
    }

}
