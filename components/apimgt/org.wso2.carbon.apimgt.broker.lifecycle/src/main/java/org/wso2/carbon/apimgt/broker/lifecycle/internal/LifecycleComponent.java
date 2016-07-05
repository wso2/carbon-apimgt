/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.carbon.apimgt.broker.lifecycle.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.andes.service.QpidService;
import org.wso2.carbon.apimgt.broker.lifecycle.impl.JMSClientShutdownListener;
import org.wso2.carbon.apimgt.broker.lifecycle.service.ShutdownNotifierService;


/**
 * This components registers a Broker Lifecycle Listener. This component will only get activated when andes bundle is
 * present.
 */

/**
 * @scr.component name="org.wso2.apimgt.broker.lifecycle" immediate="true"
 * @scr.reference name="QpidService"
 * interface="org.wso2.carbon.andes.service.QpidService" cardinality="0..1"
 * policy="dynamic" bind="setQpidService" unbind="unsetQpidService"
 */

public class LifecycleComponent {

    private static final Log log = LogFactory.getLog(LifecycleComponent.class);
    private ServiceRegistration registration;

    protected void activate(ComponentContext context) {
        log.debug("Activating component...");

        BundleContext bundleContext = context.getBundleContext();

        registration = bundleContext.registerService(
                ShutdownNotifierService.class.getName(),
                new ShutdownNotifierService() {
                    @Override
                    public void completeShutDown() {
                        ServiceReferenceHolder.getInstance().setShutDownStatus(true);
                    }
                }, null);

        return;
    }

    public void setQpidService(QpidService qpidService){
        log.debug("Setting QpidService...");
        ServiceReferenceHolder.getInstance().setQpidService(qpidService);
        if(qpidService != null){
            qpidService.registerBrokerLifecycleListener(new JMSClientShutdownListener());
        }
    }

    public void unsetQpidService(QpidService qpidService){
        log.debug("Un Setting QpidService...");
        ServiceReferenceHolder.getInstance().setQpidService(null);
    }

}
