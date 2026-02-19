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
import org.wso2.carbon.apimgt.governance.gatekeeper.model.DuplicateCheckResult;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.BackgroundHydrationService;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.DeduplicationConfigService;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.GatekeeperService;
import org.wso2.carbon.apimgt.governance.gatekeeper.service.SkippedApiTracker;
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

        if (log.isDebugEnabled()) {
            log.debug(String.format("Gatekeeper received API event: type=%s, apiId=%s, name=%s, version=%s, org=%s",
                    eventType, apiId, apiName, apiVersion, organization));
        }

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
     * Indexing is SKIPPED when deduplication is disabled to save resources.
     * Skipped APIs are tracked and hydrated when dedup is re-enabled.
     * The dedup CHECK only runs if: config enabled AND policy is associated.
     * Note: Violations are recorded via GOV_RULE_VIOLATION during compliance evaluation,
     * not via AM_DEDUP_ALERT alerts.
     */
    private void handleAPICreateOrUpdate(GatekeeperService gatekeeperService, APIEvent apiEvent) {
        String apiId = apiEvent.getUuid();
        String apiName = apiEvent.getApiName();
        String apiVersion = apiEvent.getApiVersion();
        String organization = apiEvent.getTenantDomain();

        try {
            // Check if the dedup policy is active (enabled + associated with a governance policy)
            DeduplicationConfigService configService = DeduplicationConfigService.getInstance();
            DeduplicationConfigService.DeduplicationConfig config = configService.getConfig(organization);
            boolean policyActive = configService.isDeduplicationPolicyActive(organization);

            if (!config.isEnabled() || !policyActive) {
                // Skip indexing entirely when dedup is disabled - track for later hydration
                SkippedApiTracker.getInstance().trackSkipped(apiId, organization);
                if (log.isDebugEnabled()) {
                    if (!config.isEnabled()) {
                        log.debug("Deduplication disabled for org: " + organization
                                + ". Skipping indexing for API: " + apiName + " (tracked for hydration)");
                    } else {
                        log.debug("No governance policy has the deduplication ruleset associated for org: "
                                + organization + ". Skipping indexing for API: " + apiName);
                    }
                }
                return;
            }

            // Dedup is active - check if we need to hydrate skipped APIs first
            SkippedApiTracker tracker = SkippedApiTracker.getInstance();
            if (tracker.hasSkippedApis(organization)) {
                log.debug("Dedup re-enabled with " + tracker.getSkippedCount(organization)
                        + " skipped APIs pending. Triggering background hydration.");
                BackgroundHydrationService.getInstance().triggerHydration(organization);
            }

            // Get the API definition - try OpenAPI first, then Async API
            String apiDefinition = fetchAPIDefinition(apiId, organization);
            
            if (apiDefinition == null || apiDefinition.isEmpty()) {
                log.debug("Could not fetch API definition for API: " + apiName + " (" + apiId
                        + "). Skipping deduplication check.");
                return;
            }

            // Log API definition size for debugging similarity issues
            if (log.isDebugEnabled()) {
                log.debug("API Definition for " + apiName + " has length: " + apiDefinition.length());
            }

            // Check for duplicates BEFORE indexing
            DuplicateCheckResult checkResult = gatekeeperService.checkForDuplicates(
                    apiId, apiDefinition, organization);

            if (checkResult.hasDuplicates()) {
                List<DuplicateCheckResult.SimilarAPI> similarApis = checkResult.getSimilarAPIs();

                if (log.isDebugEnabled()) {
                    StringBuilder debugMsg = new StringBuilder();
                    debugMsg.append("Duplicate API detected - New API: ")
                            .append(apiName).append(" v").append(apiVersion)
                            .append(" | Similar APIs: ");
                    for (DuplicateCheckResult.SimilarAPI similar : similarApis) {
                        debugMsg.append(String.format("[%s (%s) %.1f%%] ",
                                similar.getApiName() != null ? similar.getApiName() : "unknown",
                                similar.getApiId(),
                                similar.getSimilarityScore() * 100));
                    }
                    debugMsg.append("| Mode: ").append(config.getMode().toUpperCase());
                    log.debug(debugMsg.toString());
                }
                // Violations will be recorded in GOV_RULE_VIOLATION during the next
                // compliance evaluation cycle via the GatekeeperValidationEngine
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No duplicates found for API: " + apiName);
                }
            }

            // ALWAYS index the API when dedup is active
            gatekeeperService.indexAPI(apiId, apiDefinition, organization);
            if (log.isDebugEnabled()) {
                log.debug("Indexed API for deduplication: " + apiName + " (ID: " + apiId + ")");
            }

        } catch (Exception e) {
            log.error("Error during deduplication check for API " + apiName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Handle API delete events - remove from index.
     * The next compliance evaluation cycle will find no duplicates for
     * APIs that previously matched the deleted API, clearing violations naturally.
     */
    private void handleAPIDelete(GatekeeperService gatekeeperService, APIEvent apiEvent) {
        String apiId = apiEvent.getUuid();
        String apiName = apiEvent.getApiName();
        String organization = apiEvent.getTenantDomain();

        try {
            // Remove from deduplication index
            gatekeeperService.removeAPI(apiId, organization);
            if (log.isDebugEnabled()) {
                log.debug("Removed API from deduplication index: " + apiName + " (ID: " + apiId + ")");
            }

            // Also remove from skipped tracker if it was tracked
            SkippedApiTracker.getInstance().removeFromSkipped(apiId, organization);
        } catch (Exception e) {
            log.error("Error removing API from index: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch the API definition for an API.
     * Tries OpenAPI (REST) definition first, then falls back to AsyncAPI definition
     * for Async APIs (WebSocket, SSE, WebSub etc.).
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
                        // Try OpenAPI definition first (REST APIs)
                        String swagger = apiProvider.getOpenAPIDefinition(api.getUuid(), organization);
                        if (swagger != null && !swagger.isEmpty()) {
                            return swagger;
                        }
                        // Fallback: try AsyncAPI definition for async APIs
                        String asyncDef = apiProvider.getAsyncAPIDefinition(api.getUuid(), organization);
                        if (asyncDef != null && !asyncDef.isEmpty()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Using AsyncAPI definition for API: " + apiId);
                            }
                            return asyncDef;
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
