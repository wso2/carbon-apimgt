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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.loader.KeyManagerConfigurationDataRetriever;
import org.wso2.carbon.apimgt.impl.service.KeyMgtRegistrationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.io.FileNotFoundException;

import javax.xml.stream.XMLStreamException;

/**
 * This task provisions mandatory configs & Artifacts needed by any tenant. The reason for introducing this task is
 * to prevent {@link org.wso2.carbon.apimgt.impl.observers.TenantServiceCreator} class being run on None-synapse
 * environments. Configs such as workflow configs, analytic configs should be loaded within this task.
 */
public class CommonConfigDeployer extends AbstractAxis2ConfigurationContextObserver {

    private static final Log log = LogFactory.getLog(CommonConfigDeployer.class);


    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        final String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        final int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        try {
            //TODO adding only the policies to data wouldn't be sufficient. Need to figure out approach after tenant story has finalized
            //Add default set of policies to database
            ThrottleProperties.PolicyDeployer policyDeployer = configuration.getThrottleProperties()
                    .getPolicyDeployer();
            //This has schedule to separate thread due to issues arise when calling this method in same thread
            //Also this will avoid tenant login overhead as well
            if (policyDeployer.isEnabled()) {
                Thread t1 = new Thread(new Runnable() {
                    public void run() {

                        try {
                            APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);
                            APIUtil.addDefaultTenantAsyncThrottlePolicies(tenantDomain, tenantId);
                        } catch (APIManagementException e) {
                            log.error("Error while deploying throttle policies", e);
                        }
                    }
                });
                t1.start();
            }

        } catch (Exception e) {
            log.error("Failed to load default policies to tenant" + tenantDomain, e);
        }

        try {
            APIUtil.loadTenantExternalStoreConfig(tenantDomain);
        } catch (Exception e) {
            log.error("Failed to load external-stores.xml to tenant " + tenantDomain + "'s registry", e);
        }

        try {
            APIUtil.loadTenantGAConfig(tenantDomain);
        } catch (Exception e) {
            log.error("Failed to load ga-config.xml to tenant " + tenantDomain + "'s registry", e);
        }

        try {
            APIUtil.loadAndSyncTenantConf(tenantDomain);
        } catch (APIManagementException e) {
            log.error("Failed to load " + APIConstants.API_TENANT_CONF + " for tenant " + tenantDomain, e);
        } catch (Exception e) { // The generic Exception is handled explicitly so execution does not stop during config deployment
            log.error("Exception when loading " + APIConstants.API_TENANT_CONF + " for tenant " + tenantDomain, e);
        }

        try {
            //Load common operation policies to tenant
            APIUtil.loadCommonOperationPolicies(tenantDomain);
        } catch (Exception e) { // The generic Exception is handled explicitly so execution does not stop during config deployment
            log.error("Exception when loading " + APIConstants.OPERATION_POLICIES + " for tenant " + tenantDomain, e);
        }


        try {
            APIUtil.createDefaultRoles(tenantId);
        } catch (APIManagementException e) {
            log.error("Failed create default roles for tenant " + tenantDomain, e);
        } catch (Exception e) { // The generic Exception is handled explicitly so execution does not stop during config deployment
            log.error("Exception when creating default roles for tenant " + tenantDomain, e);
        }
        try {
            CommonUtil.addDefaultLifecyclesIfNotAvailable(ServiceReferenceHolder.getInstance().getRegistryService()
                                                                  .getConfigSystemRegistry(tenantId), CommonUtil
                                                                  .getRootSystemRegistry(tenantId));
        } catch (RegistryException e) {
            log.error("Error while accessing registry", e);
        } catch (FileNotFoundException e) {
            log.error("Error while find lifecycle.xml", e);
        } catch (XMLStreamException e) {
            log.error("Error while parsing Lifecycle.xml", e);
        }
        KeyManagerConfigurationDataRetriever keyManagerConfigurationDataRetriever =
                new KeyManagerConfigurationDataRetriever(tenantDomain);
        keyManagerConfigurationDataRetriever.startLoadKeyManagerConfigurations();
    }
}
