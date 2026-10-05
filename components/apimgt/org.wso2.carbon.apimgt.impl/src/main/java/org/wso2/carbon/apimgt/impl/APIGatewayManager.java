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
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.ArtifactSaver;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.GatewayPolicyEvent;
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

    private ArtifactSaver artifactSaver;

    private static final String PRODUCT_PREFIX = "prod";

    private APIGatewayManager() {

        this.artifactSaver = ServiceReferenceHolder.getInstance().getArtifactSaver();
    }

    public synchronized static APIGatewayManager getInstance() {
        if (instance == null) {
            instance = new APIGatewayManager();
        }
        return instance;
    }

    private void sendDeploymentEvent(API api, Set<String> publishedGateways,
                                     Set<String> platformGatewayIds, String revisionUuidForPlatform,
                                     Map<String, String> platformGatewayDeploymentIds) {
        log.info("Sending deployment event for API: " + api.getId().getName() + " version: " + api.getId().getVersion()
                + " to gateways");
        APIIdentifier apiIdentifier = api.getId();
        Set<String> gateways = publishedGateways != null ? publishedGateways : new HashSet<>();
        boolean useRevisionAsEventId = revisionUuidForPlatform != null && !revisionUuidForPlatform.isEmpty()
                && platformGatewayIds != null && !platformGatewayIds.isEmpty();
        String eventId = useRevisionAsEventId ? revisionUuidForPlatform : UUID.randomUUID().toString();
        if (log.isDebugEnabled()) {
            log.debug("Creating deployment event with eventId: " + eventId + " for API: " + apiIdentifier.getName()
                    + ", useRevisionAsEventId: " + useRevisionAsEventId);
        }
        DeployAPIInGatewayEvent deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(eventId,
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), api.getOrganization(),
                api.getId().getId(), api.getUuid(), gateways, apiIdentifier.getName(), apiIdentifier.getVersion(),
                apiIdentifier.getProviderName(), api.getType(), api.getContext());
        if (platformGatewayIds != null && !platformGatewayIds.isEmpty()) {
            deployAPIInGatewayEvent.setPlatformGatewayIds(platformGatewayIds);
        }
        deployAPIInGatewayEvent.setPlatformGatewayDeploymentIds(platformGatewayDeploymentIds);
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " + api + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }
    }

    private void sendDeploymentEvent(APIProduct api, Set<String> publishedGateways,
                                    Set<String> platformGatewayIds) {

        APIProductIdentifier apiIdentifier = api.getId();
        Set<String> gateways = publishedGateways != null ? publishedGateways : new HashSet<>();
        DeployAPIInGatewayEvent deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name(), api.getOrganization(),
                api.getProductId(), api.getUuid(), gateways, apiIdentifier.getName(), apiIdentifier.getVersion(),
                PRODUCT_PREFIX, api.getType(), api.getContext());
        if (platformGatewayIds != null && !platformGatewayIds.isEmpty()) {
            deployAPIInGatewayEvent.setPlatformGatewayIds(platformGatewayIds);
        }
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
        if (debugEnabled) {
            log.debug("Event sent to Gateway with eventID " + deployAPIInGatewayEvent.getEventId() + " for api "
                    + "with apiID " + api + " at " + deployAPIInGatewayEvent.getTimeStamp());
        }
    }

    private void sendUnDeploymentEvent(API api, Set<String> removedGateways,
                                       boolean onDeleteOrRetire, Set<String> platformGatewayIds,
                                       Map<String, String> platformGatewayDeploymentIds) {
        APIIdentifier apiIdentifier = api.getId();
        if (debugEnabled) {
            log.debug("Sending undeployment event for API: " + apiIdentifier.getName() + " version: " +
                    apiIdentifier.getVersion() + " from gateways: " + removedGateways + " onDeleteOrRetire: " +
                    onDeleteOrRetire);
        }
        Set<String> gateways = removedGateways != null ? removedGateways : new HashSet<>();
        String eventId = UUID.randomUUID().toString();
        DeployAPIInGatewayEvent deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(eventId,
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(),
                api.getOrganization(), api.getId().getId(), api.getUuid(), gateways, apiIdentifier.getName(),
                apiIdentifier.getVersion(), apiIdentifier.getProviderName(), api.getType(), api.getContext(),
                onDeleteOrRetire);
        if (platformGatewayIds != null && !platformGatewayIds.isEmpty()) {
            deployAPIInGatewayEvent.setPlatformGatewayIds(platformGatewayIds);
        }
        deployAPIInGatewayEvent.setPlatformGatewayDeploymentIds(platformGatewayDeploymentIds);
        APIUtil.sendNotification(deployAPIInGatewayEvent,
                APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
    }

    private void sendUnDeploymentEvent(APIProduct apiProduct, Set<String> removedGateways,
                                       Set<API> associatedAPIs, Set<String> platformGatewayIds) {

        APIProductIdentifier apiProductIdentifier = apiProduct.getId();
        Set<APIEvent> apiEvents = transformAPIToAPIEvent(associatedAPIs);
        Set<String> gateways = removedGateways != null ? removedGateways : new HashSet<>();
        DeployAPIInGatewayEvent deployAPIInGatewayEvent = new DeployAPIInGatewayEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name(),
                apiProduct.getOrganization(), apiProduct.getProductId(), apiProduct.getUuid(), gateways,
                apiProductIdentifier.getName(), apiProductIdentifier.getVersion(), PRODUCT_PREFIX,
                APIConstants.API_PRODUCT, apiProduct.getContext(), apiEvents);
        if (platformGatewayIds != null && !platformGatewayIds.isEmpty()) {
            deployAPIInGatewayEvent.setPlatformGatewayIds(platformGatewayIds);
        }
        APIUtil.sendNotification(deployAPIInGatewayEvent, APIConstants.NotifierType.GATEWAY_PUBLISHED_API.name());
    }

    /**
     * Send an event to gateway to deploy the policy mapping.
     *
     * @param mappingUuid       UUID of the policy mapping
     * @param tenantDomain      tenant domain of the policy mapping
     * @param publishedGateways set of gateways to which the policy mapping is published
     */
    private void sendGatewayPolicyDeploymentEvent(String mappingUuid, String tenantDomain, Set<String> publishedGateways) {

        GatewayPolicyEvent deployGatewayPolicyEvent = new GatewayPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.DEPLOY_POLICY_MAPPING_IN_GATEWAY.name(), tenantDomain, mappingUuid,
                publishedGateways);
        APIUtil.sendNotification(deployGatewayPolicyEvent, APIConstants.NotifierType.GATEWAY_POLICY.name());
        log.debug("Event sent to Gateway with eventID " + deployGatewayPolicyEvent.getEventId() + " for policy mapping "
                + "with UUID " + mappingUuid + " at " + deployGatewayPolicyEvent.getTimeStamp());
    }

    /**
     * Send an event to gateway to undeploy the policy mapping.
     *
     * @param mappingUuid         UUID of the policy mapping
     * @param tenantDomain        tenant domain of the policy mapping
     * @param unPublishedGateways set of gateways to which the policy mapping is unpublished
     */
    private void sendGatewayPolicyUndeploymentEvent(String mappingUuid, String tenantDomain, Set<String> unPublishedGateways) {

        GatewayPolicyEvent undeployGatewayPolicyEvent = new GatewayPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.REMOVE_POLICY_MAPPING_FROM_GATEWAY.name(), tenantDomain, mappingUuid,
                unPublishedGateways);
        APIUtil.sendNotification(undeployGatewayPolicyEvent, APIConstants.NotifierType.GATEWAY_POLICY.name());
            log.debug("Event sent to Gateway with eventID " + undeployGatewayPolicyEvent.getEventId()
                    + " for policy mapping " + "with UUID " + mappingUuid + " at " + undeployGatewayPolicyEvent.getTimeStamp());
    }

    public void deployToGateway(API api, String tenantDomain, Set<String> gatewaysToPublish) {
        deployToGateway(api, tenantDomain, gatewaysToPublish, null);
    }

    /**
     * Deploy API to Synapse or platform gateways. When platformGatewayIds is non-empty,
     * PlatformGatewayDeployNotifier will dispatch to the platform path.
     *
     * @param api                  API to deploy
     * @param tenantDomain         tenant domain
     * @param gatewaysToPublish    Synapse gateway environment names (labels)
     * @param platformGatewayIds   optional set of platform gateway IDs; null or empty to skip platform path
     */
    public void deployToGateway(API api, String tenantDomain, Set<String> gatewaysToPublish,
                               Set<String> platformGatewayIds) {
        deployToGateway(api, tenantDomain, gatewaysToPublish, platformGatewayIds, null);
    }

    /**
     * Deploy API to Synapse or platform gateways. When revisionUuid is non-null and platformGatewayIds
     * is non-empty, the deploy event uses revisionUuid as deploymentId so the platform gateway echoes it
     * in notify-api-deployment-status and deployment stats (deployedGatewayCount, failedGatewayCount) match.
     *
     * @param api                  API to deploy
     * @param tenantDomain         tenant domain
     * @param gatewaysToPublish    Synapse gateway environment names (labels)
     * @param platformGatewayIds   optional set of platform gateway IDs; null or empty to skip platform path
     * @param revisionUuid         optional revision UUID for platform path; when set, sent as deploymentId so notify stores correct REVISION_UUID
     */
    public void deployToGateway(API api, String tenantDomain, Set<String> gatewaysToPublish,
                                Set<String> platformGatewayIds, String revisionUuid) {
        deployToGateway(api, tenantDomain, gatewaysToPublish, platformGatewayIds, revisionUuid, null);
    }

    public void deployToGateway(API api, String tenantDomain, Set<String> gatewaysToPublish,
                                Set<String> platformGatewayIds, String revisionUuid,
                                Map<String, String> platformGatewayDeploymentIds) {
        log.info("Deploying API: " + api.getId().getName() + " version: " + api.getId().getVersion() + " to tenant: "
                + tenantDomain);
        if (platformGatewayIds != null && !platformGatewayIds.isEmpty() && log.isDebugEnabled()) {
            log.debug("Deploying to " + platformGatewayIds.size() + " platform gateways with revision: "
                    + revisionUuid);
        }
        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " has been updated to DB");
        }
        sendDeploymentEvent(api, gatewaysToPublish, platformGatewayIds, revisionUuid, platformGatewayDeploymentIds);
    }

    /**
     * Deploy API product to Synapse or platform gateways.
     *
     * @param api                  API product to deploy
     * @param gatewaysToPublish    Synapse gateway environment names (labels)
     * @param platformGatewayIds   optional set of platform gateway IDs; null or empty to skip platform path
     */
    public void deployToGateway(APIProduct api, String tenantId, Set<String> gatewaysToPublish,
                               Set<String> platformGatewayIds) {
        if (debugEnabled) {
            log.debug("Status of " + api.getId() + " for tenant " + tenantId + " has been updated to DB");
        }
        sendDeploymentEvent(api, gatewaysToPublish, platformGatewayIds);
    }

    /**
     * Undeploys the given API from the specified gateways. This method initiates an event to
     * remove the API deployment from Synapse or platform gateways.
     *
     * @param api                          The API to undeploy.
     * @param gatewaysToRemove             A set of gateway environment names (e.g., Synapse labels)
     *                                     from which the API should be removed.
     * @param onDeleteOrRetire             A boolean indicating whether the undeployment is triggered
     *                                     due to the API being deleted or retired.
     * @param platformGatewayIds           A set of platform gateway IDs to undeploy from. If null
     *                                     or empty, the platform gateway path is skipped.
     * @param platformGatewayDeploymentIds A map containing the deployment IDs for each platform gateway.
     *                                     The keys represent platform gateway IDs, and the values
     *                                     represent their corresponding deployment IDs.
     */
    public void unDeployFromGateway(API api, Set<String> gatewaysToRemove,
                                    boolean onDeleteOrRetire, Set<String> platformGatewayIds,
                                    Map<String, String> platformGatewayDeploymentIds) {
        if (debugEnabled) {
            log.debug("Undeploy API: " + api.getId().getName() + " version: " + api.getId().getVersion() +
                    " from gateways");
        }
        sendUnDeploymentEvent(api, gatewaysToRemove, onDeleteOrRetire, platformGatewayIds,
                platformGatewayDeploymentIds);
    }

    /**
     * Undeploy API Product from Synapse or platform gateways. Use this when the caller has already
     * resolved gateway names into Synapse labels and platform gateway IDs (e.g. via DeploymentModeResolver).
     *
     * @param apiProduct           API product to undeploy
     * @param tenantDomain         tenant domain
     * @param associatedAPIs       associated APIs
     * @param gatewaysToRemove     names to remove from DB/artifact (deployment names)
     * @param synapseLabelsToRemove Synapse gateway labels for the undeploy event (for Synapse path)
     * @param platformGatewayIds  platform gateway IDs for the undeploy event; null or empty to skip platform path
     */
    public void unDeployFromGateway(APIProduct apiProduct, String tenantDomain, Set<API> associatedAPIs,
                                    Set<String> gatewaysToRemove, Set<String> synapseLabelsToRemove,
                                    Set<String> platformGatewayIds) throws APIManagementException {
        log.info("Undeploying API Product: " + apiProduct.getId().getName() + " version: " + apiProduct.getId()
                .getVersion() + " from gateways");
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
                    .removePublishedGatewayLabels(apiProductUuid, APIConstants.API_PRODUCT_REVISION, gatewaysToRemove);
        } catch (ArtifactSynchronizerException e) {
            throw new APIManagementException("API " + apiProductIdentifier + "couldn't get unDeployed", e);
        }
        if (debugEnabled) {
            log.debug("Status of " + apiProductIdentifier + " has been updated to DB");
        }
        Set<String> labelsForEvent = synapseLabelsToRemove != null ? synapseLabelsToRemove : gatewaysToRemove;
        sendUnDeploymentEvent(apiProduct, labelsForEvent, associatedAPIs, platformGatewayIds);
    }

    public void deployPolicyToGateway(String mappingUuid, String tenantDomain, Set<String> gatewaysToPublish) {
        log.debug("Status of policy mapping: " + mappingUuid + " has been updated to DB");
        sendGatewayPolicyDeploymentEvent(mappingUuid, tenantDomain, gatewaysToPublish);
    }

    public void undeployPolicyFromGateway(String mappingUuid, String tenantDomain, Set<String> gatewaysToUnPublish) {
        log.debug("Status of policy mapping: " + mappingUuid + " has been updated to DB");
        sendGatewayPolicyUndeploymentEvent(mappingUuid, tenantDomain, gatewaysToUnPublish);
    }

    private Set<APIEvent> transformAPIToAPIEvent(Set<API> apiSet) {

        Set<APIEvent> apiEvents = new HashSet<>();
        for (API api : apiSet) {
            APIIdentifier id = api.getId();
            APIEvent apiEvent = new APIEvent(id.getUUID(), id.getApiName(), id.getVersion(), id.getProviderName(),
                    api.getType(), api.getStatus(), api.getApiSecurity());
            apiEvents.add(apiEvent);
        }
        return apiEvents;
    }

}
