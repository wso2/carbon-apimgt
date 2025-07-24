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

package org.wso2.carbon.apimgt.impl.internal;

import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.api.OrganizationResolver;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.WorkflowTaskService;
import org.wso2.carbon.apimgt.api.quotalimiter.ResourceQuotaLimiter;
import org.wso2.carbon.apimgt.common.gateway.jwttransformer.JWTTransformer;
import org.wso2.carbon.apimgt.eventing.EventPublisherFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.ExternalEnvironment;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.config.APIMConfigServiceImpl;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.GatewayArtifactGenerator;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;
import org.wso2.carbon.apimgt.impl.workflow.DefaultWorkflowTaskService;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private static UserRealm userRealm;
    private static ConfigurationContextService contextService;
    private RegistryService registryService;
    private APIManagerConfigurationService amConfigurationService;
    private RealmService realmService;
    private TenantIndexingLoader indexLoader;
    private OutputEventAdapterService outputEventAdapterService;
    private KeyStore trustStore;
    private KeyStore listenerTrustStore;
    private AccessTokenGenerator accessTokenGenerator;
    private KeyManagerConfigurationService keyManagerConfigurationService;
    private OAuthServerConfiguration oauthServerConfiguration;
    private Map<String, JWTTransformer> jwtTransformerMap = new HashMap<>();
    private Map<String, KeyManagerConnectorConfiguration> keyManagerConnectorConfigurationMap = new HashMap<>();
    private ArtifactSaver artifactSaver;
    private Map<String, List<Notifier>> notifiersMap = new HashMap<>();
    private ImportExportAPI importExportService;
    private Map<String, GatewayArtifactGenerator> gatewayArtifactGeneratorMap = new HashMap<>();
    private OrganizationResolver organizationResolver;
    private ResourceQuotaLimiter resourceQuotaLimiter;
    private EventPublisherFactory eventPublisherFactory;
    private APIMConfigService apimConfigService;
    private Map<String, GatewayAgentConfiguration> gatewayConnectorConfigurationMap = new HashMap<>();
    private Map<String, ExternalEnvironment> externalEnvironmentsMap = new HashMap<>();
    private Map<String, APIDefinition> apiDefinitionMap = new HashMap<>();
    private WorkflowTaskService workflowTaskService;

    private Map<String, LLMProviderService> llmProviderServiceMap = new HashMap();

    private ServiceReferenceHolder() {

    }

    public static ConfigurationContextService getContextService() {

        return contextService;
    }

    public static void setContextService(ConfigurationContextService contextService) {

        ServiceReferenceHolder.contextService = contextService;
    }

    public static ServiceReferenceHolder getInstance() {

        return instance;
    }

    public RegistryService getRegistryService() {

        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {

        this.registryService = registryService;
    }

    @UsedByMigrationClient
    public APIManagerConfigurationService getAPIManagerConfigurationService() {

        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {

        this.amConfigurationService = amConfigurationService;
    }

    @UsedByMigrationClient
    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    public TenantIndexingLoader getIndexLoaderService() {

        return indexLoader;
    }

    public void setIndexLoaderService(TenantIndexingLoader indexLoader) {

        this.indexLoader = indexLoader;
    }

    public OutputEventAdapterService getOutputEventAdapterService() {

        return outputEventAdapterService;
    }

    public void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {

        this.outputEventAdapterService = outputEventAdapterService;
    }

    public KeyStore getTrustStore() {

        return trustStore;
    }

    public void setTrustStore(KeyStore trustStore) {

        this.trustStore = trustStore;
    }

    public AccessTokenGenerator getAccessTokenGenerator() {

        return accessTokenGenerator;
    }

    public void setAccessTokenGenerator(
            AccessTokenGenerator accessTokenGenerator) {

        this.accessTokenGenerator = accessTokenGenerator;
    }

    public KeyManagerConfigurationService getKeyManagerConfigurationService() {

        return keyManagerConfigurationService;
    }

    public void setKeyManagerConfigurationService(
            KeyManagerConfigurationService keyManagerConfigurationService) {

        this.keyManagerConfigurationService = keyManagerConfigurationService;
    }

    public OAuthServerConfiguration getOauthServerConfiguration() {

        return oauthServerConfiguration;
    }

    public void setOauthServerConfiguration(OAuthServerConfiguration oauthServerConfiguration) {

        this.oauthServerConfiguration = oauthServerConfiguration;
    }

    public void addJWTTransformer(String issuer, JWTTransformer jwtTransformer) {

        jwtTransformerMap.put(issuer, jwtTransformer);
    }

    public void removeJWTTransformer(String issuer) {

        jwtTransformerMap.remove(issuer);
    }

    public JWTTransformer getJWTTransformer(String issuer) {

        return jwtTransformerMap.get(issuer);
    }

    public void addKeyManagerConnectorConfiguration(String type,
                                                    KeyManagerConnectorConfiguration keyManagerConnectorConfiguration) {

        keyManagerConnectorConfigurationMap.put(type, keyManagerConnectorConfiguration);
    }

    public void removeKeyManagerConnectorConfiguration(String type) {

        keyManagerConnectorConfigurationMap.remove(type);
    }

    public KeyManagerConnectorConfiguration getKeyManagerConnectorConfiguration(String type) {

        return keyManagerConnectorConfigurationMap.get(type);
    }

    public Map<String, KeyManagerConnectorConfiguration> getKeyManagerConnectorConfigurations() {

        return keyManagerConnectorConfigurationMap;
    }

    public Map<String, List<Notifier>> getNotifiersMap() {

        return notifiersMap;
    }

    public ArtifactSaver getArtifactSaver() {

        return artifactSaver;
    }

    public void setArtifactSaver(ArtifactSaver artifactSaver) {

        this.artifactSaver = artifactSaver;
    }

    public void setImportExportAPI(ImportExportAPI importExportService) {

        this.importExportService = importExportService;
    }

    public ImportExportAPI getImportExportService() {

        return importExportService;
    }

    public void addGatewayArtifactGenerator(GatewayArtifactGenerator gatewayArtifactGenerator) {

        if (gatewayArtifactGenerator != null) {
            gatewayArtifactGeneratorMap.put(gatewayArtifactGenerator.getType(), gatewayArtifactGenerator);
        }
    }

    public void removeGatewayArtifactGenerator(GatewayArtifactGenerator gatewayArtifactGenerator) {

        if (gatewayArtifactGenerator != null) {
            gatewayArtifactGeneratorMap.remove(gatewayArtifactGenerator.getType());
        }
    }

    public GatewayArtifactGenerator getGatewayArtifactGenerator(String type) {

        return gatewayArtifactGeneratorMap.get(type);
    }

    public Set<String> getGatewayArtifactGeneratorTypes() {

        return gatewayArtifactGeneratorMap.keySet();
    }

    public KeyStore getListenerTrustStore() {

        return listenerTrustStore;
    }

    public void setListenerTrustStore(KeyStore listenerTrustStore) {

        this.listenerTrustStore = listenerTrustStore;
    }

    public OrganizationResolver getOrganizationResolver() {
        return organizationResolver;
    }

    public void setOrganizationResolver(OrganizationResolver organizationResolver) {
        this.organizationResolver = organizationResolver;
    }

    public ResourceQuotaLimiter getResourceQuotaLimiter() {
        return resourceQuotaLimiter;
    }

    public void setResourceQuotaLimiter(ResourceQuotaLimiter resourceQuotaLimiter) {
        this.resourceQuotaLimiter = resourceQuotaLimiter;
    }

    public EventPublisherFactory getEventPublisherFactory() {
        return eventPublisherFactory;
    }

    public void setEventPublisherFactory(EventPublisherFactory eventPublisherFactory) {
        this.eventPublisherFactory = eventPublisherFactory;
    }

    public void setAPIMConfigService(APIMConfigService apimConfigService) {
        this.apimConfigService = apimConfigService;
    }

    @UsedByMigrationClient
    public APIMConfigService getApimConfigService() {
        if (apimConfigService != null){
            return apimConfigService;
        }
        return new APIMConfigServiceImpl();
    }


    public void addExternalGatewayConnectorConfiguration(String type, GatewayAgentConfiguration gatewayConfiguration) {

        gatewayConnectorConfigurationMap.put(type, gatewayConfiguration);
    }

    public void removeExternalGatewayConnectorConfiguration(String type) {

        gatewayConnectorConfigurationMap.remove(type);
    }

    public GatewayAgentConfiguration getExternalGatewayConnectorConfiguration(String type) {

        return gatewayConnectorConfigurationMap.get(type);
    }

    public Map<String, GatewayAgentConfiguration> getExternalGatewayConnectorConfigurations() {

            return gatewayConnectorConfigurationMap;
    }

    public void addExternalEnvironment(String type, ExternalEnvironment externalEnvironment) {

        externalEnvironmentsMap.put(type, externalEnvironment);
    }

    public ExternalEnvironment getExternalEnvironment(String type) {

        return externalEnvironmentsMap.get(type);
    }

    public void removeExternalEnvironments(String type) {

        externalEnvironmentsMap.remove(type);
    }

    public void addAPIDefinitionParser(String type, APIDefinition apiDefinition) {

        apiDefinitionMap.put(type, apiDefinition);
    }

    public Map<String, APIDefinition> getApiDefinitionMap() {

        return apiDefinitionMap;
    }

    public void removeAPIDefinitionParser(String type) {

        apiDefinitionMap.remove(type);
    }

    public WorkflowTaskService getWorkflowTaskService() {
        if (workflowTaskService == null) {
            this.workflowTaskService = new DefaultWorkflowTaskService();
        }
        return workflowTaskService;
    }

    public void setWorkflowTaskService(WorkflowTaskService workflowTaskService) {

        this.workflowTaskService = workflowTaskService;    
    }

    public void addLLMProviderService(String type, LLMProviderService llmProviderService) {

        llmProviderServiceMap.put(type, llmProviderService);
    }

    public void removeLLMProviderService(String type) {

        llmProviderServiceMap.remove(type);
    }

    public LLMProviderService getLLMProviderService(String type) {

        return llmProviderServiceMap.get(type);
    }

    public Map<String, LLMProviderService> getLLMProviderServiceMap() {

        return this.llmProviderServiceMap;
    }
}
