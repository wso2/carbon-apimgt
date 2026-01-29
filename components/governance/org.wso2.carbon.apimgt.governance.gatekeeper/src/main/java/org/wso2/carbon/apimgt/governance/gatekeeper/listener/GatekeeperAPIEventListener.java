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
import org.wso2.carbon.apimgt.governance.gatekeeper.service.GatekeeperService;
import org.wso2.carbon.apimgt.impl.notifier.Notifier;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.APIConstants;

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
     */
    private void handleAPICreateOrUpdate(GatekeeperService gatekeeperService, APIEvent apiEvent) {
        String apiId = apiEvent.getUuid();
        String apiName = apiEvent.getApiName();
        String organization = apiEvent.getTenantDomain();

        try {
            // Get the API definition - we need to fetch it from the registry
            String apiDefinition = fetchAPIDefinition(apiId, organization);
            
            if (apiDefinition == null || apiDefinition.isEmpty()) {
                log.warn("Could not fetch API definition for API: " + apiId + ". Skipping deduplication check.");
                return;
            }

            // Check for duplicates BEFORE indexing
            DuplicateCheckResult checkResult = gatekeeperService.checkForDuplicates(
                    apiId, apiDefinition, organization);

            if (checkResult.hasDuplicates()) {
                List<DuplicateCheckResult.SimilarAPI> similarApis = checkResult.getSimilarAPIs();
                
                StringBuilder warningMsg = new StringBuilder();
                warningMsg.append("API '").append(apiName).append("' appears to be similar to existing APIs:\n");
                
                for (DuplicateCheckResult.SimilarAPI similar : similarApis) {
                    warningMsg.append(String.format("  - API ID: %s (similarity: %.2f%%)\n",
                            similar.getApiId(), similar.getSimilarityScore() * 100));
                }
                
                log.warn(warningMsg.toString());
                
                // TODO: In future phases, this could:
                // 1. Send notifications to administrators
                // 2. Block the API creation based on policy
                // 3. Add governance violations to the compliance dashboard
            } else {
                log.info("No duplicates found for API: " + apiName);
            }

            // Index the API (add to LSH index and persist to DB)
            gatekeeperService.indexAPI(apiId, apiDefinition, organization);
            log.info("Successfully indexed API for deduplication: " + apiName + " (ID: " + apiId + ")");

        } catch (Exception e) {
            log.error("Error during deduplication check for API " + apiName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Handle API delete events - remove from index.
     */
    private void handleAPIDelete(GatekeeperService gatekeeperService, APIEvent apiEvent) {
        String apiId = apiEvent.getUuid();
        String apiName = apiEvent.getApiName();
        String organization = apiEvent.getTenantDomain();

        try {
            gatekeeperService.removeAPI(apiId, organization);
            log.info("Removed API from deduplication index: " + apiName + " (ID: " + apiId + ")");
        } catch (Exception e) {
            log.error("Error removing API from index: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch the OpenAPI definition for an API.
     * This retrieves the swagger/OpenAPI spec from the API Manager.
     */
    private String fetchAPIDefinition(String apiId, String organization) {
        try {
            // Use the API Provider to get the swagger definition
            org.wso2.carbon.apimgt.api.APIProvider apiProvider = 
                    org.wso2.carbon.apimgt.impl.APIManagerFactory.getInstance()
                            .getAPIProvider(getAdminUsername(organization));
            
            if (apiProvider != null) {
                // Get the API by UUID
                org.wso2.carbon.apimgt.api.model.API api = apiProvider.getAPIbyUUID(apiId, organization);
                if (api != null) {
                    // Get the swagger definition
                    String swagger = apiProvider.getOpenAPIDefinition(api.getUuid(), organization);
                    if (swagger != null && !swagger.isEmpty()) {
                        return swagger;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching API definition for API " + apiId + ": " + e.getMessage(), e);
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
