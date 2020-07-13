/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.keymgt.listeners;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

public class ServerStartupListener extends AbstractAxis2ConfigurationContextObserver implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        //load subscription data to memory
        SubscriptionDataHolder.getInstance().registerTenantSubscriptionStore(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {
        //load subscription data to memory
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        log.info("Initializing ServerStartupListener for SubscriptionStore for the tenant domain : " + tenantDomain);
        SubscriptionDataHolder.getInstance().registerTenantSubscriptionStore(tenantDomain);
    }

    @Override
    public void terminatedConfigurationContext(ConfigurationContext configCtx) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        SubscriptionDataHolder.getInstance().unregisterTenantSubscriptionStore(tenantDomain);
    }
}
