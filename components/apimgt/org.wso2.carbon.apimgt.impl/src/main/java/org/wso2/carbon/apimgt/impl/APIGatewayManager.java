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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
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

import java.io.File;
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
    private ArtifactSaver artifactSaver;

    private static final String PRODUCT_PREFIX = "prod";

    private APIGatewayManager() {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        environments = config.getApiGatewayEnvironments();
        this.recommendationEnvironment = config.getApiRecommendationEnvironment();
        this.artifactSaver = ServiceReferenceHolder.getInstance().getArtifactSaver();
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
                apiIdentifier.getProviderName(),api.getType(),api.getContext());
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
                PRODUCT_PREFIX, api.getType(),api.getContext());
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " + api + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }
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
                apiIdentifier.getProviderName(), api.getType(),api.getContext());
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
                apiProductIdentifier.getVersion(), PRODUCT_PREFIX, APIConstants.API_PRODUCT, apiProduct.getContext(),
                apiEvents);
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

    public void deployToGateway(API api, String tenantDomain, Set<String> gatewaysToPublish) {

        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendDeploymentEvent(api, tenantDomain, gatewaysToPublish);
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
            GatewayArtifactsMgtDAO.getInstance().addGatewayAPIArtifactAndMetaData(apiId,apiIdentifier.getName(),
                    apiIdentifier.getVersion(),APIConstants.API_PRODUCT_REVISION,tenantDomain,
                    APIConstants.API_PRODUCT,artifact);
            if (artifactSaver != null){
                artifactSaver
                        .saveArtifact(apiId, apiIdentifier.getName(), apiIdentifier.getVersion(), APIConstants.API_PRODUCT_REVISION,
                                tenantDomain, artifact);
            }
            GatewayArtifactsMgtDAO.getInstance()
                    .addAndRemovePublishedGatewayLabels(apiId, APIConstants.API_PRODUCT_REVISION, gateways);

        } catch (APIManagementException | APIImportExportException | ArtifactSynchronizerException e) {
            throw new APIManagementException("API " + api.getId() + "couldn't get deployed", e);
        }
        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendDeploymentEvent(api, tenantDomain, gateways);
    }

    public void unDeployFromGateway(API api, String tenantDomain, Set<String> gatewaysToRemove) {

        // Extracting API details for the recommendation system
        if (recommendationEnvironment != null) {
            RecommenderEventPublisher extractor = new RecommenderDetailsExtractor(api, tenantDomain);
            Thread recommendationThread = new Thread(extractor);
            recommendationThread.start();
        }

        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendUnDeploymentEvent(api, tenantDomain, gatewaysToRemove);
    }

    public void unDeployFromGateway(APIProduct apiProduct, String tenantDomain, Set<API> associatedAPIs)
            throws APIManagementException {

        String apiProductUuid = apiProduct.getUuid();
        APIProductIdentifier apiProductIdentifier = apiProduct.getId();
        try {
            if (artifactSaver != null){
                artifactSaver.removeArtifact(apiProductUuid, apiProductIdentifier.getName(),
                        apiProductIdentifier.getVersion(), APIConstants.API_PRODUCT_REVISION, tenantDomain);
            }
            GatewayArtifactsMgtDAO.getInstance().deleteGatewayArtifact(apiProductUuid,
                    APIConstants.API_PRODUCT_REVISION);
            GatewayArtifactsMgtDAO.getInstance()
                    .removePublishedGatewayLabels(apiProductUuid, APIConstants.API_PRODUCT_REVISION);
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
