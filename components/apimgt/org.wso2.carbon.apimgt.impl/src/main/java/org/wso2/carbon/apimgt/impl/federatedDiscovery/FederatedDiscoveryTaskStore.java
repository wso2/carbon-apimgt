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

package org.wso2.carbon.apimgt.impl.federatedDiscovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Cluster-aware in-memory store for on-demand Federated API Discovery tasks.
 *
 * <p>This class is the single source of truth for the two maps that track discovery jobs:
 * <ul>
 *   <li>{@link #TASK_STORE} — taskId → {@link DiscoveryTask}</li>
 *   <li>{@link #ACTIVE_TASK_BY_ENV} — "organization|envName" → taskId (de-dup index)</li>
 * </ul>
 *
 * <p>It lives in {@code org.wso2.carbon.apimgt.impl} so it is accessible by both
 * {@code org.wso2.carbon.apimgt.rest.api.publisher.v1} (which writes tasks when a
 * {@code POST /federated-apis/discover} request arrives) and
 * {@code org.wso2.carbon.apimgt.jms.listener} (which replicates remote task state
 * changes received via JMS).
 */
public final class FederatedDiscoveryTaskStore {

    private static final Log log = LogFactory.getLog(FederatedDiscoveryTaskStore.class);

    // -----------------------------------------------------------------------
    // Task status constants
    // -----------------------------------------------------------------------
    public static final String STATUS_PENDING   = "PENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED    = "FAILED";

    /** TTL after which completed / failed tasks are evicted (5 minutes). */
    private static final long TASK_TTL_MILLIS = TimeUnit.MINUTES.toMillis(5);

    // -----------------------------------------------------------------------
    // Shared static maps — intentionally static so they survive request threads
    // -----------------------------------------------------------------------

    /** Primary store: taskId → DiscoveryTask. */
    public static final ConcurrentHashMap<String, DiscoveryTask> TASK_STORE =
            new ConcurrentHashMap<>();

    /**
     * Active-task de-dup index: "organization|envName" → taskId.
     * Evicted by the worker on completion / failure.
     */
    public static final ConcurrentHashMap<String, String> ACTIVE_TASK_BY_ENV =
            new ConcurrentHashMap<>();

    // Periodic cleaner — removes expired tasks so heap does not grow unboundedly
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
                log.error("Error during stale discovery task cleanup", e);
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    // Private constructor — utility class, not instantiable
    private FederatedDiscoveryTaskStore() {}

    // -----------------------------------------------------------------------
    // Apply methods — called by FederatedDiscoveryJMSMessageListener on remote
    // nodes to replicate task state received via JMS
    // -----------------------------------------------------------------------

    /**
     * Inserts a PENDING task stub so status polls on this node return PENDING instead of 404.
     *
     * @param taskId       UUID of the task created on the originating node
     * @param envKey       "organization|envName" composite key
     * @param organization tenant organization
     */
    public static void applyRemoteTaskPending(String taskId, String envKey, String organization) {
        String environment = envKey.contains("|")
                ? envKey.substring(envKey.indexOf('|') + 1) : envKey;
        DiscoveryTask stub = new DiscoveryTask(taskId, environment, organization);
        TASK_STORE.putIfAbsent(taskId, stub);
        ACTIVE_TASK_BY_ENV.putIfAbsent(envKey, taskId);
        if (log.isDebugEnabled()) {
            log.debug("Remote PENDING applied: taskId=" + taskId + " envKey=" + envKey);
        }
    }

    /**
     * Updates a task to COMPLETED with the full API result list.
     * Creates a stub if the PENDING message was missed (defensive).
     *
     * @param taskId task UUID
     * @param result list of discovered API maps
     */
    public static void applyRemoteTaskCompleted(String taskId, List<Map<String, Object>> result) {
        DiscoveryTask task = TASK_STORE.get(taskId);
        if (task == null) {
            task = new DiscoveryTask(taskId, "", "");
            TASK_STORE.putIfAbsent(taskId, task);
            task = TASK_STORE.get(taskId);
        }
        task.markCompleted(result);
        if (log.isDebugEnabled()) {
            log.debug("Remote COMPLETED applied: taskId=" + taskId
                    + " resultSize=" + (result != null ? result.size() : 0));
        }
    }

    /**
     * Updates a task to FAILED with the error message.
     * Creates a stub if the PENDING message was missed (defensive).
     *
     * @param taskId       task UUID
     * @param errorMessage failure reason from the originating node
     */
    public static void applyRemoteTaskFailed(String taskId, String errorMessage) {
        DiscoveryTask task = TASK_STORE.get(taskId);
        if (task == null) {
            task = new DiscoveryTask(taskId, "", "");
            TASK_STORE.putIfAbsent(taskId, task);
            task = TASK_STORE.get(taskId);
        }
        task.markFailed(errorMessage);
        if (log.isDebugEnabled()) {
            log.debug("Remote FAILED applied: taskId=" + taskId + " error=" + errorMessage);
        }
    }

    // -----------------------------------------------------------------------
    // Inner class: DiscoveryTask
    // -----------------------------------------------------------------------

    /**
     * Lightweight value object representing one async discovery job.
     *
     * <p>Fields are {@code volatile} so that the worker thread's writes are immediately
     * visible to the HTTP-polling thread without synchronization overhead.
     */
    public static class DiscoveryTask {

        public final String taskId;
        public final String environment;
        public final String organization;
        public final long createdAt;

        public volatile String status;
        public volatile List<Map<String, Object>> result;
        public volatile String errorMessage;
        public volatile long completedAt;

        public DiscoveryTask(String taskId, String environment, String organization) {
            this.taskId = taskId;
            this.environment = environment;
            this.organization = organization;
            this.status = STATUS_PENDING;
            this.createdAt = System.currentTimeMillis();
        }

        public void markCompleted(List<Map<String, Object>> apiList) {
            this.result = apiList;
            this.status = STATUS_COMPLETED;
            this.completedAt = System.currentTimeMillis();
        }

        public void markFailed(String error) {
            this.errorMessage = error;
            this.status = STATUS_FAILED;
            this.completedAt = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return !STATUS_PENDING.equals(status)
                    && (System.currentTimeMillis() - completedAt) > TASK_TTL_MILLIS;
        }

        /** Minimal response for the 202 Accepted body (taskId + status only). */
        public Map<String, Object> toStatusMap() {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("taskId", taskId);
            m.put("status", status);
            return m;
        }

        /** Full response for the GET /status/{taskId} poll endpoint. */
        public Map<String, Object> toResponseMap() {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("taskId", taskId);
            m.put("status", status);
            m.put("environment", environment);
            if (STATUS_COMPLETED.equals(status) && result != null) {
                m.put("result", result);
            }
            if (STATUS_FAILED.equals(status) && errorMessage != null) {
                m.put("error", errorMessage);
            }
            return m;
        }
    }
}
