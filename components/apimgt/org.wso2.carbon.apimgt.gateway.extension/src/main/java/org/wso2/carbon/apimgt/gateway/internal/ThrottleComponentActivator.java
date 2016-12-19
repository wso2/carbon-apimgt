/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.jms.JmsThrottleReceiver;


/**
 * starts a jms listener and listens to throttle topic
 */
@Component(
        name = "org.wso2.carbon.apimgt.gateway.internal.ThrottleComponentActivator",
        immediate = true
)
public class ThrottleComponentActivator {

    private static final Logger log = LoggerFactory.getLogger(ThrottleComponentActivator.class);

    @Activate
    protected void activate(BundleContext context) {

        if (log.isDebugEnabled()) {
            log.debug("Activating throttling component ...");
        }

        //starting JMS Receiver
        JmsThrottleReceiver jmsThrottleReceiver = new JmsThrottleReceiver();
        jmsThrottleReceiver.start();
        log.info("JmsThrottleReceiver started.......");

        return;
    }

    // TODO: need to implement a shutdown method
    protected void deactivate(BundleContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating component");
        }
    }

}
