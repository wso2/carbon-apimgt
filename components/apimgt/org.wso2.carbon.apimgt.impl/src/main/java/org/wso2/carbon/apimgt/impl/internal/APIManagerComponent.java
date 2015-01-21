/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.internal;

import org.apache.axis2.engine.ListenerManager;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.listners.UserAddListener;
import org.wso2.carbon.apimgt.impl.observers.APIStatusObserverList;
import org.wso2.carbon.apimgt.impl.observers.SignupObserver;
import org.wso2.carbon.apimgt.impl.observers.TenantServiceCreator;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.RemoteAuthorizationManager;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.listener.UserStoreManagerListener;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.FileUtil;

import javax.cache.Cache;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


/**
 * @scr.component name="org.wso2.apimgt.impl.services" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic"  bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="listener.manager.service"
 * interface="org.apache.axis2.engine.ListenerManager" cardinality="0..1" policy="dynamic"
 * bind="setListenerManager" unbind="unsetListenerManager"
 *@scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic"
 * bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 * @scr.reference name="tenant.indexloader"
 * interface="org.wso2.carbon.registry.indexing.service.TenantIndexingLoader" cardinality="1..1" policy="dynamic"
 * bind="setIndexLoader" unbind="unsetIndexLoader"

 */
public class APIManagerComponent {
    //TODO refactor caching implementation

    private static final Log log = LogFactory.getLog(APIManagerComponent.class);

    private ServiceRegistration registration;

    private static TenantRegistryLoader tenantRegistryLoader;

    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("API manager component activated");
        }
        
        try {
            BundleContext bundleContext = componentContext.getBundleContext();
            addRxtConfigs();
            addTierPolicies();
            addDefinedSequencesToRegistry();
            APIUtil.loadTenantExternalStoreConfig(MultitenantConstants.SUPER_TENANT_ID);
            APIUtil.loadTenantGAConfig(MultitenantConstants.SUPER_TENANT_ID);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            APIUtil.loadTenantWorkFlowExtensions(tenantId);
            //load self sigup configuration to the registry
            APIUtil.loadTenantSelfSignUpConfigurations(tenantId);
            
            
            APIManagerConfiguration configuration = new APIManagerConfiguration();
            String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "api-manager.xml";
            configuration.load(filePath);

            //WorkflowExecutorFactory.getInstance().load(filePath);

            String gatewayType = configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
            if ("Synapse".equalsIgnoreCase(gatewayType)) {
                //Register Tenant service creator to deploy tenant specific common synapse configurations
                TenantServiceCreator listener = new TenantServiceCreator();
                bundleContext.registerService(
                        Axis2ConfigurationContextObserver.class.getName(), listener, null);
            }

            SignupObserver signupObserver = new SignupObserver();
            bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), signupObserver,null);
            
            APIManagerConfigurationServiceImpl configurationService =
                    new APIManagerConfigurationServiceImpl(configuration);
            ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);
            registration = componentContext.getBundleContext().registerService(
                    APIManagerConfigurationService.class.getName(),
                    configurationService, null);
            APIStatusObserverList.getInstance().init(configuration);

            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_CREATOR_APIMGT_EXECUTION_ID,
                    RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                                      APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                                             RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                                                              APIConstants.API_APPLICATION_DATA_LOCATION),
                    APIConstants.Permissions.API_CREATE,
                    UserMgtConstants.EXECUTE_ACTION, null);
            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_CREATOR_GOVERNANCE_EXECUTION_ID,
                    RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                                      APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                                             RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                                                              "/trunk"),
                    APIConstants.Permissions.API_CREATE,
                    UserMgtConstants.EXECUTE_ACTION, null);
            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_PUBLISHER_APIMGT_EXECUTION_ID,
                    RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                                                                                      APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                                                                             RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                                                                              APIConstants.API_APPLICATION_DATA_LOCATION),
                    APIConstants.Permissions.API_PUBLISH,
                    UserMgtConstants.EXECUTE_ACTION, null);
            
            setupImagePermissions();
            RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
            authorizationManager.init();
            APIMgtDBUtil.initialize();
            //Check User add listener enabled or not
            boolean selfSignInProcessEnabled = Boolean.parseBoolean(configuration.getFirstProperty("WorkFlowExtensions.SelfSignIn.ProcessEnabled"));
            readRecentlyAddedAPICacheConfigs(configuration);
            if (selfSignInProcessEnabled) {
                if (bundleContext != null) {
                    bundleContext.registerService(UserStoreManagerListener.class.getName(),
                            new UserAddListener(), null);
                }
            }
            //Load initially available api contexts at the server startup. This Cache is only use by the products other than the api-manager
            /* TODO: Load Config values from apimgt.core*/
            boolean apiManagementEnabled = APIUtil.isAPIManagementEnabled();
            boolean loadAPIContextsAtStartup = APIUtil.isLoadAPIContextsAtStartup();
            if (apiManagementEnabled && loadAPIContextsAtStartup) {
                List<String> contextList = ApiMgtDAO.getAllAvailableContexts();
                Cache contextCache = APIUtil.getAPIContextCache();
                for (String context : contextList) {
                    contextCache.put(context, true);
                }
            }
            APIUtil.createSelfSignUpRoles(MultitenantConstants.SUPER_TENANT_ID);
            
            /* Add Bam Server Profile for collecting southbound statistics*/
            String enabledStr = configuration.getFirstProperty(APIConstants.API_USAGE_ENABLED);
            boolean enabled = enabledStr != null && JavaUtils.isTrueExplicitly(enabledStr);
            if (enabled) {
            	String bamServerURL = configuration.getFirstProperty(APIConstants.API_USAGE_BAM_SERVER_URL);
                String bamServerUser = configuration.getFirstProperty(APIConstants.API_USAGE_BAM_SERVER_USER);
                String bamServerPassword = configuration.getFirstProperty(APIConstants.API_USAGE_BAM_SERVER_PASSWORD);
                String bamServerThriftPort = configuration.getFirstProperty(APIConstants.API_USAGE_THRIFT_PORT);
            	APIUtil.addBamServerProfile(bamServerURL, bamServerUser, bamServerPassword, 
            			bamServerThriftPort, MultitenantConstants.SUPER_TENANT_ID);
            }
            
        } catch (APIManagementException e) {
            log.error("Error while initializing the API manager component", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating API manager component");
        }
        registration.unregister();
        APIManagerFactory.getInstance().clearAll();
        RemoteAuthorizationManager authorizationManager = RemoteAuthorizationManager.getInstance();
        authorizationManager.destroy();
    }

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        ServiceReferenceHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(null);
    }

    protected void setIndexLoader(TenantIndexingLoader indexLoader) {
        if (indexLoader != null && log.isDebugEnabled()) {
            log.debug("IndexLoader service initialized");
        }
        ServiceReferenceHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader registryService) {
        ServiceReferenceHolder.getInstance().setIndexLoaderService(null);
    }

    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    protected void setListenerManager(ListenerManager listenerManager) {
        // We bind to the listener manager so that we can read the local IP
        // address and port numbers properly.
        log.debug("Listener manager bound to the API manager component");
        APIManagerConfigurationService service = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService();
        if (service != null) {
            service.getAPIManagerConfiguration().reloadSystemProperties();
        }
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        log.debug("Listener manager unbound from the API manager component");
    }

    private void addRxtConfigs() throws APIManagementException {
        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" + File.separator + "rxts";
        File file = new File(rxtDir);
        //create a FilenameFilter
        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                //if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry systemRegistry;
        try {
            systemRegistry = registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        } catch (RegistryException e) {
            throw new APIManagementException("Failed to get registry", e);
        }

        for (String rxtPath : rxtFilePaths) {
            String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH +
                    RegistryConstants.PATH_SEPARATOR + rxtPath;
            try {
                if (systemRegistry.resourceExists(resourcePath)) {
                    continue;
                }
                String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                Resource resource = systemRegistry.newResource();
                resource.setContent(rxt.getBytes());
                resource.setMediaType(APIConstants.RXT_MEDIA_TYPE);
                systemRegistry.put(resourcePath, resource);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new APIManagementException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new APIManagementException(msg, e);
            }
        }
    }

    private void setupImagePermissions() throws APIManagementException {
        try {
            AuthorizationManager accessControlAdmin = ServiceReferenceHolder.getInstance().
                    getRealmService().getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).
                    getAuthorizationManager();
            String imageLocation =
                                   APIUtil.getMountedPath(RegistryContext.getBaseInstance(),
                                                          RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) +
                                           APIConstants.API_IMAGE_LOCATION;
            if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    imageLocation, ActionConstants.GET)) {
                // Can we get rid of this?
                accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                        imageLocation, ActionConstants.GET);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while setting up permissions for image collection", e);
        }
    }

    private void addTierPolicies() throws APIManagementException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry();
            if (registry.resourceExists(APIConstants.API_TIER_LOCATION)) {
                log.debug("Tier policies already uploaded to the registry");
                return;
            }

            log.debug("Adding API tier policies to the registry");
            InputStream inputStream = APIManagerComponent.class.getResourceAsStream("/tiers/default-tiers.xml");
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = registry.newResource();
            resource.setContent(data);

            //  Properties descriptions = new Properties();
            //   descriptions.load(APIManagerComponent.class.getResourceAsStream(
            //           "/tiers/default-tier-info.properties"));
            //   Set<String> names = descriptions.stringPropertyNames();
            //   for (String name : names) {
            //       resource.setProperty(APIConstants.TIER_DESCRIPTION_PREFIX + name,
            //              descriptions.getProperty(name));
            //  }
            //  resource.setProperty(APIConstants.TIER_DESCRIPTION_PREFIX + APIConstants.UNLIMITED_TIER,
            //         APIConstants.UNLIMITED_TIER_DESC);
            registry.put(APIConstants.API_TIER_LOCATION, resource);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving policy information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading policy file content", e);
        }
    }

    private void addDefinedSequencesToRegistry() throws APIManagementException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry();
            if (registry.resourceExists(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION)) {
                if(log.isDebugEnabled()){
                    log.debug("Defined sequences have already been added to the registry");
                }
                //No need to add to add in sequences or out sequences. Do not return yet until we check for fault
                // sequences as well. (Designed to support migrations).
                //return;
            }
            else{
                if(log.isDebugEnabled()){
                    log.debug("Adding defined sequences to the registry.");
                }

                InputStream inSeqStream =
                        APIManagerComponent.class.getResourceAsStream("/definedsequences/in/log_in_message.xml");
                byte[] inSeqData = IOUtils.toByteArray(inSeqStream);
                Resource inSeqResource = registry.newResource();
                inSeqResource.setContent(inSeqData);

                registry.put(APIConstants.API_CUSTOM_INSEQUENCE_LOCATION + "log_in_message.xml", inSeqResource);

                InputStream outSeqStream =
                        APIManagerComponent.class.getResourceAsStream("/definedsequences/out/log_out_message.xml");
                byte[] outSeqData = IOUtils.toByteArray(outSeqStream);
                Resource outSeqResource = registry.newResource();
                outSeqResource.setContent(outSeqData);

                registry.put(APIConstants.API_CUSTOM_OUTSEQUENCE_LOCATION + "log_out_message.xml", outSeqResource);
            }

            if (registry.resourceExists(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION)) {
                if(log.isDebugEnabled()){
                    log.debug("Defined fault sequences have already been added to the tenant's registry");
                }
                //Fault sequences have already been added. Nothing to do beyond this. Return.
                return;
            }

            InputStream faultSeqStream =
                    APIManagerComponent.class.getResourceAsStream("/definedsequences/fault/json_fault.xml");
            byte[] faultSeqData = IOUtils.toByteArray(faultSeqStream);
            Resource faultSeqResource = registry.newResource();
            faultSeqResource.setContent(faultSeqData);

            registry.put(APIConstants.API_CUSTOM_FAULTSEQUENCE_LOCATION + "json_fault.xml", faultSeqResource);

        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving defined sequences to the registry ", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading defined sequence ", e);
        }
    }

    private void setupSelfRegistration(APIManagerConfiguration config) throws APIManagementException {
        boolean enabled = Boolean.parseBoolean(config.getFirstProperty(APIConstants.SELF_SIGN_UP_ENABLED));
        if (!enabled) {
            return;
        }

        String role = config.getFirstProperty(APIConstants.SELF_SIGN_UP_ROLE);
        if (role == null) {
            // Required parameter missing - Throw an exception and interrupt startup
            throw new APIManagementException("Required subscriber role parameter missing " +
                    "in the self sign up configuration");
        }

        String[] permissions = new String[]{
                "/permission/admin/login",
                APIConstants.Permissions.API_SUBSCRIBE
        };
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            UserRealm realm = realmService.getBootstrapRealm();
            UserStoreManager manager = realm.getUserStoreManager();
            if (!manager.isExistingRole(role)) {
                if (log.isDebugEnabled()) {
                    log.debug("Creating subscriber role: " + role);
                }
                Permission[] subscriberPermissions = new Permission[]{new Permission("/permission/admin/login", UserMgtConstants.EXECUTE_ACTION),
                        new Permission(APIConstants.Permissions.API_SUBSCRIBE, UserMgtConstants.EXECUTE_ACTION)};
                String superTenantName = ServiceReferenceHolder.getInstance().getRealmService().getBootstrapRealmConfiguration().getAdminUserName();
                String[] userList = new String[]{superTenantName};
                manager.addRole(role, userList, subscriberPermissions);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while creating subscriber role: " + role + " - " +
                    "Self registration might not function properly.", e);
        }
    }
    
    /**
     * Add the External API Stores Configuration to registry
     * @throws APIManagementException
     */
    private void addExternalStoresConfigs() throws APIManagementException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        try {
            UserRegistry registry = registryService.getGovernanceSystemRegistry();
            if (registry.resourceExists(APIConstants.EXTERNAL_API_STORES_LOCATION)) {
                log.debug("External Stores configuration already uploaded to the registry");
                return;
            }

            log.debug("Adding External Stores configuration to the registry");
            InputStream inputStream = APIManagerComponent.class.getResourceAsStream("/externalstores/default-external-api-stores.xml");
            byte[] data = IOUtils.toByteArray(inputStream);
            Resource resource = registry.newResource();
            resource.setContent(data);

            registry.put(APIConstants.EXTERNAL_API_STORES_LOCATION, resource);
        } catch (RegistryException e) {
            throw new APIManagementException("Error while saving External Stores configuration information to the registry", e);
        } catch (IOException e) {
            throw new APIManagementException("Error while reading External Stores configuration file content", e);
        }
    }
    
   protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.setContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.setContextService(null);
    }


    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = tenantRegistryLoader;
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = null;
    }

    public static TenantRegistryLoader getTenantRegistryLoader(){
        return tenantRegistryLoader;
    }
    private void readRecentlyAddedAPICacheConfigs(APIManagerConfiguration config) {
        String isRecentlyAddedAPICachedEnabled = config.getFirstProperty(APIConstants.API_STORE_RECENTLY_ADDED_API_CACHE_ENABLE);
        if (isRecentlyAddedAPICachedEnabled == null) {
            APIConstants.isRecentlyAddedAPICacheEnabled = false;
        } else {
            APIConstants.isRecentlyAddedAPICacheEnabled  =  isRecentlyAddedAPICachedEnabled.equalsIgnoreCase("true") ? true : false;
        }
    }
}
