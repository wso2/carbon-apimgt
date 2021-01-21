/*
 * Copyright WSO2 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.gateway.dto.stub.APIData;
import org.wso2.carbon.apimgt.gateway.dto.stub.ResourceData;
import org.wso2.carbon.apimgt.impl.certificatemgt.exceptions.CertificateManagementException;
import org.wso2.carbon.apimgt.impl.dao.CertificateMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderDetailsExtractor;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommenderEventPublisher;
import org.wso2.carbon.apimgt.impl.utils.APIGatewayAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.LocalEntryAdminClient;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class APIGatewayManager {

    private static final Log log = LogFactory.getLog(APIGatewayManager.class);
    private boolean debugEnabled = log.isDebugEnabled();
    private static APIGatewayManager instance;

    private Map<String, Environment> environments;
    private RecommendationEnvironment recommendationEnvironment;
    private GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties;
    private ArtifactSaver artifactSaver;
    private boolean saveArtifactsToStorage = false;

    private static final String PRODUCT_PREFIX = "prod";

    private APIGatewayManager() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        environments = config.getApiGatewayEnvironments();
        this.recommendationEnvironment = config.getApiRecommendationEnvironment();
        this.gatewayArtifactSynchronizerProperties = config.getGatewayArtifactSynchronizerProperties();
        this.artifactSaver = ServiceReferenceHolder.getInstance().getArtifactSaver();
        if (artifactSaver != null && config.getGatewayArtifactSynchronizerProperties().isSaveArtifactsEnabled()){
            this.saveArtifactsToStorage = true;
        }
    }

    public synchronized static APIGatewayManager getInstance() {
        if (instance == null) {
            instance = new APIGatewayManager();
        }
        return instance;
    }

    private void sendDeploymentEvent(API api, String tenantDomain, Set<String> publishedGateways) {

        APIIdentifier apiIdentifier = api.getId();
        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), tenantDomain,
                api.getUUID(), publishedGateways,apiIdentifier.getName(),apiIdentifier.getVersion(),
                apiIdentifier.getProviderName(),api.getType());
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " + api + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }
    }

    private void sendDeploymentEvent(APIProduct api, String tenantDomain, Set<String> publishedGateways) {

        APIProductIdentifier apiIdentifier = api.getId();
        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), tenantDomain,
                api.getUuid(), publishedGateways, apiIdentifier.getName(), apiIdentifier.getVersion(),
                PRODUCT_PREFIX, api.getType());
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " + api + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }
    }

    private GatewayContentDTO[] addGatewayContentToList(GatewayContentDTO gatewayContentDTO,
            GatewayContentDTO[] gatewayContents) {

        if (gatewayContents == null) {
            return new GatewayContentDTO[]{gatewayContentDTO};
        } else {
            Set<GatewayContentDTO> gatewayContentDTOList = new HashSet<>();
            Collections.addAll(gatewayContentDTOList, gatewayContents);
            gatewayContentDTOList.add(gatewayContentDTO);
            return gatewayContentDTOList.toArray(new GatewayContentDTO[gatewayContentDTOList.size()]);
        }
    }

    private String[] addStringToList(String key, String[] keys) {

        if (keys == null) {
            return new String[]{key};
        } else {
            Set<String> keyList = new HashSet<>();
            Collections.addAll(keyList, keys);
            keyList.add(key);
            return keyList.toArray(new String[keyList.size()]);
        }
    }

    /**
     * Returns the defined endpoint types of the in the publisher
     *
     * @param api API that the endpoint/s belong
     * @return ArrayList containing defined endpoint types
     */
    public ArrayList<String> getEndpointType(API api) {
        ArrayList<String> arrayList = new ArrayList<>();
        if (APIUtil.isProductionEndpointsExists(api.getEndpointConfig()) &&
                !APIUtil.isSandboxEndpointsExists(api.getEndpointConfig())) {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
        } else if (APIUtil.isSandboxEndpointsExists(api.getEndpointConfig()) &&
                !APIUtil.isProductionEndpointsExists(api.getEndpointConfig())) {
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        } else {
            arrayList.add(APIConstants.API_DATA_PRODUCTION_ENDPOINTS);
            arrayList.add(APIConstants.API_DATA_SANDBOX_ENDPOINTS);
        }
        return arrayList;
    }

    public Map<String, String> updateLocalEntry(APIProduct apiProduct, String tenantDomain) {

        Map<String, String> failedEnvironmentsMap = new HashMap<>();

        for (String environmentName : apiProduct.getEnvironments()) {

            Environment environment = environments.get(environmentName);
            try {
                LocalEntryAdminClient localEntryAdminClient = new LocalEntryAdminClient(environment, tenantDomain);

                String definition = apiProduct.getDefinition();
                localEntryAdminClient.deleteEntry(apiProduct.getUuid());
                localEntryAdminClient.addLocalEntry("<localEntry key=\"" + apiProduct.getUuid() + "\">" +
                        definition.replaceAll("&(?!amp;)", "&amp;").
                                replaceAll("<", "&lt;").replaceAll(">", "&gt;")
                        + "</localEntry>");
            } catch (AxisFault e) {
                failedEnvironmentsMap.put(environmentName, e.getMessage());
                log.error("Error occurred when publish to gateway " + environmentName, e);
            }
        }

        return failedEnvironmentsMap;
    }

    private void sendUnDeploymentEvent(API api, String tenantDomain, Set<String> removedGateways) {
        APIIdentifier apiIdentifier = api.getId();

        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                api.getUUID(), removedGateways,apiIdentifier.getName(),apiIdentifier.getVersion(),
                apiIdentifier.getProviderName(), api.getType());
        APIUtil.sendNotification(deployAPIInGatewayEvent,
                APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());

    }

    private void sendUnDeploymentEvent(APIProduct apiProduct, String tenantDomain, Set<String> removedGateways,
                                       Set<API> associatedAPIs) {

        APIProductIdentifier apiProductIdentifier = apiProduct.getId();
        Set<APIEvent> apiEvents = transformAPIToAPIEvent(associatedAPIs);
        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                apiProduct.getUuid(), removedGateways, apiProductIdentifier.getName(),
                apiProductIdentifier.getVersion(), PRODUCT_PREFIX, APIConstants.API_PRODUCT, apiEvents);
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
    }

    public Map<String, String> removeDefaultAPIFromGateway(API api, String tenantDomain) {

        Map<String, String> failedEnvironmentsMap = new HashMap<String, String>(0);
        LocalEntryAdminClient localEntryAdminClient;
        String localEntryUUId = api.getUUID();
        if (api.getEnvironments() != null) {
            for (String environmentName : api.getEnvironments()) {
                try {
                    Environment environment = environments.get(environmentName);
                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                    APIIdentifier id = api.getId();
                    if (client.getDefaultApi(tenantDomain, id) != null) {
                        if (debugEnabled) {
                            log.debug("Removing Default API " + api.getId().getApiName() + " From environment " +
                                    environment.getName());
                        }
                        client.deleteDefaultApi(tenantDomain, api.getId());
                    }
                    if (APIConstants.APITransportType.GRAPHQL.toString().equals(api.getType())) {
                        localEntryUUId = localEntryUUId + APIConstants.GRAPHQL_LOCAL_ENTRY_EXTENSION;
                    }
                    localEntryAdminClient = new LocalEntryAdminClient(environment, tenantDomain);
                    localEntryAdminClient.deleteEntry(localEntryUUId);
                } catch (AxisFault axisFault) {
                    /*
                didn't throw this exception to handle multiple gateway publishing
                if gateway is unreachable we collect that environments into map with issue and show on popup in ui
                therefore this didn't break the gateway unpublisihing if one gateway unreachable
                 */
                    log.error("Error occurred when removing default api from gateway " + environmentName, axisFault);
                    failedEnvironmentsMap.put(environmentName, axisFault.getMessage());
                }
            }
        }
        return failedEnvironmentsMap;
    }

    /**
     * Checks whether the API has been published.
     *
     * @param api          - The API to be checked.
     * @param tenantDomain - Tenant Domain of the publisher
     * @return True if the API is available in at least one Gateway. False if available in none.
     */
    public boolean isAPIPublished(API api, String tenantDomain) throws APIManagementException {

        if (gatewayArtifactSynchronizerProperties.isPublishDirectlyToGatewayEnabled()) {
            for (Environment environment : environments.values()) {
                try {
                    APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                    // If the API exists in at least one environment, consider as
                    // published and return true.
                    APIIdentifier id = api.getId();
                    if (client.getApi(tenantDomain, id) != null) {
                        return true;
                    }
                } catch (AxisFault axisFault) {
                    /*
                    didn't throw this exception to check api available in all the environments
                    therefore we didn't throw exception to avoid if gateway unreachable affect
                    */
                    if (!APIConstants.CREATED.equals(api.getStatus())) {
                        log.error("Error occurred when check api is published on gateway" + environment.getName(),
                                axisFault);
                    }
                }
            }
        } else {
            if (saveArtifactsToStorage) {
                try {
                    return artifactSaver.isAPIPublished(api.getUUID(), "CURRENT");
                } catch (ArtifactSynchronizerException e) {
                    throw new APIManagementException("Error while check existence of api in gateway", e);
                }
            }
        }
        return false;
    }

    /**
     * Get the endpoint Security type of the published API
     *
     * @param api          - The API to be checked.
     * @param tenantDomain - Tenant Domain of the publisher
     * @return Endpoint security type; Basic or Digest
     */
    public String getAPIEndpointSecurityType(API api, String tenantDomain) throws APIManagementException {

        for (Environment environment : environments.values()) {
            try {
                APIGatewayAdminClient client = new APIGatewayAdminClient(environment);
                APIIdentifier id = api.getId();
                APIData apiData = client.getApi(tenantDomain, id);
                if (apiData != null) {
                    ResourceData[] resourceData = apiData.getResources();
                    for (ResourceData resource : resourceData) {
                        if (resource != null && resource.getInSeqXml() != null) {
                            if(resource.getInSeqXml().contains("DigestAuthMediator")) {
                                return APIConstants.APIEndpointSecurityConstants.DIGEST_AUTH;
                            } else if(resource.getInSeqXml().contains("OAuthMediator")) {
                                return APIConstants.APIEndpointSecurityConstants.OAUTH;
                            }
                        }
                    }
                }
            } catch (AxisFault axisFault) {
                // didn't throw this exception to check api available in all the environments
                // therefore we didn't throw exception to avoid if gateway unreachable affect
                if (!APIConstants.CREATED.equals(api.getStatus())) {
                    log.error("Error occurred when check api endpoint security type on gateway"
                            + environment.getName(), axisFault);
                }
            }
        }
        return APIConstants.APIEndpointSecurityConstants.BASIC_AUTH;
    }

    /**
     * To update the database instance with the successfully removed client certificates from teh gateway.
     *
     * @param api          Relevant API related with teh removed certificate.
     * @param tenantDomain Tenant domain of the API.
     */
    private void updateRemovedClientCertificates(API api, String tenantDomain) {

        try {
            CertificateMgtDAO.getInstance().updateRemovedCertificatesFromGateways(api.getId(),
                    APIUtil.getTenantIdFromTenantDomain(tenantDomain));
            /* The flow does not need to be blocked, as this failure do not related with updating client certificates
             in gateway, rather updating in database. There is no harm in database having outdated certificate
             information.*/
        } catch (CertificateManagementException e) {
            log.error("Certificate Management Exception while trying to update the remove certificate from gateways "
                    + "for the api " + api.getId() + " for the tenant domain " + tenantDomain, e);
        }
    }

    public void deployToGateway(API api, String tenantDomain) throws APIManagementException {

        String apiId = api.getUUID();
        APIIdentifier apiIdentifier = api.getId();
        Set<String> environments = api.getEnvironments();
        List<Label> gatewayLabels = api.getGatewayLabels();
        Set<String> gateways = new HashSet<>();
        if (gatewayLabels != null) {
            for (Label gatewayLabel : gatewayLabels) {
                gateways.add(gatewayLabel.getName());
            }
        }
        if (environments != null) {
            Map<String, Environment> apiGatewayEnvironments =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration().getApiGatewayEnvironments();
            for (String environment : environments) {
                if (apiGatewayEnvironments != null) {
                    if (apiGatewayEnvironments.containsKey(environment)) {
                        gateways.add(environment);
                    }
                }
            }
        }
            try {
                setThreadLocalContext(tenantDomain);
                File artifact = ServiceReferenceHolder.getInstance().getImportExportService()
                        .exportAPI(apiId, null, null, null, true,
                                ExportFormat.JSON, false, true);
                artifactSaver.saveArtifact(apiId, apiIdentifier.getApiName(), apiIdentifier.getVersion(), "Current",
                        tenantDomain, artifact, gateways.toArray(new String[0]), api.getType());

            } catch (APIManagementException | APIImportExportException | ArtifactSynchronizerException e) {
                throw new APIManagementException("API " + api.getId() + "couldn't get deployed", e);
            }
            if (debugEnabled) {
                log.debug("Status of " + api.getId() + " has been updated to DB");
            }
            sendDeploymentEvent(api, tenantDomain, gateways);
    }

    private void setThreadLocalContext(String tenantDomain) throws APIManagementException {

        String tenantAdminUserName = APIUtil.getTenantAdminUserName(tenantDomain);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tenantDomain,true);
        carbonContext.setUsername(tenantAdminUserName);
    }

    public void deployToGateway(APIProduct api, String tenantDomain) throws APIManagementException {

        String apiId = api.getUuid();
        APIProductIdentifier apiIdentifier = api.getId();
        Set<String> environments = api.getEnvironments();
        List<Label> gatewayLabels = api.getGatewayLabels();
        Set<String> gateways = new HashSet<>();
        if (gatewayLabels != null) {
            for (Label gatewayLabel : gatewayLabels) {
                gateways.add(gatewayLabel.getName());
            }
        }
        if (environments != null) {
            Map<String, Environment> apiGatewayEnvironments =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                            .getAPIManagerConfiguration().getApiGatewayEnvironments();
            for (String environment : environments) {
                if (apiGatewayEnvironments != null) {
                    if (apiGatewayEnvironments.containsKey(environment)) {
                        gateways.add(environment);
                    }
                }
            }
        }
            try {
                File artifact = ServiceReferenceHolder.getInstance().getImportExportService()
                        .exportAPIProduct(apiId, null, null, null, ExportFormat.JSON, true, false, true);
                artifactSaver.saveArtifact(apiId, apiIdentifier.getName(), apiIdentifier.getVersion(), "Current",
                        tenantDomain, artifact, gateways.toArray(new String[0]), APIConstants.ApiTypes.PRODUCT_API.name());

            } catch (APIManagementException | APIImportExportException | ArtifactSynchronizerException e) {
                throw new APIManagementException("API " + api.getId() + "couldn't get deployed", e);
            }
            if (debugEnabled) {
                log.debug("Status of " + api.getId() + " has been updated to DB");
            }
            sendDeploymentEvent(api, tenantDomain, gateways);
    }

    public void unDeployFromGateway(API api, String tenantDomain) throws APIManagementException {

        updateRemovedClientCertificates(api, tenantDomain);
        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(api, tenantDomain);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }
        String apiId = api.getUUID();
        APIIdentifier apiIdentifier = api.getId();
        try {
            artifactSaver.removeArtifact(apiId, apiIdentifier.getName(), apiIdentifier.getVersion(), "Current",
                    tenantDomain);

        } catch (ArtifactSynchronizerException e) {
            throw new APIManagementException("API " + api.getId() + "couldn't get unDeployed", e);
        }
        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendUnDeploymentEvent(api, tenantDomain, Collections.emptySet());
    }

    public void unDeployFromGateway(APIProduct apiProduct, String tenantDomain,Set<API> associatedAPIs) throws APIManagementException {

        String apiProductUuid = apiProduct.getUuid();
        APIProductIdentifier apiProductIdentifier = apiProduct.getId();
        try {
            artifactSaver.removeArtifact(apiProductUuid, apiProductIdentifier.getName(),
                    apiProductIdentifier.getVersion(), "Current", tenantDomain);

        } catch (ArtifactSynchronizerException e) {
            throw new APIManagementException("API " + apiProductIdentifier + "couldn't get unDeployed", e);
        }
        if (debugEnabled) {
            log.debug("Status of " + apiProductIdentifier + " has been updated to DB");
        }
        sendUnDeploymentEvent(apiProduct, tenantDomain, Collections.emptySet(), associatedAPIs);

    }

    private Set<APIEvent> transformAPIToAPIEvent(Set<API> apiSet) {

        Set<APIEvent> apiEvents = new HashSet<>();
        for (API api : apiSet) {
            APIIdentifier id = api.getId();
            APIEvent apiEvent = new APIEvent(id.getApiName(), id.getVersion(), id.getProviderName(), api.getType(),
                    api.getStatus());
            apiEvents.add(apiEvent);
        }
        return apiEvents;
    }
}
