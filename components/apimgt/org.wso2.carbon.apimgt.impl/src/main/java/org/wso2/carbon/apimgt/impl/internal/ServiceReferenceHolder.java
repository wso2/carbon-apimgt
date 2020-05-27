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

import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.jwt.transformer.JWTTransformer;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactRetriever;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactPublisher;
import org.wso2.carbon.apimgt.impl.workflow.events.APIMgtWorkflowDataPublisher;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private RegistryService registryService;
    private APIManagerConfigurationService amConfigurationService;
    private RealmService realmService;
    private static UserRealm userRealm;
    private TenantIndexingLoader indexLoader;
    private OutputEventAdapterService outputEventAdapterService;
    private APIMgtWorkflowDataPublisher apiMgtWorkflowDataPublisher;
    private KeyStore trustStore;
    private AccessTokenGenerator accessTokenGenerator;
    private KeyManagerConfigurationService keyManagerConfigurationService;
    private OAuthServerConfiguration oauthServerConfiguration;
    private Map<String,JWTTransformer> jwtTransformerMap = new HashMap<>();
    private Map<String,KeyManagerConnectorConfiguration> keyManagerConnectorConfigurationMap  = new HashMap<>();
    private ArtifactPublisher artifactPublisher;
    private ArtifactRetriever artifactRetriever;
    private Map<String, List<Notifier>> notifiersMap = new HashMap<>();

    public static ConfigurationContextService getContextService() {
        return contextService;
    }

    public static void setContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.contextService = contextService;
    }

    private static ConfigurationContextService contextService;
    private ServiceReferenceHolder() {
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

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {
        this.amConfigurationService = amConfigurationService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
    public static void setUserRealm(UserRealm realm) {
        userRealm = realm;
    }

    public static UserRealm getUserRealm() {
        return userRealm;
    }

    public void setIndexLoaderService(TenantIndexingLoader indexLoader) {
        this.indexLoader = indexLoader;
    }

    public TenantIndexingLoader getIndexLoaderService(){
        return indexLoader;
    }

    public OutputEventAdapterService getOutputEventAdapterService() {
        return outputEventAdapterService;
    }

    public void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        this.outputEventAdapterService = outputEventAdapterService;
    }

    public APIMgtWorkflowDataPublisher getApiMgtWorkflowDataPublisher() {
        return apiMgtWorkflowDataPublisher;
    }

    public void setApiMgtWorkflowDataPublisher(APIMgtWorkflowDataPublisher apiMgtWorkflowDataPublisher) {
        this.apiMgtWorkflowDataPublisher = apiMgtWorkflowDataPublisher;
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

    public void setOauthServerConfiguration(OAuthServerConfiguration oauthServerConfiguration) {
        this.oauthServerConfiguration = oauthServerConfiguration;
    }

    public OAuthServerConfiguration getOauthServerConfiguration() {

        return oauthServerConfiguration;
    }

    public void addJWTTransformer(String issuer, JWTTransformer jwtTransformer) {

        jwtTransformerMap.put(issuer, jwtTransformer);
    }

    public void removeJWTTransformer(String issuer) {

        jwtTransformerMap.remove(issuer);
    }
    public JWTTransformer getJWTTransformer(String issuer){
        return jwtTransformerMap.get(issuer);
    }

    public void addKeyManagerConnectorConfiguration(String type,
                                                    KeyManagerConnectorConfiguration keyManagerConnectorConfiguration) {
        keyManagerConnectorConfigurationMap.put(type,keyManagerConnectorConfiguration);
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

    public ArtifactPublisher getArtifactPublisher() {
        return artifactPublisher;
    }

    public void setArtifactPublisher(ArtifactPublisher artifactPublisher) {
        this.artifactPublisher = artifactPublisher;
    }

    public ArtifactRetriever getArtifactRetriever() {
        return artifactRetriever;
    }

    public void setArtifactRetriever(ArtifactRetriever artifactRetriever) {
        this.artifactRetriever = artifactRetriever;
    }
}
