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
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiKeyMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.GatewayHolder;
import org.wso2.carbon.apimgt.impl.notifier.events.APIKeyAssociationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.APIKeyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.APIKeyRegenerationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Notifier (event handler) for federated API key operations.
 * Processes async events to push API key changes to external gateways.
 */
public class FederatedApiKeyNotifier implements Notifier {

    private static final Log log = LogFactory.getLog(FederatedApiKeyNotifier.class);

    public static final String PROP_API_KEY_NAME = "apiKeyName";
    public static final String PROP_API_UUID = "apiUuid";
    public static final String PROP_AUTHZ_USER = "authzUser";
    public static final String PROP_ORGANIZATION_ID = "organizationId";
    public static final String PROP_VALIDITY_PERIOD = "validityPeriod";
    public static final String PROP_PERMITTED_IP = "permittedIP";
    public static final String PROP_PERMITTED_REFERER = "permittedReferer";

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (log.isDebugEnabled()) {
            log.debug("Processing federated API key event: " + event);
        }

        try {
            if (event instanceof APIKeyEvent) {
                handleAPIKeyEvent((APIKeyEvent) event);
            } else if (event instanceof APIKeyAssociationEvent) {
                handleAPIKeyAssociationEvent((APIKeyAssociationEvent) event);
            } else if (event instanceof APIKeyRegenerationEvent) {
                handleRegenerate((APIKeyRegenerationEvent) event);
            }
            return true;
        } catch (APIManagementException e) {
            log.error("Failed to process federated API key event: " + event, e);
            throw new NotifierException("Failed to process federated API key event", e);
        }
    }

    private void handleAPIKeyEvent(APIKeyEvent apiKeyEvent) throws APIManagementException {

        APIConstants.EventType eventType = resolveEventType(apiKeyEvent.getType());
        if (eventType == null) {
            log.warn("Unknown federated API key event type: " + apiKeyEvent.getType());
            return;
        }

        switch (eventType) {
            case API_KEY_CREATE:
                handleCreate(apiKeyEvent);
                break;
            case API_KEY_DELETE:
                handleRevoke(apiKeyEvent);
                break;
            default:
                log.warn("Unsupported federated API key event type: " + eventType.name());
        }
    }

    private void handleAPIKeyAssociationEvent(APIKeyAssociationEvent associationEvent) throws APIManagementException {

        APIConstants.EventType eventType = resolveEventType(associationEvent.getType());
        if (eventType == null) {
            log.warn("Unknown federated API key association event type: " + associationEvent.getType());
            return;
        }

        switch (eventType) {
            case API_KEY_ASSOCIATION_CREATE:
                handleAssociateSubscriptionPolicy(associationEvent);
                break;
            case API_KEY_ASSOCIATION_DELETE:
                handleDissociateSubscriptionPolicy(associationEvent);
                break;
            default:
                log.warn("Unsupported federated API key association event type: " + eventType.name());
        }
    }

    private APIConstants.EventType resolveEventType(String type) {

        if (type == null) {
            return null;
        }

        try {
            return APIConstants.EventType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.API_KEY.name();
    }

    /**
     * Creates remote API-key credentials in each mapped external gateway and stores connector-owned reference artifacts.
     */
    private void handleCreate(APIKeyEvent event) throws APIManagementException {
        String apiUuid = event.getApiUUId();
        if (!isFederatedApi(apiUuid)) {
            return;
        }
        String organization = resolveOrganization(event);
        String apiKeyValue = event.getApiKey();
        if (StringUtils.isBlank(apiKeyValue)) {
            throw new APIManagementException("Federated API key create event is missing the generated key value");
        }

        Map<String, String> apiMappings = resolveApiExternalMappingsByEnvironmentName(apiUuid);
        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        Map<String, String> newlyCreatedReferenceArtifacts = new LinkedHashMap<>();
        Map<String, String> properties = buildProperties(apiUuid, event.getName(), event.getUser(), organization,
                event.getValidityPeriod(), event.getPermittedIP(), event.getPermittedReferer());

        try {
            for (Map.Entry<String, String> apiMapping : apiMappings.entrySet()) {
                Environment environment = environments.get(apiMapping.getKey());
                if (environment == null) {
                    throw new APIManagementException("Gateway environment not found: " + apiMapping.getKey());
                }
                FederatedApiKeyConnector connector = resolveConnector(organization, environment.getUuid());
                String referenceArtifact = connector.createApiKey(event.getUuid(), apiKeyValue,
                        apiMapping.getValue(), null, properties);
                if (StringUtils.isBlank(referenceArtifact)) {
                    throw new APIManagementException("Federated API key creation did not return a reference artifact "
                            + "for environment: " + environment.getUuid());
                }
                newlyCreatedReferenceArtifacts.put(environment.getUuid(), referenceArtifact);
            }
        } catch (APIManagementException e) {
            rollbackCreatedApiKeys(organization, newlyCreatedReferenceArtifacts);
            throw e;
        }

        try {
            for (Map.Entry<String, String> entry : newlyCreatedReferenceArtifacts.entrySet()) {
                getApiKeyMgtDAO().addApiKeyExternalApiKeyMapping(event.getUuid(), entry.getKey(), entry.getValue());
            }
        } catch (APIManagementException e) {
            getApiKeyMgtDAO().deleteApiKeyExternalApiKeyMappings(event.getUuid());
            rollbackCreatedApiKeys(organization, newlyCreatedReferenceArtifacts);
            throw e;
        }
        log.info("Successfully created federated API key on " + newlyCreatedReferenceArtifacts.size()
                + " gateway environment(s). KeyUuid: " + event.getUuid());
    }

    /**
     * Revokes previously created remote API-key credentials using stored connector-owned reference artifacts.
     */
    private void handleRevoke(APIKeyEvent event) throws APIManagementException {
        Map<String, String> apiKeyReferenceArtifacts = getApiKeyMgtDAO().getApiKeyExternalApiKeyMappings(event.getUuid());
        if (apiKeyReferenceArtifacts.isEmpty()) {
            log.warn("No per-environment remote API key reference artifacts found for federated API key UUID: "
                    + event.getUuid() + ". Skipping remote revocation.");
            return;
        }

        String organization = resolveOrganization(event);
        for (Map.Entry<String, String> entry : apiKeyReferenceArtifacts.entrySet()) {
            String apiKeyReferenceArtifact = entry.getValue();
            if (StringUtils.isBlank(apiKeyReferenceArtifact)) {
                throw new APIManagementException("Remote API key reference artifact is missing for federated API key "
                        + "UUID: " + event.getUuid() + " in environment: " + entry.getKey());
            }
            FederatedApiKeyConnector connector = resolveConnector(organization, entry.getKey());
            connector.revokeApiKey(apiKeyReferenceArtifact);
        }
        getApiKeyMgtDAO().deleteApiKeyExternalApiKeyMappings(event.getUuid());

        log.info("Successfully revoked federated API key on " + apiKeyReferenceArtifacts.size()
                + " gateway environment(s). KeyUuid: " + event.getUuid());
    }

    /**
     * Associates the mapped remote subscription policy/plan with an existing remote credential when a local key is
     * associated to an application.
     */
    private void handleAssociateSubscriptionPolicy(APIKeyAssociationEvent event) throws APIManagementException {
        if (StringUtils.isBlank(event.getApiKeyUUId())) {
            throw new APIManagementException("Federated API key association event is missing API key UUID context");
        }
        Map<String, String> apiKeyReferenceArtifacts =
                getApiKeyMgtDAO().getApiKeyExternalApiKeyMappings(event.getApiKeyUUId());
        if (apiKeyReferenceArtifacts.isEmpty()) {
            log.warn("No per-environment remote API key reference artifacts found for federated API key UUID: "
                    + event.getApiKeyUUId() + ". Skipping remote subscription policy association.");
            return;
        }

        String apiUuid = event.getApiUUId();
        String applicationUuid = resolveApplicationUuid(event);
        String organization = resolveOrganization(event);
        String localPolicyId = resolveSubscriptionPolicyId(applicationUuid, apiUuid, event.getTenantId());

        int successCount = 0;
        for (Map.Entry<String, String> entry : apiKeyReferenceArtifacts.entrySet()) {
            String environmentId = entry.getKey();
            String apiKeyReferenceArtifact = entry.getValue();
            if (StringUtils.isBlank(apiKeyReferenceArtifact)) {
                log.warn("Remote API key reference artifact is missing for federated API key UUID: "
                        + event.getApiKeyUUId() + " in environment: " + environmentId
                        + ". Skipping subscription policy association.");
                continue;
            }

            FederatedApiKeyConnector connector = resolveConnector(organization, environmentId);
            connector.associateSubscriptionPolicy(apiKeyReferenceArtifact, localPolicyId);
            successCount++;
        }

        log.info("Successfully associated subscription policy with federated API key across "
                + successCount + " gateway environment(s). KeyUuid: " + event.getApiKeyUUId());
    }

    /**
     * Dissociates the mapped remote subscription policy from an existing remote credential when a local association is
     * removed.
     */
    private void handleDissociateSubscriptionPolicy(APIKeyAssociationEvent event) throws APIManagementException {
        if (StringUtils.isBlank(event.getApiKeyUUId())) {
            throw new APIManagementException("Federated API key association event is missing API key UUID context");
        }
        Map<String, String> apiKeyReferenceArtifacts =
                getApiKeyMgtDAO().getApiKeyExternalApiKeyMappings(event.getApiKeyUUId());
        if (apiKeyReferenceArtifacts.isEmpty()) {
            log.warn("No per-environment remote API key reference artifacts found for federated API key UUID: "
                    + event.getApiKeyUUId() + ". Skipping remote subscription policy dissociation.");
            return;
        }

        String apiUuid = event.getApiUUId();
        String applicationUuid = resolveApplicationUuid(event);
        String organization = resolveOrganization(event);
        String localPolicyId = resolveSubscriptionPolicyId(applicationUuid, apiUuid, event.getTenantId());

        int successCount = 0;
        for (Map.Entry<String, String> entry : apiKeyReferenceArtifacts.entrySet()) {
            String environmentId = entry.getKey();
            String apiKeyReferenceArtifact = entry.getValue();
            if (StringUtils.isBlank(apiKeyReferenceArtifact)) {
                log.warn("Remote API key reference artifact is missing for federated API key UUID: "
                        + event.getApiKeyUUId() + " in environment: " + environmentId
                        + ". Skipping remote subscription policy dissociation.");
                continue;
            }

            FederatedApiKeyConnector connector = resolveConnector(organization, environmentId);
            connector.dissociateSubscriptionPolicy(apiKeyReferenceArtifact, localPolicyId);
            successCount++;
        }

        log.info("Successfully dissociated subscription policy from federated API key across "
                + successCount + " gateway environment(s). KeyUuid: " + event.getApiKeyUUId());
    }

    /**
     * Revokes remote credentials created during a partially failed create operation.
     */
    private void rollbackCreatedApiKeys(String organization, Map<String, String> apiKeyReferenceArtifacts) {
        if (apiKeyReferenceArtifacts.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : apiKeyReferenceArtifacts.entrySet()) {
            try {
                FederatedApiKeyConnector connector = resolveConnector(organization, entry.getKey());
                connector.revokeApiKey(entry.getValue());
            } catch (APIManagementException e) {
                log.error("Failed to rollback federated API key creation in environment: " + entry.getKey(), e);
            }
        }
    }

    /**
     * Replaces remote API-key credentials during local API-key regeneration and persists returned reference artifacts.
     */
    private void handleRegenerate(APIKeyRegenerationEvent event) throws APIManagementException {
        if (StringUtils.isBlank(event.getApiKey()) || StringUtils.isBlank(event.getOldApiKeyUuid())
                || StringUtils.isBlank(event.getNewApiKeyUuid())) {
            throw new APIManagementException("Federated API key regenerate event is missing key context");
        }
        Map<String, String> apiKeyReferenceArtifacts =
                getApiKeyMgtDAO().getApiKeyExternalApiKeyMappings(event.getOldApiKeyUuid());
        if (apiKeyReferenceArtifacts.isEmpty()) {
            log.warn("No per-environment remote API key reference artifacts found for federated API key UUID: "
                    + event.getOldApiKeyUuid() + ". Skipping remote replacement.");
            return;
        }
        String organization = resolveOrganization(event);
        Map<String, String> replacementReferenceArtifacts = new LinkedHashMap<>();
        Map<String, String> properties = buildProperties(null, null, null, organization, null, null, null);

        for (Map.Entry<String, String> entry : apiKeyReferenceArtifacts.entrySet()) {
            String environmentId = entry.getKey();
            String apiKeyReferenceArtifact = entry.getValue();
            if (StringUtils.isBlank(apiKeyReferenceArtifact)) {
                throw new APIManagementException("Remote API key reference artifact is missing for federated API key "
                        + "UUID: " + event.getOldApiKeyUuid() + " in environment: " + environmentId);
            }

            FederatedApiKeyConnector connector = resolveConnector(organization, environmentId);
            String referenceArtifact = connector.replaceApiKey(apiKeyReferenceArtifact, event.getApiKey(), properties);
            if (StringUtils.isBlank(referenceArtifact)) {
                throw new APIManagementException("Federated API key replacement did not return a reference artifact "
                        + "for environment: " + environmentId);
            }
            replacementReferenceArtifacts.put(environmentId, referenceArtifact);
        }

        try {
            for (Map.Entry<String, String> entry : replacementReferenceArtifacts.entrySet()) {
                getApiKeyMgtDAO().addApiKeyExternalApiKeyMapping(event.getNewApiKeyUuid(),
                        entry.getKey(), entry.getValue());
            }
            getApiKeyMgtDAO().deleteApiKeyExternalApiKeyMappings(event.getOldApiKeyUuid());
        } catch (APIManagementException e) {
            getApiKeyMgtDAO().deleteApiKeyExternalApiKeyMappings(event.getNewApiKeyUuid());
            throw e;
        }
        log.info("Successfully regenerated federated API key on " + replacementReferenceArtifacts.size()
                + " gateway environment(s). OldKeyUuid: " + event.getOldApiKeyUuid() + ", NewKeyUuid: "
                + event.getNewApiKeyUuid());
    }

    private Map<String, String> resolveApiExternalMappingsByEnvironmentName(String apiUuid)
            throws APIManagementException {
        return new LinkedHashMap<>(APIUtil.getApiExternalApiMappingReferenceByApiId(apiUuid));
    }

    /**
     * Creates the connector instance for the selected external gateway environment.
     */
    private FederatedApiKeyConnector resolveConnector(String organization, String environmentId)
            throws APIManagementException {
        FederatedApiKeyConnector connector =
                GatewayHolder.getTenantApiKeyConnectorInstance(organization, environmentId);
        // Current GatewayHolder implementation throws on lookup failures; kept this as a defensive guard.
        if (connector == null) {
            throw new APIManagementException("Federated API key connector resolution returned null for organization: "
                    + organization + ", environmentId: " + environmentId);
        }
        return connector;
    }

    /**
     * Maps environment UUIDs to API reference artifacts by resolving environment names from the name-keyed API mapping.
     */
    private Map<String, String> resolveEnvIdToApiRefMap(Map<String, Environment> environments,
                                                        Map<String, String> apiMappingsByName) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> mapping : apiMappingsByName.entrySet()) {
            Environment env = environments.get(mapping.getKey());
            if (env != null) {
                result.put(env.getUuid(), mapping.getValue());
            }
        }
        return result;
    }

    /**
     * Builds the properties map for connector operations.
     */
    private Map<String, String> buildProperties(String apiUuid, String apiKeyName, String authzUser,
                                                String organizationId, Long validityPeriod, String permittedIP,
                                                String permittedReferer) {
        Map<String, String> properties = new HashMap<>();
        if (StringUtils.isNotBlank(apiUuid)) {
            properties.put(PROP_API_UUID, apiUuid);
        }
        if (StringUtils.isNotBlank(apiKeyName)) {
            properties.put(PROP_API_KEY_NAME, apiKeyName);
        }
        if (StringUtils.isNotBlank(authzUser)) {
            properties.put(PROP_AUTHZ_USER, authzUser);
        }
        if (StringUtils.isNotBlank(organizationId)) {
            properties.put(PROP_ORGANIZATION_ID, organizationId);
        }
        if (validityPeriod != null) {
            properties.put(PROP_VALIDITY_PERIOD, String.valueOf(validityPeriod));
        }
        if (StringUtils.isNotBlank(permittedIP)) {
            properties.put(PROP_PERMITTED_IP, permittedIP);
        }
        if (StringUtils.isNotBlank(permittedReferer)) {
            properties.put(PROP_PERMITTED_REFERER, permittedReferer);
        }
        return properties;
    }

    private String resolveOrganization(Event event) throws APIManagementException {
        if (StringUtils.isBlank(event.getTenantDomain())) {
            throw new APIManagementException("Tenant domain is required for federated API key event processing");
        }
        return event.getTenantDomain();
    }

    private boolean isFederatedApi(String apiUuid) throws APIManagementException {
        return StringUtils.isNotBlank(apiUuid) && APIUtil.isFederatedGatewayApi(apiUuid);
    }

    /**
     * Resolves the application UUID for association events.
     */
    private String resolveApplicationUuid(APIKeyAssociationEvent event) throws APIManagementException {
        String applicationUuid = event.getApplicationUUId();
        if (StringUtils.isBlank(applicationUuid)) {
            throw new APIManagementException("Application UUID is required for federated API key association");
        }
        return applicationUuid;
    }

    /**
     * Resolves the active local subscription policy UUID for the application and API pair.
     */
    private String resolveSubscriptionPolicyId(String applicationUuid, String apiUuid, int tenantId)
            throws APIManagementException {
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
                throw new APIManagementException("Subscription tier is required for federated external plan assignment");
            }
            return resolveSubscriptionPolicyId(subscribedAPI.getTier().getName(), tenantId);
        }
        throw new APIManagementException("No active subscription found for application " + applicationUuid
                + " and API " + apiUuid);
    }

    private String resolveSubscriptionPolicyId(String policyName, int tenantId) throws APIManagementException {
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new APIManagementException("Tenant ID is required for federated API key association");
        }
        SubscriptionPolicy subscriptionPolicy = getApiMgtDAO().getSubscriptionPolicy(policyName, tenantId);
        if (subscriptionPolicy == null || StringUtils.isBlank(subscriptionPolicy.getUUID())) {
            throw new APIManagementException("Subscription policy UUID not found for tier: " + policyName);
        }
        return subscriptionPolicy.getUUID();
    }

    /**
     * Returns the API management DAO singleton.
     */
    private ApiMgtDAO getApiMgtDAO() {
        return ApiMgtDAO.getInstance();
    }

    /**
     * Returns the API key management DAO singleton.
     */
    private ApiKeyMgtDAO getApiKeyMgtDAO() {
        return ApiKeyMgtDAO.getInstance();
    }
}
