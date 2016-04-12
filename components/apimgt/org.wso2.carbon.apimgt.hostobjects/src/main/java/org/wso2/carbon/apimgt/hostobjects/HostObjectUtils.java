/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.hostobjects;

import java.util.Comparator;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.hostobjects.internal.HostObjectComponent;
import org.wso2.carbon.apimgt.hostobjects.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.client.SubscriberKeyMgtClient;
import org.wso2.carbon.apimgt.keymgt.client.ProviderKeyMgtClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.cache.Caching;

public class HostObjectUtils {
    private static final Log log = LogFactory.getLog(APIProviderHostObject.class);
    private static ConfigurationContextService configContextService = null;

    public static void setConfigContextService(ConfigurationContextService configContext) {
        HostObjectUtils.configContextService = configContext;
    }

    public static ConfigurationContext getConfigContext() throws APIManagementException {
        if (configContextService == null) {
            throw new APIManagementException("ConfigurationContextService is null");
        }

        return configContextService.getServerConfigContext();

    }

    /**
     * Get the running transport port
     *
     * @param transport [http/https]
     * @return port
     */
    public static String getBackendPort(String transport) {
        int port;
        String backendPort;
        try {
            port = CarbonUtils.getTransportProxyPort(getConfigContext(), transport);
            if (port == -1) {
                port = CarbonUtils.getTransportPort(getConfigContext(), transport);
            }
            backendPort = Integer.toString(port);
            return backendPort;
        } catch (APIManagementException e) {
            log.error(e.getMessage());
            return null;

        }
    }

    protected static SubscriberKeyMgtClient getKeyManagementClient() throws APIManagementException {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        if (username == null || password == null) {
            handleException("Authentication credentials for API key manager unspecified");
        }

        try {
            return new SubscriberKeyMgtClient(url, username, password);
        } catch (Exception e) {
            handleException("Error while initializing the subscriber key management client", e);
            return null;
        }
    }

    /**
     * Used to get instance of ProviderKeyMgtClient
     *
     * @return ProviderKeyMgtClient
     * @throws APIManagementException
     */
    protected static ProviderKeyMgtClient getProviderClient() throws APIManagementException {
        APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
        String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        String username = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        if (username == null || password == null) {
            handleException("Authentication credentials for API Provider manager unspecified");
        }

        return new ProviderKeyMgtClient(url, username, password);

    }

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public static class RequiredUserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (!filed1.getRequired() && filed2.getRequired()) {
                return 1;
            }

            if (filed1.getRequired() && filed2.getRequired()) {
                return 0;
            }

            if (filed1.getRequired() && !filed2.getRequired()) {
                return -1;
            }

            return 0;
        }

    }

    public static class UserFieldComparator implements Comparator<UserFieldDTO> {

        public int compare(UserFieldDTO filed1, UserFieldDTO filed2) {
            if (filed1.getDisplayOrder() == 0) {
                filed1.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed2.getDisplayOrder() == 0) {
                filed2.setDisplayOrder(Integer.MAX_VALUE);
            }

            if (filed1.getDisplayOrder() < filed2.getDisplayOrder()) {
                return -1;
            }
            if (filed1.getDisplayOrder() == filed2.getDisplayOrder()) {
                return 0;
            }
            if (filed1.getDisplayOrder() > filed2.getDisplayOrder()) {
                return 1;
            }
            return 0;
        }

    }

    /**
     * This methods is to check whether stat publishing is enabled
     *
     * @return boolean
     */
    protected static boolean checkDataPublishingEnabled() {
        return APIUtil.isAnalyticsEnabled();
    }

    /**
     * This method will clear recently added API cache.
     *
     * @param username
     */
    public static void invalidateRecentlyAddedAPICache(String username) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            APIManagerConfiguration config = HostObjectComponent.getAPIManagerConfiguration();
            boolean isRecentlyAddedAPICacheEnabled = Boolean
                    .parseBoolean(config.getFirstProperty(APIConstants.API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE));

            if (username != null && isRecentlyAddedAPICacheEnabled) {
                String tenantDomainFromUserName = MultitenantUtils.getTenantDomain(username);
                if (tenantDomainFromUserName != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME
                        .equals(tenantDomainFromUserName)) {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantDomain(tenantDomainFromUserName, true);
                } else {
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                }
                Caching.getCacheManager(APIConstants.API_MANAGER_CACHE_MANAGER).getCache("RECENTLY_ADDED_API")
                        .remove(username + ":" + tenantDomainFromUserName);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    protected static boolean isUsageDataSourceSpecified() {
        try {
            return (null != HostObjectComponent.getDataSourceService().
                    getDataSource(APIConstants.API_USAGE_DATA_SOURCE_NAME));
        } catch (DataSourceException e) {
            return false;
        }
    }

    protected static boolean isStatPublishingEnabled() {
        return APIUtil.isAnalyticsEnabled();
    }
}
