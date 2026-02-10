/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.gatekeeper.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DeduplicationAlert;
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.DeduplicationAlertService;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.DeduplicationConfigService;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.GatekeeperService;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;

import java.util.List;

/**
 * API Event Listener that triggers the Gatekeeper for deduplication checks
 * when APIs are created or updated.
 */
@Component(
        name = "org.wso2.carbon.apimgt.governance.gatekeeper.listener.GatekeeperAPIEventListener",
        service = Notifier.class,
        immediate = true
)
public class GatekeeperAPIEventListener implements Notifier {

    private static final Log log = LogFactory.getLog(GatekeeperAPIEventListener.class);

    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        if (event instanceof APIEvent) {
            APIEvent apiEvent = (APIEvent) event;
            processAPIEvent(apiEvent);
        }
        return true;
    }

    /**
     * Process API lifecycle events for deduplication.
     *
     * @param apiEvent The API event
     */
    private void processAPIEvent(APIEvent apiEvent) {
        String eventType = apiEvent.getType();
        String apiId = apiEvent.getUuid();
        String apiName = apiEvent.getApiName();
        String apiVersion = apiEvent.getApiVersion();
        String organization = apiEvent.getTenantDomain();

        log.info(String.format("Gatekeeper received API event: type=%s, apiId=%s, name=%s, version=%s, org=%s",
                eventType, apiId, apiName, apiVersion, organization));

        try {
            GatekeeperService gatekeeperService = GatekeeperService.getInstance();

            // Event type is a String that matches the enum name
            if (APIConstants.EventType.API_CREATE.name().equals(eventType)
                    || APIConstants.EventType.API_UPDATE.name().equals(eventType)) {
                handleAPICreateOrUpdate(gatekeeperService, apiEvent);

            } else if (APIConstants.EventType.API_DELETE.name().equals(eventType)) {
                handleAPIDelete(gatekeeperService, apiEvent);

            } else if (APIConstants.EventType.API_LIFECYCLE_CHANGE.name().equals(eventType)) {
                // Re-index on lifecycle changes (e.g., PUBLISHED)
                if ("PUBLISHED".equals(apiEvent.getApiStatus())) {
                    handleAPICreateOrUpdate(gatekeeperService, apiEvent);
                }
            } else {
                log.debug("Ignoring event type: " + eventType);
            }
        } catch (Exception e) {
            log.error("Error processing API event for deduplication: " + e.getMessage(), e);
        }
    }

    /**
     * Handle API create or update events - check for duplicates and index.
     * The API is ALWAYS indexed (to keep the LSH index hydrated).
     * The dedup CHECK only runs if: config enabled AND policy is associated.
     */
    private void handleAPICreateOrUpdate(GatekeeperService gatekeeperService, APIEvent apiEvent) {
        String apiId = apiEvent.getUuid();
        String apiName = apiEvent.getApiName();
        String apiVersion = apiEvent.getApiVersion();
        String organization = apiEvent.getTenantDomain();

        try {
            // Get the API definition - we need to fetch it from the registry
            String apiDefinition = fetchAPIDefinition(apiId, organization);
            
            if (apiDefinition == null || apiDefinition.isEmpty()) {
                log.warn("Could not fetch API definition for API: " + apiId + ". Skipping deduplication check.");
                return;
            }

            // Log API definition size for debugging similarity issues
            if (log.isDebugEnabled()) {
                log.debug("API Definition for " + apiName + " has length: " + apiDefinition.length());
            }

            // Check if the dedup policy is active (enabled + associated with a governance policy)
            DeduplicationConfigService configService = DeduplicationConfigService.getInstance();
            DeduplicationConfigService.DeduplicationConfig config = configService.getConfig(organization);
            boolean policyActive = configService.isDeduplicationPolicyActive(organization);
            
            if (!config.isEnabled()) {
                log.info("Deduplication is disabled in config for org: " + organization 
                        + ". Skipping dedup check, but still indexing API.");
            } else if (!policyActive) {
                log.info("No governance policy has the deduplication ruleset associated for org: " 
                        + organization + ". Skipping dedup check, but still indexing API.");
            } else {
                // Check for duplicates BEFORE indexing
                DuplicateCheckResult checkResult = gatekeeperService.checkForDuplicates(
                        apiId, apiDefinition, organization);

                if (checkResult.hasDuplicates()) {
                    List<DuplicateCheckResult.SimilarAPI> similarApis = checkResult.getSimilarAPIs();

                    StringBuilder warningMsg = new StringBuilder();
                    warningMsg.append("\n╔════════════════════════════════════════════════════════════════════════╗\n");
                    warningMsg.append("║                      DUPLICATE API DETECTED                            ║\n");
                    warningMsg.append("╠════════════════════════════════════════════════════════════════════════╣\n");
                    warningMsg.append("║ New API: ").append(apiName).append(" v").append(apiVersion).append("\n");
                    warningMsg.append("║ Similar APIs found:\n");

                    for (DuplicateCheckResult.SimilarAPI similar : similarApis) {
                        warningMsg.append(String.format("║   • API UUID: %s%n", similar.getApiId()));
                        warningMsg.append(String.format("║     Similarity: %.2f%% (threshold: %.2f%%)%n",
                                similar.getSimilarityScore() * 100,
                                checkResult.getThreshold() * 100));
                    }
                    warningMsg.append("╠════════════════════════════════════════════════════════════════════════╣\n");
                    warningMsg.append("║ MODE: ").append(config.getMode().toUpperCase())
                            .append(" - API was created but flagged for review                  ║\n");
                    warningMsg.append("║ ACTION REQUIRED: Review pending alerts in Governance dashboard        ║\n");
                    warningMsg.append("╚════════════════════════════════════════════════════════════════════════╝\n");

                    log.warn(warningMsg.toString());
                    
                    // Create a deduplication alert for user decision
                    try {
                        DeduplicationAlertService alertService = DeduplicationAlertService.getInstance();
                        
                        // Get API context if available
                        String apiContext = getApiContext(apiId, organization);
                        String createdBy = getApiCreator(apiId, organization);
                        
                        DeduplicationAlert alert = alertService.createAlertFromCheckResult(
                                checkResult, apiName, apiVersion, apiContext, createdBy, organization);
                        
                        if (alert != null) {
                            log.info("Created deduplication alert: " + alert.getAlertId() + 
                                    " with severity: " + alert.getSeverity() +
                                    " for API: " + apiName);
                            
                            // Log available actions for visibility
                            log.info("Available actions for alert " + alert.getAlertId() + ": " +
                                    alert.getAvailableActions());
                            
                            // Log instructions for resolving the alert
                            log.info("To resolve this alert, use the Governance API: " +
                                    "POST /api/am/governance/deduplication/alerts/" + alert.getAlertId() + "/decision");
                        }
                    } catch (Exception e) {
                        log.error("Failed to create deduplication alert for API " + apiName + 
                                ": " + e.getMessage(), e);
                        // Continue with indexing even if alert creation fails
                    }
                } else {
                    log.info("No duplicates found for API: " + apiName);
                }
            }

            // ALWAYS index the API (add to LSH index and persist to DB)
            // This keeps the index up-to-date even when the policy is disabled,
            // so when re-enabled, the index is ready immediately.
            gatekeeperService.indexAPI(apiId, apiDefinition, organization);
            log.info("Successfully indexed API for deduplication: " + apiName + " (ID: " + apiId + ")");

        } catch (Exception e) {
            log.error("Error during deduplication check for API " + apiName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Get API context from the API Manager.
     */
    private String getApiContext(String apiId, String organization) {
        try {
            org.wso2.carbon.apimgt.api.APIProvider apiProvider = 
                    org.wso2.carbon.apimgt.impl.APIManagerFactory.getInstance()
                            .getAPIProvider(getAdminUsername(organization));
            if (apiProvider != null) {
                org.wso2.carbon.apimgt.api.model.API api = apiProvider.getAPIbyUUID(apiId, organization);
                if (api != null) {
                    return api.getContext();
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch API context for API " + apiId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Get API creator from the API Manager.
     */
    private String getApiCreator(String apiId, String organization) {
        try {
            org.wso2.carbon.apimgt.api.APIProvider apiProvider = 
                    org.wso2.carbon.apimgt.impl.APIManagerFactory.getInstance()
                            .getAPIProvider(getAdminUsername(organization));
            if (apiProvider != null) {
                org.wso2.carbon.apimgt.api.model.API api = apiProvider.getAPIbyUUID(apiId, organization);
                if (api != null && api.getId() != null) {
                    return api.getId().getProviderName();
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch API creator for API " + apiId + ": " + e.getMessage());
        }
        return null;
    }


    /**
     * Handle API delete events - remove from index.
     */
    private void handleAPIDelete(GatekeeperService gatekeeperService, APIEvent apiEvent) {
        String apiId = apiEvent.getUuid();
        String apiName = apiEvent.getApiName();
        String organization = apiEvent.getTenantDomain();

        try {
            // Remove from deduplication index
            gatekeeperService.removeAPI(apiId, organization);
            log.info("Removed API from deduplication index: " + apiName + " (ID: " + apiId + ")");
            
            // Auto-resolve any pending alerts for this API
            try {
                DeduplicationAlertService alertService = DeduplicationAlertService.getInstance();
                alertService.autoResolveAlertsForDeletedApi(apiId, organization);
            } catch (Exception e) {
                log.warn("Failed to auto-resolve alerts for deleted API " + apiId + ": " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error removing API from index: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch the OpenAPI definition for an API.
     * This retrieves the swagger/OpenAPI spec from the API Manager.
     * Includes retry logic for newly created APIs where the definition
     * may not be immediately available (e.g., APIs created from scratch).
     */
    private String fetchAPIDefinition(String apiId, String organization) {
        int maxRetries = 3;
        long retryDelayMs = 500;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                org.wso2.carbon.apimgt.api.APIProvider apiProvider = 
                        org.wso2.carbon.apimgt.impl.APIManagerFactory.getInstance()
                                .getAPIProvider(getAdminUsername(organization));
                
                if (apiProvider != null) {
                    org.wso2.carbon.apimgt.api.model.API api = apiProvider.getAPIbyUUID(apiId, organization);
                    if (api != null) {
                        String swagger = apiProvider.getOpenAPIDefinition(api.getUuid(), organization);
                        if (swagger != null && !swagger.isEmpty()) {
                            return swagger;
                        }
                    }
                }
                
                // If we didn't get a definition and this isn't the last attempt, wait and retry
                if (attempt < maxRetries) {
                    log.debug("API definition not yet available for " + apiId 
                            + ", retrying in " + retryDelayMs + "ms (attempt " + attempt + "/" + maxRetries + ")");
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2; // Exponential backoff
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    log.error("Error fetching API definition for API " + apiId + ": " + e.getMessage(), e);
                } else {
                    log.debug("Attempt " + attempt + " failed to fetch API definition for " + apiId 
                            + ": " + e.getMessage());
                }
            }
        }
        return null;
    }

    /**
     * Get the admin username for the organization.
     */
    private String getAdminUsername(String organization) {
        if (organization == null || "carbon.super".equals(organization)) {
            return "admin";
        }
        return "admin@" + organization;
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.API.name();
    }
}
