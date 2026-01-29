/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.gatekeeper.observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.GatekeeperService;
import org.wso2.carbon.core.ServerStartupObserver;

/**
 * Server startup observer for the Gatekeeper module.
 * Performs hydration of the LSH index from the database during server startup.
 */
public class GatekeeperStartupObserver implements ServerStartupObserver {

    private static final Log log = LogFactory.getLog(GatekeeperStartupObserver.class);

    @Override
    public void completingServerStartup() {
        // Not used
    }

    @Override
    public void completedServerStartup() {
        log.info("Gatekeeper startup observer triggered - initiating LSH index hydration");

        try {
            // Initialize the GatekeeperService which performs hydration
            GatekeeperService gatekeeperService = GatekeeperService.getInstance();
            gatekeeperService.initialize();

            log.info("Gatekeeper LSH index hydration completed successfully. " +
                    "Index contains " + gatekeeperService.getIndexSize() + " API signatures.");

        } catch (Exception e) {
            log.error("Failed to initialize Gatekeeper service during server startup. " +
                    "Deduplication checks may not function correctly.", e);
        }
    }
}
