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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.analytics.collectors.AnalyticsCustomDataProvider;
import org.wso2.carbon.apimgt.common.gateway.jwtgenerator.AbstractAPIMgtGatewayJWTGenerator;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.gateway.utils.redis.RedisCacheUtils;
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.caching.CacheInvalidationService;
import org.wso2.carbon.apimgt.impl.dto.RedisConfig;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerDataService;
import org.wso2.carbon.apimgt.impl.throttling.APIThrottleDataService;
import org.wso2.carbon.apimgt.impl.token.RevokedTokenService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.webhooks.SubscriptionsDataService;
import org.wso2.carbon.apimgt.tracing.TracingService;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryService;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryTracer;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.endpoint.service.EndpointAdmin;
import org.wso2.carbon.localentry.service.LocalEntryAdmin;
import org.wso2.carbon.mediation.initializer.services.SynapseConfigurationService;
import org.wso2.carbon.mediation.security.vault.MediationSecurityAdminService;
import org.wso2.carbon.rest.api.service.RestApiAdmin;
import org.wso2.carbon.sequences.services.SequenceAdmin;
import org.wso2.carbon.utils.ConfigurationContextService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class ServiceReferenceHolder {

    private static final Log log = LogFactory.getLog(ServiceReferenceHolder.class);

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private ConfigurationContextService cfgCtxService;
    private APIManagerConfigurationService amConfigService;
    private final ThrottleDataHolder throttleDataHolder = ThrottleDataHolder.getInstance();
    private ThrottleProperties throttleProperties;
    private ConfigurationContext axis2ConfigurationContext;
    private TracingService tracingService;
    private TelemetryService telemetryService;
    private ServerConfigurationService serverConfigurationService;
    private RestApiAdmin restAPIAdmin;
    private SequenceAdmin sequenceAdmin;
    private LocalEntryAdmin localEntryAdmin;
    private EndpointAdmin endpointAdmin;
    private MediationSecurityAdminService mediationSecurityAdminService;
    private ThrottleDataPublisher throttleDataPublisher;
    private Map<String,AbstractAPIMgtGatewayJWTGenerator> apiMgtGatewayJWTGenerators  = new HashMap<>();
    private TracingTracer tracer;
    private TelemetryTracer telemetryTracer;
    private CacheInvalidationService cacheInvalidationService;
    private RevokedTokenService revokedTokenService;
    private APIThrottleDataService throttleDataService;
    private SynapseConfigurationService synapseConfigurationService;
    private Certificate publicCert;
    private PrivateKey privateKey;

    private JWTValidationService jwtValidationService;
    private KeyManagerDataService keyManagerDataService;
    private SubscriptionsDataService subscriptionsDataService;
    private AnalyticsCustomDataProvider analyticsCustomDataProvider;

    private Set<String> activeTenants = new ConcurrentSkipListSet<>();
    private JedisPool redisPool;

    public ThrottleDataHolder getThrottleDataHolder() {
        return throttleDataHolder;
    }
    private ArtifactRetriever artifactRetriever;
    private int gatewayCount = 1;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigurationContextService(ConfigurationContextService cfgCtxService) {
        this.cfgCtxService = cfgCtxService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return cfgCtxService;
    }

    public ConfigurationContext getServerConfigurationContext() {
        return cfgCtxService.getServerConfigContext();
    }

    public APIManagerConfiguration getAPIManagerConfiguration() {
        return amConfigService.getAPIManagerConfiguration();
    }

    public APIManagerConfigurationService getApiManagerConfigurationService() {
        return amConfigService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigService) {
        this.amConfigService = amConfigService;
        if (amConfigService != null) {
            if (amConfigService.getAPIManagerConfiguration() != null) {
                setThrottleProperties(amConfigService.getAPIManagerConfiguration().getThrottleProperties());
            }
            if (amConfigService.getAPIAnalyticsConfiguration() != null) {
                setAnalyticsCustomDataProvider(amConfigService.getAPIAnalyticsConfiguration());
            }
        }
    }

    public ThrottleProperties getThrottleProperties() {
        return throttleProperties;
    }

    public void setThrottleProperties(ThrottleProperties throttleProperties) {
        this.throttleProperties = throttleProperties;
    }

    public void setAxis2ConfigurationContext(ConfigurationContext axis2ConfigurationContext) {
        this.axis2ConfigurationContext = axis2ConfigurationContext;
    }

    public ConfigurationContext getAxis2ConfigurationContext() {

        return cfgCtxService.getClientConfigContext();
    }

    public TracingService getTracingService() {
        return tracingService;
    }

    public TelemetryService getTelemetryService() { return telemetryService; }
    public void setTracingService(TracingService tracingService) {
        this.tracingService = tracingService;
    }
    public void setTelemetryService(TelemetryService telemetryService) { this.telemetryService = telemetryService; }
    /**
     * To get the server configuration service.
     *
     * @return an instance of {@link ServerConfigurationService}
     */
    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    /**
     * To set the server configuration service.
     *
     * @param serverConfigurationService {@link ServerConfigurationService}
     */
    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    public void setRestAPIAdmin(RestApiAdmin restAPIAdmin) {
        this.restAPIAdmin = restAPIAdmin;

    }

    public RestApiAdmin getRestAPIAdmin() {

        return restAPIAdmin;
    }

    public void setSequenceAdmin(SequenceAdmin sequenceAdmin) {
        this.sequenceAdmin = sequenceAdmin;

    }

    public SequenceAdmin getSequenceAdmin() {

        return sequenceAdmin;
    }

    public void setLocalEntryAdmin(LocalEntryAdmin localEntryAdmin) {
        this.localEntryAdmin = localEntryAdmin;
    }

    public LocalEntryAdmin getLocalEntryAdmin() {

        return localEntryAdmin;
    }

    public EndpointAdmin getEndpointAdmin() {

        return endpointAdmin;
    }

    public void setEndpointAdmin(EndpointAdmin endpointAdmin) {

        this.endpointAdmin = endpointAdmin;
    }

    public MediationSecurityAdminService getMediationSecurityAdminService() {

        return mediationSecurityAdminService;
    }

    public void setMediationSecurityAdminService(MediationSecurityAdminService mediationSecurityAdminService) {

        this.mediationSecurityAdminService = mediationSecurityAdminService;
    }

    public ThrottleDataPublisher getThrottleDataPublisher() {

        return throttleDataPublisher;
    }

    public void setThrottleDataPublisher(ThrottleDataPublisher throttleDataPublisher) {

        this.throttleDataPublisher = throttleDataPublisher;
    }



    public Map<String,AbstractAPIMgtGatewayJWTGenerator> getApiMgtGatewayJWTGenerator() {

        return apiMgtGatewayJWTGenerators;
    }

    public TracingTracer getTracer() {

        return tracer;
    }

    public TelemetryTracer getTelemetryTracer() {

        return telemetryTracer;
    }

    public void setTracer(TracingTracer tracer) {

        this.tracer = tracer;
    }

    public void setTelemetry(TelemetryTracer telemetryTracer) {

        this.telemetryTracer = telemetryTracer;
    }

    public JWTValidationService getJwtValidationService() {

        return jwtValidationService;
    }

    public void setJwtValidationService(JWTValidationService jwtValidationService) {

        this.jwtValidationService = jwtValidationService;
    }

    public ArtifactRetriever getArtifactRetriever() {

        return artifactRetriever;
    }

    public void setArtifactRetriever(ArtifactRetriever artifactRetriever) {

        this.artifactRetriever = artifactRetriever;
    }
    public void setCacheInvalidationService(CacheInvalidationService cacheInvalidationService) {
        this.cacheInvalidationService = cacheInvalidationService;

    }

    public CacheInvalidationService getCacheInvalidationService() {

        return cacheInvalidationService;
    }

    public void setRevokedTokenService(RevokedTokenService revokedTokenService) {
        this.revokedTokenService = revokedTokenService;
    }

    public RevokedTokenService getRevokedTokenService() {

        return revokedTokenService;
    }
    public void setAPIThrottleDataService(APIThrottleDataService dataService) {
        if (dataService != null) {
            throttleDataService = dataService;
        } else {
            throttleDataService = null;
        }
    }

    public APIThrottleDataService getAPIThrottleDataService() {
        return throttleDataService;
    }

    public KeyManagerDataService getKeyManagerDataService() {

        return keyManagerDataService;
    }

    public void setKeyManagerDataService(KeyManagerDataService keyManagerDataService) {

        this.keyManagerDataService = keyManagerDataService;
    }

    public SubscriptionsDataService getSubscriptionsDataService() {
        return subscriptionsDataService;
    }

    public void setSubscriptionsDataService(SubscriptionsDataService subscriptionsDataService) {
        if (subscriptionsDataService != null) {
            this.subscriptionsDataService = subscriptionsDataService;
        } else {
            this.subscriptionsDataService = null;
        }
    }

    public void setPublicCert() {
        try {
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
            this.publicCert = keyStoreManager.getDefaultPrimaryCertificate();

        } catch (Exception e) {
            String error = "Error in obtaining keystore";
            log.debug(error, e);

        }
    }

    public Certificate getPublicCert() {
        return this.publicCert;
    }

    public void setPrivateKey() {
        try {
            KeyStoreManager keyStoreManager = KeyStoreManager.getInstance(MultitenantConstants.SUPER_TENANT_ID);
            this.privateKey = keyStoreManager.getDefaultPrivateKey();
        } catch (Exception e) {
            String error = "Error in obtaining keystore";
            log.debug(error, e);
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void addLoadedTenant(String tenantDomain) {

        activeTenants.add(tenantDomain);
    }

    public void removeUnloadedTenant(String tenantDomain) {

        activeTenants.remove(tenantDomain);
    }

    public boolean isTenantLoaded(String tenantDomain) {

        return activeTenants.contains(tenantDomain);
    }

    public Set<String> getActiveTenants() {
        return activeTenants;
    }

    public void setRedisCacheUtil(RedisCacheUtils redisCacheUtils) {

    }

    public boolean isRedisEnabled() {

        RedisConfig redisConfigProperties = getAPIManagerConfiguration().getRedisConfig();
        if (redisConfigProperties != null && redisConfigProperties.isRedisEnabled()) {
            return true;
        } else {
            return false;
        }
    }

    public JedisPool getRedisPool() {

        return redisPool;
    }

    public void setRedisPool(JedisPool redisPool) {

        this.redisPool = redisPool;
    }

    public AnalyticsCustomDataProvider getAnalyticsCustomDataProvider() {
        return analyticsCustomDataProvider;
    }

    private void setAnalyticsCustomDataProvider(APIManagerAnalyticsConfiguration apiManagerAnalyticsConfiguration) {
        String customPublisherClass = null;
        if (apiManagerAnalyticsConfiguration.getReporterProperties() != null && apiManagerAnalyticsConfiguration
                .getReporterProperties().containsKey(Constants.API_ANALYTICS_CUSTOM_DATA_PROVIDER_CLASS)) {
            customPublisherClass = apiManagerAnalyticsConfiguration.getReporterProperties()
                    .get(Constants.API_ANALYTICS_CUSTOM_DATA_PROVIDER_CLASS);
        }
        if (customPublisherClass != null) {
            try {
                Class<?> c = APIUtil.getClassForName(customPublisherClass);
                Constructor<?> cons = c.getConstructors()[0];
                analyticsCustomDataProvider = (AnalyticsCustomDataProvider) cons.newInstance();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException
                    | ClassNotFoundException e) {
                log.error("Error in obtaining custom publisher class", e);
            }
        }
    }

    public SynapseConfigurationService getSynapseConfigurationService() {
        return synapseConfigurationService;
    }

    public void setSynapseConfigurationService(SynapseConfigurationService synapseConfigurationService) {
        this.synapseConfigurationService = synapseConfigurationService;
    }

    public int getGatewayCount() {
        return gatewayCount;
    }

    public void setGatewayCount(int gatewayCount) {
        this.gatewayCount = gatewayCount;
    }

}
