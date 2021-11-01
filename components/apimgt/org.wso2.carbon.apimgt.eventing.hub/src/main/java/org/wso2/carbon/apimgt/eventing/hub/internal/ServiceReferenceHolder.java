/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.eventing.hub.internal;

import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * Service reference holder for eventing hub.
 */
public class ServiceReferenceHolder {
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private OutputEventAdapterService outputEventAdapterService;
    private EventStreamService eventStreamService;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        this.outputEventAdapterService = outputEventAdapterService;
    }

    public OutputEventAdapterService getOutputEventAdapterService() {
        return outputEventAdapterService;
    }

    public void setEventStreamService(EventStreamService eventStreamService) {
        this.eventStreamService = eventStreamService;
    }

    public EventStreamService getEventStreamService() {
        return eventStreamService;
    }
}
