/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.FederatedAPIDiscoveryService;
import org.wso2.carbon.apimgt.api.model.DiscoveredAPI;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.FederatedApisApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the Federated API Discovery REST endpoints.
 *
 * <p><b>Async Discovery Pattern (sequence diagram):</b>
 * <ol>
 *   <li>POST /federated-apis/discover → checks {@code ACTIVE_TASK_BY_ENV} for a running task.
 *       If one exists (de-duplication), the same task ID is returned immediately with HTTP 202.</li>
 *   <li>Otherwise a new {@link DiscoveryTask} is created, registered in both
 *       {@code TASK_STORE} and {@code ACTIVE_TASK_BY_ENV}, submitted to the
 *       shared {@link #DISCOVERY_EXECUTOR} and HTTP 202 is returned with the new task ID.</li>
 *   <li>The background worker calls {@code discoverExternalAPIs}, converts the result,
 *       <b>persists it to the AM_FEDERATED_DISCOVERY_CACHE DB table</b>, and marks status COMPLETED
 *       (or FAILED on error). It also evicts itself from {@code ACTIVE_TASK_BY_ENV}.</li>
 *   <li>GET /federated-apis/status/{taskId} → reads from {@code TASK_STORE} and returns the
 *       current status. When COMPLETED, results are read from the DB cache.</li>
 *   <li>GET /federated-apis/cached?environment=X → returns previously cached results from the DB
 *       without triggering a new gateway call. Includes lastDiscoveredAt timestamp.</li>
 * </ol>
 *
 * <p><b>Key change:</b> Discovery results are no longer held in JVM-local {@link ConcurrentHashMap}
 * objects. They are persisted to the {@code AM_FEDERATED_DISCOVERY_CACHE} table so they survive
 * page reloads, server restarts, and are available across cluster nodes.
 * The in-memory maps ({@code TASK_STORE}, {@code ACTIVE_TASK_BY_ENV}) are only used for
 * ephemeral, in-flight task tracking and de-duplication.
 */
public class FederatedApisApiServiceImpl implements FederatedApisApiService {

    private static final Log log = LogFactory.getLog(FederatedApisApiServiceImpl.class);

    // -----------------------------------------------------------------------
    // Task status constants
    // -----------------------------------------------------------------------
    private static final String STATUS_PENDING   = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED    = "FAILED";

    /**
     * TTL after which completed/failed tasks are evicted from the store (5 minutes).
     * The UI should poll well within this window.
     */
    private static final long TASK_TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);

    // -----------------------------------------------------------------------
    // Shared, JVM-wide state (ephemeral — only tracks in-flight tasks)
    // -----------------------------------------------------------------------

    /**
     * Primary task store: taskId → DiscoveryTask.
     * Read by GET /status/{taskId}, written by POST /discover and the worker.
     * Results are NOT stored here — they go to the DB.
     */
    private static final ConcurrentHashMap<String, DiscoveryTask> TASK_STORE =
            new ConcurrentHashMap<>();

    /**
     * Active-task index: "tenantOrg|envName" → taskId.
     * Enables O(1) de-duplication. Evicted by the worker on completion/failure.
     */
    private static final ConcurrentHashMap<String, String> ACTIVE_TASK_BY_ENV =
            new ConcurrentHashMap<>();

    /**
     * Background executor for discovery work.
     */
    private static final ExecutorService DISCOVERY_EXECUTOR =
            Executors.newFixedThreadPool(5, r -> {
                Thread t = new Thread(r, "federated-api-discovery-worker");
                t.setDaemon(true);
                return t;
            });

    /**
     * Active cleanup scheduler — runs every 5 minutes and removes expired tasks.
     */
    static {
        ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "federated-api-discovery-cleaner");
            t.setDaemon(true);
            return t;
        });
        cleaner.scheduleAtFixedRate(() -> {
            try {
                TASK_STORE.forEach((taskId, task) -> {
                    if (task.isExpired()) {
                        TASK_STORE.remove(taskId);
                        String envKey = task.organization + "|" + task.environment;
                        ACTIVE_TASK_BY_ENV.remove(envKey, taskId);
                    }
                });
            } catch (Exception e) {
                LogFactory.getLog(FederatedApisApiServiceImpl.class)
                        .error("Error during stale discovery task cleanup", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    // -----------------------------------------------------------------------
    // Endpoints
    // -----------------------------------------------------------------------

    /**
     * POST /federated-apis/discover
     *
     * <p>Submits an async discovery task and returns HTTP 202 immediately.
     * De-duplicates: if a task for the same environment is already PENDING,
     * the existing task ID is returned without submitting a duplicate.
     */
    @Override
    public Response discoverFederatedAPIs(String environment, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String envKey = organization + "|" + environment;

        // --- Evict stale completed/failed entries so a fresh run can start ----
        evictStaleTask(envKey);

        // --- De-duplication check --------------------------------------------
        String existingTaskId = ACTIVE_TASK_BY_ENV.get(envKey);
        if (existingTaskId != null) {
            DiscoveryTask existing = TASK_STORE.get(existingTaskId);
            if (existing != null && STATUS_PENDING.equals(existing.status)) {
                log.debug("Discovery already in progress for env [" + environment
                        + "] org [" + organization + "], returning existing taskId: " + existingTaskId);
                return Response.accepted(existing.toStatusMap()).build();
            }
        }

        // --- Resolve environment (needs credentials) ---------------------------
        Environment env;
        try {
            env = resolveEnvironment(environment, organization);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Failed to resolve environment: " + environment, e, log);
            return null;
        }

        // --- Create and register new task -------------------------------------
        String taskId = environment + "_" + UUID.randomUUID().toString();
        DiscoveryTask task = new DiscoveryTask(taskId, environment, organization);

        TASK_STORE.put(taskId, task);
        ACTIVE_TASK_BY_ENV.put(envKey, taskId);

        // --- Submit to background worker --------------------------------------
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        DISCOVERY_EXECUTOR.submit(() ->
                runDiscovery(task, env, organization, tenantId, tenantDomain, envKey));

        log.info("Federated API discovery task [" + taskId + "] submitted for env ["
                + environment + "] org [" + organization + "]");

        return Response.accepted(task.toStatusMap()).build();
    }

    /**
     * GET /federated-apis/status/{taskId}
     *
     * <p>Returns the current status of an async discovery task.
     * When status is COMPLETED, results are read from the DB cache.
     */
    @Override
    public Response getDiscoveryTaskStatus(String taskId, MessageContext messageContext)
            throws APIManagementException {

        DiscoveryTask task = TASK_STORE.get(taskId);
        if (task == null) {
            int index = taskId.lastIndexOf('_');
            if (index > 0) {
                String environment = taskId.substring(0, index);
                String organization = RestApiUtil.getValidatedOrganization(messageContext);
                try {
                    Environment env = resolveEnvironment(environment, organization);
                    String envKey = organization + "|" + environment;

                    DiscoveryTask newTask = new DiscoveryTask(taskId, environment, organization);
                    TASK_STORE.put(taskId, newTask);
                    ACTIVE_TASK_BY_ENV.put(envKey, taskId);

                    int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                    String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

                    DISCOVERY_EXECUTOR.submit(() ->
                            runDiscovery(newTask, env, organization, tenantId, tenantDomain, envKey));

                    log.info("Federated API discovery task [" + taskId + "] lazily created on this node for env ["
                            + environment + "] org [" + organization + "]");
                    return Response.ok(newTask.toResponseMap(organization)).build();
                } catch (Exception e) {
                    log.error("Failed to lazily create discovery task for taskId: " + taskId, e);
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\": \"Task not found or has expired: " + taskId + "\"}")
                            .build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Task not found or has expired: " + taskId + "\"}")
                        .build();
            }
        }
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        return Response.ok(task.toResponseMap(organization)).build();
    }

    /**
     * POST /federated-apis/import
     */
    @Override
    public Response importFederatedAPIs(String environment, List<String> requestBody,
            MessageContext messageContext) throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Environment env = resolveEnvironment(environment, organization);
            FederatedAPIDiscoveryService service = getFederatedDiscoveryService();
            if (service == null) {
                throw new APIManagementException("FederatedAPIDiscoveryService OSGi service is not available.");
            }
            service.importNewExternalAPIs(requestBody, env, organization);
            return Response.ok("{\"status\": \"APIs imported successfully\"}").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while importing federated APIs for environment: " + environment, e, log);
            return null;
        }
    }

    /**
     * POST /federated-apis/update
     */
    @Override
    public Response updateFederatedAPIs(String environment, List<String> requestBody,
            MessageContext messageContext) throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Environment env = resolveEnvironment(environment, organization);
            FederatedAPIDiscoveryService service = getFederatedDiscoveryService();
            if (service == null) {
                throw new APIManagementException("FederatedAPIDiscoveryService OSGi service is not available.");
            }
            service.updateExternalAPIs(requestBody, env, organization);
            return Response.ok("{\"status\": \"APIs updated successfully\"}").build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while updating federated APIs for environment: " + environment, e, log);
            return null;
        }
    }

    /**
     * GET /federated-apis/cached?environment=X
     *
     * <p>Returns previously cached discovery results from the DB for the given environment,
     * WITHOUT triggering a new gateway call. Includes lastDiscoveredAt timestamp.
     */
    @Override
    public Response getCachedDiscoveryResults(String environment, MessageContext messageContext)
            throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            ApiMgtDAO dao = ApiMgtDAO.getInstance();

            List<Map<String, Object>> cachedApis = dao.getFederatedDiscoveryCache(environment, organization);
            java.sql.Timestamp lastDiscovered = dao.getLastFederatedDiscoveryTime(environment, organization);

            Map<String, Object> response = new HashMap<>();
            response.put("environment", environment);
            response.put("result", cachedApis);
            response.put("lastDiscoveredAt", lastDiscovered != null
                    ? lastDiscovered.toInstant().toString() : null);
            response.put("status", STATUS_COMPLETED);

            return Response.ok(response).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error fetching cached discovery results for environment: " + environment, e, log);
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Background worker
    // -----------------------------------------------------------------------

    /**
     * Executed on the {@link #DISCOVERY_EXECUTOR} thread.
     *
     * <p>Performs the external network call, converts the result, persists it to the
     * {@code AM_FEDERATED_DISCOVERY_CACHE} DB table, and marks the task as completed.
     */
    private void runDiscovery(DiscoveryTask task, Environment env, String organization,
            int tenantId, String tenantDomain, String envKey) {
        try {
            // Restore Carbon context on the worker thread
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);

            FederatedAPIDiscoveryService service = getFederatedDiscoveryService();
            if (service == null) {
                throw new IllegalStateException("FederatedAPIDiscoveryService OSGi service is not available.");
            }

            log.debug("Discovery worker starting for task [" + task.taskId + "] env ["
                    + env.getName() + "]");

            Map<String, List<DiscoveredAPI>> discovered = service.discoverExternalAPIs(env, organization);
            List<Map<String, Object>> result = convertToListOfMaps(discovered, env);

            // Persist results to the DB cache
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            ApiMgtDAO.getInstance().saveFederatedDiscoveryCache(
                    env.getName(), organization, result, now);

            task.markCompleted(result.size());
            log.info("Discovery task [" + task.taskId + "] completed with "
                    + result.size() + " APIs. Results persisted to DB cache.");

        } catch (Exception e) {
            task.markFailed(e.getMessage());
            log.error("Discovery task [" + task.taskId + "] failed: " + e.getMessage(), e);
        } finally {
            // Always release the active-task lock so the next discovery can proceed
            ACTIVE_TASK_BY_ENV.remove(envKey, task.taskId);
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    // -----------------------------------------------------------------------
    // Helper: evict a stale (completed/failed and past TTL) task from maps
    // -----------------------------------------------------------------------

    private void evictStaleTask(String envKey) {
        String staleId = ACTIVE_TASK_BY_ENV.get(envKey);
        if (staleId == null) {
            return;
        }
        DiscoveryTask stale = TASK_STORE.get(staleId);
        if (stale == null || stale.isExpired()) {
            ACTIVE_TASK_BY_ENV.remove(envKey, staleId);
            if (stale != null) {
                TASK_STORE.remove(staleId);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helper: convert discovered API map to a flat list of response maps
    // -----------------------------------------------------------------------

    private List<Map<String, Object>> convertToListOfMaps(
            Map<String, List<DiscoveredAPI>> categorizedApis, Environment environment) {

        List<Map<String, Object>> result = new ArrayList<>();
        String discoveredAt = Instant.now().toString();

        for (Map.Entry<String, List<DiscoveredAPI>> entry : categorizedApis.entrySet()) {
            String status = entry.getKey();
            for (DiscoveredAPI discoveredAPI : entry.getValue()) {
                Map<String, Object> map = new HashMap<>();
                if (discoveredAPI.getApi() != null) {
                    String generatedId = discoveredAPI.getApi().getUuid();
                    if (generatedId == null && discoveredAPI.getApi().getId() != null) {
                        generatedId = discoveredAPI.getApi().getId().getApiName()
                                + ":" + discoveredAPI.getApi().getId().getVersion();
                    }
                    map.put("id", generatedId);
                    if (discoveredAPI.getApi().getId() != null) {
                        map.put("apiName", discoveredAPI.getApi().getId().getApiName());
                        map.put("version", discoveredAPI.getApi().getId().getVersion());
                    }
                    map.put("description", discoveredAPI.getApi().getDescription());
                    map.put("context", discoveredAPI.getApi().getContext());
                    String apiType = discoveredAPI.getApi().getType();
                    if (apiType == null) {
                        apiType = "HTTP";
                    }
                    map.put("apiType", apiType);
                }
                map.put("gatewayName", environment.getName());
                map.put("gatewayType", environment.getGatewayType());
                map.put("discoveredAt", discoveredAt);
                map.put("status", status);
                // Include reference artifact for DB persistence
                if (discoveredAPI.getReferenceArtifact() != null) {
                    map.put("referenceArtifact", discoveredAPI.getReferenceArtifact());
                }
                result.add(map);
            }
        }
        return result;
    }

    // -----------------------------------------------------------------------
    // Helper: resolve environment with credentials decrypted
    // -----------------------------------------------------------------------

    private Environment resolveEnvironment(String environmentName, String organization)
            throws APIManagementException {
        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        Environment env = environments.get(environmentName);
        if (env == null) {
            throw new APIManagementException("Environment not found: " + environmentName);
        }
        APIAdminImpl apiAdmin = new APIAdminImpl();
        env = apiAdmin.getEnvironmentWithoutPropertyMasking(organization, env.getUuid());
        return apiAdmin.decryptGatewayConfigurationValues(env);
    }

    // -----------------------------------------------------------------------
    // Helper: OSGi service lookup
    // -----------------------------------------------------------------------

    private FederatedAPIDiscoveryService getFederatedDiscoveryService() {
        return (FederatedAPIDiscoveryService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(FederatedAPIDiscoveryService.class, null);
    }

    // -----------------------------------------------------------------------
    // Inner class: DiscoveryTask
    // -----------------------------------------------------------------------

    /**
     * Lightweight value object representing one async discovery job.
     *
     * <p>Results are no longer stored in this object — they are persisted to the DB.
     * This object only tracks task lifecycle (PENDING → COMPLETED/FAILED).
     */
    private static class DiscoveryTask {

        final String taskId;
        final String environment;
        final String organization;
        final long createdAt;

        volatile String status;
        volatile int resultCount;
        volatile String errorMessage;
        volatile long completedAt;

        DiscoveryTask(String taskId, String environment, String organization) {
            this.taskId = taskId;
            this.environment = environment;
            this.organization = organization;
            this.status = STATUS_PENDING;
            this.createdAt = System.currentTimeMillis();
        }

        void markCompleted(int count) {
            this.resultCount = count;
            this.status = STATUS_COMPLETED;
            this.completedAt = System.currentTimeMillis();
        }

        void markFailed(String error) {
            this.errorMessage = error;
            this.status = STATUS_FAILED;
            this.completedAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return !STATUS_PENDING.equals(status)
                    && (System.currentTimeMillis() - completedAt) > TASK_TTL_MILLIS;
        }

        /** Minimal response for the 202 Accepted body. */
        Map<String, Object> toStatusMap() {
            Map<String, Object> m = new HashMap<>();
            m.put("taskId", taskId);
            m.put("status", status);
            return m;
        }

        /** Full response for the GET /status/{taskId} poll endpoint. */
        Map<String, Object> toResponseMap(String organization) {
            Map<String, Object> m = new HashMap<>();
            m.put("taskId", taskId);
            m.put("status", status);
            m.put("environment", environment);
            if (STATUS_COMPLETED.equals(status)) {
                // Read results from DB cache instead of in-memory
                try {
                    ApiMgtDAO dao = ApiMgtDAO.getInstance();
                    List<Map<String, Object>> cachedApis =
                            dao.getFederatedDiscoveryCache(environment, organization);
                    m.put("result", cachedApis);
                    java.sql.Timestamp lastDiscovered =
                            dao.getLastFederatedDiscoveryTime(environment, organization);
                    m.put("lastDiscoveredAt", lastDiscovered != null
                            ? lastDiscovered.toInstant().toString() : null);
                } catch (Exception e) {
                    LogFactory.getLog(FederatedApisApiServiceImpl.class)
                            .error("Error reading cached results for task: " + taskId, e);
                    m.put("result", new ArrayList<>());
                }
            }
            if (STATUS_FAILED.equals(status) && errorMessage != null) {
                m.put("error", errorMessage);
            }
            return m;
        }
    }
}
