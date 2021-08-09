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
package org.wso2.carbon.apimgt.eventing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * This class used to activate eventing bundle.
 */
@Component(name = "org.wso2.carbon.apimgt.eventing.internal.ServiceComponent", immediate = true)
public class ServiceComponent {
    private static final Log log = LogFactory.getLog(ServiceComponent.class);

    @Activate
    protected void activate(ComponentContext componentContext) {
        log.info("[TEST][FEATURE_FLAG_REPLACE_EVENT_HUB] ServiceComponent activated");
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        log.info("[TEST][FEATURE_FLAG_REPLACE_EVENT_HUB] ServiceComponent deactivated");
    }
}
