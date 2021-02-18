/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse;

import org.wso2.carbon.apimgt.common.gateway.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.API;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Application;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Error;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Latencies;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.MetaInfo;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Operation;
import org.wso2.carbon.apimgt.common.gateway.analytics.publishers.dto.Target;

public class SseEventDataProvider implements AnalyticsDataProvider {

    @Override
    public boolean isSuccessRequest() {
        return true;
    }

    @Override
    public boolean isFaultRequest() {
        return false;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public boolean isAuthFaultRequest() {
        return false;
    }

    @Override
    public boolean isThrottledFaultRequest() {
        return false;
    }

    @Override
    public boolean isTargetFaultRequest() {
        return false;
    }

    @Override
    public boolean isResourceNotFound() {
        return false;
    }

    @Override
    public boolean isMethodNotAllowed() {
        return false;
    }

    @Override
    public API getApi() {
        return null;
    }

    @Override
    public Application getApplication() {
        return null;
    }

    @Override
    public Latencies getLatency() {
        return null;
    }

    @Override
    public Operation getOperation() {
        return null;
    }

    @Override
    public Target getTarget() {
        return null;
    }

    @Override
    public Latencies getLatencies() {
        return null;
    }

    @Override
    public MetaInfo getMetaInfo() {
        return null;
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
        return 0;
    }

    @Override
    public Error getError() {
        return null;
    }

    @Override
    public String getUserAgentHeader() {
        return null;
    }
}
