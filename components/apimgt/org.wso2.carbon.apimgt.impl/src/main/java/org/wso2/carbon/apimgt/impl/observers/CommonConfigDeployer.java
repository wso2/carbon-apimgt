/*
 *
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.observers;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This task provisions mandatory configs & Artifacts needed by any tenant. The reason for introducing this task is
 * to prevent {@link org.wso2.carbon.apimgt.gateway.internal.TenantServiceCreator} class being run on None-synapse
 * environments. Configs such as workflow configs, analytic configs should be loaded within this task.
 */
public class CommonConfigDeployer extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(CommonConfigDeployer.class);


    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();


        try {
            APIUtil.loadTenantAPIPolicy(tenantDomain, tenantId);
        } catch (Exception e) {
            log.error("Failed to load tiers.xml to tenant's registry");
        }

        try {
            APIUtil.writeDefinedSequencesToTenantRegistry(tenantId);
        }
        // Need to continue the execution even if we encounter an error.
        catch (Exception e) {
            log.error("Failed to write defined sequences to tenant " + tenantDomain + "'s registry");
        }

        try {
            APIUtil.loadTenantExternalStoreConfig(tenantId);
        } catch (Exception e) {
            log.error("Failed to load external-stores.xml to tenant " + tenantDomain + "'s registry");
        }

        try {
            APIUtil.loadTenantGAConfig(tenantId);
        } catch (Exception e) {
            log.error("Failed to load ga-config.xml to tenant " + tenantDomain + "'s registry");
        }

        try {
            //load workflow-extension configuration to the registry
            APIUtil.loadTenantWorkFlowExtensions(tenantId);
        } catch (Exception e) {
            log.error("Failed to load workflow-extension.xml to tenant " + tenantDomain + "'s registry");
        }

        try {
            //load self signup configurations to the registry
            APIUtil.loadTenantSelfSignUpConfigurations(tenantId);
        } catch (Exception e) {
            log.error("Failed to load sign-up-config.xml to tenant " + tenantDomain + "'s registry");
        }
        try {
            APIManagerAnalyticsConfiguration configuration = ServiceReferenceHolder.getInstance().
                    getAPIManagerConfigurationService().getAPIAnalyticsConfiguration();
            boolean enabled = configuration.isAnalyticsEnabled();
            if (enabled) {
                String bamServerURL = configuration.getBamServerUrlGroups();
                String bamServerUser = configuration.getBamServerUser();
                String bamServerPassword = configuration.getBamServerPassword();
                APIUtil.addBamServerProfile(bamServerURL, bamServerUser, bamServerPassword, tenantId);
            }
        } catch (APIManagementException e) {
            log.error("Failed to load bam profile configuration to tenant " + tenantDomain + "'s registry");
        }
    }
}
