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

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.ConfigType;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dao.SystemConfigurationsDAO;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Config Service Implementation for retrieve configurations.
 */
public class APIMConfigServiceImpl implements APIMConfigService {

    private static final Log log = LogFactory.getLog(APIMConfigServiceImpl.class);
    private static final String SUBSCRIPTION_APPROVAL_VIEW_SCOPE = "apim:subscription_approval_view";
    private static final String SUBSCRIPTION_APPROVAL_MANAGE_SCOPE = "apim:subscription_approval_manage";
    private static final String PUBLISHER_ORG_READ = "apim:publisher_organization_read";
    private static final String ADMIN_ORG_READ = "apim:organization_read";
    private static final String ADMIN_ORG_MANAGE = "apim:organization_manage";
    
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
    @UsedByMigrationClient
    public void addTenantConfig(String organization, String tenantConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        systemConfigurationsDAO.addSystemConfig(organization, ConfigType.TENANT.toString(), tenantConfig);
    }

    @Override
    @UsedByMigrationClient
    public String getTenantConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        return addMissingScopes(systemConfigurationsDAO.getSystemConfig(organization, ConfigType.TENANT.toString()));
    }

    /*
     *  This method facilitates the on-the-fly migration of the scope section in the tenant-config.json. This
     *  checks whether RESTAPIScopes section has newly introduced scopes and add them to the json String if it
     *  is not available. 
     */
    private String addMissingScopes(String systemConfig) {
        if (systemConfig == null) {
            return null;
        }

        // List of newly introduced scopes
        Map<String, String> scopesToCheck = new HashMap<>();
        scopesToCheck.put("apim:admin_tier_view", "admin");
        scopesToCheck.put("apim:admin_tier_manage", "admin");
        scopesToCheck.put("apim:keymanagers_manage", "admin");
        scopesToCheck.put("apim:api_category", "admin");
        scopesToCheck.put("apim:api_provider_change", "admin");
        scopesToCheck.put("apim:gateway_policy_manage", "admin");
        scopesToCheck.put("apim:gateway_policy_view", "admin,Internal/creator,Internal/publisher,Internal/observer");
        scopesToCheck.put("apim:llm_provider_manage", "admin");
        scopesToCheck.put("apim:llm_provider_read", "admin,Internal/publisher,Internal/creator");
        scopesToCheck.put("apim:gov_rule_manage", "admin");
        scopesToCheck.put("apim:gov_rule_read", "admin,Internal/publisher,Internal/creator,Internal/observer");
        scopesToCheck.put("apim:gov_result_read", "admin,Internal/publisher,Internal/creator,Internal/observer");
        scopesToCheck.put("apim:gov_policy_manage", "admin");
        scopesToCheck.put("apim:gov_policy_read", "admin,Internal/publisher,Internal/creator,Internal/observer");
        scopesToCheck.put(SUBSCRIPTION_APPROVAL_VIEW_SCOPE, "admin,Internal/publisher");
        scopesToCheck.put(SUBSCRIPTION_APPROVAL_MANAGE_SCOPE, "admin,Internal/publisher");
        scopesToCheck.put(PUBLISHER_ORG_READ, "admin,Internal/creator");
        scopesToCheck.put(ADMIN_ORG_MANAGE, "admin");
        scopesToCheck.put(ADMIN_ORG_READ, "admin");
        
        ArrayList<String> missingScopesList = new ArrayList<>(scopesToCheck.keySet());
        
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(systemConfig).getAsJsonObject();

        // Get the existing rest api scopes
        if (jsonObject.has("RESTAPIScopes")) {
            JsonObject restApiScopes = jsonObject.getAsJsonObject("RESTAPIScopes");
            if (restApiScopes.has("Scope")) {
                JsonArray scopeArray = restApiScopes.getAsJsonArray("Scope");                
                for (int i = 0; i < scopeArray.size(); i++) {
                    String existingScope = scopeArray.get(i).getAsJsonObject().get("Name").getAsString();
                    if (missingScopesList.contains(existingScope)) {
                        missingScopesList.remove(existingScope);
                    }
                }
            }
        }
        
        // Check if there is no missing scopes in the tenant-conf.json and return the original file
        if (missingScopesList.isEmpty()) {
            return systemConfig;
        }
                
        JsonArray scopeArray = jsonObject.getAsJsonObject("RESTAPIScopes").getAsJsonArray("Scope"); 
        // Add the missing scopes to the tenant-conf 
        for (String missingScope : missingScopesList) {
            JsonObject newScope = new JsonObject();
            newScope.addProperty("Name", missingScope);
            newScope.addProperty("Roles", scopesToCheck.get(missingScope));
            scopeArray.add(newScope);
        }
              
        // Convert the modified JSON back to a string
        return jsonObject.toString();
    }

    @Override
    @UsedByMigrationClient
    public void updateTenantConfig(String organization, String tenantConfig) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        Cache tenantConfigCache = CacheProvider.getTenantConfigCache();
        String cacheName = organization + "_" + APIConstants.TENANT_CONFIG_CACHE_NAME;
        tenantConfigCache.remove(cacheName);
        
        // Clear restapi scope cache
        CacheProvider.getRESTAPIScopeCache().remove(organization);
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
    public UserRegistrationConfigDTO getSelfSighupConfig(String organization) throws APIManagementException {

        if (organization == null) {
            organization = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(organization, true);
            JsonObject tenantConfig = (JsonObject) new JsonParser().parse(getTenantConfig(organization));
            if (tenantConfig.has(APIConstants.SELF_SIGN_UP_NAME)) {
                return getSignupUserRegistrationConfigDTO((JsonObject) tenantConfig.get(APIConstants.SELF_SIGN_UP_NAME));
            } else {
                return null;
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private static UserRegistrationConfigDTO getSignupUserRegistrationConfigDTO(JsonObject selfSighupConfig) {

        UserRegistrationConfigDTO config = new UserRegistrationConfigDTO();
        JsonArray roles = (JsonArray) selfSighupConfig.get(APIConstants.SELF_SIGN_UP_REG_ROLES_ELEM);
        Iterator<JsonElement> rolesIterator = roles.iterator();
        while (rolesIterator.hasNext()) {
            config.getRoles().add(rolesIterator.next().getAsString());
        }
        return config;
    }

    @Override
    public void updateSelfSighupConfig(String organization, String selfSignUpConfig) {
    }

    @Override
    public void addSelfSighupConfig(String organization, String selfSignUpConfig) {
    }
}
