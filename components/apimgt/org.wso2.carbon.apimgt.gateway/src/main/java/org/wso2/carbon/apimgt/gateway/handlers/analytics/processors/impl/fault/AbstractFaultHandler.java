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

package org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.impl.fault;

import org.wso2.carbon.apimgt.usage.publisher.dto.FaultyEvent;
import org.wso2.carbon.apimgt.usage.publisher.dto.enums.FAULT_EVENT_TYPE;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.processors.FaultHandler;
import org.wso2.carbon.apimgt.usage.publisher.RequestDataPublisher;

/**
 * Abstract faulty request data collector
 */
public abstract class AbstractFaultHandler implements FaultHandler {
    private FAULT_EVENT_TYPE subType;
    private RequestDataPublisher processor;

    public AbstractFaultHandler(FAULT_EVENT_TYPE subType, RequestDataPublisher processor) {
        this.subType = subType;
        this.processor = processor;
    }

    protected final void processRequest(FaultyEvent faultyEvent) {
        faultyEvent.setErrorType(this.subType.name());
        this.processor.publish(faultyEvent);
    }
}
