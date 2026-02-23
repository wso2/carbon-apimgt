/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.internal.service.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Registers the WebSocket-based platform gateway deployment dispatcher when the Internal Data Service
 * webapp starts, so deploy/undeploy events are pushed to connected gateways.
 */
public class PlatformGatewayDeploymentDispatcherListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(PlatformGatewayDeploymentDispatcherListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            WebSocketPlatformGatewayDeploymentDispatcher dispatcher = new WebSocketPlatformGatewayDeploymentDispatcher();
            ServiceReferenceHolder.getInstance().setPlatformGatewayDeploymentDispatcher(dispatcher);
            if (log.isInfoEnabled()) {
                log.info("Platform gateway deployment dispatcher registered (WebSocket push enabled)");
            }
        } catch (Exception e) {
            log.warn("Could not register platform gateway deployment dispatcher: " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            ServiceReferenceHolder.getInstance().setPlatformGatewayDeploymentDispatcher(null);
            if (log.isDebugEnabled()) {
                log.debug("Platform gateway deployment dispatcher unregistered");
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error unregistering dispatcher: " + e.getMessage());
            }
        }
    }
}
