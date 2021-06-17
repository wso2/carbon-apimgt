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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.recommendationmgt.RecommendationEnvironment;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.HashSet;
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
        try {
            environments = APIUtil.getEnvironments();
        } catch (APIManagementException e) {
            // TODO (renuka) do we want to set "environments = APIUtil.getReadOnlyEnvironments();"
            log.error("Error occurred when reading configured gateway environments", e);
        }
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
                api.getId().getId(), api.getUuid(), publishedGateways, apiIdentifier.getName(), apiIdentifier.getVersion(),
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
                api.getProductId(),api.getUuid(), publishedGateways, apiIdentifier.getName(), apiIdentifier.getVersion(),
                PRODUCT_PREFIX, api.getType(),api.getContext());
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " + api + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }
    }

    private void sendUnDeploymentEvent(API api, String tenantDomain, Set<String> removedGateways) {
        APIIdentifier apiIdentifier = api.getId();

        DeployAPIInGatewayEvent
                deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(), tenantDomain,
                api.getId().getId(),api.getUuid(), removedGateways,apiIdentifier.getName(),apiIdentifier.getVersion(),
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
                apiProduct.getProductId(),apiProduct.getUuid(), removedGateways, apiProductIdentifier.getName(),
                apiProductIdentifier.getVersion(), PRODUCT_PREFIX, APIConstants.API_PRODUCT, apiProduct.getContext(),
                apiEvents);
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
    }

    public void deployToGateway(API api, String tenantDomain, Set<String> gatewaysToPublish) {

        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendDeploymentEvent(api, tenantDomain, gatewaysToPublish);
    }

    public void deployToGateway(APIProduct api, String tenantDomain, Set<String> gatewaysToPublish) {
        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendDeploymentEvent(api, tenantDomain, gatewaysToPublish);
    }

    public void unDeployFromGateway(API api, String tenantDomain, Set<String> gatewaysToRemove) {

        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendUnDeploymentEvent(api, tenantDomain, gatewaysToRemove);
    }

    public void unDeployFromGateway(APIProduct apiProduct, String tenantDomain, Set<API> associatedAPIs,
                                    Set<String> gatewaysToRemove) throws APIManagementException {
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
        sendUnDeploymentEvent(apiProduct, tenantDomain, gatewaysToRemove, associatedAPIs);

    }

    private Set<APIEvent> transformAPIToAPIEvent(Set<API> apiSet) {

        Set<APIEvent> apiEvents = new HashSet<>();
        for (API api : apiSet) {
            APIIdentifier id = api.getId();
            APIEvent apiEvent = new APIEvent(id.getUUID(), id.getApiName(), id.getVersion(), id.getProviderName(),
                    api.getType(), api.getStatus());
            apiEvents.add(apiEvent);
        }
        return apiEvents;
    }
}
