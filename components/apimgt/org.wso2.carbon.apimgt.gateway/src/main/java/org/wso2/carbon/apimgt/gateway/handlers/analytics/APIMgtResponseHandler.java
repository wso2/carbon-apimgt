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

import org.apache.synapse.MessageContext;
import org.wso2.carbon.apimgt.gateway.mediators.APIMgtCommonExecutionPublisher;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;

/**
 * This mediator is to publish events upon success API invocations
 */

public class APIMgtResponseHandler extends APIMgtCommonExecutionPublisher {

    public APIMgtResponseHandler() {
        super();
    }

    public boolean mediate(MessageContext mc) {
        // Skip handling analytics
        return true; // Should never stop the message flow
    }

    protected APIManagerAnalyticsConfiguration getApiAnalyticsConfiguration() {
        return UsageComponent.getAmConfigService().getAPIAnalyticsConfiguration();
    }

    public boolean isContentAware() {
        return false;
    }
}

