/*
 *
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(
        name = "api.publisher.component",
        immediate = true)
public class PublisherServiceComponent {

    private static final Log log = LogFactory.getLog(PublisherServiceComponent.class);

    public PublisherServiceComponent() {
    }

    @Activate
    protected void activate(org.osgi.service.component.ComponentContext context) {
    }

    @Reference(
            name = "org.wso2.carbon.apimgt.governance.service",
            service = org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService.class,
            cardinality = org.osgi.service.component.annotations.ReferenceCardinality.MANDATORY,
            policy = org.osgi.service.component.annotations.ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIMGovernanceService")
    protected void setAPIMGovernanceService(org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService service) {
        if (log.isDebugEnabled()) {
            log.debug("APIMGovernanceService set in PublisherServiceComponent");
        }
        ServiceReferenceHolder.getInstance().setAPIMGovernanceService(service);
    }

    protected void unsetAPIMGovernanceService(org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService service) {
        if (log.isDebugEnabled()) {
            log.debug("APIMGovernanceService unset in PublisherServiceComponent");
        }
        ServiceReferenceHolder.getInstance().setAPIMGovernanceService(null);
    }
}
