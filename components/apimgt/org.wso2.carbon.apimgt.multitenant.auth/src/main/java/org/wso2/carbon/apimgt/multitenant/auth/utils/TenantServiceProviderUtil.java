/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.multitenant.auth.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.multitenant.auth.internal.MultiTenantAuthDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;

/**
 * Utility class to resolve OAuth2 application credentials (client_id / client_secret)
 * for a given tenant's service provider registered in IS.
 */
public final class TenantServiceProviderUtil {

    private static final Log LOG = LogFactory.getLog(TenantServiceProviderUtil.class);
    private static final String OAUTH2_INBOUND_AUTH_TYPE = "oauth2";

    private TenantServiceProviderUtil() {
    }

    /**
     * Retrieve the service provider for the given application name within the specified tenant.
     *
     * @param tenantDomain The tenant domain (e.g., "abc.com").
     * @param appName      The name of the service provider.
     * @return The resolved {@link ServiceProvider}.
     * @throws Exception If the service is unavailable or the SP is not found.
     */
    public static ServiceProvider getServiceProvider(String tenantDomain, String appName) throws Exception {

        ApplicationManagementService appMgtService =
                MultiTenantAuthDataHolder.getInstance().getApplicationManagementService();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving service provider '" + appName + "' for tenant: " + tenantDomain);
        }

        if (appMgtService == null) {
            throw new IllegalStateException(
                    "ApplicationManagementService is not available. Cannot retrieve service provider for tenant: "
                            + tenantDomain);
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);

            ServiceProvider sp = appMgtService.getServiceProvider(appName, tenantDomain);
            if (sp == null) {
                throw new Exception(
                        "Service provider '" + appName + "' not found in tenant: " + tenantDomain);
            }
            return sp;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Resolve the OAuth2 client ID for a given tenant domain by looking up the
     * specified application name in that tenant's application registry.
     *
     * @param tenantDomain The tenant domain (e.g., "abc.com").
     * @param appName      The name of the service provider / application registered in the tenant.
     * @return The OAuth2 client ID (consumer key) of the application.
     * @throws Exception If the SP is not found or has no OAuth2 inbound config.
     */
    public static String resolveClientId(String tenantDomain, String appName) throws Exception {

        ServiceProvider sp = getServiceProvider(tenantDomain, appName);
        InboundAuthenticationRequestConfig[] authRequestConfigs =
                getInboundAuthenticationRequestConfigs(sp, tenantDomain, appName);

        for (InboundAuthenticationRequestConfig config : authRequestConfigs) {
            if (config != null && OAUTH2_INBOUND_AUTH_TYPE.equals(config.getInboundAuthType())) {
                String clientId = config.getInboundAuthKey();
                if (clientId == null || clientId.trim().isEmpty()) {
                    throw new Exception("OAuth2 inbound authentication key is missing for SP '"
                            + appName + "' in tenant: " + tenantDomain);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Resolved client ID for tenant '" + tenantDomain
                            + "', app '" + appName + "': " + clientId);
                }
                return clientId;
            }
        }

        throw new Exception("No OAuth2/OIDC inbound authentication config found for SP '"
                + appName + "' in tenant: " + tenantDomain);
    }

    /**
     * Extract the inbound authentication request configurations from the given service provider.
     *
     * @param sp           The service provider.
     * @param tenantDomain The tenant domain (for error messages).
     * @param appName      The application name (for error messages).
     * @return The array of {@link InboundAuthenticationRequestConfig}.
     * @throws Exception If no inbound authentication config is found.
     */
    private static InboundAuthenticationRequestConfig[] getInboundAuthenticationRequestConfigs(
            ServiceProvider sp, String tenantDomain, String appName) throws Exception {

        InboundAuthenticationConfig inboundAuthConfig = sp.getInboundAuthenticationConfig();
        if (inboundAuthConfig == null) {
            throw new Exception("No inbound authentication config found for SP '"
                    + appName + "' in tenant: " + tenantDomain);
        }

        InboundAuthenticationRequestConfig[] authRequestConfigs =
                inboundAuthConfig.getInboundAuthenticationRequestConfigs();
        if (authRequestConfigs == null) {
            throw new Exception("No inbound authentication request configs found for SP '"
                    + appName + "' in tenant: " + tenantDomain);
        }
        return authRequestConfigs;
    }
}
