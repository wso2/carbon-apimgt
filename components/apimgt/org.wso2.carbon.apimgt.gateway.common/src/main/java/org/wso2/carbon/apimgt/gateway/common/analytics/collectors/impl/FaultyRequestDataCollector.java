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

package org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.common.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.FaultDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl.fault.AuthFaultDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl.fault.MethodNotAllowedFaultDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl.fault.ResourceNotFoundFaultDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl.fault.TargetFaultDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl.fault.ThrottledFaultDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl.fault.UnclassifiedFaultDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Event;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Target;

/**
 * Faulty request data collector
 */
public class FaultyRequestDataCollector extends CommonRequestDataCollector implements RequestDataCollector {
    private static final Log log = LogFactory.getLog(FaultyRequestDataCollector.class);
    private FaultDataCollector authDataCollector;
    private FaultDataCollector throttledDataCollector;
    private FaultDataCollector targetDataCollector;
    private FaultDataCollector resourceNotFoundDataCollector;
    private FaultDataCollector methodNotAllowedDataCollector;
    private FaultDataCollector unclassifiedFaultDataCollector;
    private AnalyticsDataProvider provider;

    public FaultyRequestDataCollector(AnalyticsDataProvider provider) {
        super(provider);
        this.provider = provider;
        this.authDataCollector = new AuthFaultDataCollector(provider);
        this.throttledDataCollector = new ThrottledFaultDataCollector(provider);
        this.targetDataCollector = new TargetFaultDataCollector(provider);
        this.resourceNotFoundDataCollector = new ResourceNotFoundFaultDataCollector(provider);
        this.methodNotAllowedDataCollector = new MethodNotAllowedFaultDataCollector(provider);
        this.unclassifiedFaultDataCollector = new UnclassifiedFaultDataCollector(provider);
    }

    public void collectData() {
        log.debug("Handling faulty analytics types");
        Event faultyEvent = getFaultyEvent();

        if (provider.isAuthFaultRequest()) {
            handleAuthFaultRequest(faultyEvent);
        } else if (provider.isThrottledFaultRequest()) {
            handleThrottledFaultRequest(faultyEvent);
        } else if (provider.isTargetFaultRequest()) {
            handleTargetFaultRequest(faultyEvent);
        } else if (provider.isResourceNotFound()) {
            handleResourceNotFoundFaultRequest(faultyEvent);
        } else if (provider.isMethodNotAllowed()) {
            handleMethodNotAllowedFaultRequest(faultyEvent);
        } else {
            handleOtherFaultRequest(faultyEvent);
        }
    }

    private void handleAuthFaultRequest(Event faultyEvent) {
        authDataCollector.collectFaultData(faultyEvent);
    }

    private void handleThrottledFaultRequest(Event faultyEvent) {
        throttledDataCollector.collectFaultData(faultyEvent);
    }

    private void handleTargetFaultRequest(Event faultyEvent) {
        targetDataCollector.collectFaultData(faultyEvent);
    }

    private void handleResourceNotFoundFaultRequest(Event faultyEvent) {
        resourceNotFoundDataCollector.collectFaultData(faultyEvent);
    }

    private void handleMethodNotAllowedFaultRequest(Event faultyEvent) {
        methodNotAllowedDataCollector.collectFaultData(faultyEvent);
    }

    private void handleOtherFaultRequest(Event faultyEvent) {
        unclassifiedFaultDataCollector.collectFaultData(faultyEvent);
    }

    private Event getFaultyEvent() {
        long requestInTime = provider.getRequestTime();
        String offsetDateTime = getTimeInISO(requestInTime);

        Event event = new Event();
        API api = provider.getApi();
        Target target = new Target();
        target.setTargetResponseCode(Constants.UNKNOWN_INT_VALUE);
        MetaInfo metaInfo = provider.getMetaInfo();
        Error error = provider.getError();
        event.setApi(api);
        event.setTarget(target);
        event.setProxyResponseCode(provider.getProxyResponseCode());
        event.setRequestTimestamp(offsetDateTime);
        event.setError(error);
        event.setMetaInfo(metaInfo);

        return event;
    }
}
