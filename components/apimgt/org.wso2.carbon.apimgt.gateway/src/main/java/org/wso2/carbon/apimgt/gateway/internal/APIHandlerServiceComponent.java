/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.internal;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.keys.APIKeyValidatorClientPool;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTMapCleaner;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTTokensRetriever;
import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataService;
import org.wso2.carbon.apimgt.gateway.service.APIThrottleDataServiceImpl;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.util.BlockingConditionRetriever;
import org.wso2.carbon.apimgt.gateway.throttling.util.KeyTemplateRetriever;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.tracing.TracingService;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "org.wso2.carbon.apimgt.handlers", 
         immediate = true)
public class APIHandlerServiceComponent {

    private static final Log log = LogFactory.getLog(APIHandlerServiceComponent.class);

    private APIKeyValidatorClientPool clientPool;

    private APIManagerConfiguration configuration = new APIManagerConfiguration();

    private ServiceRegistration registration;

    @Activate
    protected void activate(ComponentContext context) {
        BundleContext bundleContext = context.getBundleContext();
        if (log.isDebugEnabled()) {
            log.debug("API handlers component activated");
        }
        try {
            ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(getClientRepoLocation(), getAxis2ClientXmlLocation());
            ServiceReferenceHolder.getInstance().setAxis2ConfigurationContext(ctx);
            if (APIConstants.API_KEY_VALIDATOR_WS_CLIENT.equals(APISecurityUtils.getKeyValidatorClientType())) {
                clientPool = APIKeyValidatorClientPool.getInstance();
            }
            String filePath = getFilePath();
            configuration.load(filePath);
            String gatewayType = configuration.getFirstProperty(APIConstants.API_GATEWAY_TYPE);
            if ("Synapse".equalsIgnoreCase(gatewayType)) {
                // Register Tenant service creator to deploy tenant specific common synapse configurations
                TenantServiceCreator listener = new TenantServiceCreator();
                bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(), listener, null);
                if (configuration.getThrottleProperties().isEnabled()) {
                    ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
                    APIThrottleDataServiceImpl throttleDataServiceImpl = new APIThrottleDataServiceImpl();
                    throttleDataServiceImpl.setThrottleDataHolder(throttleDataHolder);
                    // Register APIThrottleDataService so that ThrottleData maps are available to other components.
                    registration = context.getBundleContext().registerService(APIThrottleDataService.class.getName(), throttleDataServiceImpl, null);
                    ServiceReferenceHolder.getInstance().setThrottleDataHolder(throttleDataHolder);
                    log.debug("APIThrottleDataService Registered...");
                    ServiceReferenceHolder.getInstance().setThrottleProperties(configuration.getThrottleProperties());
                    // start web service throttle data retriever as separate thread and start it.
                    if (configuration.getThrottleProperties().getBlockCondition().isEnabled()) {
                        BlockingConditionRetriever webServiceThrottleDataRetriever = new BlockingConditionRetriever();
                        webServiceThrottleDataRetriever.startWebServiceThrottleDataRetriever();
                        KeyTemplateRetriever webServiceBlockConditionsRetriever = new KeyTemplateRetriever();
                        webServiceBlockConditionsRetriever.startKeyTemplateDataRetriever();

                        // Start web service based revoked JWT tokens retriever.
                        // Advanced throttle properties & blocking conditions have to be enabled for JWT token retrieval due to the throttle config dependency for this feature.
                        RevokedJWTTokensRetriever webServiceRevokedJWTTokensRetriever = new RevokedJWTTokensRetriever();
                        webServiceRevokedJWTTokensRetriever.startRevokedJWTTokensRetriever();
                    }
                }


                // Start JWT revoked map cleaner.
                RevokedJWTMapCleaner revokedJWTMapCleaner = new RevokedJWTMapCleaner();
                revokedJWTMapCleaner.startJWTRevokedMapCleaner();

                // Read the trust store
                ServerConfiguration config = CarbonUtils.getServerConfiguration();
                String trustStorePassword = config.getFirstProperty(APIMgtGatewayConstants.TRUST_STORE_PASSWORD);
                String trustStoreLocation = config.getFirstProperty(APIMgtGatewayConstants.TRUST_STORE_LOCATION);
                if (trustStoreLocation != null && trustStorePassword != null) {
                    try (FileInputStream trustStoreStream = new FileInputStream(new File(trustStoreLocation))) {
                        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                        trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
                        ServiceReferenceHolder.getInstance().setTrustStore(trustStore);
                    } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
                        log.error("Error in loading trust store.", e);
                    }
                } else {
                    log.error("Error in loading trust store. Configurations are not set.");
                }
            }
        } catch (APIManagementException | AxisFault e) {
            log.error("Error while initializing the API Gateway (APIHandlerServiceComponent) component", e);
        }
        // Create caches for the super tenant
        ServerConfiguration.getInstance().overrideConfigurationProperty("Cache.ForceLocalCache", "true");
        CacheProvider.createGatewayKeyCache();
        CacheProvider.createResourceCache();
        CacheProvider.createGatewayTokenCache();
        CacheProvider.createInvalidTokenCache();
        CacheProvider.createGatewayBasicAuthResourceCache();
        CacheProvider.createGatewayUsernameCache();
        CacheProvider.createInvalidUsernameCache();
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("API handlers component deactivated");
        }
        if (APIConstants.API_KEY_VALIDATOR_WS_CLIENT.equals(APISecurityUtils.getKeyValidatorClientType())) {
            clientPool.cleanup();
        }
        if (registration != null) {
            log.debug("Unregistering ThrottleDataService...");
            registration.unregister();
        }
    }

    @Reference(
             name = "configuration.context.service", 
             service = org.wso2.carbon.utils.ConfigurationContextService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetConfigurationContextService")
    protected void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("Configuration context service bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setConfigurationContextService(cfgCtxService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService cfgCtxService) {
        if (log.isDebugEnabled()) {
            log.debug("Configuration context service unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setConfigurationContextService(null);
    }

    /**
     * This method will be called when {@link ServerConfigurationService} instance is available in OSGI environment.
     *
     * @param serverConfigurationService Instance of {@link ServerConfigurationService}
     */
    @Reference(
             name = "server.configuration.service", 
             service = org.wso2.carbon.base.api.ServerConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Server configuration service is bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setServerConfigurationService(serverConfigurationService);
    }

    /**
     * This method will be called when {@link ServerConfigurationService} instance is being removed from OSGI
     * environment.
     *
     * @param serverConfigurationService Instance of {@link ServerConfigurationService}
     */
    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Server configuration service is unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setServerConfigurationService(null);
    }

    @Reference(
             name = "api.manager.config.service", 
             service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service bound to the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {
        if (log.isDebugEnabled()) {
            log.debug("API manager configuration service unbound from the API handlers");
        }
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

    protected String getFilePath() {
        return CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
    }

    protected void setConfiguration(APIManagerConfiguration configuration) {
        this.configuration = configuration;
    }

    protected String getAxis2ClientXmlLocation() {
        String axis2ClientXml = ServerConfiguration.getInstance().getFirstProperty("Axis2Config" + ".clientAxis2XmlLocation");
        return axis2ClientXml;
    }

    protected String getClientRepoLocation() {
        String axis2ClientXml = ServerConfiguration.getInstance().getFirstProperty("Axis2Config" + ".ClientRepositoryLocation");
        return axis2ClientXml;
    }

    @Reference(
             name = "org.wso2.carbon.apimgt.tracing", 
             service = org.wso2.carbon.apimgt.tracing.TracingService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetTracingService")
    protected void setTracingService(TracingService tracingService) {
        ServiceReferenceHolder.getInstance().setTracingService(tracingService);
    }

    protected void unsetTracingService(TracingService tracingService) {
        ServiceReferenceHolder.getInstance().setTracingService(null);
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
}

