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
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.FederatedApiKeyContext;
import org.wso2.carbon.apimgt.api.model.FederatedApiKeyCreationResult;
import org.wso2.carbon.apimgt.api.model.GatewayTierMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiKeyMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.federated.gateway.FederatedApiKeyConnectorFactory;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.events.FederatedApiKeyEvent;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Notifier (event handler) for federated API key operations.
 * Processes async events to push API key changes to external gateways.
 */
public class FederatedApiKeyNotifier implements Notifier {

    private static final Log log = LogFactory.getLog(FederatedApiKeyNotifier.class);
    private static final String FEDERATED_API_KEY_REMOTE_ID = "federated.remoteApiKeyId";

    private final ApiKeyMgtDAO apiKeyMgtDAO;
    private final ApiMgtDAO apiMgtDAO;

    public FederatedApiKeyNotifier() {
        this.apiKeyMgtDAO = ApiKeyMgtDAO.getInstance();
        this.apiMgtDAO = ApiMgtDAO.getInstance();
    }

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (!(event instanceof FederatedApiKeyEvent)) {
            return true;
        }

        FederatedApiKeyEvent fedEvent = (FederatedApiKeyEvent) event;
        if (log.isDebugEnabled()) {
            log.debug("Processing federated API key event: " + fedEvent);
        }

        try {
            switch (fedEvent.getFederatedEventType()) {
                case CREATE:
                    handleCreate(fedEvent);
                    break;
                case REVOKE:
                    handleRevoke(fedEvent);
                    break;
                case APPLY_RATE_LIMIT_POLICY:
                    handleApplyRateLimitPolicy(fedEvent);
                    break;
                case REMOVE_RATE_LIMIT_POLICY:
                    handleRemoveRateLimitPolicy(fedEvent);
                    break;
                default:
                    log.warn("Unknown federated API key event type: " + fedEvent.getFederatedEventType());
            }
            return true;
        } catch (APIManagementException e) {
            log.error("Failed to process federated API key event: " + fedEvent, e);
            updateStatusToFailed(fedEvent);
            throw new NotifierException("Failed to process federated API key event", e);
        }
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.FEDERATED_API_KEY.name();
    }

    private void handleCreate(FederatedApiKeyEvent event) throws APIManagementException {
        FederatedApiKeyConnector connector = resolveConnector(event);
        if (connector == null) {
            log.warn("No federated API key connector found for environment: " + event.getEnvironmentId());
            return;
        }

        FederatedApiKeyContext context = FederatedApiKeyContext.builder()
                .apiUuid(event.getApiUuid())
                .apiName(event.getApiName())
                .apiReferenceArtifact(event.getApiReferenceArtifact())
                .apiKeyUuid(event.getKeyUuid())
                .apiKeyName(event.getKeyName())
                .apiKeyValue(event.getApiKeyValue())
                .remoteApiKeyId(null)
                .authzUser(event.getAuthzUser())
                .applicationUuid(event.getApplicationUuid())
                .organizationId(event.getOrganization())
                .environmentId(event.getEnvironmentId())
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

        apiKeyMgtDAO.updateApiKeyGatewaySync(event.getKeyUuid(), props, APIConstants.ApiKeyStatus.ACTIVE);
        log.info("Successfully created federated API key on gateway. KeyUuid: " + event.getKeyUuid()
                + ", RemoteId: " + result.getRemoteCredentialId());
    }

    private void handleRevoke(FederatedApiKeyEvent event) throws APIManagementException {
        FederatedApiKeyConnector connector = resolveConnector(event);
        if (connector == null) {
            log.warn("No federated API key connector found for environment: " + event.getEnvironmentId());
            return;
        }

        String remoteApiKeyId = event.getRemoteApiKeyId();
        if (StringUtils.isBlank(remoteApiKeyId)) {
            log.warn("Remote API key ID is missing for federated API key UUID: " + event.getKeyUuid()
                    + ". Skipping remote revocation.");
            return;
        }

        FederatedApiKeyContext context = FederatedApiKeyContext.builder()
                .apiUuid(event.getApiUuid())
                .apiName(event.getApiName())
                .apiReferenceArtifact(event.getApiReferenceArtifact())
                .apiKeyUuid(event.getKeyUuid())
                .apiKeyName(event.getKeyName())
                .apiKeyValue(null)
                .remoteApiKeyId(remoteApiKeyId)
                .authzUser(event.getAuthzUser())
                .applicationUuid(event.getApplicationUuid())
                .organizationId(event.getOrganization())
                .environmentId(event.getEnvironmentId())
                .build();

        connector.revokeApiKey(context);
        log.info("Successfully revoked federated API key on gateway. KeyUuid: " + event.getKeyUuid()
                + ", RemoteId: " + remoteApiKeyId);
    }

    private void handleApplyRateLimitPolicy(FederatedApiKeyEvent event) throws APIManagementException {
        FederatedApiKeyConnector connector = resolveConnector(event);
        if (connector == null) {
            log.warn("No federated API key connector found for environment: " + event.getEnvironmentId());
            return;
        }

        String remoteApiKeyId = event.getRemoteApiKeyId();
        if (StringUtils.isBlank(remoteApiKeyId)) {
            throw new APIManagementException("Remote API key ID is required for applying rate limit policy");
        }

        String remotePolicyId = resolveRemotePolicyId(event.getOrganization(), event.getEnvironmentId(),
                event.getLocalTierName(), connector);

        FederatedApiKeyContext context = FederatedApiKeyContext.builder()
                .apiUuid(event.getApiUuid())
                .apiName(event.getApiName())
                .apiReferenceArtifact(event.getApiReferenceArtifact())
                .apiKeyUuid(event.getKeyUuid())
                .apiKeyName(event.getKeyName())
                .apiKeyValue(null)
                .remoteApiKeyId(remoteApiKeyId)
                .authzUser(event.getAuthzUser())
                .applicationUuid(event.getApplicationUuid())
                .organizationId(event.getOrganization())
                .environmentId(event.getEnvironmentId())
                .build();

        connector.applyRateLimitPolicy(context, remotePolicyId);
        log.info("Successfully applied rate limit policy to federated API key. KeyUuid: " + event.getKeyUuid()
                + ", RemotePolicyId: " + remotePolicyId);
    }

    private void handleRemoveRateLimitPolicy(FederatedApiKeyEvent event) throws APIManagementException {
        FederatedApiKeyConnector connector = resolveConnector(event);
        if (connector == null) {
            log.warn("No federated API key connector found for environment: " + event.getEnvironmentId());
            return;
        }

        String remoteApiKeyId = event.getRemoteApiKeyId();
        if (StringUtils.isBlank(remoteApiKeyId)) {
            log.warn("Remote API key ID is missing for federated API key UUID: " + event.getKeyUuid()
                    + ". Skipping remote policy removal.");
            return;
        }

        FederatedApiKeyContext context = FederatedApiKeyContext.builder()
                .apiUuid(event.getApiUuid())
                .apiName(event.getApiName())
                .apiReferenceArtifact(event.getApiReferenceArtifact())
                .apiKeyUuid(event.getKeyUuid())
                .apiKeyName(event.getKeyName())
                .apiKeyValue(null)
                .remoteApiKeyId(remoteApiKeyId)
                .authzUser(event.getAuthzUser())
                .applicationUuid(event.getApplicationUuid())
                .organizationId(event.getOrganization())
                .environmentId(event.getEnvironmentId())
                .build();

        connector.removeRateLimitPolicy(context);
        log.info("Successfully removed rate limit policy from federated API key. KeyUuid: " + event.getKeyUuid());
    }

    private FederatedApiKeyConnector resolveConnector(FederatedApiKeyEvent event) throws APIManagementException {
        Environment environment = apiMgtDAO.getEnvironment(event.getOrganization(), event.getEnvironmentId());
        if (environment == null) {
            throw new APIManagementException("Gateway environment not found: " + event.getEnvironmentId());
        }
        return FederatedApiKeyConnectorFactory.getApiKeyConnector(environment, event.getOrganization());
    }

    private String resolveRemotePolicyId(String organization, String envId, String localTierName,
                                         FederatedApiKeyConnector connector) throws APIManagementException {
        Environment environment = apiMgtDAO.getEnvironment(organization, envId);
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

    private void updateStatusToFailed(FederatedApiKeyEvent event) {
        try {
            Map<String, String> props = new HashMap<>();
            if (StringUtils.isNotBlank(event.getPermittedIP())) {
                props.put(APIConstants.JwtTokenConstants.PERMITTED_IP, event.getPermittedIP());
            }
            if (StringUtils.isNotBlank(event.getPermittedReferer())) {
                props.put(APIConstants.JwtTokenConstants.PERMITTED_REFERER, event.getPermittedReferer());
            }
            if (StringUtils.isNotBlank(event.getRemoteApiKeyId())) {
                props.put(FEDERATED_API_KEY_REMOTE_ID, event.getRemoteApiKeyId());
            }
            apiKeyMgtDAO.updateApiKeyGatewaySync(event.getKeyUuid(), props,
                    APIConstants.ApiKeyStatus.GATEWAY_SYNC_FAILED);
        } catch (APIManagementException e) {
            log.error("Failed to update API key status to GATEWAY_SYNC_FAILED for key: " + event.getKeyUuid(), e);
        }
    }
}
