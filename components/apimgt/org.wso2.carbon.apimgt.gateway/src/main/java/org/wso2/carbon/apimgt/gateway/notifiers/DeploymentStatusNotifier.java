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

package org.wso2.carbon.apimgt.gateway.notifiers;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration.DeploymentAcknowledgementConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Notifies deployment status of APIs to the event hub in batches.
 */
public class DeploymentStatusNotifier {

    private static final BlockingQueue<DeploymentStatusJob> currentBatch = new LinkedBlockingQueue<>();
    private static final Object batchLock = new Object();
    private static volatile DeploymentStatusNotifier instance;
    private final Log log = LogFactory.getLog(DeploymentStatusNotifier.class);
    private final ThreadPoolExecutor threadPoolExecutor;
    private final ScheduledExecutorService periodicBatchProcessor;
    private final Gson gson = new Gson();
    private final int batchSize;
    private final long batchIntervalMillis;
    private final long retryDuration;
    private final int maxRetryCount;
    private final double retryProgressionFactor;
    private final int batchProcessorMinThread;
    private final int batchProcessorMaxThread;
    private final long batchProcessorKeepAlive;
    private final int batchProcessorQueueSize;
    private GatewayNotificationConfiguration gatewayNotificationConfig;

    private DeploymentStatusNotifier() {
        gatewayNotificationConfig =
                ServiceReferenceHolder.getInstance().getApiManagerConfigurationService().getAPIManagerConfiguration()
                        .getGatewayNotificationConfiguration();
        DeploymentAcknowledgementConfiguration config = gatewayNotificationConfig.getDeploymentAcknowledgement();
        this.batchSize = config.getBatchSize();
        this.batchIntervalMillis = config.getBatchIntervalMillis();
        this.maxRetryCount = config.getMaxRetryCount();
        this.retryProgressionFactor = config.getRetryProgressionFactor();
        this.retryDuration = config.getRetryDuration();
        this.batchProcessorMinThread = config.getBatchProcessorMinThread();
        this.batchProcessorMaxThread = config.getBatchProcessorMaxThread();
        this.batchProcessorKeepAlive = config.getBatchProcessorKeepAlive();
        this.batchProcessorQueueSize = config.getBatchProcessorQueueSize();
        this.periodicBatchProcessor = createSchedulerExecutor();
        this.threadPoolExecutor = createThreadPoolExecutor();

        startPeriodicBatchProcessing();
    }

    /**
     * Returns the singleton instance of DeploymentStatusNotifier.
     */
    public static DeploymentStatusNotifier getInstance() {
        if (instance == null) {
            synchronized (DeploymentStatusNotifier.class) {
                if (instance == null) {
                    instance = new DeploymentStatusNotifier();
                }
            }
        }
        return instance;
    }

    private ScheduledExecutorService createSchedulerExecutor() {
        return Executors.newSingleThreadScheduledExecutor(createThreadFactory("DeploymentStatusNotifierScheduler"));
    }

    private ThreadPoolExecutor createThreadPoolExecutor() {
        return new ThreadPoolExecutor(batchProcessorMinThread, batchProcessorMaxThread, batchProcessorKeepAlive,
                                      TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(batchProcessorQueueSize),
                                      createThreadFactory("DeploymentStatusProcessor"),
                                      new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private ThreadFactory createThreadFactory(String namePrefix) {
        final AtomicInteger threadNumber = new AtomicInteger(1);
        return r -> new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
    }

    private void startPeriodicBatchProcessing() {
        if (!gatewayNotificationConfig.isEnabled()){
            return;
        }
        periodicBatchProcessor.scheduleAtFixedRate(this::processPeriodicBatch, batchIntervalMillis, batchIntervalMillis,
                                                   TimeUnit.MILLISECONDS);
    }

    /**
     * Shutdown the notifier and clean up resources.
     */
    public void shutdown() {
        log.info("Shutting down DeploymentStatusNotifier");

        periodicBatchProcessor.shutdown();
        threadPoolExecutor.shutdown();

        try {
            if (!periodicBatchProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                periodicBatchProcessor.shutdownNow();
            }
            if (!threadPoolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPoolExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            periodicBatchProcessor.shutdownNow();
            threadPoolExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void processPeriodicBatch() {
        if (!currentBatch.isEmpty() && DataHolder.getInstance().isGatewayRegistered()) {
            synchronized (batchLock) {
                if (log.isDebugEnabled()) {
                    log.debug("Periodic batch processor triggered with " + currentBatch.size() + " items");
                }
                processCurrentBatch();
            }
        }
    }

    /**
     * Submits an API deployment status notification for batched processing.
     *
     * @param gatewayAPIDTO the GatewayAPIDTO containing API information (required)
     * @param success       true if the deployment was successful, false otherwise
     * @param action        the deployment action performed (e.g., "DEPLOY", "UNDEPLOY")
     * @param errorCode     error code for failed deployments (can be null)
     * @param errorMessage  error message for failed deployments (can be null or empty)
     */
    public void submitDeploymentStatus(GatewayAPIDTO gatewayAPIDTO, boolean success, String action, Long errorCode,
                                       String errorMessage) {
        if (!gatewayNotificationConfig.isEnabled()){
            return;
        }
        if (gatewayAPIDTO == null || gatewayAPIDTO.getApiId() == null || action == null || 
                gatewayAPIDTO.getTenantDomain() == null) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid deployment status submission: gatewayAPIDTO, apiId, action," +
                        " and tenantDomain cannot be null");
            }
            return;
        }
        DeploymentStatusJob job = new DeploymentStatusJob(gatewayAPIDTO.getApiId(), gatewayAPIDTO.getRevision(), 
                                                          success, action, errorCode, errorMessage,
                                                          gatewayAPIDTO.getTenantDomain());
        if (!currentBatch.offer(job)) {
            log.error(String.format("Deployment status notification queue is full. Cannot accept new job for API:" +
                            " %s. Current queue size: %d, Max capacity: %d",
                    job.apiId, currentBatch.size(), Integer.MAX_VALUE));
            return;
        }
        if (currentBatch.size() >= batchSize && DataHolder.getInstance().isGatewayRegistered()) {
            synchronized (batchLock) {
                if (currentBatch.size() >= batchSize) {
                    processCurrentBatch();
                }
            }
        }

    }


    private void processCurrentBatch() {
        if (currentBatch.isEmpty()) {
            return;
        }

        List<DeploymentStatusJob> batchToProcess = new ArrayList<>();
        currentBatch.drainTo(batchToProcess);
        try {
            threadPoolExecutor.submit(() -> processBatchedNotifications(batchToProcess));

            if (log.isDebugEnabled()) {
                log.debug(String.format("Submitted batch of %d deployment status jobs for processing. "
                                                + "Active threads: %d, Queue size: %d, Remaining in currentBatch: %d",
                                        batchToProcess.size(), threadPoolExecutor.getActiveCount(),
                                        threadPoolExecutor.getQueue().size(), currentBatch.size()));
            }
        } catch (RejectedExecutionException e) {
            log.error("Failed to submit batch for processing due to thread pool rejection. "
                              + "Batch size: " + batchToProcess.size(), e);
            // Fallback: put batch back to currentBatch
            currentBatch.addAll(batchToProcess);
        }

    }

    private void processBatchedNotifications(List<DeploymentStatusJob> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return;
        }
        try {
            String threadName = Thread.currentThread().getName();
            if (log.isDebugEnabled()) {
                log.debug(String.format("[%s] Processing batch of %d deployment status notifications", threadName,
                                        jobs.size()));
            }
            BatchPayload payload = new BatchPayload(jobs.size(), new ArrayList<>(jobs));
            String jsonPayload = gson.toJson(payload);

            EventHubConfigurationDto config =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
            String serviceURLStr = config.getServiceUrl().concat(
                    APIConstants.GatewayNotification.NOTIFY_API_DEPLOYMENT_STATUS_BATCH_PATH);
            URL url = new URL(serviceURLStr);

            HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());

            HttpPost request = new HttpPost(serviceURLStr);
            request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC + new String(
                    Base64.encodeBase64(
                            (config.getUsername() + APIConstants.DELEM_COLON + config.getPassword()).getBytes(
                                    StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            request.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            request.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(request, httpClient,
                                                                                        retryDuration, maxRetryCount,
                                                                                        retryProgressionFactor)) {
                if (log.isDebugEnabled()) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    log.debug("Batched deployment status notification response. Status: " + statusCode
                                      + ", Response: " + responseBody);

                }
            }
        } catch (IOException | APIManagementException e) {
            log.error("Error occurred while calling batched deployment status notification", e);
        }
    }

    /**
     * Represents the batch payload structure for GSON serialization.
     */
    private static class BatchPayload {
        private final int count;
        private final List<DeploymentStatusJob> list;

        BatchPayload(int count, List<DeploymentStatusJob> list) {
            this.count = count;
            this.list = list;
        }
    }

    /**
     * Represents a deployment status job to be processed.
     * This class matches the GatewayDeploymentStatusAcknowledgment DTO specification.
     */
    private static class DeploymentStatusJob {
        private final String gatewayId;
        private final String apiId;
        private final String tenantDomain;
        private final String deploymentStatus;
        private final Long timeStamp;
        private final String action;
        private final String revisionId;
        private final Long errorCode;
        private final String errorMessage;

        DeploymentStatusJob(String apiId, String revisionId, boolean success, String action, Long errorCode,
                            String errorMessage, String tenantDomain) {
            this.gatewayId = DataHolder.getInstance().getGatewayID();
            this.apiId = apiId;
            this.tenantDomain = tenantDomain;
            this.deploymentStatus = success ?
                    APIConstants.GatewayNotification.DEPLOYMENT_STATUS_SUCCESS :
                    APIConstants.GatewayNotification.DEPLOYMENT_STATUS_FAILURE;
            this.timeStamp = Instant.now().toEpochMilli();
            this.action = action;
            this.revisionId = revisionId;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
    }
}
