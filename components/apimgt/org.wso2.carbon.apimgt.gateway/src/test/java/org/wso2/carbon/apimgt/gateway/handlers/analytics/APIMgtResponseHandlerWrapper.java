/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;

public class APIMgtResponseHandlerWrapper extends APIMgtResponseHandler {
    private APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration;

    public APIMgtResponseHandlerWrapper(APIMgtUsageDataPublisher apiMgtUsageDataPublisher, boolean enabled, boolean
            skipEventReceiverConnection, APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration) {
        this.enabled = enabled;
        this.skipEventReceiverConnection = skipEventReceiverConnection;
        this.publisher = apiMgtUsageDataPublisher;
        this.apiManagerAnalyticsConfiguration = apiManagerAnalyticsConfiguration;
    }

    @Override
    protected APIManagerAnalyticsConfiguration getApiAnalyticsConfiguration() {
        return apiManagerAnalyticsConfiguration;
    }
}

