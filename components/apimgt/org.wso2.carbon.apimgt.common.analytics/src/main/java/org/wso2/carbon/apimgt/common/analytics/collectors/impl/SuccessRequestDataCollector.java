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
package org.wso2.carbon.apimgt.common.analytics.collectors.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.analytics.Constants;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.analytics.collectors.RequestDataCollector;
import org.wso2.carbon.apimgt.common.analytics.exceptions.AnalyticsException;
import org.wso2.carbon.apimgt.common.analytics.publishers.RequestDataPublisher;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Event;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.analytics.publishers.dto.Target;
import org.wso2.carbon.apimgt.common.analytics.publishers.impl.SuccessRequestDataPublisher;

/**
 * Success request data collector.
 */
public class SuccessRequestDataCollector extends CommonRequestDataCollector implements RequestDataCollector {
    private static final Log log = LogFactory.getLog(SuccessRequestDataCollector.class);
    private RequestDataPublisher processor;
    private AnalyticsDataProvider provider;

    public SuccessRequestDataCollector(AnalyticsDataProvider provider, RequestDataPublisher processor) {
        super(provider);
        this.processor = processor;
        this.provider = provider;
    }

    public SuccessRequestDataCollector(AnalyticsDataProvider provider) {
        this(provider, new SuccessRequestDataPublisher());
    }

    public void collectData() throws AnalyticsException {
        log.debug("Handling success analytics types");

        long requestInTime = provider.getRequestTime();
        String offsetDateTime = getTimeInISO(requestInTime);

        Event event = new Event();
        API api = provider.getApi();
        Operation operation = provider.getOperation();
        Target target = provider.getTarget();

        Application application;
        if (provider.isAnonymous()) {
            application = getAnonymousApp();
        } else {
            application = provider.getApplication();
        }
        Latencies latencies = provider.getLatencies();
        MetaInfo metaInfo = provider.getMetaInfo();
        String userAgent = provider.getUserAgentHeader();
        String userIp = provider.getEndUserIP();
        if (userIp == null) {
            userIp = Constants.UNKNOWN_VALUE;
        }

        event.setApi(api);
        event.setOperation(operation);
        event.setTarget(target);
        event.setApplication(application);
        event.setLatencies(latencies);
        event.setProxyResponseCode(provider.getProxyResponseCode());
        event.setRequestTimestamp(offsetDateTime);
        event.setMetaInfo(metaInfo);
        event.setUserAgentHeader(userAgent);
        event.setUserIp(userIp);

        this.processor.publish(event);
    }

}
