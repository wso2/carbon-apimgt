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
 */

package org.wso2.carbon.apimgt.solace.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.solace.notifiers.SolaceApplicationNotifier;
import org.wso2.carbon.apimgt.solace.notifiers.SolaceKeyGenNotifier;
import org.wso2.carbon.apimgt.solace.notifiers.SolaceSubscriptionsNotifier;

@Component
public class SolaceManagerComponent extends APIManagerComponent {

    private static final Log log = LogFactory.getLog(SolaceManagerComponent.class);


    @Activate
    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("API manager component activated");
        }
        BundleContext bundleContext = componentContext.getBundleContext();

        //Registering Notifiers
        bundleContext.registerService(Notifier.class.getName(), new SolaceSubscriptionsNotifier(), null);
        bundleContext.registerService(Notifier.class.getName(), new SolaceApplicationNotifier(), null);
        bundleContext.registerService(Notifier.class.getName(), new SolaceKeyGenNotifier(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating API manager component");
        }
    }
}
