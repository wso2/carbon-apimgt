/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.federated.gateway;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.federated.gateway.util.FederatedGatewayUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

import static org.wso2.carbon.apimgt.impl.APIConstants.DELEM_COLON;
import static org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil.fromAPItoDTO;
import static org.wso2.carbon.federated.gateway.util.FederatedGatewayConstants.DISCOVERED_API_LIST;
import static org.wso2.carbon.federated.gateway.util.FederatedGatewayConstants.PUBLISHED_API_LIST;

/**
 * This class is responsible for scheduling and executing the discovery of APIs in a federated gateway environment.
 * It uses a scheduled executor service to periodically discover APIs and process them accordingly.
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
                    FederatedAPIDiscovery federatedAPIDiscovery = (FederatedAPIDiscovery)
                            Class.forName(gatewayConfiguration.getDiscoveryImplementation())
                                    .getDeclaredConstructor().newInstance();
                    log.info("Initializing federated API discovery for environment: " + environment.getName()
                            + " and organization: " + organization);
                    federatedAPIDiscovery.init(environment, organization);
                    String taskKey = "FederatedAPIDiscovery" + DELEM_COLON + environment.getName() + DELEM_COLON
                            + organization;
                    ScheduledFuture<?> scheduledFuture = scheduledDiscoveryTasks.get(taskKey);
                    // Cancel existing task if one exists
                    if (scheduledFuture != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Cancelling existing discovery task for: " + taskKey + " to reschedule.");
                        }
                        scheduledFuture.cancel(true);
                        scheduledDiscoveryTasks.remove(taskKey);
                    }
                    int scheduleWindow = environment.getApiDiscoveryScheduledWindow();
                    if (GatewayMode.WRITE_ONLY.getMode().equals(environment.getMode()) || scheduleWindow <= 0) {
                        log.info("Federated API discovery is disabled for environment: " + environment.getName());
                    } else {
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
                                                log.debug("Discovered " + discoveredAPIs.size() +
                                                        " APIs for environment: " + environment.getName() +
                                                        " and organization: " + organization);
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
                                    + environment.getName() + " in organization: " + organization +
                                    " with interval: " + scheduleWindow + " minutes");
                        }
                    }
                } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                         NoSuchMethodException | InvocationTargetException | APIManagementException e) {
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
                API api = discoveredAPI.getApi();
                try {
                    String apiKey = apidto.getName() + DELEM_COLON + apidto.getVersion();
                    String envScopedKey = apidto.getName() + APIConstants.DELEM_UNDERSCORE
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
                        String envPathName = apidto.getName() + APIConstants.DELEM_UNDERSCORE
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
                        apidto.setName(apidto.getName() + APIConstants.DELEM_UNDERSCORE + environment.getName());
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
}
