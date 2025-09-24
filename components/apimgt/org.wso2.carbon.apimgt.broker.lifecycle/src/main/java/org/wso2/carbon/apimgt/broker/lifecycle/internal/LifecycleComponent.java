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
        if (log.isDebugEnabled()) {
            log.debug("Activating APIMGT broker lifecycle component");
        }
        log.info("APIMGT broker lifecycle component activated successfully");
        return;
    }

    @Reference(
             name = "QpidService", 
             service = org.wso2.carbon.andes.service.QpidService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetQpidService")
    public void setQpidService(QpidService qpidService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting QpidService reference");
        }
        ServiceReferenceHolder.getInstance().setQpidService(qpidService);
        if (qpidService != null) {
            log.info("QpidService bound successfully, registering broker lifecycle listener");
            qpidService.registerBrokerLifecycleListener(new BrokerLifecycleListener() {

                @Override
                public void onShuttingdown() {
                    if (ServiceReferenceHolder.getInstance().getListenerShutdownServices().isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("No JMS listeners to shutdown");
                        }
                        return;
                    }
                    log.info("Broker shutting down, initiating JMS listener shutdown sequence");
                    for (JMSListenerShutDownService listenerShutdownService :
                            ServiceReferenceHolder.getInstance().getListenerShutdownServices()) {
                        listenerShutdownService.shutDownListener();
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("JMS listener shutdown sequence completed");
                    }
                }

                @Override
                public void onShutdown() {
                    log.info("Broker shutdown completed");
                }
            });
        }
    }

    public void unsetQpidService(QpidService qpidService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting QpidService reference");
        }
        log.info("QpidService unbound");
        ServiceReferenceHolder.getInstance().setQpidService(null);
    }

    @Reference(
             name = "shutdown.listener", 
             service = org.wso2.carbon.apimgt.impl.jms.listener.JMSListenerShutDownService.class,
             cardinality = ReferenceCardinality.MULTIPLE,
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "removeShutDownService")
    public void addShutDownService(JMSListenerShutDownService shutDownService) {
        if (log.isDebugEnabled()) {
            log.debug("Adding JMS listener shutdown service: " + shutDownService.getClass().getSimpleName());
        }
        ServiceReferenceHolder.getInstance().addListenerShutdownService(shutDownService);
    }

    public void removeShutDownService(JMSListenerShutDownService shutDownService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing JMS listener shutdown service: " + shutDownService.getClass().getSimpleName());
        }
        ServiceReferenceHolder.getInstance().removeListenerShutdownService(shutDownService);

    }
}

