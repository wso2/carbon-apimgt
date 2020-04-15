/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.observers;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.dao.KMApplicationDAO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This task provisions the OAuth application for the keymanager management operations needed by any tenant.
 */
public class KMConfigDeployer extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(KMConfigDeployer.class);

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            //check whether an application is already registered for the tenant
            OAuthApplicationInfo oAuthApplicationInfo =
                    KMApplicationDAO.getInstance().getApplicationForTenant(tenantId);
            if (oAuthApplicationInfo == null) { // if not registered
                oAuthApplicationInfo =
                        KeyManagerHolder.getKeyManagerInstance().registerKeyManagerMgtApplication(tenantDomain);
                // add the application info to the AM database
                KMApplicationDAO.getInstance().addApplication(oAuthApplicationInfo.getClientId(),
                        oAuthApplicationInfo.getClientSecret(), tenantId);
            }
        } catch (APIManagementException e) {
            log.error("Failed to register key manager management application for tenant: " + tenantDomain
                    + e.getMessage());
        }
    }
}
