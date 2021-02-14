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

package org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl.fault;

import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.AnalyticsDataProvider;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.FaultDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.collectors.impl.CommonRequestDataCollector;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.RequestDataPublisher;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.Event;
import org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto.enums.FaultEventType;

/**
 * Abstract faulty request data collector
 */
public abstract class AbstractFaultDataCollector extends CommonRequestDataCollector implements FaultDataCollector {
    private FaultEventType subType;
    private RequestDataPublisher processor;

    public AbstractFaultDataCollector(AnalyticsDataProvider provider, FaultEventType subType,
            RequestDataPublisher processor) {
        super(provider);
        this.subType = subType;
        this.processor = processor;
    }

    protected final void processRequest(Event faultyEvent) {
        faultyEvent.setErrorType(this.subType.name());
        this.processor.publish(faultyEvent);
    }
}
