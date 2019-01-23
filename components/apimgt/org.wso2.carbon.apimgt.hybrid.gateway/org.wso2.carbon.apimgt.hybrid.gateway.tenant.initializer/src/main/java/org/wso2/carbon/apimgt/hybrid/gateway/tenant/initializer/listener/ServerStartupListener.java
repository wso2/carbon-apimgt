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

package org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.listener;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.common.GatewayListenerNotifier;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.MicroGatewayCommonUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.internal.ServiceDataHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.utils.TenantInitializationConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class for creating tenants upon initial server startup
 */
public class ServerStartupListener implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completedServerStartup() {
        ScheduledThreadPoolExecutorImpl.waitAndInitialize();
    }

    /**
     * Inner class for holding API and Throttling Synchronization until Authentication Admin Service starts
     */
    static class ScheduledThreadPoolExecutorImpl implements Runnable {
        private int retryCount = 4;
        private int executionCount = 1;
        private static ScheduledThreadPoolExecutor executor;
        private static String adminName;
        private static char[] adminPwd;
        private static String url;

        public static void waitAndInitialize() {
            try {
                String mgtTransport = CarbonUtils.getManagementTransport();
                AxisConfiguration axisConfiguration = ServiceReferenceHolder
                        .getContextService().getServerConfigContext().getAxisConfiguration();
                int mgtTransportPort = CarbonUtils.getTransportProxyPort(axisConfiguration, mgtTransport);
                if (mgtTransportPort <= 0) {
                    mgtTransportPort = CarbonUtils.getTransportPort(axisConfiguration, mgtTransport);
                }
                // Using localhost as the hostname since this is always an internal admin service call.
                // Hostnames that can be retrieved using other approaches does not work in this context.
                url = mgtTransport + "://" + TenantInitializationConstants.LOCAL_HOST_NAME + ":" + mgtTransportPort
                        + "/services/";
                adminName = ServiceDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getRealmConfiguration()
                        .getAdminUserName();
                adminPwd = ServiceDataHolder.getInstance().getRealmService()
                        .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getRealmConfiguration()
                        .getAdminPassword().toCharArray();
                executor = new ScheduledThreadPoolExecutor(1);
                executor.scheduleAtFixedRate(new ScheduledThreadPoolExecutorImpl(),
                        TenantInitializationConstants.DEFAULT_WAIT_DURATION,
                        TenantInitializationConstants.DEFAULT_WAIT_DURATION, TimeUnit.SECONDS);
            } catch (UserStoreException e) {
                log.error("An error occurred while retrieving admin credentials for initializing on-premise " +
                        "gateway configuration.", e);
            }
        }

        public void run() {
            String serviceEndPoint = url + "AuthenticationAdmin";
            try {
                String host = new URL(url).getHost();
                AuthenticationAdminStub authAdminStub =
                        new AuthenticationAdminStub(null, serviceEndPoint);
                ServiceClient client = authAdminStub._getServiceClient();
                Options options = client.getOptions();
                options.setManageSession(true);
                boolean isLoginSuccessful = authAdminStub.login(adminName, String.valueOf(adminPwd), host);
                if (isLoginSuccessful) {
                    try {
                        initializeTenant();
                        loadTenant();
                        GatewayListenerNotifier.notifyListeners();
                    } catch (Exception e) {
                        log.error("An error occurred while initializing tenant upon initial server " +
                                "startup.", e);
                    }
                    executor.shutdown();
                }
            } catch (RemoteException e) {
                log.warn("Login request to authentication admin service failed for URL: " + serviceEndPoint +
                        " with exception " + e.getMessage() + " Retry attempt: " + executionCount +
                        "/" + retryCount);
            } catch (LoginAuthenticationExceptionException e) {
                log.error("Error while authenticating against the authentication admin service for URL: "
                        + serviceEndPoint, e);
                executor.shutdown();
            } catch (MalformedURLException e) {
                log.error("Service URL: " + serviceEndPoint + " is malformed.", e);
                executor.shutdown();
            }
            if (executionCount++ >= retryCount) {
                log.error("Login request to authentication admin service failed for the maximum no. of " +
                        "attempts(" + retryCount + ") for URL: " + serviceEndPoint);
                executor.shutdown();
            }
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
        char[] password = MicroGatewayCommonUtil.getRandomString(20).toCharArray();
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            if (CommonUtil.isDomainNameAvailable(tenantDomain)) {
                tenantInfoBean.setActive(true);
                tenantInfoBean.setAdmin(tenantAwareUsername);
                tenantInfoBean.setAdminPassword(password.toString());
                tenantInfoBean.setFirstname(TenantInitializationConstants.DEFAULT_FIRST_NAME);
                tenantInfoBean.setLastname(TenantInitializationConstants.DEFAULT_LAST_NAME);
                tenantInfoBean.setTenantDomain(tenantDomain);
                tenantInfoBean.setEmail(TenantInitializationConstants.DEFAULT_EMAIL);
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
                MicroGatewayCommonUtil.cleanPasswordCharArray(password);
                log.info("Successfully initialized tenant with tenant domain: " + tenantDomain);
            } else {
                log.info("Tenant with tenant domain " + tenantDomain + " already exists.");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Skipping initializing super tenant space since execution is currently in super tenant flow.");
            }
        }
    }

    /**
     * Method to load the configurations of a tenant
     */
    private static void loadTenant() throws IOException {
        String tenantDomain;
        APIManagerConfiguration config = ServiceDataHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        tenantDomain = MultitenantUtils.getTenantDomain(username);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            try {
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setUsername(MultitenantUtils.getTenantAwareUsername(username));
                ConfigurationContext context =
                        ServiceDataHolder.getInstance().getConfigurationContextService().getServerConfigContext();
                int tenantId = carbonContext.getTenantId(true);
                String path = CarbonUtils.getCarbonTenantsDirPath() + File.separator + tenantId;
                // delete existing configurations of tenant
                FileUtils.deleteDirectory(new File(path));
                // load tenant configuration
                TenantAxisUtils.getTenantAxisConfiguration(tenantDomain, context);
                log.info("Successfully loaded tenant with tenant domain : " + tenantDomain);
            } finally {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Skipping loading super tenant space since execution is currently in super tenant flow.");
            }
        }
    }

    @Override
    public void completingServerStartup() {
    }
}
