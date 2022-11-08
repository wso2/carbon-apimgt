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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.eventing.EventPublisherFactory;
import org.wso2.carbon.apimgt.eventing.hub.EventHubEventPublisherFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.stream.core.EventStreamService;

/**
 * This class is used to activate eventing hub bundle.
 */
@Component(name = "org.wso2.carbon.apimgt.eventing.hub.internal.ServiceComponent", immediate = true)
public class ServiceComponent {
    private static final Log log = LogFactory.getLog(
            org.wso2.carbon.apimgt.eventing.hub.internal.ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        componentContext.getBundleContext().registerService(EventPublisherFactory.class.getName(),
                new EventHubEventPublisherFactory(), null);
        log.info("Eventing Hub ServiceComponent is activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        log.info("Eventing Hub ServiceComponent is deactivated");
    }

    /**
     * Initialize the OutputEventAdapterService dependency.
     *
     * @param outputEventAdapterService OutputEventAdapterService reference
     */
    @Reference(
            name = "event.output.adapter.service",
            service = org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOutputEventAdapterService")
    protected void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        ServiceReferenceHolder.getInstance().setOutputEventAdapterService(outputEventAdapterService);
    }

    /**
     * De-reference the OutputEventAdapterService dependency.
     *
     * @param outputEventAdapterService OutputEventAdapter service object to be unset
     */
    protected void unsetOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        ServiceReferenceHolder.getInstance().setOutputEventAdapterService(null);
    }

    /**
     * Initialize the EventStreamService dependency.
     *
     * @param eventStreamService EventStreamService service reference
     */
    @Reference(
            name = "apimgt.event.stream.service.reference",
            service = org.wso2.carbon.event.stream.core.EventStreamService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {
        ServiceReferenceHolder.getInstance().setEventStreamService(eventStreamService);
    }

    /**
     * De-reference the EventStreamService dependency.
     *
     * @param eventStreamService EventStreamService object to be unset
     */
    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        ServiceReferenceHolder.getInstance().setEventStreamService(null);
    }
}
