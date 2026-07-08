/*
*
* Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/

package org.wso2.carbon.apimgt.federated.gateway;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FederatedAPIDiscovery;
import org.wso2.carbon.apimgt.api.FederatedAPIDiscoveryService;
import org.wso2.carbon.apimgt.api.dto.ImportedAPIDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiResult;
import org.wso2.carbon.apimgt.api.model.DiscoveredAPI;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.GatewayMode;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.federated.gateway.util.FederatedGatewayUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.wso2.carbon.apimgt.impl.APIConstants.DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS;
import static org.wso2.carbon.apimgt.impl.APIConstants.DELEM_COLON;
import static org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil.fromAPItoDTO;
import static org.wso2.carbon.apimgt.federated.gateway.util.FederatedGatewayConstants.DISCOVERED_API_LIST;
import static org.wso2.carbon.apimgt.federated.gateway.util.FederatedGatewayConstants.PUBLISHED_API_LIST;

/**
 * This class is responsible for scheduling and executing the discovery of APIs in a federated gateway environment.
 * It uses a scheduled executor service to periodically discover APIs and process them accordingly.
 *
 * <p>Two complementary discovery strategies are supported and both are intentionally retained so that a deployment
 * can switch between them:</p>
 * <ul>
 *     <li><b>Scheduler based</b> - {@link #scheduleDiscovery(Environment, String)} periodically polls the gateway
 *     and pushes changes automatically via {@link #processDiscoveredAPIs}.</li>
 *     <li><b>On demand</b> - {@link #discoverExternalAPIs}, {@link #importNewExternalAPIs} and
 *     {@link #updateExternalAPIs} allow a user to review and import/update APIs explicitly.</li>
 * </ul>
 */
@Component(
        name = "apim.gateway.federation.service",
        service = FederatedAPIDiscoveryService.class,
        immediate = true
)
public class FederatedAPIDiscoveryRunner implements FederatedAPIDiscoveryService {

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(5, r ->
            new Thread(r, "FederatedAPIDiscoveryExecutor"));
    private static final ScheduledExecutorService ttlUpdateExecutor = Executors.newSingleThreadScheduledExecutor(r ->
            new Thread(r, "FederatedAPIDiscoveryExecutor - TTL Update"));
    private static final Map<String, ScheduledFuture<?>> scheduledDiscoveryTasks = new ConcurrentHashMap<>();
    private static final Map<String, ScheduledFuture<?>> scheduledHeartBeatTasks = new ConcurrentHashMap<>();
    private static Log log = LogFactory.getLog(FederatedAPIDiscoveryRunner.class);

    private static final String nodeId = UUID.randomUUID().toString();

    /**
     * Schedules the discovery of APIs at a specified interval.
     *
     * @param environment  The environment from which APIs will be discovered.
     * @param organization The organization context for the discovery.
     */
    public void scheduleDiscovery(Environment environment, String organization) {
        if (log.isDebugEnabled()) {
            log.debug("Scheduling federated API discovery for environment: " + environment.getName()
                    + " in organization: " + organization);
        }
        GatewayAgentConfiguration gatewayConfiguration = ServiceReferenceHolder.getInstance().
                getExternalGatewayConnectorConfiguration(environment.getGatewayType());
        if (gatewayConfiguration != null && gatewayConfiguration.getDiscoveryImplementation() != null) {
            if (StringUtils.isNotEmpty(gatewayConfiguration.getDiscoveryImplementation())) {
                try {
                    String taskKey = "FederatedAPIDiscovery" + DELEM_COLON + environment.getName() + DELEM_COLON
                            + organization;
                    ScheduledFuture<?> scheduledFuture = scheduledDiscoveryTasks.get(taskKey);
                    // Cancel an existing task if one exists
                    if (scheduledFuture != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Cancelling existing discovery task for: " + taskKey + " to reschedule.");
                        }
                        scheduledFuture.cancel(true);
                        scheduledDiscoveryTasks.remove(taskKey);
                    }
                    APIManagerConfiguration apimConfig = ServiceReferenceHolder.getInstance()
                            .getAPIManagerConfigurationService().getAPIManagerConfiguration();
                    boolean schedulerEnabled = apimConfig != null
                            && apimConfig.isFederatedAPIDiscoverySchedulerEnabled();
                    if (!schedulerEnabled) {
                        if (log.isDebugEnabled()) {
                            log.debug("Federated API discovery scheduler is disabled via configuration.");
                        }
                        return;
                    }
                    int scheduleWindow = environment.getApiDiscoveryScheduledWindow();
                    if (GatewayMode.WRITE_ONLY.getMode().equals(environment.getMode()) || scheduleWindow <= 0) {
                        log.info("Federated API discovery is disabled for environment: " + environment.getName());
                    } else {
                        log.info("Initializing federated API discovery for environment: " + environment.getName()
                                + " and organization: " + organization);
                        // Reuse the shared connector loader instead of duplicating the reflection logic.
                        FederatedAPIDiscovery federatedAPIDiscovery =
                                getFederatedAPIDiscovery(environment, organization);
                        long ttl = TimeUnit.MINUTES.toMillis(scheduleWindow) / 2;
                        ScheduledFuture<?> newTask = executor.scheduleAtFixedRate(() -> {
                            boolean acquired = false;
                            long scheduledTime = System.currentTimeMillis();
                            try {
                                acquired = acquireLockToExecuteDiscovery(scheduledTime, taskKey, ttl);
                                if (acquired) {
                                    try {
                                        List<DiscoveredAPI> discoveredAPIs = federatedAPIDiscovery.discoverAPI();
                                        if (discoveredAPIs != null && !discoveredAPIs.isEmpty()) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("Discovered " + discoveredAPIs.size()
                                                        + " APIs for environment: " + environment.getName()
                                                        + " and organization: " + organization);
                                            }
                                            processDiscoveredAPIs(discoveredAPIs, environment, organization,
                                                    federatedAPIDiscovery);
                                        } else {
                                            if (log.isDebugEnabled()) {
                                                log.debug("No APIs discovered in environment: "
                                                        + environment.getName());
                                            }
                                        }
                                    } catch (Exception e) {
                                        log.error("Error during federated API discovery for environment: "
                                                + environment.getName(), e);
                                    }
                                }
                            } finally {
                                if (acquired) {
                                    releaseAcquiredLock(taskKey);
                                }
                            }
                        }, 0, scheduleWindow, TimeUnit.MINUTES);
                        scheduledDiscoveryTasks.put(taskKey, newTask);
                        if (log.isDebugEnabled()) {
                            log.debug("Successfully scheduled federated API discovery for environment: "
                                    + environment.getName() + " in organization: " + organization
                                    + " with interval: " + scheduleWindow + " minutes");
                        }
                    }
                } catch (APIManagementException e) {
                    log.error("Error while loading federated API discovery for environment "
                            + environment.getName(), e);
                } catch (Throwable e) {
                    log.error("Unexpected error while initializing federated API discovery for environment "
                            + environment.getName(), e);
                }
            }
        }
    }

    /**
     * Processes the discovered APIs and deploys them to the federated gateway environment.
     *
     * <p><b>NOTE:</b> This method is owned by another author. Only whitespace/indentation has been normalised
     * here; no statement, expression or control flow has been altered.</p>
     *
     * @param apisToDeployInGatewayEnv List of APIs to be deployed.
     * @param environment              The environment where the APIs will be deployed.
     * @param organization             The organization context for the deployment.
     */
    private void processDiscoveredAPIs(List<DiscoveredAPI> apisToDeployInGatewayEnv, Environment environment,
                                       String organization, FederatedAPIDiscovery discovery) {
        boolean debugLogEnabled = log.isDebugEnabled();
        if (debugLogEnabled) {
            log.debug("Processing discovered APIs for environment: " + environment.getName()
                    + " in organization: " + organization);
        }
        try {
            String adminUsername = APIUtil.getTenantAdminUserName(organization);
            FederatedGatewayUtil.startTenantFlow(organization, adminUsername);
            Map<String, Map<String, ApiResult>> alreadyAvailableAPIs =
                    FederatedGatewayUtil.getDiscoveredAPIsFromFederatedGateway(environment, organization);
            List<String> discoveredAPIsFromFederatedGW = new ArrayList<>();
            Map<String, ApiResult> alreadyDiscoveredAPIMap = alreadyAvailableAPIs.get(DISCOVERED_API_LIST);

            List<String> alreadyDiscoveredAPIsList = new ArrayList<>(alreadyDiscoveredAPIMap.keySet());

            for (DiscoveredAPI discoveredAPI : apisToDeployInGatewayEnv) {
                if (discoveredAPI == null) {
                    if (debugLogEnabled) {
                        log.debug("Discovered API is null. Skipping...");
                    }
                    continue;
                }
                APIDTO apidto = fromAPItoDTO(discoveredAPI.getApi());
                if (apidto.getPolicies() == null || apidto.getPolicies().isEmpty()) {
                    apidto.setPolicies(Collections.singletonList(DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS));
                }
                API api = discoveredAPI.getApi();
                try {
                    String apiKey = apidto.getName() + DELEM_COLON + apidto.getVersion();
                    String envScopedKey = apidto.getName() + APIConstants.KEY_SEPARATOR
                            + environment.getName() + DELEM_COLON + apidto.getVersion();

                    // Determine import mode
                    boolean isNewVersion = false;
                    String existingAPI = null;
                    boolean isPublishedAPIFromCP = alreadyAvailableAPIs.get(PUBLISHED_API_LIST).containsKey(apiKey) ||
                            alreadyAvailableAPIs.get(PUBLISHED_API_LIST).containsKey(envScopedKey);
                    boolean update = alreadyDiscoveredAPIsList.contains(apiKey) ||
                            alreadyDiscoveredAPIsList.contains(envScopedKey);
                    boolean alreadyExistsWithEnvScope = alreadyDiscoveredAPIsList.contains(envScopedKey);
                    if (!update && !alreadyExistsWithEnvScope) {
                        String envPathName = apidto.getName() + APIConstants.KEY_SEPARATOR
                                + environment.getName();
                        Optional<String> existingApiOpt = alreadyDiscoveredAPIsList.stream()
                                .map(String::trim)
                                .map(s -> {
                                    int idx = s.lastIndexOf(DELEM_COLON);
                                    if (idx <= 0 || idx >= s.length() - 1) return null;
                                    String name = s.substring(0, idx);
                                    String version = s.substring(idx + 1);
                                    return new String[]{name, version};
                                })
                                .filter(Objects::nonNull)
                                .filter(parts -> (parts[0].equals(apidto.getName())
                                        || parts[0].equals(envPathName))
                                        && !parts[1].equals(apidto.getVersion()))
                                .map(parts -> parts[0] + DELEM_COLON + parts[1])
                                .findFirst();
                        isNewVersion = existingApiOpt.isPresent();
                        existingAPI = existingApiOpt.orElse(null);
                    }

                    String referenceArtifact = null;
                    if (alreadyExistsWithEnvScope) {
                        referenceArtifact = getReferenceObjectForExistingAPIs(environment,
                                alreadyDiscoveredAPIMap.get(envScopedKey));
                    } else if (update) {
                        referenceArtifact = getReferenceObjectForExistingAPIs(environment,
                                alreadyDiscoveredAPIMap.get(apiKey));
                    }

                    if (isPublishedAPIFromCP || (update &&
                            !discovery.isAPIUpdated(referenceArtifact, discoveredAPI.getReferenceArtifact()))) {
                        discoveredAPIsFromFederatedGW.add(alreadyExistsWithEnvScope ? envScopedKey : apiKey);
                        if (log.isDebugEnabled()) {
                            log.debug("API: " + api.getId().getName() + " is already deployed in environment: "
                                    + environment.getName() + " and new changes are not available. Skipping...");
                        }
                        continue;
                    }
                    // Adjust the name if needed
                    if (alreadyExistsWithEnvScope) {
                        if (api.getDisplayName() == null) {
                            apidto.displayName(apidto.getName());
                        }
                        apidto.setName(apidto.getName() + APIConstants.KEY_SEPARATOR + environment.getName());
                    }
                    //if the discovered API is a new version, we need to create a new API version in the system
                    API newAPI = null;
                    if (isNewVersion) {
                        String existingApiUUID = FederatedGatewayUtil.getAPIUUID(existingAPI, adminUsername,
                                organization);
                        if (existingApiUUID != null) {
                            newAPI = FederatedGatewayUtil.createNewAPIVersion(existingApiUUID, apidto.getVersion(),
                                    organization);
                            update = true;
                        }
                    }

                    // Map to DTO and create ZIP
                    JsonObject apiJson = (JsonObject) new Gson().toJsonTree(apidto);
                    apiJson = CommonUtil.addTypeAndVersionToFile(ImportExportConstants.TYPE_API,
                            ImportExportConstants.APIM_VERSION, apiJson);
                    InputStream apiZip = FederatedGatewayUtil.createZipAsInputStream(
                            apiJson.toString(), api.getSwaggerDefinition(),
                            FederatedGatewayUtil.createDeploymentYaml(environment),
                            apidto.getName());

                    ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();

                    // Import API
                    ImportedAPIDTO importedApi = importExportAPI.importAPI(apiZip, false,
                            true, update, true,
                            new String[]{APIConstants.APIM_PUBLISHER_SCOPE, APIConstants.APIM_CREATOR_SCOPE},
                            organization);

                    if (update) {
                        if (newAPI != null) {
                            APIUtil.addApiExternalApiMapping(newAPI.getUuid(),
                                    environment.getUuid(), discoveredAPI.getReferenceArtifact());
                        } else {
                            APIUtil.updateApiExternalApiMapping(importedApi.getApi().getUuid(),
                                    environment.getUuid(), discoveredAPI.getReferenceArtifact());
                        }
                    } else {
                        APIUtil.addApiExternalApiMapping(importedApi.getApi().getUuid(),
                                environment.getUuid(), discoveredAPI.getReferenceArtifact());
                    }

                    // Track deployed
                    discoveredAPIsFromFederatedGW.add(alreadyExistsWithEnvScope ? envScopedKey : apiKey);

                    if (!update) {
                        alreadyDiscoveredAPIsList.add(apidto.getName() + APIConstants.DELEM_COLON
                                + apidto.getVersion());
                    }
                    if (debugLogEnabled) {
                        log.debug((update ? "Updated" : "Created") + " API: " + api.getId().getName()
                                + " in environment: " + environment.getName());
                    }
                } catch (IOException e) {
                    log.error("IO error while processing API: " + api.getId().getName()
                            + " in organization: " + organization, e);
                } catch (APIManagementException e) {
                    log.error("API management error while processing API: " + api.getId().getName()
                            + " in organization: " + organization, e);
                }
            }
            for (String apiName : alreadyDiscoveredAPIsList) {
                if (!discoveredAPIsFromFederatedGW.contains(apiName)) {
                    try {
                        String apiUUID = FederatedGatewayUtil.getAPIUUID(apiName, adminUsername, organization);
                        if (apiUUID != null) {
                            FederatedGatewayUtil.deleteDeployment(apiUUID, organization, environment);
                        } else {
                            if (debugLogEnabled) {
                                log.debug("API UUID not found for: " + apiName
                                        + ". Skipping removal from environment: " + environment.getName());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to delete revision for API: " + apiName + " from environment: "
                                + environment.getName(), e);
                    }
                }
            }
            if (debugLogEnabled) {
                log.debug("Successfully processed discovered APIs for environment: " + environment.getName()
                        + " in organization: " + organization);
            }
        } catch (APIManagementException e) {
            log.error("Failed during federated API processing for environment: " + environment.getName() +
                    " and organization: " + organization, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static void shutdown() {
        scheduledDiscoveryTasks.values().forEach(f -> f.cancel(false));
        executor.shutdown();
        scheduledHeartBeatTasks.values().forEach(f -> f.cancel(false));
        ttlUpdateExecutor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                log.warn("Forced shutdown of federated API discovery executor after timeout");
            }
            if (!ttlUpdateExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                ttlUpdateExecutor.shutdownNow();
                log.warn("Forced shutdown of TTL update executor after timeout");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            ttlUpdateExecutor.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Interrupted during federated API discovery shutdown", e);
        }
    }

    /**
     * Stops the federated API discovery task for the specified environment and organization.
     * If a discovery task is currently running for the given environment and organization, it will be
     * cancelled and removed from the scheduled tasks.
     *
     * @param environment  The environment for which the discovery task should be stopped.
     * @param organization The organization context for which the discovery task should be stopped.
     */
    @Override
    public void stopDiscovery(Environment environment, String organization) {
        String taskKey = "FederatedAPIDiscovery" + DELEM_COLON + environment.getName() + DELEM_COLON
                + organization;
        if (scheduledDiscoveryTasks.containsKey(taskKey)) {
            scheduledDiscoveryTasks.get(taskKey).cancel(true);
            scheduledDiscoveryTasks.remove(taskKey);
            // Cancel and remove associated heartbeat task
            ScheduledFuture<?> heartbeat = scheduledHeartBeatTasks.get(taskKey);
            if (heartbeat != null) {
                heartbeat.cancel(true);
                scheduledHeartBeatTasks.remove(taskKey);
            }
            log.info("Stopped federated API discovery task for environment: " + environment.getName()
                    + " in organization: " + organization);
        }
    }

    /**
     * Attempts to acquire a lock for a discovery task.
     * If the lock is acquired, the task is scheduled for execution.
     *
     * @param scheduledTimeMs The scheduled time for the task in milliseconds.
     * @param taskKey         The unique key for the task.
     * @param ttlMilliseconds The time-to-live for the lock in milliseconds.
     * @return true if the lock was acquired and the task can proceed, false otherwise.
     */
    private boolean acquireLockToExecuteDiscovery(long scheduledTimeMs, String taskKey, long ttlMilliseconds) {
        final ApiMgtDAO dao = ApiMgtDAO.getInstance();
        final long now = System.currentTimeMillis();

        try {
            // 1) First attempt: create a new executor task (happy path)
            if (dao.addExecutorTask(scheduledTimeMs, taskKey, FederatedAPIDiscoveryRunner.nodeId)) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully acquired lock for discovery task " + taskKey);
                }
                scheduledHeartBeatTasks.put(taskKey, scheduleHeartbeat(taskKey, ttlMilliseconds));
                return true;
            }

            // 2) Someone already holds it. Check if it's expired.
            long holderScheduledTimeMs = dao.getScheduledTimeFromExecutorTask(taskKey);
            long expiryMs = holderScheduledTimeMs + ttlMilliseconds;
            if (now <= expiryMs) {
                // Still valid; don't steal it.
                return false;
            }
            // 3) Expired; try to take over
            if (dao.updateExecutorTask(scheduledTimeMs, taskKey, FederatedAPIDiscoveryRunner.nodeId)) {
                //4) If we succeeded, schedule the task immediately.
                if (log.isDebugEnabled()) {
                    log.debug("Successfully acquired lock for discovery task " + taskKey + " after stealing from "
                            + holderScheduledTimeMs);
                }
                scheduledHeartBeatTasks.put(taskKey, scheduleHeartbeat(taskKey, ttlMilliseconds));
                return true;
            }
            // 5) Failed to steal; give up.
            if (log.isDebugEnabled()) {
                log.debug("Failed to acquire lock for discovery task " + taskKey + " after stealing from "
                        + holderScheduledTimeMs);
            }
            return false;
        } catch (APIManagementException e) {
            log.warn("Failed to acquire lock for discovery task " + taskKey, e);
            return false;
        }
    }

    /**
     * The heartbeat task periodically updates the ScheduledTime of the task to prevent it from expiring.
     *
     * @param taskKey         The unique key for the task.
     * @param ttlMilliseconds The time-to-live for the lock in milliseconds.
     * @return A ScheduledFuture representing the heartbeat task.
     */
    private ScheduledFuture<?> scheduleHeartbeat(String taskKey, long ttlMilliseconds) {
        if (log.isDebugEnabled()) {
            log.debug("Successfully scheduled heartbeat for discovery task " + taskKey);
        }
        return ttlUpdateExecutor.scheduleAtFixedRate(() -> {
            long newExpiryMs = System.currentTimeMillis() + ttlMilliseconds;
            try {
                ApiMgtDAO.getInstance().updateScheduledTimeOfExecutorTask(newExpiryMs, taskKey);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully updated heartbeat for discovery task " + taskKey + " to " + newExpiryMs);
                }
            } catch (Exception e) {
                log.warn("Failed to extend TTL for task " + taskKey, e);
            }
        }, ttlMilliseconds / 2, ttlMilliseconds / 2, TimeUnit.MILLISECONDS);
    }

    /**
     * Releases the acquired lock for a discovery task.
     *
     * @param taskKey The unique key for the task.
     */
    private void releaseAcquiredLock(String taskKey) {
        ScheduledFuture<?> heartbeat = scheduledHeartBeatTasks.get(taskKey);
        if (heartbeat != null) {
            heartbeat.cancel(true);
            scheduledHeartBeatTasks.remove(taskKey);
        }
        try {
            ApiMgtDAO.getInstance().deleteExecutorTask(taskKey);
            if (log.isDebugEnabled()) {
                log.debug("Successfully released lock for discovery task " + taskKey);
            }
        } catch (APIManagementException e) {
            log.error("Error while deleting executor task for " + taskKey, e);
        }
    }

    /**
     * Get the reference object for an existing API.
     *
     * @param environment The environment where the API is deployed.
     * @param apiResult   The API result object.
     * @return The reference object for the API.
     * @throws APIManagementException If an error occurs while retrieving the reference object.
     */
    private String getReferenceObjectForExistingAPIs(Environment environment, ApiResult apiResult)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving reference object for API ID: " + apiResult.getId());
        }
        return APIUtil.getApiExternalApiMappingReferenceByApiId(apiResult.getId(), environment.getUuid());
    }

    /**
     * Helper to initialize the correct Gateway Connector dynamically.
     *
     * @param environment  The environment whose connector should be loaded.
     * @param organization The organization context.
     * @return An initialized {@link FederatedAPIDiscovery} connector.
     * @throws APIManagementException if no implementation is configured or it cannot be instantiated.
     */
    private FederatedAPIDiscovery getFederatedAPIDiscovery(Environment environment, String organization)
            throws APIManagementException {
        GatewayAgentConfiguration gatewayConfiguration = ServiceReferenceHolder.getInstance().
                getExternalGatewayConnectorConfiguration(environment.getGatewayType());
        if (gatewayConfiguration == null || StringUtils.isEmpty(gatewayConfiguration.getDiscoveryImplementation())) {
            throw new APIManagementException("No discovery implementation configured for gateway type: "
                    + environment.getGatewayType());
        }
        try {
            FederatedAPIDiscovery federatedAPIDiscovery = (FederatedAPIDiscovery)
                    Class.forName(gatewayConfiguration.getDiscoveryImplementation())
                            .getDeclaredConstructor().newInstance();
            federatedAPIDiscovery.init(environment, organization);
            return federatedAPIDiscovery;
        } catch (Exception e) {
            throw new APIManagementException("Error initializing Federated API Discovery connector", e);
        }
    }

    @Override
    public Map<String, List<DiscoveredAPI>> discoverExternalAPIs(Environment environment, String organization)
            throws APIManagementException {
        FederatedAPIDiscovery discovery = getFederatedAPIDiscovery(environment, organization);
        List<DiscoveredAPI> allAPIs = discovery.discoverMetadata();
        List<DiscoveredAPI> newAPIs = new ArrayList<>();
        List<DiscoveredAPI> updatedAPIs = new ArrayList<>();
        Map<String, List<DiscoveredAPI>> categorizedApis = new HashMap<>();
        categorizedApis.put("NEW", newAPIs);
        categorizedApis.put("UPDATE", updatedAPIs);
        if (allAPIs == null || allAPIs.isEmpty()) {
            return categorizedApis;
        }
        try {
            String adminUsername = APIUtil.getTenantAdminUserName(organization);
            FederatedGatewayUtil.startTenantFlow(organization, adminUsername);
            Map<String, Map<String, ApiResult>> alreadyAvailableAPIs =
                    FederatedGatewayUtil.getDiscoveredAPIsFromFederatedGateway(environment, organization);
            List<String> alreadyDiscoveredAPIsList =
                    new ArrayList<>(alreadyAvailableAPIs.get(DISCOVERED_API_LIST).keySet());
            List<String> publishedAPIsList =
                    new ArrayList<>(alreadyAvailableAPIs.get(PUBLISHED_API_LIST).keySet());
            Map<String, ApiResult> allTrackedAPIs = new ConcurrentHashMap<>();
            allTrackedAPIs.putAll(alreadyAvailableAPIs.get(DISCOVERED_API_LIST));
            allTrackedAPIs.putAll(alreadyAvailableAPIs.get(PUBLISHED_API_LIST));

            List<String> discoveredAPIsFromFederatedGW = new ArrayList<>();

            for (DiscoveredAPI discoveredAPI : allAPIs) {
                if (discoveredAPI == null || discoveredAPI.getApi() == null) {
                    continue;
                }

                String apiKey = discoveredAPI.getApi().getId().getApiName() + DELEM_COLON
                        + discoveredAPI.getApi().getId().getVersion();
                String envScopedKey = discoveredAPI.getApi().getId().getApiName() + APIConstants.KEY_SEPARATOR
                        + environment.getName() + DELEM_COLON + discoveredAPI.getApi().getId().getVersion();

                // Track gateway APIs to identify deleted ones
                discoveredAPIsFromFederatedGW.add(apiKey);
                discoveredAPIsFromFederatedGW.add(envScopedKey);

                boolean isExists = alreadyDiscoveredAPIsList.contains(apiKey)
                        || alreadyDiscoveredAPIsList.contains(envScopedKey)
                        || publishedAPIsList.contains(apiKey)
                        || publishedAPIsList.contains(envScopedKey);
                if (!isExists) {
                    stripHeavyDefinition(discoveredAPI);
                    newAPIs.add(discoveredAPI);
                    continue;
                }
                String matchedKey = null;
                if (allTrackedAPIs.containsKey(envScopedKey)) {
                    matchedKey = envScopedKey;
                } else if (allTrackedAPIs.containsKey(apiKey)) {
                    matchedKey = apiKey;
                }
                if (matchedKey != null) {
                    ApiResult wso2ApiResult = allTrackedAPIs.get(matchedKey);
                    String existingReferenceArtifact = getReferenceObjectForExistingAPIs(environment, wso2ApiResult);

                    if (discovery.isAPIUpdated(existingReferenceArtifact, discoveredAPI.getReferenceArtifact())) {
                        stripHeavyDefinition(discoveredAPI);
                        updatedAPIs.add(discoveredAPI);
                    }
                }
            }

            // Cleanup orphaned APIs (previously discovered but no longer on the gateway)
            for (String apiName : alreadyDiscoveredAPIsList) {
                if (!discoveredAPIsFromFederatedGW.contains(apiName)) {
                    try {
                        String apiUUID = FederatedGatewayUtil.getAPIUUID(apiName, adminUsername, organization);
                        if (apiUUID != null) {
                            FederatedGatewayUtil.deleteDeployment(apiUUID, organization, environment);
                            if (log.isDebugEnabled()) {
                                log.debug("Automatically cleaned up orphaned API deployment: " + apiName
                                        + " (UUID: " + apiUUID + ") from environment: " + environment.getName());
                            }
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("API UUID not found for: " + apiName
                                        + ". Skipping removal from environment: " + environment.getName());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to delete revision for API: " + apiName + " from environment: "
                                + environment.getName(), e);
                    }
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return categorizedApis;
    }

    private void stripHeavyDefinition(DiscoveredAPI discoveredAPI) {
        if (discoveredAPI != null && discoveredAPI.getApi() != null) {
            discoveredAPI.getApi().setSwaggerDefinition(null);
        }
    }

    @Override
    public void importNewExternalAPIs(List<String> apiIds, Environment environment, String organization)
            throws APIManagementException {
        FederatedAPIDiscovery discovery = getFederatedAPIDiscovery(environment, organization);
        String adminUsername = APIUtil.getTenantAdminUserName(organization);
        FederatedGatewayUtil.startTenantFlow(organization, adminUsername);
        try {
            ParsedApiIdentifiers parsed = parseApiIdentifiers(apiIds);
            List<String> realApiIds = parsed.getRealApiIds();
            Map<String, String> customDisplayNames = parsed.getCustomDisplayNames();
            Map<String, String> customDescriptions = parsed.getCustomDescriptions();

            // Fetch the specific list of DiscoveredAPIs from the connector.
            List<DiscoveredAPI> allDiscoveredAPIs = discovery.discoverAPI(realApiIds);
            // Build a lookup keyed by UUID and name:version so the requested IDs can be matched by any of them.
            Map<String, DiscoveredAPI> apiLookup = buildApiLookup(allDiscoveredAPIs);

            Map<String, Map<String, ApiResult>> alreadyAvailableAPIs =
                    FederatedGatewayUtil.getDiscoveredAPIsFromFederatedGateway(environment, organization);
            Map<String, ApiResult> alreadyDiscoveredAPIMap = alreadyAvailableAPIs.get(DISCOVERED_API_LIST);
            List<String> alreadyDiscoveredAPIsList = new ArrayList<>(alreadyDiscoveredAPIMap.keySet());

            ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
            for (String apiId : realApiIds) {
                try {
                    DiscoveredAPI discoveredAPI = apiLookup.get(apiId);
                    if (discoveredAPI == null) {
                        log.error("Could not find discovered API matching ID: " + apiId + ". Skipping.");
                        continue;
                    }
                    API api = discoveredAPI.getApi();
                    APIDTO apidto = fromAPItoDTO(api);
                    applyCustomMetadata(api, apidto, apiId, customDisplayNames, customDescriptions);
                    applyDefaultPoliciesIfMissing(apidto);
                    apidto.setInitiatedFromGateway(true);

                    String apiKey = apidto.getName() + DELEM_COLON + apidto.getVersion();
                    String envScopedKey = apidto.getName() + APIConstants.KEY_SEPARATOR
                            + environment.getName() + DELEM_COLON + apidto.getVersion();

                    boolean update = false;
                    boolean isNewVersion = false;
                    String existingAPI = null;
                    boolean alreadyExistsWithEnvScope = alreadyDiscoveredAPIsList.contains(envScopedKey);

                    if (!alreadyDiscoveredAPIsList.contains(apiKey) && !alreadyExistsWithEnvScope) {
                        String envPathName = apidto.getName() + APIConstants.KEY_SEPARATOR
                                + environment.getName();
                        Optional<String> existingApiOpt = detectExistingVersion(alreadyDiscoveredAPIsList,
                                apidto.getName(), envPathName, apidto.getVersion());
                        isNewVersion = existingApiOpt.isPresent();
                        existingAPI = existingApiOpt.orElse(null);
                    }

                    if (alreadyExistsWithEnvScope) {
                        if (api.getDisplayName() == null) {
                            apidto.displayName(apidto.getName());
                        }
                        apidto.setName(apidto.getName() + APIConstants.KEY_SEPARATOR + environment.getName());
                    }

                    API newAPI = null;
                    if (isNewVersion) {
                        String existingApiUUID = FederatedGatewayUtil.getAPIUUID(existingAPI, adminUsername,
                                organization);
                        if (existingApiUUID != null) {
                            newAPI = FederatedGatewayUtil.createNewAPIVersion(existingApiUUID, apidto.getVersion(),
                                    organization);
                            update = true;
                        }
                    }

                    // Build deployment ZIP
                    InputStream apiZip = buildApiDeploymentZip(apidto, api, environment);

                    // Import API
                    ImportedAPIDTO importedApi = importExportAPI.importAPI(apiZip, false,
                            true, update, true,
                            new String[]{APIConstants.APIM_PUBLISHER_SCOPE, APIConstants.APIM_CREATOR_SCOPE},
                            organization);

                    // Record the mapping using the connector's own reference artifact
                    if (update) {
                        if (newAPI != null) {
                            APIUtil.addApiExternalApiMapping(newAPI.getUuid(),
                                    environment.getUuid(), discoveredAPI.getReferenceArtifact());
                        } else {
                            APIUtil.updateApiExternalApiMapping(importedApi.getApi().getUuid(),
                                    environment.getUuid(), discoveredAPI.getReferenceArtifact());
                        }
                    } else {
                        APIUtil.addApiExternalApiMapping(importedApi.getApi().getUuid(),
                                environment.getUuid(), discoveredAPI.getReferenceArtifact());
                    }
                    log.info("Successfully imported new API: " + api.getId().getApiName()
                            + " from environment: " + environment.getName());

                    // Delete the API from discovery cache to avoid duplicates in the UI
                    ApiMgtDAO.getInstance().deleteFederatedDiscoveryCacheEntry(environment.getName(), organization,
                            apiId);
                } catch (Exception e) {
                    log.error("Error importing API with ID: " + apiId
                            + " from environment: " + environment.getName(), e);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public void updateExternalAPIs(List<String> apiIds, Environment environment, String organization)
            throws APIManagementException {
        FederatedAPIDiscovery discovery = getFederatedAPIDiscovery(environment, organization);
        String adminUsername = APIUtil.getTenantAdminUserName(organization);
        FederatedGatewayUtil.startTenantFlow(organization, adminUsername);
        try {
            ParsedApiIdentifiers parsed = parseApiIdentifiers(apiIds);
            List<String> realApiIds = parsed.getRealApiIds();
            Map<String, String> customDisplayNames = parsed.getCustomDisplayNames();
            Map<String, String> customDescriptions = parsed.getCustomDescriptions();

            List<DiscoveredAPI> allDiscoveredAPIs = discovery.discoverAPI(realApiIds);
            Map<String, DiscoveredAPI> apiLookup = buildApiLookup(allDiscoveredAPIs);

            ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
            for (String apiId : realApiIds) {
                try {
                    DiscoveredAPI discoveredAPI = apiLookup.get(apiId);
                    if (discoveredAPI == null) {
                        log.error("Could not find discovered API matching ID: " + apiId + ". Skipping.");
                        continue;
                    }
                    API api = discoveredAPI.getApi();
                    APIDTO apidto = fromAPItoDTO(api);
                    applyCustomMetadata(api, apidto, apiId, customDisplayNames, customDescriptions);
                    applyDefaultPoliciesIfMissing(apidto);
                    apidto.setInitiatedFromGateway(true);

                    // Build deployment ZIP
                    InputStream apiZip = buildApiDeploymentZip(apidto, api, environment);

                    // Import as UPDATE (update=true)
                    ImportedAPIDTO importedApi = importExportAPI.importAPI(apiZip, false,
                            true, true, true,
                            new String[]{APIConstants.APIM_PUBLISHER_SCOPE, APIConstants.APIM_CREATOR_SCOPE},
                            organization);

                    // Update the mapping using the connector's own reference artifact
                    APIUtil.updateApiExternalApiMapping(importedApi.getApi().getUuid(),
                            environment.getUuid(), discoveredAPI.getReferenceArtifact());
                    log.info("Successfully updated API: " + api.getId().getApiName()
                            + " from environment: " + environment.getName());

                    // Delete the API from discovery cache to avoid duplicates in the UI
                    ApiMgtDAO.getInstance().deleteFederatedDiscoveryCacheEntry(environment.getName(), organization,
                            apiId);
                } catch (Exception e) {
                    log.error("Error updating API with ID: " + apiId
                            + " from environment: " + environment.getName(), e);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Parses the incoming API identifiers. Each identifier may be either a plain ID/UUID or a JSON object of the
     * form {@code {"id":"...","displayName":"...","description":"..."}} that carries custom metadata supplied from
     * the UI. Any custom metadata is collected keyed by the resolved real ID.
     *
     * @param apiIds the raw identifiers received from the caller
     * @return a holder containing the resolved IDs and any custom display names / descriptions
     */
    private ParsedApiIdentifiers parseApiIdentifiers(List<String> apiIds) {
        ParsedApiIdentifiers parsed = new ParsedApiIdentifiers();
        for (String apiId : apiIds) {
            String realId = apiId;
            if (apiId != null && apiId.trim().startsWith("{") && apiId.trim().endsWith("}")) {
                try {
                    JsonObject json = new JsonParser().parse(apiId).getAsJsonObject();
                    if (json.has("id")) {
                        realId = json.get("id").getAsString();
                    }
                    if (json.has("displayName")) {
                        parsed.customDisplayNames.put(realId, json.get("displayName").getAsString());
                    }
                    if (json.has("description")) {
                        parsed.customDescriptions.put(realId, json.get("description").getAsString());
                    }
                } catch (Exception e) {
                    log.error("Error parsing JSON apiId: " + apiId, e);
                }
            }
            parsed.realApiIds.add(realId);
        }
        return parsed;
    }

    /**
     * Builds a lookup map for discovered APIs keyed by both the gateway UUID and the composite "name:version" key,
     * so a requested identifier can be matched by any of them. Name-only indexing is intentionally avoided to
     * prevent clashes between different versions.
     *
     * @param discoveredAPIs the APIs returned by the connector
     * @return a lookup map from identifier to {@link DiscoveredAPI}
     */
    private Map<String, DiscoveredAPI> buildApiLookup(List<DiscoveredAPI> discoveredAPIs) {
        Map<String, DiscoveredAPI> apiLookup = new HashMap<>();
        for (DiscoveredAPI discovered : discoveredAPIs) {
            API api = discovered.getApi();
            // Index by UUID (the gateway's native ID)
            if (api.getUuid() != null) {
                apiLookup.put(api.getUuid(), discovered);
            }
            // Index by name:version (composite key fallback)
            apiLookup.put(api.getId().getApiName() + DELEM_COLON + api.getId().getVersion(), discovered);
        }
        return apiLookup;
    }

    /**
     * Applies any caller-supplied custom display name and description to both the API model and its DTO. Applying
     * it to the model as well keeps subsequent checks (such as the environment-scoped display-name check) behaving
     * exactly as before.
     *
     * @param api                the API model
     * @param apidto             the API DTO
     * @param apiId              the resolved API identifier used to look up custom values
     * @param customDisplayNames map of custom display names keyed by API identifier
     * @param customDescriptions map of custom descriptions keyed by API identifier
     */
    private void applyCustomMetadata(API api, APIDTO apidto, String apiId,
                                     Map<String, String> customDisplayNames,
                                     Map<String, String> customDescriptions) {
        if (customDisplayNames.containsKey(apiId)) {
            String displayName = customDisplayNames.get(apiId);
            api.setDisplayName(displayName);
            apidto.setDisplayName(displayName);
        }
        if (customDescriptions.containsKey(apiId)) {
            String description = customDescriptions.get(apiId);
            api.setDescription(description);
            apidto.setDescription(description);
        }
    }

    /**
     * Assigns the default subscription-less policy to the API when no policy has been set.
     *
     * @param apidto the API DTO to inspect and (if required) update
     */
    private void applyDefaultPoliciesIfMissing(APIDTO apidto) {
        if (apidto.getPolicies() == null || apidto.getPolicies().isEmpty()) {
            apidto.setPolicies(Collections.singletonList(DEFAULT_SUB_POLICY_SUBSCRIPTIONLESS));
        }
    }

    /**
     * Determines whether the API being processed is a new version of an already discovered API (i.e. an existing
     * entry shares the same name but has a different version).
     *
     * @param alreadyDiscoveredAPIsList the existing discovered API keys in "name:version" form
     * @param apiName                   the plain API name
     * @param envScopedName             the environment-scoped API name (name--envName)
     * @param version                   the version of the API being processed
     * @return an {@link Optional} holding the matched "name:version" key, or empty if none match
     */
    private Optional<String> detectExistingVersion(List<String> alreadyDiscoveredAPIsList, String apiName,
                                                   String envScopedName, String version) {
        return alreadyDiscoveredAPIsList.stream()
                .map(String::trim)
                .map(entry -> {
                    int idx = entry.lastIndexOf(DELEM_COLON);
                    if (idx <= 0 || idx >= entry.length() - 1) {
                        return null;
                    }
                    return new String[]{entry.substring(0, idx), entry.substring(idx + 1)};
                })
                .filter(Objects::nonNull)
                .filter(parts -> (parts[0].equals(apiName) || parts[0].equals(envScopedName))
                        && !parts[1].equals(version))
                .map(parts -> parts[0] + DELEM_COLON + parts[1])
                .findFirst();
    }

    /**
     * Serialises the given API DTO into the deployment archive expected by the import/export layer.
     *
     * @param apidto      the API DTO to serialise
     * @param api         the backing API model (used for the swagger definition)
     * @param environment the target environment (used to build the deployment descriptor)
     * @return the deployment archive as an {@link InputStream}
     * @throws APIManagementException if the archive cannot be created
     * @throws IOException            if an I/O error occurs while building the archive
     */
    private InputStream buildApiDeploymentZip(APIDTO apidto, API api, Environment environment)
            throws APIManagementException, IOException {
        JsonObject apiJson = (JsonObject) new Gson().toJsonTree(apidto);
        apiJson = CommonUtil.addTypeAndVersionToFile(ImportExportConstants.TYPE_API,
                ImportExportConstants.APIM_VERSION, apiJson);
        return FederatedGatewayUtil.createZipAsInputStream(
                apiJson.toString(), api.getSwaggerDefinition(),
                FederatedGatewayUtil.createDeploymentYaml(environment),
                apidto.getName());
    }

    /**
     * Simple holder for the result of {@link #parseApiIdentifiers(List)}.
     */
    private static class ParsedApiIdentifiers {
        private final List<String> realApiIds = new ArrayList<>();
        private final Map<String, String> customDisplayNames = new HashMap<>();
        private final Map<String, String> customDescriptions = new HashMap<>();

        private List<String> getRealApiIds() {
            return realApiIds;
        }

        private Map<String, String> getCustomDisplayNames() {
            return customDisplayNames;
        }

        private Map<String, String> getCustomDescriptions() {
            return customDescriptions;
        }
    }
}
