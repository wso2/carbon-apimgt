/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration.DeploymentAcknowledgementConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeploymentStatusNotifier {
    private static volatile DeploymentStatusNotifier instance;
    private static int batchSize = 200;
    private static long batchIntervalMillis = 1000;
    private final Log log = LogFactory.getLog(DeploymentStatusNotifier.class);
    private final String NOTIFY_API_DEPLOYMENT_STATUS_BATCH_PATH = "/notify-api-deployment-status";
    private final String CONTENT_TYPE = "application/json";
    private final int BATCH_PROCESSOR_MIN_THREAD = 2;
    private final int BATCH_PROCESSOR_MAX_THREAD = 8;
    private final long BATCH_PROCESSOR_KEEP_ALIVE = 60000L;
    private final int BATCH_PROCESSOR_QUEUE_SIZE = 50;
    private final BlockingQueue<DeploymentStatusJob> currentBatch = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor threadPoolExecutor;
    private final ScheduledExecutorService periodicBatchProcessor;
    private final Object batchLock = new Object();
    private long retryDuration = 10000;
    private int maxRetryCount = 5;
    private double retryProgressionFactor = 2.0;

    private DeploymentStatusNotifier(int batchSize, long batchIntervalMillis) {
        DeploymentAcknowledgementConfiguration deploymentAcknowledgementConfiguration =
                ServiceReferenceHolder.getInstance().getApiManagerConfigurationService().getAPIManagerConfiguration()
                        .getGatewayNotificationConfiguration().getDeploymentAcknowledgement();

        if (deploymentAcknowledgementConfiguration != null) {
            batchSize = deploymentAcknowledgementConfiguration.getBatchSize();
            batchIntervalMillis = deploymentAcknowledgementConfiguration.getBatchIntervalMillis();
            maxRetryCount = deploymentAcknowledgementConfiguration.getMaxRetryCount();
            retryProgressionFactor = deploymentAcknowledgementConfiguration.getRetryProgressionFactor();
            retryDuration = deploymentAcknowledgementConfiguration.getRetryDuration();

            if (log.isDebugEnabled()) {
                log.debug("Using configuration values - BatchSize: " + batchSize + ", BatchIntervalMillis: "
                                  + batchIntervalMillis);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Configuration not available, using default values - BatchSize: " + batchSize
                                  + ", BatchIntervalMillis: " + batchIntervalMillis);
            }
        }
        DeploymentStatusNotifier.batchSize = batchSize;
        DeploymentStatusNotifier.batchIntervalMillis = batchIntervalMillis;
        periodicBatchProcessor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "DeploymentStatusNotifier-Scheduler");
            t.setDaemon(true);
            return t;
        });
        threadPoolExecutor = new ThreadPoolExecutor(BATCH_PROCESSOR_MIN_THREAD, BATCH_PROCESSOR_MAX_THREAD,
                                                    BATCH_PROCESSOR_KEEP_ALIVE, TimeUnit.MILLISECONDS,
                                                    new LinkedBlockingQueue<>(BATCH_PROCESSOR_QUEUE_SIZE), r -> {
            Thread t = new Thread(r, "DeploymentStatusProcessor");
            t.setDaemon(true);
            return t;
        }, new ThreadPoolExecutor.CallerRunsPolicy());
        periodicBatchProcessor.scheduleAtFixedRate(this::processPeriodicBatch, batchIntervalMillis, batchIntervalMillis,
                                                   TimeUnit.MILLISECONDS);
    }

    /**
     * Returns the singleton instance of DeploymentStatusNotifier.
     *
     * @return the singleton DeploymentStatusNotifier instance
     */
    public static DeploymentStatusNotifier getInstance() {
        if (instance == null) {
            synchronized (DeploymentStatusNotifier.class) {
                if (instance == null) {
                    instance = new DeploymentStatusNotifier(batchSize, batchIntervalMillis);
                }
            }
        }
        return instance;
    }

    private void processPeriodicBatch() {
        if (!currentBatch.isEmpty() && DataHolder.getInstance().isGatewayRegistered()) {
            synchronized (batchLock) {
                if (log.isDebugEnabled()) {
                    log.debug("Periodic batch processor triggered with " + currentBatch.size() + " items");
                }
                processBatch();
            }
        }
    }

    /**
     * Submits an API deployment status notification for processing.
     *
     * @param apiId             the API identifier
     * @param apiRevisionId     the API revision identifier (used for successful deployments)
     * @param success           true if the deployment was successful, false otherwise
     * @param action            the deployment action performed (e.g., "DEPLOY", "UNDEPLOY")
     * @param errorCode         error code for failed deployments (can be null)
     * @param errorMessage      error message for failed deployments (can be null or empty)
     * @param isBatchingEnabled true to enable batching, false to process immediately
     */
    public void submitDeploymentStatus(String apiId, String apiRevisionId, boolean success, String action,
                                       Long errorCode, String errorMessage, boolean isBatchingEnabled) {
        DeploymentStatusJob job = new DeploymentStatusJob(apiId, apiRevisionId, success, action, errorCode,
                                                          errorMessage);

        try {
            if (isBatchingEnabled) {
                currentBatch.offer(job);

                if (currentBatch.size() >= batchSize && DataHolder.getInstance().isGatewayRegistered()) {
                    synchronized (batchLock) {
                        if (currentBatch.size() >= batchSize) {
                            processBatch();
                        }
                    }
                }
            } else if (DataHolder.getInstance().isGatewayRegistered()) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing deployment status for API: " + apiId + ", Action: " + action);
                }
                List<DeploymentStatusJob> singleJobList = Collections.singletonList(job);
                threadPoolExecutor.submit(() -> processBatchedNotifications(singleJobList));
            } else {
                currentBatch.offer(job);
            }

        } catch (Exception e) {
            log.error("Failed to submit deployment status job", e);
        }
    }

    private void processBatch() {
        if (!currentBatch.isEmpty()) {
            List<DeploymentStatusJob> batchToProcess = new ArrayList<>();

            currentBatch.drainTo(batchToProcess);

            if (!batchToProcess.isEmpty()) {
                try {
                    threadPoolExecutor.submit(() -> processBatchedNotifications(batchToProcess));

                    if (log.isDebugEnabled()) {
                        log.debug("Submitted batch of " + batchToProcess.size()
                                          + " deployment status jobs for processing. " + "Active threads: "
                                          + threadPoolExecutor.getActiveCount() + ", Queue size: "
                                          + threadPoolExecutor.getQueue().size() + ", Remaining in currentBatch: "
                                          + currentBatch.size());
                    }
                } catch (RejectedExecutionException e) {
                    log.error("Failed to submit batch for processing due to thread pool rejection. "
                                      + "Processing in current thread. Batch size: " + batchToProcess.size(), e);
                    // Fallback: process in current thread
                    processBatchedNotifications(batchToProcess);
                }
            }
        }
    }

    private void processBatchedNotifications(List<DeploymentStatusJob> jobs) {
        long startTime = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();

        try {
            if (log.isDebugEnabled()) {
                log.debug(
                        "[" + threadName + "] Processing batch of " + jobs.size() + " deployment status notifications");
            }

            List<DeploymentStatusJob> acknowledgments = new ArrayList<>(jobs);

            BatchPayload payload = new BatchPayload(jobs.size(), acknowledgments);
            String jsonPayload = new Gson().toJson(payload);

            notifyBatchedApiDeploymentStatus(jsonPayload);

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("[" + threadName + "] Error notifying batched API deployment status for " + jobs.size()
                              + " APIs after " + processingTime + "ms", e);
        }
    }

    private void notifyBatchedApiDeploymentStatus(String payload) {
        try {
            EventHubConfigurationDto config =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
            String serviceURLStr = config.getServiceUrl().concat(NOTIFY_API_DEPLOYMENT_STATUS_BATCH_PATH);
            URL url = new URL(serviceURLStr);

            HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());

            HttpPost request = new HttpPost(serviceURLStr);
            request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC + new String(
                    Base64.encodeBase64(
                            (config.getUsername() + APIConstants.DELEM_COLON + config.getPassword()).getBytes(
                                    StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            request.setHeader("Content-Type", CONTENT_TYPE);
            request.setEntity(new StringEntity(payload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(request, httpClient,
                                                                                        retryDuration, maxRetryCount,
                                                                                        retryProgressionFactor)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes(),
                                                 StandardCharsets.UTF_8);
                if (log.isDebugEnabled()) {
                    log.debug("Batched deployment status notification. Status: " + statusCode + ", Response: "
                                      + responseBody);
                }
            }
        } catch (IOException | APIManagementException e) {
            log.error("Error occurred while calling batched deployment status notification", e);
        }
    }

    /**
     * Represents the batch payload structure for GSON serialization
     */
    private static class BatchPayload {
        private final int count;
        private final List<DeploymentStatusJob> list;

        public BatchPayload(int count, List<DeploymentStatusJob> list) {
            this.count = count;
            this.list = list;
        }
    }

    private static class DeploymentStatusJob {
        final String gatewayId;
        final String apiId;
        final String apiRevisionId;
        final long timeStamp;
        final boolean success;
        final String action;
        final Long errorCode;
        final String errorMessage;

        DeploymentStatusJob(String apiId, String apiRevisionId, boolean success, String action, Long errorCode,
                            String errorMessage) {
            this.gatewayId = DataHolder.getInstance().getGatewayID();
            this.apiId = apiId;
            this.apiRevisionId = apiRevisionId;
            this.timeStamp = Instant.now().toEpochMilli();
            this.success = success;
            this.action = action;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return "DeploymentStatusJob{" + "apiId='" + apiId + '\'' + ", success=" + success + ", action='" + action
                    + '\'' + '}';
        }
    }
}
