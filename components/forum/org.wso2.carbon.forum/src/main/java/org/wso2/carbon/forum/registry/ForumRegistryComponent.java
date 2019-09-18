/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.forum.registry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.forum.ForumException;
import org.wso2.carbon.forum.ServiceReferenceHolder;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.realm.RegistryAuthorizationManager;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.forum.services", 
         immediate = true)
public class ForumRegistryComponent {

    private static final Log log = LogFactory.getLog(ForumRegistryComponent.class);

    public static final String TOPICS_ROOT = "forumtopics";

    ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();

    @Activate
    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Forum Registry Component Activated");
        }
        try {
            TenantServiceCreator tenantServiceCreator = new TenantServiceCreator();
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), tenantServiceCreator, null);
            createTopicsRootCollection(MultitenantConstants.SUPER_TENANT_ID);
            addRxtConfigs(MultitenantConstants.SUPER_TENANT_ID);
        } catch (ForumException e) {
            log.error("Could not activate Forum Registry Component " + e.getMessage());
            throw e;
        }
    }

    public static void addRxtConfigs(int tenantId) throws ForumException {
        String forumRxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" + File.separator + "rxts" + File.separator + "forum";
        File file = new File(forumRxtDir);
        // create a FilenameFilter
        FilenameFilter filenameFilter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                // if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);
        UserRegistry systemRegistry;
        try {
            systemRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
        // getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        } catch (RegistryException e) {
            throw new ForumException("Failed to get registry", e);
        }
        for (String rxtPath : rxtFilePaths) {
            String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH + RegistryConstants.PATH_SEPARATOR + rxtPath;
            resourcePath = RegistryUtils.getRelativePathToOriginal(resourcePath, RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH);
            try {
                if (systemRegistry.resourceExists(resourcePath)) {
                    continue;
                }
                String rxt = FileUtil.readFileToString(forumRxtDir + File.separator + rxtPath);
                Resource resource = systemRegistry.newResource();
                resource.setContent(rxt.getBytes());
                resource.setMediaType(GovernanceConstants.GOVERNANCE_ARTIFACT_CONFIGURATION_MEDIA_TYPE);
                systemRegistry.put(resourcePath, resource);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new ForumException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new ForumException(msg, e);
            }
        }
    }

    public static void createTopicsRootCollection(int tenantId) throws ForumException {
        UserRegistry systemRegistry;
        try {
            systemRegistry = ServiceReferenceHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(tenantId);
            String resourcePath = RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(), TOPICS_ROOT);
            if (systemRegistry.resourceExists(resourcePath)) {
                return;
            }
            Collection collection = systemRegistry.newCollection();
            systemRegistry.put(resourcePath, collection);
            AuthorizationManager authorizationManager;
            try {
                if (org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID == tenantId) {
                    authorizationManager = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getAuthorizationManager();
                } else {
                    authorizationManager = new RegistryAuthorizationManager(ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry().getUserRealm());
                }
                String everyOneRole = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration().getEveryOneRoleName();
                authorizationManager.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, collection.getPath(), ActionConstants.GET.toString());
                authorizationManager.authorizeRole(everyOneRole, collection.getPath(), ActionConstants.GET.toString());
                authorizationManager.authorizeRole(everyOneRole, collection.getPath(), ActionConstants.PUT.toString());
            } catch (UserStoreException e) {
                log.error("Error when getting user store for applying permissions on forum root collection!", e);
                throw new ForumException("Error when getting user store for applying permissions on forum root collection!", e);
            }
        } catch (RegistryException e) {
            throw new ForumException("Failed to get registry", e);
        }
    }

    @Reference(
             name = "registry.service", 
             service = org.wso2.carbon.registry.core.service.RegistryService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled() && registryService != null) {
            log.debug("Registry service initialized");
        }
        serviceReferenceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        serviceReferenceHolder.setRegistryService(null);
    }

    @Reference(
             name = "user.realm.service", 
             service = org.wso2.carbon.user.core.service.RealmService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled() && realmService != null) {
            log.debug("Realm service initialized");
        }
        serviceReferenceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        serviceReferenceHolder.setRealmService(null);
    }
}

