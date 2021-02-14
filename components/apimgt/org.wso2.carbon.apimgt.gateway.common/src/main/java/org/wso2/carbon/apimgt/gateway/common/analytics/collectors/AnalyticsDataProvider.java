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

package org.wso2.carbon.apimgt.gateway.common.analytics.collectors;

import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Target;

/**
 * Data provider interface to extract request data
 */
public interface AnalyticsDataProvider {
    boolean isSuccessRequest();

    boolean isFaultRequest();

    boolean isAnonymous();

    boolean isAuthenticated();

    boolean isAuthFaultRequest();

    boolean isThrottledFaultRequest();

    boolean isTargetFaultRequest();

    boolean isResourceNotFound();

    boolean isMethodNotAllowed();

    API getApi();

    Application getApplication();

    Latencies getLatency();

    Operation getOperation();

    Target getTarget();

    Latencies getLatencies();

    MetaInfo getMetaInfo();

    int getProxyResponseCode();

    int getTargetResponseCode();

    long getRequestTime();

    Error getError();

    String getUserAgentHeader();

}
