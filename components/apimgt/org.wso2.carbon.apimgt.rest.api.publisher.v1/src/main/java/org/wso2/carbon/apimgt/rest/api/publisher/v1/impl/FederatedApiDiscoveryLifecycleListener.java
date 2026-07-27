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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Shuts down the federated API discovery executor when the Publisher REST API webapp is undeployed,
 * so its worker threads (and the classloader they reference) are not leaked across redeployments.
 */
public class FederatedApiDiscoveryLifecycleListener implements ServletContextListener {

    private static final Log log = LogFactory.getLog(FederatedApiDiscoveryLifecycleListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // No initialization required; the executor is created lazily by the class loader.
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            FederatedApisApiServiceImpl.shutdownDiscoveryExecutor();
            if (log.isDebugEnabled()) {
                log.debug("Federated API discovery executor shut down on webapp undeployment");
            }
        } catch (Exception e) {
            log.error("Error while shutting down the federated API discovery executor", e);
        }
    }
}
