/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FederatedApiKeyConnector;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.FederatedApiKeyContext;
import org.wso2.carbon.apimgt.api.model.FederatedApiKeyCreationResult;
import org.wso2.carbon.apimgt.api.model.GatewayTierMapping;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiKeyMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.federated.gateway.FederatedApiKeyConnectorFactory;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.events.APIKeyEvent;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Notifier (event handler) for federated API key operations.
 * Processes async events to push API key changes to external gateways.
 */
public class FederatedApiKeyNotifier implements Notifier {

    private static final Log log = LogFactory.getLog(FederatedApiKeyNotifier.class);
    private static final String FEDERATED_API_KEY_REMOTE_ID = "federated.remoteApiKeyId";
    private static final String FEDERATED_API_KEY_VALUE = "federated.apiKeyValue";

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (!(event instanceof APIKeyEvent)) {
            return true;
        }

        APIKeyEvent fedEvent = (APIKeyEvent) event;
        if (log.isDebugEnabled()) {
            log.debug("Processing federated API key event: " + fedEvent);
        }

        try {
            switch (fedEvent.getType()) {
                case "API_KEY_CREATE":
                    handleCreate(fedEvent);
                    break;
                case "API_KEY_DELETE":
                    handleRevoke(fedEvent);
                    break;
                case "API_KEY_ASSOCIATION_CREATE":
                    handleApplyRateLimitPolicy(fedEvent);
                    break;
                case "API_KEY_ASSOCIATION_DELETE":
                    handleRemoveRateLimitPolicy(fedEvent);
                    break;
                default:
                    log.warn("Unknown federated API key event type: " + fedEvent.getType());
            }
            return true;
        } catch (APIManagementException e) {
            log.error("Failed to process federated API key event: " + fedEvent, e);
            throw new NotifierException("Failed to process federated API key event", e);
        }
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.FEDERATED_API_KEY.name();
    }

    private void handleCreate(APIKeyEvent event) throws APIManagementException {
        APIKeyInfo keyInfo = getApiKeyMgtDAO().getAPIKey(event.getUuid(), event.getUser());
        String apiUuid = resolveApiUuid(event, keyInfo);
        String organization = resolveOrganization(apiUuid);
        String environmentId = resolveEnvironmentId(apiUuid);
        String apiReferenceArtifact = resolveApiReferenceArtifact(apiUuid, environmentId);
        FederatedApiKeyConnector connector = resolveConnector(organization, environmentId);
        String apiKeyValue = getEventProperties(event).get(FEDERATED_API_KEY_VALUE);
        if (StringUtils.isBlank(apiKeyValue)) {
            throw new APIManagementException("Federated API key create event is missing the generated key value");
        }

        FederatedApiKeyContext context = FederatedApiKeyContext.builder()
                .apiUuid(apiUuid)
                .apiName(null)
                .apiReferenceArtifact(apiReferenceArtifact)
                .apiKeyUuid(event.getUuid())
                .apiKeyName(event.getName())
                .apiKeyValue(apiKeyValue)
                .remoteApiKeyId(null)
                .authzUser(resolveAuthUser(event, keyInfo))
                .applicationUuid(resolveApplicationUuid(event, keyInfo))
                .organizationId(organization)
                .environmentId(environmentId)
                .validityPeriod(event.getValidityPeriod())
                .permittedIP(event.getPermittedIP())
                .permittedReferer(event.getPermittedReferer())
                .build();

        FederatedApiKeyCreationResult result = connector.createApiKey(context);
        if (result == null || StringUtils.isBlank(result.getRemoteCredentialId())) {
            throw new APIManagementException("Federated API key creation did not return a remote credential ID");
        }

        Map<String, String> props = new HashMap<>();
        props.put(FEDERATED_API_KEY_REMOTE_ID, result.getRemoteCredentialId());
        if (StringUtils.isNotBlank(event.getPermittedIP())) {
            props.put(APIConstants.JwtTokenConstants.PERMITTED_IP, event.getPermittedIP());
        }
        if (StringUtils.isNotBlank(event.getPermittedReferer())) {
            props.put(APIConstants.JwtTokenConstants.PERMITTED_REFERER, event.getPermittedReferer());
        }

        getApiKeyMgtDAO().updateApiKeyGatewaySync(event.getUuid(), props);
        log.info("Successfully created federated API key on gateway. KeyUuid: " + event.getUuid()
                + ", RemoteId: " + result.getRemoteCredentialId());
    }

    private void handleRevoke(APIKeyEvent event) throws APIManagementException {
        String apiUuid = resolveApiUuid(event, null);
        String organization = resolveOrganization(apiUuid);
        String environmentId = resolveEnvironmentId(apiUuid);
        String apiReferenceArtifact = resolveApiReferenceArtifact(apiUuid, environmentId);
        FederatedApiKeyConnector connector = resolveConnector(organization, environmentId);
        String remoteApiKeyId = getEventProperties(event).get(FEDERATED_API_KEY_REMOTE_ID);
        if (StringUtils.isBlank(remoteApiKeyId)) {
            log.warn("Remote API key ID is missing for federated API key UUID: " + event.getUuid()
                    + ". Skipping remote revocation.");
            return;
        }

        FederatedApiKeyContext context = FederatedApiKeyContext.builder()
                .apiUuid(apiUuid)
                .apiName(null)
                .apiReferenceArtifact(apiReferenceArtifact)
                .apiKeyUuid(event.getUuid())
                .apiKeyName(event.getName())
                .apiKeyValue(null)
                .remoteApiKeyId(remoteApiKeyId)
                .authzUser(event.getUser())
                .applicationUuid(event.getApplicationUUId())
                .organizationId(organization)
                .environmentId(environmentId)
                .build();

        connector.revokeApiKey(context);
        log.info("Successfully revoked federated API key on gateway. KeyUuid: " + event.getUuid()
                + ", RemoteId: " + remoteApiKeyId);
    }

    private void handleApplyRateLimitPolicy(APIKeyEvent event) throws APIManagementException {
        APIKeyInfo keyInfo = getApiKeyMgtDAO().getAPIKey(event.getUuid(), event.getUser());
        String apiUuid = resolveApiUuid(event, keyInfo);
        String applicationUuid = resolveApplicationUuid(event, keyInfo);
        String organization = resolveOrganization(apiUuid);
        String environmentId = resolveEnvironmentId(apiUuid);
        String apiReferenceArtifact = resolveApiReferenceArtifact(apiUuid, environmentId);
        FederatedApiKeyConnector connector = resolveConnector(organization, environmentId);
        String remoteApiKeyId = resolveRemoteApiKeyId(keyInfo);
        if (StringUtils.isBlank(remoteApiKeyId)) {
            throw new APIManagementException("Remote API key ID is required for applying rate limit policy");
        }

        String remotePolicyId = resolveRemotePolicyId(organization, environmentId,
                resolveSubscriptionTierName(applicationUuid, apiUuid), connector);

        FederatedApiKeyContext context = FederatedApiKeyContext.builder()
                .apiUuid(apiUuid)
                .apiName(null)
                .apiReferenceArtifact(apiReferenceArtifact)
                .apiKeyUuid(event.getUuid())
                .apiKeyName(event.getName())
                .apiKeyValue(null)
                .remoteApiKeyId(remoteApiKeyId)
                .authzUser(resolveAuthUser(event, keyInfo))
                .applicationUuid(applicationUuid)
                .organizationId(organization)
                .environmentId(environmentId)
                .build();

        connector.applyRateLimitPolicy(context, remotePolicyId);
        log.info("Successfully applied rate limit policy to federated API key. KeyUuid: " + event.getUuid()
                + ", RemotePolicyId: " + remotePolicyId);
    }

    private void handleRemoveRateLimitPolicy(APIKeyEvent event) throws APIManagementException {
        APIKeyInfo keyInfo = getApiKeyMgtDAO().getAPIKey(event.getUuid(), event.getUser());
        String apiUuid = resolveApiUuid(event, keyInfo);
        String applicationUuid = resolveApplicationUuid(event, keyInfo);
        String organization = resolveOrganization(apiUuid);
        String environmentId = resolveEnvironmentId(apiUuid);
        String apiReferenceArtifact = resolveApiReferenceArtifact(apiUuid, environmentId);
        FederatedApiKeyConnector connector = resolveConnector(organization, environmentId);
        String remoteApiKeyId = resolveRemoteApiKeyId(keyInfo);
        if (StringUtils.isBlank(remoteApiKeyId)) {
            log.warn("Remote API key ID is missing for federated API key UUID: " + event.getUuid()
                    + ". Skipping remote policy removal.");
            return;
        }

        FederatedApiKeyContext context = FederatedApiKeyContext.builder()
                .apiUuid(apiUuid)
                .apiName(null)
                .apiReferenceArtifact(apiReferenceArtifact)
                .apiKeyUuid(event.getUuid())
                .apiKeyName(event.getName())
                .apiKeyValue(null)
                .remoteApiKeyId(remoteApiKeyId)
                .authzUser(resolveAuthUser(event, keyInfo))
                .applicationUuid(applicationUuid)
                .organizationId(organization)
                .environmentId(environmentId)
                .build();

        connector.removeRateLimitPolicy(context);
        log.info("Successfully removed rate limit policy from federated API key. KeyUuid: " + event.getUuid());
    }

    private FederatedApiKeyConnector resolveConnector(String organization, String environmentId)
            throws APIManagementException {
        Environment environment = getApiMgtDAO().getEnvironment(organization, environmentId);
        if (environment == null) {
            throw new APIManagementException("Gateway environment not found: " + environmentId);
        }
        return FederatedApiKeyConnectorFactory.getApiKeyConnector(environment, organization);
    }

    private String resolveRemotePolicyId(String organization, String envId, String localTierName,
                                         FederatedApiKeyConnector connector) throws APIManagementException {
        Environment environment = getApiMgtDAO().getEnvironment(organization, envId);
        if (environment == null) {
            throw new APIManagementException("Gateway environment not found: " + envId);
        }
        if (StringUtils.isBlank(localTierName)) {
            throw new APIManagementException("Local application tier is required for external tier mapping");
        }
        List<GatewayTierMapping> tierMappings = environment.getTierMappings();
        if (tierMappings == null || tierMappings.isEmpty()) {
            throw new APIManagementException("No external tier mappings configured for environment: " + envId);
        }
        for (GatewayTierMapping tierMapping : tierMappings) {
            if (tierMapping != null && StringUtils.equalsIgnoreCase(localTierName, tierMapping.getLocalTierName())) {
                if (StringUtils.isBlank(tierMapping.getRemotePlanReference())) {
                    throw new APIManagementException("External tier is not configured for local tier: " + localTierName);
                }
                String remotePolicyId = connector.resolveRemotePolicyId(tierMapping.getRemotePlanReference());
                if (StringUtils.isBlank(remotePolicyId)) {
                    throw new APIManagementException("External tier is not configured for local tier: " + localTierName);
                }
                return remotePolicyId;
            }
        }
        throw new APIManagementException("No external tier mapping found for local tier: " + localTierName);
    }

    private String resolveOrganization(String apiUuid) throws APIManagementException {
        if (StringUtils.isBlank(apiUuid)) {
            throw new APIManagementException("API UUID is required for federated API key event processing");
        }
        String organization = getApiMgtDAO().getOrganizationByAPIUUID(apiUuid);
        if (StringUtils.isBlank(organization)) {
            throw new APIManagementException("Unable to resolve organization for federated API UUID: " + apiUuid);
        }
        return organization;
    }

    private String resolveEnvironmentId(String apiUuid) throws APIManagementException {
        String environmentId = getApiMgtDAO().getGatewayEnvironmentIdForExternalApi(apiUuid);
        if (StringUtils.isBlank(environmentId)) {
            throw new APIManagementException("No external gateway environment mapping found for federated API: "
                    + apiUuid);
        }
        return environmentId;
    }

    private String resolveApiReferenceArtifact(String apiUuid, String environmentId) throws APIManagementException {
        return getApiMgtDAO().getApiExternalApiMappingReference(apiUuid, environmentId);
    }

    private String resolveApiUuid(APIKeyEvent event, APIKeyInfo keyInfo) throws APIManagementException {
        String apiUuid = event.getApiUUId();
        if (StringUtils.isBlank(apiUuid) && keyInfo != null) {
            apiUuid = keyInfo.getApiUUId();
        }
        if (StringUtils.isBlank(apiUuid)) {
            throw new APIManagementException("API UUID is required for federated API key event processing");
        }
        return apiUuid;
    }

    private String resolveApplicationUuid(APIKeyEvent event, APIKeyInfo keyInfo) {
        if (StringUtils.isNotBlank(event.getApplicationUUId())) {
            return event.getApplicationUUId();
        }
        return keyInfo != null ? keyInfo.getApplicationId() : null;
    }

    private String resolveAuthUser(APIKeyEvent event, APIKeyInfo keyInfo) {
        if (StringUtils.isNotBlank(event.getUser())) {
            return event.getUser();
        }
        return keyInfo != null ? keyInfo.getAuthUser() : null;
    }

    private String resolveRemoteApiKeyId(APIKeyInfo keyInfo) {
        if (keyInfo == null || keyInfo.getProperties() == null) {
            return null;
        }
        return keyInfo.getProperties().get(FEDERATED_API_KEY_REMOTE_ID);
    }

    private String resolveSubscriptionTierName(String applicationUuid, String apiUuid) throws APIManagementException {
        if (StringUtils.isBlank(applicationUuid)) {
            throw new APIManagementException("Application UUID is required for federated API key association");
        }
        Application application = getApiMgtDAO().getApplicationByUUID(applicationUuid);
        if (application == null) {
            throw new APIManagementException("Application not found for UUID: " + applicationUuid);
        }
        Set<SubscribedAPI> subscribedAPIs = getApiMgtDAO().getSubscribedAPIsByApplication(application);
        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            if (!StringUtils.equals(apiUuid, subscribedAPI.getAPIUUId())) {
                continue;
            }
            if (!APIConstants.SubscriptionStatus.UNBLOCKED.equals(subscribedAPI.getSubStatus())) {
                throw new APIManagementException("API key association requires an active subscription for API: "
                        + apiUuid);
            }
            if (subscribedAPI.getTier() == null || StringUtils.isBlank(subscribedAPI.getTier().getName())) {
                throw new APIManagementException("Subscription tier is required for federated external tier mapping");
            }
            return subscribedAPI.getTier().getName();
        }
        throw new APIManagementException("No active subscription found for application " + applicationUuid
                + " and API " + apiUuid);
    }

    private Map<String, String> getEventProperties(APIKeyEvent event) {
        return event.getProperties() == null ? Collections.emptyMap() : (Map<String, String>) event.getProperties();
    }

    private ApiKeyMgtDAO getApiKeyMgtDAO() {
        return ApiKeyMgtDAO.getInstance();
    }

    private ApiMgtDAO getApiMgtDAO() {
        return ApiMgtDAO.getInstance();
    }
}
