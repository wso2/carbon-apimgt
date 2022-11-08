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
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.andes.listeners.BrokerLifecycleListener;
import org.wso2.carbon.andes.service.QpidService;
import org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.apimgt.broker.lifecycle", 
         immediate = true)
public class LifecycleComponent {

    private static final Log log = LogFactory.getLog(LifecycleComponent.class);

    @Activate
    protected void activate(ComponentContext context) {
        log.debug("Activating component...");
        return;
    }

    @Reference(
             name = "QpidService", 
             service = org.wso2.carbon.andes.service.QpidService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetQpidService")
    public void setQpidService(QpidService qpidService) {
        log.debug("Setting QpidService...");
        ServiceReferenceHolder.getInstance().setQpidService(qpidService);
        if (qpidService != null) {
            qpidService.registerBrokerLifecycleListener(new BrokerLifecycleListener() {

                @Override
                public void onShuttingdown() {
                    if (ServiceReferenceHolder.getInstance().getListenerShutdownServices().isEmpty()) {
                        return;
                    }
                    log.debug("Triggering a Shutdown of the Listener...");
                    for (JMSListenerShutDownService listenerShutdownService :
                            ServiceReferenceHolder.getInstance().getListenerShutdownServices()) {
                        listenerShutdownService.shutDownListener();
                    }
                }

                @Override
                public void onShutdown() {
                }
            });
        }
    }

    public void unsetQpidService(QpidService qpidService) {
        log.debug("Un Setting QpidService...");
        ServiceReferenceHolder.getInstance().setQpidService(null);
    }

    @Reference(
             name = "shutdown.listener", 
             service = org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService.class,
             cardinality = ReferenceCardinality.MULTIPLE,
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "removeShutDownService")
    public void addShutDownService(JMSListenerShutDownService shutDownService) {
        log.debug("Adding JMS Listener Shutdown Service");
        ServiceReferenceHolder.getInstance().addListenerShutdownService(shutDownService);
    }

    public void removeShutDownService(JMSListenerShutDownService shutDownService) {
        log.debug("Removing JMS Listener Shutdown Service");
        ServiceReferenceHolder.getInstance().removeListenerShutdownService(shutDownService);

    }
}

