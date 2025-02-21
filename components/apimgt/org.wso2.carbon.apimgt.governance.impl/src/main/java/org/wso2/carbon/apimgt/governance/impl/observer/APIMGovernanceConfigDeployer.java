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

package org.wso2.carbon.apimgt.governance.impl.observer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.impl.util.APIMGovernanceUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This class represents the Governance Config Deployer
 */
public class APIMGovernanceConfigDeployer extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(APIMGovernanceConfigDeployer.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {

        final String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        log.info("Starting to load default rulesets.");
        APIMGovernanceUtil.loadDefaultRulesets(tenantDomain);
        log.info("Starting to load default policies.");
        APIMGovernanceUtil.loadDefaultPolicies(tenantDomain);

    }
}
