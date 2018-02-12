/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.micro.gateway.tenant.initializer.listener;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.micro.gateway.common.GatewayListenerNotifier;
import org.wso2.carbon.apimgt.micro.gateway.tenant.initializer.internal.ServiceDataHolder;
import org.wso2.carbon.apimgt.micro.gateway.tenant.initializer.utils.TenantInitializationConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class for creating tenants upon initial server startup
 */
public class ServerStartupListener implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completedServerStartup() {
        ScheduledThreadPoolExecutorImpl.waitForAdminServiceActivation();
    }

    /**
     * Inner class for holding API Synchronization until Authentication Admin Service starts
     * NOTE: The time delay introduced here is a temporary fix.
     * This is tracked by issue https://github.com/wso2/cloud/issues/1581 and will be handled shortly
     */
    static class ScheduledThreadPoolExecutorImpl {
        static void waitForAdminServiceActivation() {
            ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            executor.schedule(new Runnable() {
                public void run() {
                    try {
                        initializeTenant();
                        loadTenant();
                        GatewayListenerNotifier.notifyListeners();
                    } catch (Exception e) {
                        log.error("An error occurred while initializing and loading tenant upon initial server " +
                                "startup.", e);
                    }
                }
            }, TenantInitializationConstants.DEFAULT_WAIT_DURATION, TimeUnit.SECONDS);
        }
    }

    /**
     * Method to create a tenant upon initial server startup
     */
    private static void initializeTenant() throws Exception {
        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        TenantMgtAdminService tenantMgtAdminService = new TenantMgtAdminService();
        APIManagerConfiguration config = ServiceDataHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        String email = MultitenantUtils.getTenantAwareUsername(username);
        try {
            CommonUtil.validateEmail(email);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid email is provided: " + email);
            }
            email = TenantInitializationConstants.DEFAULT_EMAIL;
        }
        if (CommonUtil.isDomainNameAvailable(tenantDomain)) {
            tenantInfoBean.setActive(true);
            tenantInfoBean.setAdmin(email);
            tenantInfoBean.setAdminPassword(password);
            tenantInfoBean.setFirstname(TenantInitializationConstants.DEFAULT_FIRST_NAME);
            tenantInfoBean.setLastname(TenantInitializationConstants.DEFAULT_LAST_NAME);
            tenantInfoBean.setTenantDomain(tenantDomain);
            tenantInfoBean.setEmail(email);
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext()
                        .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

                tenantMgtAdminService.addTenant(tenantInfoBean);
                tenantMgtAdminService.activateTenant(tenantDomain);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
            log.info("Successfully initialized tenant with tenant domain: " + tenantDomain);
        } else {
            log.info("Tenant with tenant domain " + tenantDomain + " already exists.");
        }
    }

    /**
     * Method to load the configurations of a tenant
     */
    private static void loadTenant() {
        String tenantDomain;
        APIManagerConfiguration config = ServiceDataHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        tenantDomain = MultitenantUtils.getTenantDomain(username);
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantDomain(tenantDomain);
            ConfigurationContext context =
                    ServiceDataHolder.getInstance().getConfigurationContextService().getServerConfigContext();
            TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, context);
            log.info("Successfully loaded tenant with tenant domain : " + tenantDomain);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void completingServerStartup() {
    }
}
