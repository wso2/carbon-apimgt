/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.config;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.ConfigType;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dao.SystemConfigurationsDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;

import javax.cache.Cache;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Config Service Implementation for retrieve configurations.
 */
public class APIMConfigServiceImpl implements APIMConfigService {

    private static final Log log = LogFactory.getLog(APIMConfigServiceImpl.class);
    protected SystemConfigurationsDAO systemConfigurationsDAO;

    public APIMConfigServiceImpl() {
        systemConfigurationsDAO = SystemConfigurationsDAO.getInstance();
    }

    @Override
    public void addExternalStoreConfig(String organization, String externalStoreConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            org.wso2.carbon.user.api.AuthorizationManager authManager =
                    ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId).
                            getAuthorizationManager();
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (!registry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
                Resource resource = registry.newResource();
                resource.setContent(IOUtils.toByteArray(new StringReader(externalStoreConfig)));
                registry.put(APIConstants.EXTERNAL_API_STORES_LOCATION, resource);
                String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                        APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                                + APIConstants.EXTERNAL_API_STORES_LOCATION);
                authManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);

            }

        } catch (RegistryException | IOException | UserStoreException e) {
            String msg = "Error while adding External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void updateExternalStoreConfig(String organization, String externalStoreConfig)
            throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
                Resource resource = registry.get(APIConstants.EXTERNAL_API_STORES_LOCATION);
                resource.setContent(IOUtils.toByteArray(new StringReader(externalStoreConfig)));
                registry.put(APIConstants.EXTERNAL_API_STORES_LOCATION, resource);
            }

        } catch (RegistryException | IOException e) {
            String msg = "Error while updating External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public String getExternalStoreConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
                Resource resource = registry.get(APIConstants.EXTERNAL_API_STORES_LOCATION);
                return new String((byte[]) resource.getContent(), Charset.defaultCharset());
            } else {
                return null;
            }

        } catch (RegistryException e) {
            String msg = "Error while retrieving External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void addTenantConfig(String organization, String tenantConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        systemConfigurationsDAO.addSystemConfig(organization, ConfigType.TENANT.toString(), tenantConfig);
    }

    @Override
    public String getTenantConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return systemConfigurationsDAO.getSystemConfig(organization, ConfigType.TENANT.toString());
    }

    @Override
    public void updateTenantConfig(String organization, String tenantConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        Cache tenantConfigCache = CacheProvider.getTenantConfigCache();
        String cacheName = organization + "_" + APIConstants.TENANT_CONFIG_CACHE_NAME;
        tenantConfigCache.remove(cacheName);
        systemConfigurationsDAO.updateSystemConfig(organization, ConfigType.TENANT.toString(), tenantConfig);
    }

    @Override
    public String getWorkFlowConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.WORKFLOW_EXECUTOR_LOCATION)) {
                Resource resource = registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION);
                return new String((byte[]) resource.getContent(), Charset.defaultCharset());
            } else {
                return null;
            }

        } catch (RegistryException e) {
            String msg = "Error while retrieving External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void updateWorkflowConfig(String organization, String workflowConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.WORKFLOW_EXECUTOR_LOCATION)) {
                Resource resource = registry.get(APIConstants.WORKFLOW_EXECUTOR_LOCATION);
                byte[] data = IOUtils.toByteArray(new StringReader(workflowConfig));
                resource.setContent(data);
                resource.setMediaType(APIConstants.WORKFLOW_MEDIA_TYPE);
                registry.put(APIConstants.WORKFLOW_EXECUTOR_LOCATION, resource);
            }
        } catch (RegistryException | IOException e) {
            String msg = "Error while retrieving External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void addWorkflowConfig(String organization, String workflowConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (!registry.resourceExists(APIConstants.WORKFLOW_EXECUTOR_LOCATION)) {
                Resource resource = registry.newResource();
                byte[] data = IOUtils.toByteArray(new StringReader(workflowConfig));
                resource.setContent(data);
                resource.setMediaType(APIConstants.WORKFLOW_MEDIA_TYPE);
                registry.put(APIConstants.WORKFLOW_EXECUTOR_LOCATION, resource);
            }

        } catch (RegistryException | IOException e) {
            String msg = "Error while retrieving External Stores Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public String getGAConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.GA_CONFIGURATION_LOCATION)) {
                Resource resource = registry.get(APIConstants.GA_CONFIGURATION_LOCATION);
                return new String((byte[]) resource.getContent(), Charset.defaultCharset());
            } else {
                return null;
            }

        } catch (RegistryException e) {
            String msg = "Error while retrieving Google Analytics Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void updateGAConfig(String organization, String gaConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.GA_CONFIGURATION_LOCATION)) {
                byte[] data = IOUtils.toByteArray(new StringReader(gaConfig));
                Resource resource = registry.get(APIConstants.GA_CONFIGURATION_LOCATION);
                resource.setContent(data);
                resource.setMediaType(APIConstants.GA_CONF_MEDIA_TYPE);
                registry.put(APIConstants.GA_CONFIGURATION_LOCATION, resource);
            }
        } catch (RegistryException | IOException e) {
            String msg = "Error while updating Google Analytics Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void addGAConfig(String organization, String gaConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (!registry.resourceExists(APIConstants.GA_CONFIGURATION_LOCATION)) {
                byte[] data = IOUtils.toByteArray(new StringReader(gaConfig));
                Resource resource = registry.newResource();
                resource.setContent(data);
                resource.setMediaType(APIConstants.GA_CONF_MEDIA_TYPE);
                registry.put(APIConstants.GA_CONFIGURATION_LOCATION, resource);
                /*set resource permission*/
                org.wso2.carbon.user.api.AuthorizationManager authManager =
                        ServiceReferenceHolder.getInstance().getRealmService().
                                getTenantUserRealm(tenantId).getAuthorizationManager();
                String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                        APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH)
                                + APIConstants.GA_CONFIGURATION_LOCATION);
                authManager.denyRole(APIConstants.EVERYONE_ROLE, resourcePath, ActionConstants.GET);
            }
        } catch (RegistryException | IOException | UserStoreException e) {
            String msg = "Error while add Google Analytics Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public String getSelfSighupConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)) {
                Resource resource = registry.get(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION);
                return new String((byte[]) resource.getContent(), Charset.defaultCharset());
            } else {
                return null;
            }

        } catch (RegistryException e) {
            String msg = "Error while retrieving Self-SignUp Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void updateSelfSighupConfig(String organization, String selfSignUpConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)) {
                byte[] data = IOUtils.toByteArray(new StringReader(selfSignUpConfig));
                Resource resource = registry.get(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION);
                resource.setContent(data);
                resource.setMediaType(APIConstants.SELF_SIGN_UP_CONFIG_MEDIA_TYPE);
                registry.put(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION, resource);
            }
        } catch (RegistryException | IOException e) {
            String msg = "Error while updating Self-SignUp Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void addSelfSighupConfig(String organization, String selfSignUpConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            int tenantId = APIUtil.getTenantIdFromTenantDomain(organization);
            if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(organization)) {
                APIUtil.loadTenantRegistry(tenantId);
            }
            UserRegistry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(tenantId);
            if (!registry.resourceExists(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)) {
                byte[] data = IOUtils.toByteArray(new StringReader(selfSignUpConfig));
                Resource resource = registry.newResource();
                resource.setContent(data);
                resource.setMediaType(APIConstants.SELF_SIGN_UP_CONFIG_MEDIA_TYPE);
                registry.put(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION, resource);
            }
        } catch (RegistryException | IOException e) {
            String msg = "Error while adding Self-SignUp Configuration from registry";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
