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

package org.wso2.carbon.apimgt.gateway.handlers.analytics.collectors.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.AnalyticsUtils;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.apimgt.keymgt.model.SubscriptionDataStore;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.apimgt.keymgt.model.exception.DataLoadingException;
import org.wso2.carbon.apimgt.keymgt.model.impl.SubscriptionDataLoaderImpl;
import org.wso2.carbon.apimgt.usage.publisher.dto.AnalyticsEvent;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Contain the common data collectors
 */
public class CommonRequestDataCollector {
    private static final Log log = LogFactory.getLog(AnalyticsUtils.class);

    public static API getAPIMetaData(MessageContext messageContext) {
        String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(apiContext);
        if (tenantDomain == null) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        SubscriptionDataStore store = SubscriptionDataHolder.getInstance().getTenantSubscriptionStore(tenantDomain);
        API api = store.getApiByContextAndVersion(apiContext, apiVersion);
        if (api == null) {
            try {
                api = new SubscriptionDataLoaderImpl().getApi(apiContext, apiVersion);
            } catch (DataLoadingException e) {
                log.error("Error occurred when getting api.", e);
            }
        }
        return api;
    }

    public void setUnknownApp(AnalyticsEvent analyticsEvent) {
        analyticsEvent.setApplicationId(Constants.UNKNOWN_VALUE);
        analyticsEvent.setApplicationName(Constants.UNKNOWN_VALUE);
        analyticsEvent.setKeyType(Constants.UNKNOWN_VALUE);
        analyticsEvent.setApplicationOwner(Constants.UNKNOWN_VALUE);
    }

    public void setAnonymousApp(AnalyticsEvent analyticsEvent) {
        analyticsEvent.setApplicationId(Constants.ANONYMOUS_VALUE);
        analyticsEvent.setApplicationName(Constants.ANONYMOUS_VALUE);
        analyticsEvent.setKeyType(Constants.ANONYMOUS_VALUE);
        analyticsEvent.setApplicationOwner(Constants.ANONYMOUS_VALUE);
    }

    public void setApplicationData(AuthenticationContext authContext, AnalyticsEvent analyticsEvent) {
        analyticsEvent.setApplicationId(authContext.getApplicationUUID());
        analyticsEvent.setApplicationName(authContext.getApplicationName());
        analyticsEvent.setKeyType(authContext.getKeyType());
        analyticsEvent.setApplicationOwner(authContext.getSubscriber());
    }

    public long getBackendLatency(MessageContext messageContext) {
        long backendStartTime = (long) messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY);
        long backendEndTime = (long) messageContext.getProperty(Constants.BACKEND_END_TIME_PROPERTY);
        return backendEndTime - backendStartTime;
    }

    public long getResponseLatency(MessageContext messageContext) {
        long requestInTime = (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
        return System.currentTimeMillis() - requestInTime;
    }

    public long getRequestMediationLatency(MessageContext messageContext) {
        long requestInTime = (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
        long backendStartTime = (long) messageContext.getProperty(Constants.BACKEND_START_TIME_PROPERTY);
        return backendStartTime - requestInTime;
    }

    public long getResponseMediationLatency(MessageContext messageContext) {
        long backendEndTime = (long) messageContext.getProperty(Constants.BACKEND_END_TIME_PROPERTY);
        return System.currentTimeMillis() - backendEndTime;
    }

    public long getRequestTime(MessageContext messageContext) {
        return (long) messageContext.getProperty(Constants.REQUEST_START_TIME_PROPERTY);
    }
}
