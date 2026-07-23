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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of the Federated API Discovery REST endpoints.
 *
 * <p><b>Async Discovery Pattern:</b>
 * <ol>
 *   <li>POST /federated-apis/discover → checks the DB for an active task for this env+org.
 *       If one exists (de-duplication), the same task ID is returned immediately with HTTP 202.</li>
 *   <li>Otherwise a new task is persisted as PENDING in the DB, submitted to the
 *       shared {@link #DISCOVERY_EXECUTOR}, and HTTP 202 is returned with the new task ID.</li>
 *   <li>The background worker calls {@code discoverExternalAPIs}, converts the result,
 *       <b>persists it to the AM_FEDERATED_DISCOVERY_CACHE DB table</b>, and marks status COMPLETED
 *       (or FAILED on error).</li>
 *   <li>GET /federated-apis/status/{taskId} → reads task status from the DB and returns it.
 *       When COMPLETED, results are read from the DB cache.</li>
 *   <li>GET /federated-apis/cached?environment=X → returns previously cached results from the DB
 *       without triggering a new gateway call. Includes lastDiscoveredAt timestamp.</li>
 * </ol>
 *
 * <p>All task state and discovery results are persisted to the database, ensuring they survive
 * page reloads, server restarts, and are available across cluster nodes.</p>
 */
public class FederatedApisApiServiceImpl implements FederatedApisApiService {

    private static final Log log = LogFactory.getLog(FederatedApisApiServiceImpl.class);

    // -----------------------------------------------------------------------
    // Task status constants
    // -----------------------------------------------------------------------
    private static final String STATUS_PENDING   = "PENDING";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED    = "FAILED";
    private static final long PENDING_TASK_TIMEOUT_MS = 300000; // 5 minutes

    // -----------------------------------------------------------------------
    // Background executor for discovery work
    // -----------------------------------------------------------------------

    private static final ExecutorService DISCOVERY_EXECUTOR =
            Executors.newFixedThreadPool(5, r -> {
                Thread t = new Thread(r, "federated-api-discovery-worker");
                t.setDaemon(true);
                return t;
            });

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

        // --- DB-backed De-duplication check ----------------------------------
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        Map<String, Object> activeTask = dao.getActiveDiscoveryTask(environment, organization);
        if (activeTask != null) {
            String dbTaskId = (String) activeTask.get("taskId");
            String dbStatus = (String) activeTask.get("status");
            java.sql.Timestamp updatedAt = (java.sql.Timestamp) activeTask.get("updatedAt");

            if (STATUS_PENDING.equals(dbStatus) && updatedAt != null) {
                long elapsed = System.currentTimeMillis() - updatedAt.getTime();
                if (elapsed < PENDING_TASK_TIMEOUT_MS) {
                    log.info("Discovery already in progress in the DB cluster for env [" + environment
                            + "] org [" + organization + "], returning existing taskId: " + dbTaskId);
                    Map<String, Object> m = new HashMap<>();
                    m.put("taskId", dbTaskId);
                    m.put("status", STATUS_PENDING);
                    return Response.accepted(m).build();
                }
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

        // Persist task status as PENDING in DB
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        ApiMgtDAO.getInstance().updateDiscoveryTaskStatus(
                environment, organization, taskId, STATUS_PENDING, null, now);

        // --- Atomic guard: verify we won the race -----------------------------
        Map<String, Object> currentTask = dao.getActiveDiscoveryTask(environment, organization);
        String currentTaskId = currentTask != null ? (String) currentTask.get("taskId") : null;
        if (!taskId.equals(currentTaskId)) {
            // Another concurrent request won — return theirs instead
            String existingStatus = currentTask != null ? (String) currentTask.get("status") : null;
            log.info("Discovery task for env [" + environment + "] org [" + organization
                    + "] was claimed by another request. Returning existing taskId: " + currentTaskId);
            Map<String, Object> m = new HashMap<>();
            m.put("taskId", currentTaskId);
            m.put("status", existingStatus != null ? existingStatus : STATUS_PENDING);
            return Response.accepted(m).build();
        }

        // --- Submit to background worker --------------------------------------
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        DISCOVERY_EXECUTOR.submit(() ->
                runDiscovery(taskId, env, organization, tenantId, tenantDomain));

        log.info("Federated API discovery task [" + taskId + "] submitted for env ["
                + environment + "] org [" + organization + "]");

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("taskId", taskId);
        responseMap.put("status", STATUS_PENDING);
        return Response.accepted(responseMap).build();
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

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        // Parse environment name from taskId
        int lastUnderscore = taskId.lastIndexOf('_');
        if (lastUnderscore <= 0) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Invalid task ID format: " + taskId + "\"}")
                    .build();
        }
        String environment = taskId.substring(0, lastUnderscore);

        // Fetch task status from database
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        Map<String, String> statusMap = dao.getDiscoveryTaskStatus(environment, organization, taskId);

        if (statusMap == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Task not found, expired, or superseded: " + taskId + "\"}")
                    .build();
        }

        String status = statusMap.get("status");
        String error = statusMap.get("error");

        Map<String, Object> m = new HashMap<>();
        m.put("taskId", taskId);
        m.put("status", status);
        m.put("environment", environment);

        if (STATUS_COMPLETED.equals(status)) {
            List<Map<String, Object>> cachedApis = dao.getFederatedDiscoveryCache(environment, organization);
            m.put("result", cachedApis);
            java.sql.Timestamp lastDiscovered = dao.getLastFederatedDiscoveryTime(environment, organization);
            m.put("lastDiscoveredAt", lastDiscovered != null ? lastDiscovered.toInstant().toString() : null);
        } else if (STATUS_FAILED.equals(status) && error != null) {
            m.put("error", error);
        }

        return Response.ok(m).build();
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
            List<String> failedIds = service.importNewExternalAPIs(requestBody, env, organization);
            if (failedIds.isEmpty()) {
                return Response.ok("{\"status\": \"APIs imported successfully\"}").build();
            }
            String json = "{\"status\": \"APIs imported with some failures\","
                    + "\"failedIds\": [" + failedIds.stream()
                    .map(id -> "\"" + id.replace("\"", "\\\"") + "\"")
                    .collect(java.util.stream.Collectors.joining(",")) + "]}";
            return Response.ok(json).build();
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
            List<String> failedIds = service.updateExternalAPIs(requestBody, env, organization);
            if (failedIds.isEmpty()) {
                return Response.ok("{\"status\": \"APIs updated successfully\"}").build();
            }
            String json = "{\"status\": \"APIs updated with some failures\","
                    + "\"failedIds\": [" + failedIds.stream()
                    .map(id -> "\"" + id.replace("\"", "\\\"") + "\"")
                    .collect(java.util.stream.Collectors.joining(",")) + "]}";
            return Response.ok(json).build();
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
    private void runDiscovery(String taskId, Environment env, String organization,
            int tenantId, String tenantDomain) {
        try {
            // Restore Carbon context on the worker thread
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);

            FederatedAPIDiscoveryService service = getFederatedDiscoveryService();
            if (service == null) {
                throw new IllegalStateException("FederatedAPIDiscoveryService OSGi service is not available.");
            }

            log.debug("Discovery worker starting for task [" + taskId + "] env ["
                    + env.getName() + "]");

            Map<String, List<DiscoveredAPI>> discovered = service.discoverExternalAPIs(env, organization);
            List<Map<String, Object>> result = convertToListOfMaps(discovered, env);

            // Persist results to the DB cache
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            ApiMgtDAO.getInstance().saveFederatedDiscoveryCache(
                    env.getName(), organization, result, now);

            // Update status to COMPLETED in DB
            ApiMgtDAO.getInstance().updateDiscoveryTaskStatus(
                    env.getName(), organization, taskId, STATUS_COMPLETED, null, now);

            log.info("Discovery task [" + taskId + "] completed with "
                    + result.size() + " APIs. Results persisted to DB cache.");

        } catch (Exception e) {
            log.error("Discovery task [" + taskId + "] failed: " + e.getMessage(), e);
            try {
                java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
                ApiMgtDAO.getInstance().updateDiscoveryTaskStatus(
                        env.getName(), organization, taskId, STATUS_FAILED, e.getMessage(), now);
            } catch (Exception ex) {
                log.error("Failed to update discovery task status to FAILED in DB for task: " + taskId, ex);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
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


}
