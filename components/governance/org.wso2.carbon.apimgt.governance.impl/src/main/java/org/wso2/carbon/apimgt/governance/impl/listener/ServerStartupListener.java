/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.governance.impl.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.impl.GovernanceConstants;
import org.wso2.carbon.apimgt.governance.impl.config.GovernanceConfiguration;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.governance.impl.util.GovernanceUtil;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This listener is triggered at server startup
 */
public class ServerStartupListener implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completedServerStartup() {
        String migrationEnabled = System.getProperty(GovernanceConstants.MIGRATE);
        if (migrationEnabled == null) {
            GovernanceConfiguration governanceConfiguration =
                    ServiceReferenceHolder.getInstance().getGovernanceConfigurationService().getGovernanceConfiguration();
            if (governanceConfiguration != null) {
                GovernanceUtil.loadDefaultRulesets(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            }
        }
    }

    @Override
    public void completingServerStartup() {
    }
}
