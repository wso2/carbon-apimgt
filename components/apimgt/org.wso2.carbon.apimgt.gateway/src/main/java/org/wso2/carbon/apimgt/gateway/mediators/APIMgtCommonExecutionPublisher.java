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

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataBridgeDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.internal.ServiceReferenceHolder;

public class APIMgtCommonExecutionPublisher extends AbstractMediator {
    protected boolean enabled;

    protected boolean skipEventReceiverConnection;

    protected volatile APIMgtUsageDataPublisher publisher;

    public APIMgtCommonExecutionPublisher() {
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            this.initializeDataPublisher();
        }
    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        return true;
    }

    protected void initializeDataPublisher() {
        enabled = APIUtil.isAnalyticsEnabled();
        if (publisher == null) {
            synchronized (this) {
                if (publisher == null) {
                    publisher = new APIMgtUsageDataBridgeDataPublisher();
                }
            }
        }
    }

    protected APIManagerAnalyticsConfiguration getApiManagerAnalyticsConfiguration() {
        return DataPublisherUtil.getApiManagerAnalyticsConfiguration();
    }
}
