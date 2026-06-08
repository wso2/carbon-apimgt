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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.federatedDiscovery.FederatedDiscoveryTaskStore;
import org.wso2.carbon.apimgt.impl.federatedDiscovery.FederatedDiscoveryTaskStore.DiscoveryTask;
import org.wso2.carbon.apimgt.impl.notifier.events.FederatedDiscoverySyncEvent;
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
 *       stores it in the task, and marks status COMPLETED (or FAILED on error).
 *       It also evicts itself from {@code ACTIVE_TASK_BY_ENV} so fresh discoveries can start.</li>
 *   <li>GET /federated-apis/status/{taskId} → reads from {@code TASK_STORE} and returns the
 *       current status plus the API list if COMPLETED.</li>
 * </ol>
 *
 * <p><b>Cluster note:</b> The task store is JVM-local ({@link ConcurrentHashMap}).
 * In a multi-node cluster each node maintains its own store; the UI should POST to the same
 * node it polls (sticky sessions / same LB node), which is the default in WSO2 deployments.
 * To go cluster-wide, replace the two static maps with a Hazelcast IMap — the surrounding
 * logic is identical.
 *
 * <p><b>Thread-pool sizing:</b> A single-thread executor is used deliberately.
 * Discovery is network-bound (one external call per gateway) so parallelism beyond the
 * number of concurrent gateways brings no benefit and only risks resource exhaustion.
 * The de-duplication guard ensures a single admin's repeated clicks never queue more than
 * one pending task per environment.
 */
public class FederatedApisApiServiceImpl implements FederatedApisApiService {

    private static final Log log = LogFactory.getLog(FederatedApisApiServiceImpl.class);

    // -----------------------------------------------------------------------
    // Task status constants
    // -----------------------------------------------------------------------
    static final String STATUS_PENDING   = "PENDING";
    static final String STATUS_COMPLETED = "COMPLETED";
    static final String STATUS_FAILED    = "FAILED";

    /**
     * Unique identifier for this JVM node — used to avoid re-processing events that
     * originated locally.  Falls back to a random UUID if the Carbon node-id property
     * is not set (e.g. single-node dev mode).
     */
    static final String NODE_ID = System.getProperty("carbon.id", UUID.randomUUID().toString());

    /**
     * TTL after which completed/failed tasks are evicted from the store (5 minutes).
     * The UI should poll well within this window.
     */
    private static final long TASK_TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);

    // Shared state now resides in FederatedDiscoveryTaskStore.java in apimgt.impl to prevent circular bundle references

    /**
     * Background executor for discovery work.
     * Fixed pool of 5 threads so multiple environments / tenants can discover concurrently.
     * Discovery is network I/O-bound, so 5 threads cover most real-world multi-tenant deployments
     * without significant resource overhead.
     * Named thread prefix aids log debugging.
     */
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

        // --- Evict stale completed/failed entries so a fresh run can start ----
        evictStaleTask(envKey);

        // --- De-duplication check --------------------------------------------
        String existingTaskId = FederatedDiscoveryTaskStore.ACTIVE_TASK_BY_ENV.get(envKey);
        if (existingTaskId != null) {
            DiscoveryTask existing = FederatedDiscoveryTaskStore.TASK_STORE.get(existingTaskId);
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
        String taskId = UUID.randomUUID().toString();
        DiscoveryTask task = new DiscoveryTask(taskId, environment, organization);

        FederatedDiscoveryTaskStore.TASK_STORE.put(taskId, task);
        FederatedDiscoveryTaskStore.ACTIVE_TASK_BY_ENV.put(envKey, taskId);

        // --- Broadcast PENDING state to sibling CP nodes via JMS --------------
        publishDiscoverySync(taskId, envKey, STATUS_PENDING, organization, null, null);

        // --- Submit to background worker --------------------------------------
        // Carbon context is thread-local; capture the tenant info before jumping threads.
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
     * When status is COMPLETED the full API list is included in the response.
     */
    @Override
    public Response getDiscoveryTaskStatus(String taskId, MessageContext messageContext)
            throws APIManagementException {

        DiscoveryTask task = FederatedDiscoveryTaskStore.TASK_STORE.get(taskId);
        if (task == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Task not found or has expired: " + taskId + "\"}")
                    .build();
        }
        return Response.ok(task.toResponseMap()).build();
    }

    /**
     * POST /federated-apis/import
     *
     * <p>Synchronous: the user has already seen the discovery results and explicitly
     * selected a subset of NEW APIs to import. The network round-trip is to WSO2's own
     * database — fast and predictable.
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
     *
     * <p>Synchronous for the same reason as import.
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

    // -----------------------------------------------------------------------
    // Background worker
    // -----------------------------------------------------------------------

    /**
     * Executed on the {@link #DISCOVERY_EXECUTOR} thread.
     *
     * <p>Establishes a Carbon tenant context (required by DAO/registry calls inside
     * {@code discoverExternalAPIs}), performs the external network call, converts the
     * result and stores it in the task. Always evicts the task from
     * {@code ACTIVE_TASK_BY_ENV} — whether it succeeds or fails — so the admin
     * can trigger a new discovery without being locked out.
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

            task.markCompleted(result);
            log.info("Discovery task [" + task.taskId + "] completed with "
                    + result.size() + " APIs.");

            // Broadcast COMPLETED state (with full result) to sibling CP nodes
            publishDiscoverySync(task.taskId, envKey, STATUS_COMPLETED, organization, result, null);

        } catch (Exception e) {
            task.markFailed(e.getMessage());
            log.error("Discovery task [" + task.taskId + "] failed: " + e.getMessage(), e);

            // Broadcast FAILED state to sibling CP nodes
            publishDiscoverySync(task.taskId, envKey, STATUS_FAILED, organization, null, e.getMessage());
        } finally {
            // Always release the active-task lock so the next discovery can proceed
            FederatedDiscoveryTaskStore.ACTIVE_TASK_BY_ENV.remove(envKey, task.taskId);
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    // -----------------------------------------------------------------------
    // Helper: evict a stale (completed/failed and past TTL) task from maps
    // -----------------------------------------------------------------------

    private void evictStaleTask(String envKey) {
        String staleId = FederatedDiscoveryTaskStore.ACTIVE_TASK_BY_ENV.get(envKey);
        if (staleId == null) {
            return;
        }
        DiscoveryTask stale = FederatedDiscoveryTaskStore.TASK_STORE.get(staleId);
        if (stale == null || stale.isExpired()) {
            FederatedDiscoveryTaskStore.ACTIVE_TASK_BY_ENV.remove(envKey, staleId);
            if (stale != null) {
                FederatedDiscoveryTaskStore.TASK_STORE.remove(staleId);
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
                }
                map.put("gatewayName", environment.getName());
                map.put("gatewayType", environment.getGatewayType());
                map.put("discoveredAt", discoveredAt);
                map.put("status", status);
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
    // JMS event publishing
    // -----------------------------------------------------------------------

    /**
     * Publishes a {@link FederatedDiscoverySyncEvent} to the notification JMS topic.
     * The event is received by {@code FederatedDiscoveryJMSMessageListener} on every sibling
     * CP node, which calls the appropriate {@code applyRemoteTask*()} method below.
     *
     * @param taskId       task UUID
     * @param envKey       "organization|environmentName" composite key
     * @param status       PENDING | COMPLETED | FAILED
     * @param organization tenant organization
     * @param result       API list (non-null for COMPLETED events only)
     * @param errorMessage failure reason (non-null for FAILED events only)
     */
    private static void publishDiscoverySync(String taskId, String envKey, String status,
            String organization, List<Map<String, Object>> result, String errorMessage) {
        try {
            FederatedDiscoverySyncEvent event =
                    new FederatedDiscoverySyncEvent(taskId, envKey, status, organization, NODE_ID);
            if (result != null) {
                event.setResult(result);
            }
            if (errorMessage != null) {
                event.setErrorMessage(errorMessage);
            }
            APIUtil.sendNotification(event, APIConstants.NotifierType.FEDERATED_DISCOVERY.name());
            if (log.isDebugEnabled()) {
                log.debug("Published federated-discovery-sync event: taskId=" + taskId
                        + " status=" + status);
            }
        } catch (Exception e) {
            // Non-fatal: the local node already has the correct state; only siblings are affected
            log.warn("Failed to publish federated-discovery-sync JMS event for task [" + taskId
                    + "]: " + e.getMessage(), e);
        }
    }


}
