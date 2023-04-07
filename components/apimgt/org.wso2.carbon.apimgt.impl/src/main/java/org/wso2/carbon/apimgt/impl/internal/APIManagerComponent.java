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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.ssl.SSLContexts;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIManagerDatabaseException;
import org.wso2.carbon.apimgt.api.APIMgtInternalException;
import org.wso2.carbon.apimgt.api.OrganizationResolver;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.WorkflowTaskService;
import org.wso2.carbon.apimgt.api.quotalimiter.ResourceQuotaLimiter;
import org.wso2.carbon.apimgt.common.gateway.configdto.HttpClientConfigurationDTO;
import org.wso2.carbon.apimgt.common.gateway.http.BrowserHostnameVerifier;
import org.wso2.carbon.apimgt.common.gateway.jwttransformer.JWTTransformer;
import org.wso2.carbon.apimgt.eventing.EventPublisherException;
import org.wso2.carbon.apimgt.eventing.EventPublisherFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.PasswordResolverFactory;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.config.APIMConfigServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.ExternalEnvironment;
import org.wso2.carbon.apimgt.impl.deployer.ExternalGatewayDeployer;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.factory.SQLConstantManagerFactory;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.DBRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;
import org.wso2.carbon.apimgt.impl.handlers.UserPostSelfRegistrationHandler;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.issuers.SystemScopesIssuer;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationServiceImpl;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.notifier.ApisNotifier;
import org.wso2.carbon.apimgt.impl.notifier.ApplicationNotifier;
import org.wso2.carbon.apimgt.impl.notifier.ApplicationRegistrationNotifier;
import org.wso2.carbon.apimgt.impl.notifier.CertificateNotifier;
import org.wso2.carbon.apimgt.impl.notifier.CorrelationConfigNotifier;
import org.wso2.carbon.apimgt.impl.notifier.DeployAPIInGatewayNotifier;
import org.wso2.carbon.apimgt.impl.notifier.ExternalGatewayNotifier;
import org.wso2.carbon.apimgt.impl.notifier.ExternallyDeployedApiNotifier;
import org.wso2.carbon.apimgt.impl.notifier.GoogleAnalyticsNotifier;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.impl.notifier.PolicyNotifier;
import org.wso2.carbon.apimgt.impl.notifier.ScopesNotifier;
import org.wso2.carbon.apimgt.impl.notifier.SubscriptionsNotifier;
import org.wso2.carbon.apimgt.impl.notifier.KeyTemplateNotifier;
import org.wso2.carbon.apimgt.impl.observers.APIStatusObserverList;
import org.wso2.carbon.apimgt.impl.observers.CommonConfigDeployer;
import org.wso2.carbon.apimgt.impl.observers.KeyMgtConfigDeployer;
import org.wso2.carbon.apimgt.impl.observers.TenantLoadMessageSender;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.GatewayArtifactsMgtDBUtil;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.identity.event.handler.AbstractEventHandler;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.validators.scope.ScopeValidator;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
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
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.cache.Cache;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import static org.wso2.carbon.apimgt.common.gateway.util.CommonAPIUtil.ALLOW_ALL;
import static org.wso2.carbon.apimgt.common.gateway.util.CommonAPIUtil.HOST_NAME_VERIFIER;
import static org.wso2.carbon.apimgt.common.gateway.util.CommonAPIUtil.STRICT;

@Component(
         name = "org.wso2.apimgt.impl.services",
         immediate = true)
public class APIManagerComponent {

    // TODO refactor caching implementation
    private static final Log log = LogFactory.getLog(APIManagerComponent.class);

    ServiceRegistration registration;

    private static TenantRegistryLoader tenantRegistryLoader;

    private APIManagerConfiguration configuration = new APIManagerConfiguration();

    public static final String APPLICATION_ROOT_PERMISSION = "applications";

    public static final String API_RXT = "api.rxt";

    public static final String AUTHORIZATION_HEADER = "Authorization Header";

    public static final String LABELS = "Labels";

    public static final String API_SECURITY = "API Security";

    public static final String TYPE_ELEMENT = "<name>Type</name>";

    public static final String ENABLE_SCHEMA_VALIDATION = "Enable Schema Validation";

    @Activate
    protected void activate(ComponentContext componentContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("API manager component activated");
        }
        try {
            BundleContext bundleContext = componentContext.getBundleContext();
            addRxtConfigs();
            addApplicationsPermissionsToRegistry();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            configuration.load(filePath);

            //Registering Notifiers
            bundleContext.registerService(Notifier.class.getName(), new SubscriptionsNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(), new ApisNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(), new ApplicationNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(), new ApplicationRegistrationNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(), new PolicyNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(), new DeployAPIInGatewayNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(), new ScopesNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(), new CertificateNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(),new GoogleAnalyticsNotifier(),null);
            bundleContext.registerService(Notifier.class.getName(),new ExternalGatewayNotifier(),null);
            bundleContext.registerService(Notifier.class.getName(),new ExternallyDeployedApiNotifier(),null);
            bundleContext.registerService(Notifier.class.getName(),new KeyTemplateNotifier(), null);
            bundleContext.registerService(Notifier.class.getName(), new CorrelationConfigNotifier(), null);
            APIManagerConfigurationServiceImpl configurationService = new APIManagerConfigurationServiceImpl(configuration);
            ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);
            APIMgtDBUtil.initialize();
            APIUtil.init();
            String migrationEnabled = System.getProperty(APIConstants.MIGRATE);
            if (migrationEnabled == null) {
                CommonConfigDeployer configDeployer = new CommonConfigDeployer();
                bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), configDeployer, null);
                TenantLoadMessageSender tenantLoadMessageSender = new TenantLoadMessageSender();
                bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), tenantLoadMessageSender, null);
                KeyMgtConfigDeployer keyMgtConfigDeployer = new KeyMgtConfigDeployer();
                bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), keyMgtConfigDeployer, null);

                APIUtil.loadAndSyncTenantConf(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                APIUtil.loadTenantExternalStoreConfig(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                APIUtil.loadTenantGAConfig(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                APIUtil.loadTenantWorkFlowExtensions(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                APIUtil.loadCommonOperationPolicies(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                APIManagerAnalyticsConfiguration analyticsConfiguration = APIManagerAnalyticsConfiguration.getInstance();
                analyticsConfiguration.setAPIManagerConfiguration(configuration);
                registration = componentContext.getBundleContext().registerService(APIManagerConfigurationService.class.getName(), configurationService, null);
                KeyManagerConfigurationServiceImpl keyManagerConfigurationService = new KeyManagerConfigurationServiceImpl();
                registration = componentContext.getBundleContext().registerService(KeyManagerConfigurationService.class,
                        keyManagerConfigurationService, null);
                JWTValidationService jwtValidationService = new JWTValidationServiceImpl();
                registration = componentContext.getBundleContext().registerService(JWTValidationService.class,
                        jwtValidationService, null);
                ServiceReferenceHolder.getInstance().setKeyManagerConfigurationService(keyManagerConfigurationService);
                APIStatusObserverList.getInstance().init(configuration);
                MonetizationDataHolder.getInstance().init();
            }
            registration = componentContext.getBundleContext()
                    .registerService(APIManagerConfigurationService.class.getName(), configurationService, null);
            log.debug("Reading Analytics Configuration from file...");
            // This method is called in two places. Mostly by the time activate hits,
            // ServiceDataPublisherAdmin is not activated. Therefore, this same method is run,
            // when ServiceDataPublisherAdmin is set.

            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_CREATOR_APIMGT_EXECUTION_ID, RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(), APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + APIConstants.API_APPLICATION_DATA_LOCATION), APIConstants.Permissions.API_CREATE, UserMgtConstants.EXECUTE_ACTION, null);
            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_CREATOR_GOVERNANCE_EXECUTION_ID, RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(), APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + "/trunk"), APIConstants.Permissions.API_CREATE, UserMgtConstants.EXECUTE_ACTION, null);
            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_PUBLISHER_APIMGT_EXECUTION_ID, RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(), APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + APIConstants.API_APPLICATION_DATA_LOCATION), APIConstants.Permissions.API_PUBLISH, UserMgtConstants.EXECUTE_ACTION, null);
            // Enabling API Publishers/Creators to make changes on life-cycle history.
            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_CREATOR_LIFECYCLE_EXECUTION_ID, RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(), APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + APIConstants.API_LIFE_CYCLE_HISTORY), APIConstants.Permissions.API_CREATE, UserMgtConstants.EXECUTE_ACTION, null);
            AuthorizationUtils.addAuthorizeRoleListener(APIConstants.AM_PUBLISHER_LIFECYCLE_EXECUTION_ID, RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(), APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + APIConstants.API_LIFE_CYCLE_HISTORY), APIConstants.Permissions.API_PUBLISH, UserMgtConstants.EXECUTE_ACTION, null);
            setupImagePermissions();
            GatewayArtifactsMgtDBUtil.initialize();
            configureEventPublisherProperties();
            configureNotificationEventPublisher();
            // Load initially available api contexts at the server startup. This Cache is only use by the products other than the api-manager
            /* TODO: Load Config values from apimgt.core*/
            boolean apiManagementEnabled = APIUtil.isAPIManagementEnabled();
            boolean loadAPIContextsAtStartup = APIUtil.isLoadAPIContextsAtStartup();
            if (apiManagementEnabled && loadAPIContextsAtStartup) {
                List<String> contextList = ApiMgtDAO.getInstance().getAllAvailableContexts();
                Cache contextCache = APIUtil.getAPIContextCache();
                for (String context : contextList) {
                    contextCache.put(context, Boolean.TRUE);
                }
            }
            if (migrationEnabled == null) {
                try {
                    APIUtil.createDefaultRoles(MultitenantConstants.SUPER_TENANT_ID);
                } catch (APIManagementException e) {
                    log.error("Failed create default roles for tenant " + MultitenantConstants.SUPER_TENANT_ID, e);
                } catch (Exception e) {
                    // The generic Exception is handled explicitly so execution does not stop during config deployment
                    log.error("Exception when creating default roles for tenant " + MultitenantConstants.SUPER_TENANT_ID, e);
                }
                // Adding default throttle policies
                addDefaultAdvancedThrottlePolicies(tenantDomain, tenantId);
                addDefaultAsyncThrottlePolicies(tenantDomain, tenantId);

                //Adding default correlation configs at initial server start up
                APIUtil.addDefaultCorrelationConfigs();
                // Update all NULL THROTTLING_TIER values to Unlimited
                boolean isNullThrottlingTierConversionEnabled = APIUtil.updateNullThrottlingTierAtStartup();
                try {
                    if (isNullThrottlingTierConversionEnabled) {
                        ApiMgtDAO.getInstance().convertNullThrottlingTiers();
                    }
                } catch (APIManagementException e) {
                    log.error("Failed to convert NULL THROTTLING_TIERS to Unlimited");
                }

//            // Initialise KeyManager.
//            KeyManagerHolder.initializeKeyManager(configuration);
                // Initialise sql constants
                SQLConstantManagerFactory.initializeSQLConstantManager();
                // Initialize PasswordResolver
                PasswordResolverFactory.initializePasswordResolver();
            }

            APIUtil.init();

            // Activating UserPostSelfRegistration handler componeAPITemplateBuilderImplnt
            try {
                registration = componentContext.getBundleContext()
                        .registerService(AbstractEventHandler.class.getName(), new UserPostSelfRegistrationHandler(),
                                null);
            } catch (Exception e) {
                log.error("Error while activating UserPostSelfRegistration handler component.", e);
            }

            // Read the trust store
            ServerConfiguration config = CarbonUtils.getServerConfiguration();

            //Initialize product REST API token caches
            CacheProvider.createRESTAPITokenCache();
            CacheProvider.createRESTAPIInvalidTokenCache();
            CacheProvider.createGatewayJWTTokenCache();
            CacheProvider.createTenantConfigCache();
            CacheProvider.createRecommendationsCache();
            CacheProvider.createParsedSignJWTCache();
            CacheProvider.createGatewayBasicAuthResourceCache();
            CacheProvider.createGatewayUsernameCache();
            CacheProvider.createIntrospectionCache();
            //Initialize Recommendation wso2event output publisher
            configureRecommendationEventPublisherProperties();
            setupAccessTokenGenerator();
            retrieveAndSetParentTrustStore();
            populateHttpClientConfiguration();
            if (configuration.getGatewayArtifactSynchronizerProperties().isRetrieveFromStorageEnabled()) {
                if (APIConstants.GatewayArtifactSynchronizer.DB_RETRIEVER_NAME
                        .equals(configuration.getGatewayArtifactSynchronizerProperties().getRetrieverName())) {
                    bundleContext.registerService(ArtifactRetriever.class.getName(), new DBRetriever(), null);
                }
            }
            bundleContext.registerService(ScopeValidator.class, new SystemScopesIssuer(), null);
        } catch (APIManagementException e) {
            log.error("Error while initializing the API manager component", e);
        } catch (APIManagerDatabaseException e) {
            log.fatal("Error while Creating the database", e);
        }
    }


    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating API manager component");
        }

        registration.unregister();
        APIManagerFactory.getInstance().clearAll();
    }

    @Reference(
             name = "registry.service",
             service = org.wso2.carbon.registry.core.service.RegistryService.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetRegistryService")
    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        ServiceReferenceHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(null);
    }

    @Reference(
             name = "tenant.indexloader",
             service = org.wso2.carbon.registry.indexing.service.TenantIndexingLoader.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetIndexLoader")
    protected void setIndexLoader(TenantIndexingLoader indexLoader) {
        if (indexLoader != null && log.isDebugEnabled()) {
            log.debug("IndexLoader service initialized");
        }
        ServiceReferenceHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader registryService) {
        ServiceReferenceHolder.getInstance().setIndexLoaderService(null);
    }

    @Reference(
             name = "user.realm.service",
             service = org.wso2.carbon.user.core.service.RealmService.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetRealmService")
    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    @Reference(
             name = "listener.manager.service",
             service = org.apache.axis2.engine.ListenerManager.class,
             cardinality = ReferenceCardinality.OPTIONAL,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetListenerManager")
    protected void setListenerManager(ListenerManager listenerManager) {
        // address and port numbers properly.
        if (log.isDebugEnabled()) {
            log.debug("Listener manager bound to the API manager component");
        }
        APIManagerConfigurationService service = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService();
        if (service != null) {
            service.getAPIManagerConfiguration().reloadSystemProperties();
        }
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        log.debug("Listener manager unbound from the API manager component");
    }

    private void addRxtConfigs() throws APIManagementException {
        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator + "resources" + File.separator + "rxts";
        File file = new File(rxtDir);
        // create a FilenameFilter
        FilenameFilter filenameFilter = new FilenameFilter() {

            public boolean accept(File dir, String name) {
                // if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);
        if (rxtFilePaths == null || rxtFilePaths.length == 0) {
            log.info("No RXTs Found.");
            return;
        }
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry systemRegistry;
        try {
            systemRegistry = registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
        } catch (RegistryException e) {
            throw new APIManagementException("Failed to get registry", e);
        }
        for (String rxtPath : rxtFilePaths) {
            String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH + RegistryConstants.PATH_SEPARATOR + rxtPath;
            try {
                if (systemRegistry.resourceExists(resourcePath)) {
                    // Adding Authorization header to the template if not exist
                    if (API_RXT.equals(rxtPath)) {
                        //get Registry resource
                        Resource resource = systemRegistry.get(resourcePath);
                        if (resource.getContent() != null) {
                            // check whether the resource contains a field called authorization header.
                            if (!RegistryUtils.decodeBytes((byte[]) resource.getContent()).
                                    contains(AUTHORIZATION_HEADER)) {
                                updateRegistryResourceContent(resource, systemRegistry, rxtDir, rxtPath, resourcePath);
                            }
                            // check whether the resource contains a section called 'Labels' and add it
                            if (!RegistryUtils.decodeBytes((byte[]) resource.getContent()).contains(LABELS)) {
                                updateRegistryResourceContent(resource, systemRegistry, rxtDir, rxtPath, resourcePath);
                            }
                            // check whether the resource contains a section called 'API Security' and add it
                            if (!RegistryUtils.decodeBytes((byte[]) resource.getContent()).contains(API_SECURITY)) {
                                updateRegistryResourceContent(resource, systemRegistry, rxtDir, rxtPath, resourcePath);
                            }
                            // check whether the resource contains a section called 'Type' and add it
                            if (!RegistryUtils.decodeBytes((byte[]) resource.getContent()).contains(TYPE_ELEMENT)) {
                                updateRegistryResourceContent(resource, systemRegistry, rxtDir, rxtPath, resourcePath);
                            }
                            // check whether the resource contains a section called 'enable Schema Validation' and
                            // add it
                            Object enableValidation = resource.getContent();
                            if (!RegistryUtils.decodeBytes((byte[]) enableValidation).
                                    contains(ENABLE_SCHEMA_VALIDATION)) {
                                updateRegistryResourceContent(resource, systemRegistry, rxtDir, rxtPath, resourcePath);
                            }
                        }
                    }
                    continue;
                }
                String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                Resource resource = systemRegistry.newResource();
                resource.setContent(rxt.getBytes(Charset.defaultCharset()));
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

    private void updateRegistryResourceContent(Resource resource, UserRegistry systemRegistry, String rxtDir, String rxtPath, String resourcePath) throws RegistryException, IOException {
        String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
        resource.setContent(rxt.getBytes(Charset.defaultCharset()));
        resource.setMediaType(APIConstants.RXT_MEDIA_TYPE);
        systemRegistry.put(resourcePath, resource);
    }

    private void setupImagePermissions() throws APIManagementException {
        try {
            AuthorizationManager accessControlAdmin = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getAuthorizationManager();
            String imageLocation = APIUtil.getMountedPath(RegistryContext.getBaseInstance(), RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH) + APIConstants.API_IMAGE_LOCATION;
            if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, imageLocation, ActionConstants.GET)) {
                // Can we get rid of this?
                accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, imageLocation, ActionConstants.GET);
            }
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while setting up permissions for image collection", e);
        }
    }

    /**
     * This method will create new permission name  "applications" in registry permission.
     */
    private void addApplicationsPermissionsToRegistry() throws APIManagementException {
        Registry tenantGovReg = getRegistry();
        String permissionResourcePath = CarbonConstants.UI_PERMISSION_NAME + RegistryConstants.PATH_SEPARATOR + APPLICATION_ROOT_PERMISSION;
        try {
            if (!tenantGovReg.resourceExists(permissionResourcePath)) {
                String loggedInUser = CarbonContext.getThreadLocalCarbonContext().getUsername();
                UserRealm realm = (UserRealm) CarbonContext.getThreadLocalCarbonContext().getUserRealm();
                // Logged in user is not authorized to create the permission.
                // Temporarily change the user to the admin for creating the permission
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(realm.getRealmConfiguration().getAdminUserName());
                tenantGovReg = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);
                Collection appRootNode = tenantGovReg.newCollection();
                appRootNode.setProperty("name", "Applications");
                tenantGovReg.put(permissionResourcePath, appRootNode);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(loggedInUser);
            }
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new APIManagementException("Error while reading user store information.", e);
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            throw new APIManagementException("Error while creating new permission in registry", e);
        }
    }

    protected Registry getRegistry() {
        return CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.USER_GOVERNANCE);
    }

    private void configureEventPublisherProperties() {
        OutputEventAdapterConfiguration adapterConfiguration = new OutputEventAdapterConfiguration();
        adapterConfiguration.setName(APIConstants.BLOCKING_EVENT_PUBLISHER);
        adapterConfiguration.setType(APIConstants.BLOCKING_EVENT_TYPE);
        adapterConfiguration.setMessageFormat(APIConstants.BLOCKING_EVENT_FORMAT);
        Map<String, String> adapterParameters = new HashMap<>();
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
            if (configuration.getThrottleProperties().getTrafficManager() != null && configuration.getThrottleProperties().getPolicyDeployer().isEnabled()) {
                ThrottleProperties.TrafficManager trafficManager = configuration.getThrottleProperties().getTrafficManager();
                adapterParameters.put(APIConstants.RECEIVER_URL, trafficManager.getReceiverUrlGroup());
                adapterParameters.put(APIConstants.AUTHENTICATOR_URL, trafficManager.getAuthUrlGroup());
                adapterParameters.put(APIConstants.USERNAME, trafficManager.getUsername());
                adapterParameters.put(APIConstants.PASSWORD, trafficManager.getPassword());
                adapterParameters.put(APIConstants.PROTOCOL, trafficManager.getType());
                adapterParameters.put(APIConstants.PUBLISHING_MODE, APIConstants.NON_BLOCKING);
                adapterParameters.put(APIConstants.PUBLISHING_TIME_OUT, "0");
                adapterConfiguration.setStaticProperties(adapterParameters);
                try {
                    ServiceReferenceHolder.getInstance().getOutputEventAdapterService().create(adapterConfiguration);
                } catch (OutputEventAdapterException e) {
                    log.warn("Exception occurred while creating WSO2 Event Adapter. Request Blocking may not work " + "properly", e);
                }
            } else {
                log.info("Wso2Event Publisher not enabled.");
            }
        } else {
            log.info("api-manager.xml not loaded. Wso2Event Publisher will not be enabled.");
        }
    }

    private void addDefaultAdvancedThrottlePolicies(String tenantDomain, int tenantId) throws APIManagementException {
        APIUtil.addDefaultTenantAdvancedThrottlePolicies(tenantDomain, tenantId);
    }

    private void addDefaultAsyncThrottlePolicies(String tenantDomain, int tenantId) throws APIManagementException {
        APIUtil.addDefaultTenantAsyncThrottlePolicies(tenantDomain, tenantId);
    }

    @Reference(
             name = "config.context.service",
             service = org.wso2.carbon.utils.ConfigurationContextService.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.setContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.setContextService(null);
    }

    @Reference(
             name = "tenant.registryloader",
             service = org.wso2.carbon.registry.core.service.TenantRegistryLoader.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetTenantRegistryLoader")
    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = tenantRegistryLoader;
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        this.tenantRegistryLoader = null;
    }

    public static TenantRegistryLoader getTenantRegistryLoader() {
        return tenantRegistryLoader;
    }

    /**
     * Initialize the Output EventAdapter Service dependency
     *
     * @param outputEventAdapterService Output EventAdapter Service reference
     */
    @Reference(
             name = "event.output.adapter.service",
             service = org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService.class,
             cardinality = ReferenceCardinality.MANDATORY,
             policy = ReferencePolicy.DYNAMIC,
             unbind = "unsetOutputEventAdapterService")
    protected void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        ServiceReferenceHolder.getInstance().setOutputEventAdapterService(outputEventAdapterService);
    }

    /**
     * De-reference the Output EventAdapter Service dependency.
     *
     * @param outputEventAdapterService
     */
    protected void unsetOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        ServiceReferenceHolder.getInstance().setOutputEventAdapterService(null);
    }

    private void configureRecommendationEventPublisherProperties() {
        OutputEventAdapterConfiguration adapterConfiguration = new OutputEventAdapterConfiguration();
        adapterConfiguration.setName(APIConstants.RECOMMENDATIONS_WSO2_EVENT_PUBLISHER);
        adapterConfiguration.setType(APIConstants.BLOCKING_EVENT_TYPE);
        adapterConfiguration.setMessageFormat(APIConstants.BLOCKING_EVENT_FORMAT);
        Map<String, String> adapterParameters = new HashMap<>();
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            if (configuration.getApiRecommendationEnvironment() != null) {
                try {
                    String receiverPort = System.getProperty(configuration.RECEIVER_URL_PORT);
                    String authPort = System.getProperty(configuration.AUTH_URL_PORT);
                    adapterParameters.put(APIConstants.RECEIVER_URL, "tcp://localhost:" + receiverPort);
                    adapterParameters.put(APIConstants.AUTHENTICATOR_URL, "ssl://localhost:" + authPort);
                    adapterParameters.put(APIConstants.USERNAME, APIUtil.getAdminUsername());
                    adapterParameters.put(APIConstants.PASSWORD, APIUtil.getAdminPassword());
                    adapterParameters.put(APIConstants.PROTOCOL, "Binary");
                    adapterParameters.put(APIConstants.PUBLISHING_MODE, APIConstants.NON_BLOCKING);
                    adapterParameters.put(APIConstants.PUBLISHING_TIME_OUT, "0");
                    adapterConfiguration.setStaticProperties(adapterParameters);
                    ServiceReferenceHolder.getInstance().getOutputEventAdapterService().create(adapterConfiguration);
                    log.info("API Recommendation system for dev portal is activated");
                } catch (OutputEventAdapterException e) {
                    log.error("Exception occurred while creating recommendationEventPublisher Adapter." +
                            " Request Blocking may not work properly", e);
                } catch (APIMgtInternalException e) {
                    log.error("Exception occurred while reading the admin username and password", e);
                }
            }
        }
    }

    /**
     * Initialize the Oauth Server configuration Service Service dependency
     *
     * @param oauthServerConfiguration Output EventAdapter Service reference
     */
    @Reference(
            name = "oauth.config.service",
            service = OAuthServerConfiguration.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetOauthServerConfiguration")
    protected void setOauthServerConfiguration(OAuthServerConfiguration oauthServerConfiguration) {
        ServiceReferenceHolder.getInstance().setOauthServerConfiguration(oauthServerConfiguration);
    }

    /**
     * De-reference the Oauth Server configuration Service dependency.
     *
     * @param oAuthServerConfiguration
     */
    protected void unsetOauthServerConfiguration(OAuthServerConfiguration oAuthServerConfiguration) {
        ServiceReferenceHolder.getInstance().setOauthServerConfiguration(null);
    }

    /**
     * Initialize the JWTTransformer Server configuration Service Service dependency
     *
     * @param jwtTransformer {@link JWTTransformer} service reference.
     */
    @Reference(
            name = "jwt.transformer.service",
            service = JWTTransformer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeJWTTransformer")
    protected void addJWTTransformer(JWTTransformer jwtTransformer) {

        ServiceReferenceHolder.getInstance().addJWTTransformer(jwtTransformer.getIssuer(), jwtTransformer);
    }

    /**
     * De-reference the JWTTransformer service
     *
     * @param jwtTransformer
     */
    protected void removeJWTTransformer(JWTTransformer jwtTransformer) {
        ServiceReferenceHolder.getInstance().removeJWTTransformer(jwtTransformer.getIssuer());
    }

    /**
     * Initialize the KeyManager Connector configuration Service Service dependency
     *
     * @param keyManagerConnectorConfiguration {@link KeyManagerConnectorConfiguration} service reference.
     */
    @Reference(
            name = "keyManager.connector.service",
            service = KeyManagerConnectorConfiguration.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeKeyManagerConnectorConfiguration")
    protected void addKeyManagerConnectorConfiguration(
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration) {

        ServiceReferenceHolder.getInstance()
                .addKeyManagerConnectorConfiguration(keyManagerConnectorConfiguration.getType(),
                        keyManagerConnectorConfiguration);
    }

    /**
     * De-reference the JWTTransformer service
     *
     * @param keyManagerConnectorConfiguration
     */
    protected void removeKeyManagerConnectorConfiguration(
            KeyManagerConnectorConfiguration keyManagerConnectorConfiguration, Map<String, Object> properties) {
        if (properties.containsKey(APIConstants.KeyManager.KEY_MANAGER_TYPE)){
            String type = (String) properties.get(APIConstants.KeyManager.KEY_MANAGER_TYPE);
            ServiceReferenceHolder.getInstance().removeKeyManagerConnectorConfiguration(type);
        }
    }


    private void setupAccessTokenGenerator(){

        RecommendationEnvironment recommendationEnvironment = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getApiRecommendationEnvironment();
        if (recommendationEnvironment != null && recommendationEnvironment.getOauthURL()!= null){
            AccessTokenGenerator accessTokenGenerator = new AccessTokenGenerator(
                    recommendationEnvironment.getOauthURL(),
                    recommendationEnvironment.getConsumerKey(),
                    recommendationEnvironment.getConsumerSecret());
            ServiceReferenceHolder.getInstance().setAccessTokenGenerator(accessTokenGenerator);
        }
    }

    @Reference(
            name = "notifier.component",
            service = Notifier.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeNotifiers")
    protected void addNotifier(Notifier notifier) {
        List<Notifier> notifierList = ServiceReferenceHolder.getInstance().getNotifiersMap().get(notifier.getType());
        if (notifierList == null) {
            notifierList = new ArrayList<>();
        }
        notifierList.add(notifier);
        ServiceReferenceHolder.getInstance().getNotifiersMap().put(notifier.getType(), notifierList);
    }

    protected void removeNotifiers(Notifier notifier) {

        ServiceReferenceHolder.getInstance().getNotifiersMap().remove(notifier.getType());
    }

    @Reference(
            name = "externalGatewayDeployer.component",
            service = ExternalGatewayDeployer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeExternalGatewayDeployers")
    protected void addExternalGatewayDeployer(ExternalGatewayDeployer deployer) {
        ServiceReferenceHolder.getInstance().addExternalGatewayDeployer(deployer.getType(), deployer);
    }

    protected void removeExternalGatewayDeployers(ExternalGatewayDeployer deployer) {

        ServiceReferenceHolder.getInstance().removeExternalGatewayDeployer(deployer.getType());
    }

    @Reference(
            name = "externalEnvironment.component",
            service = ExternalEnvironment.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeExternalEnvironments")
    protected void addExternalEnvironmentParser(ExternalEnvironment externalEnvironment) {
        ServiceReferenceHolder.getInstance().addExternalEnvironment(externalEnvironment.getType(),
                externalEnvironment);
    }

    protected void removeExternalEnvironments(ExternalEnvironment externalEnvironment) {

        ServiceReferenceHolder.getInstance().removeExternalEnvironments(externalEnvironment.getType());
    }

    @Reference(
            name = "apiDefinitionParser.component",
            service = APIDefinition.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeAPIDefinitionParsers")
    protected void addAPIDefinitionParser(APIDefinition apiDefinitionParser) {
        ServiceReferenceHolder.getInstance().addAPIDefinitionParser(apiDefinitionParser.getType(), apiDefinitionParser);
    }

    protected void removeAPIDefinitionParsers(APIDefinition apiDefinitionParser) {
        ServiceReferenceHolder.getInstance().removeAPIDefinitionParser(apiDefinitionParser.getType());
    }

    @Reference(
            name = "gateway.artifact.saver",
            service = ArtifactSaver.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeArtifactSaver")
    protected void addArtifactSaver(ArtifactSaver artifactSaver) {
        ServiceReferenceHolder.getInstance().setArtifactSaver(artifactSaver);
        try {
            ServiceReferenceHolder.getInstance().getArtifactSaver().init();
        } catch (Exception e) {
            log.error("Error connecting with the Artifact Saver");
            removeArtifactSaver(null);
        }
    }

    protected void removeArtifactSaver(ArtifactSaver artifactSaver) {
        ServiceReferenceHolder.getInstance().getArtifactSaver().disconnect();
        ServiceReferenceHolder.getInstance().setArtifactSaver(null);
    }

    @Reference(
            name = "import.export.implementation",
            service = ImportExportAPI.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetImportExportService")
    protected void setImportExportService (ImportExportAPI importExportService) {

        ServiceReferenceHolder.getInstance().setImportExportAPI(importExportService);
    }

    protected void unsetImportExportService(ImportExportAPI importExportAPI) {
        ServiceReferenceHolder.getInstance().setImportExportAPI(null);
    }

    /**
     * Method to configure wso2event type event adapter to be used for event notification.
     */
    private void configureNotificationEventPublisher() throws APIManagementException {

        Map<String, String> properties = new HashMap<>();
        if (ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService() != null) {
            APIManagerConfiguration configuration =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration();
            if (configuration.getEventHubConfigurationDto().getEventHubPublisherConfiguration() != null &&
                    configuration.getEventHubConfigurationDto().isEnabled()) {
                EventHubConfigurationDto eventHubConfigurationDto = configuration.getEventHubConfigurationDto();
                EventHubConfigurationDto.EventHubPublisherConfiguration eventHubPublisherConfiguration =
                        eventHubConfigurationDto.getEventHubPublisherConfiguration();
                properties.put(APIConstants.RECEIVER_URL, eventHubPublisherConfiguration.getReceiverUrlGroup());
                properties.put(APIConstants.AUTHENTICATOR_URL, eventHubPublisherConfiguration.getAuthUrlGroup());
                properties.put(APIConstants.USERNAME, eventHubConfigurationDto.getUsername());
                properties.put(APIConstants.PASSWORD, eventHubConfigurationDto.getPassword());
                properties.put(APIConstants.PROTOCOL, eventHubPublisherConfiguration.getType());
                properties.put(APIConstants.PUBLISHING_MODE, APIConstants.NON_BLOCKING);
                properties.put(APIConstants.PUBLISHING_TIME_OUT, "0");
                for (String key : eventHubPublisherConfiguration.getProperties().keySet()) {
                    properties.put(key, eventHubPublisherConfiguration.getProperties().get(key));
                }
                properties.put(APIConstants.IS_ENABLED,
                        Boolean.toString(configuration.getEventHubConfigurationDto().isEnabled()));
                try {
                    ServiceReferenceHolder.getInstance().getEventPublisherFactory().configure(properties);
                } catch (EventPublisherException e) {
                    throw new APIManagementException(e);
                }
            } else {
                log.info("Wso2Event Publisher not enabled.");
            }
        } else {
            log.info("api-manager.xml not loaded. Wso2Event Publisher will not be enabled.");
        }
    }

    @Reference(
            name = "artifactGenerator.service",
            service = GatewayArtifactGenerator.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeGatewayArtifactGenerator")
    protected void addGatewayArtifactGenerator(GatewayArtifactGenerator gatewayArtifactGenerator) {

        ServiceReferenceHolder.getInstance().addGatewayArtifactGenerator(gatewayArtifactGenerator);
    }

    protected void removeGatewayArtifactGenerator(GatewayArtifactGenerator gatewayArtifactGenerator) {
        ServiceReferenceHolder.getInstance().removeGatewayArtifactGenerator(gatewayArtifactGenerator);
    }

    private void retrieveAndSetParentTrustStore() throws APIManagementException {

        char[] trustStorePassword = System.getProperty("javax.net.ssl.trustStorePassword").toCharArray();
        String trustStoreLocation = System.getProperty("javax.net.ssl.trustStore");
        File trustStoreFile = new File(trustStoreLocation);
        try (InputStream localTrustStoreStream = new FileInputStream(trustStoreFile)) {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(localTrustStoreStream, trustStorePassword);
            ServiceReferenceHolder.getInstance().setTrustStore(trustStore);
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            throw new APIManagementException("Error while Reading and set truststore", e);
        }
    }
    
    @Reference(
            name = "organizationResolver.service",
            service = OrganizationResolver.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeOrganizationResolver")
    protected void addOrganizationResolver(OrganizationResolver resolver) {
        ServiceReferenceHolder.getInstance().setOrganizationResolver(resolver);
    }

    protected void removeOrganizationResolver(OrganizationResolver resolver) {
        ServiceReferenceHolder.getInstance().setOrganizationResolver(null);
    }

    @Reference(
            name = "resourceQuotaLimiter.service",
            service = ResourceQuotaLimiter.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeQuotaLimiter")
    protected void addQuotaLimiter(ResourceQuotaLimiter rateLimiter) {
        ServiceReferenceHolder.getInstance().setResourceQuotaLimiter(rateLimiter);
    }

    protected void removeQuotaLimiter(ResourceQuotaLimiter rateLimiter) {
        ServiceReferenceHolder.getInstance().setResourceQuotaLimiter(null);
    }

    @Reference(
            name = "event.publisher.factory",
            service = org.wso2.carbon.apimgt.eventing.EventPublisherFactory.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetEventPublisherFactory")
    protected void setEventPublisherFactory(EventPublisherFactory eventPublisherFactory) {
        ServiceReferenceHolder.getInstance().setEventPublisherFactory(eventPublisherFactory);
    }

    protected void unsetEventPublisherFactory(EventPublisherFactory eventPublisherFactory) {
        ServiceReferenceHolder.getInstance().setEventPublisherFactory(null);
    }
    @Reference(
            name = "apim.config.service",
            service = org.wso2.carbon.apimgt.impl.config.APIMConfigService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIMConfigService")
    protected void setAPIMConfigService(APIMConfigService apimConfigService) {
        ServiceReferenceHolder.getInstance().setAPIMConfigService(apimConfigService);
    }

    protected void unsetAPIMConfigService(APIMConfigService apimConfigService) {
        ServiceReferenceHolder.getInstance().setAPIMConfigService(null);
    }

    /**
     * Populate the http client configuration {@link HttpClientConfigurationDTO} assigned to the
     * API Manager Configuration
     */
    void populateHttpClientConfiguration() {
        APIManagerConfiguration configuration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                        .getAPIManagerConfiguration();
        HttpClientConfigurationDTO.Builder builder  = new HttpClientConfigurationDTO.Builder();

        int maxTotal = Integer.parseInt(configuration.getFirstProperty(APIConstants.HTTP_CLIENT_MAX_TOTAL));
        int defaultMaxPerRoute = Integer.parseInt(configuration.getFirstProperty(APIConstants.HTTP_CLIENT_DEFAULT_MAX_PER_ROUTE));

        boolean proxyEnabled = Boolean.parseBoolean(configuration.getFirstProperty(APIConstants.PROXY_ENABLE));

        if (proxyEnabled) {
            String proxyHost = configuration.getFirstProperty(APIConstants.PROXY_HOST);
            int proxyPort = Integer.parseInt(configuration.getFirstProperty(APIConstants.PROXY_PORT));
            String proxyUsername = configuration.getFirstProperty(APIConstants.PROXY_USERNAME);
            String proxyPassword = configuration.getFirstProperty(APIConstants.PROXY_PASSWORD);
            String nonProxyHostsString = configuration.getFirstProperty(APIConstants.NON_PROXY_HOSTS);
            String[] nonProxyHosts = configuration.getFirstProperty(nonProxyHostsString) != null ?
                    nonProxyHostsString.split("\\|") : null;
            String proxyProtocol = configuration.getFirstProperty(APIConstants.PROXY_PROTOCOL);
            builder = builder.withProxy(proxyHost, proxyPort, proxyUsername, proxyPassword, proxyProtocol,
                    nonProxyHosts);
        }

        SSLContext sslContext = null;
        try {
            KeyStore trustStore = ServiceReferenceHolder.getInstance().getTrustStore();
            sslContext = SSLContexts.custom().loadTrustMaterial(trustStore, null).build();
        } catch ( KeyStoreException e) {
            log.error("Failed to read from Key Store", e);
        } catch (
                NoSuchAlgorithmException e) {
            log.error("Failed to load Key Store.", e);
        } catch (
                KeyManagementException e) {
            log.error("Failed to load key from Key Store" , e);
        }
        String hostnameVerifierOption = System.getProperty(HOST_NAME_VERIFIER);
        HostnameVerifier hostnameVerifier;
        switch(hostnameVerifierOption) {
            case ALLOW_ALL:
                hostnameVerifier = NoopHostnameVerifier.INSTANCE;;
                break;
            case STRICT:
                hostnameVerifier = new DefaultHostnameVerifier();
                break;
            default:
                hostnameVerifier = new BrowserHostnameVerifier();
        }
        configuration.setHttpClientConfiguration(builder.withConnectionParams(maxTotal, defaultMaxPerRoute)
                .withSSLContext(sslContext, hostnameVerifier).build());
    }
    @Reference(
            name = "apim.workflow.task.service",
            service = org.wso2.carbon.apimgt.api.model.WorkflowTaskService.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetWorkflowTaskService")
    protected void setWorkflowTaskService(WorkflowTaskService workflowTaskService) {
        ServiceReferenceHolder.getInstance().setWorkflowTaskService(workflowTaskService);
    }

    protected void unsetWorkflowTaskService(WorkflowTaskService workflowTaskService) {
        ServiceReferenceHolder.getInstance().setWorkflowTaskService(null);
    }
}

