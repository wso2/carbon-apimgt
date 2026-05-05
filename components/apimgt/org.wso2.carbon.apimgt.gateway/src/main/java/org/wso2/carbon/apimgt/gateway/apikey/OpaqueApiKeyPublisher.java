/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.apikey;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.APIKeyConfigurationDTO;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.notifier.events.APIKeyUsageEvent;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Publisher class to notify api key info
 */
public class OpaqueApiKeyPublisher {

    private static final Log log = LogFactory.getLog(OpaqueApiKeyPublisher.class);

    private static volatile OpaqueApiKeyPublisher opaqueApiKeyPublisher = null;
    private final BlockingQueue<APIKeyUsageEvent> currentUsageBatch;
    private static final Object usageBatchLock = new Object();
    private final ThreadPoolExecutor usageBatchProcessorExecutor;
    private final ScheduledExecutorService usageBatchScheduler;
    private final int maxRetryCount;
    private final double retryProgressionFactor;
    private volatile boolean shutdownStarted;
    private final int batchSize;
    private final long retryDuration;
    private final long batchIntervalMillis;
    private final int batchProcessorMinThread;
    private final int batchProcessorMaxThread;
    private final long batchProcessorKeepAlive;
    private final int batchProcessorQueueSize;
    private final boolean notificationsEnabled;

    private OpaqueApiKeyPublisher() {

        GatewayNotificationConfiguration gatewayNotificationConfig = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfiguration().getGatewayNotificationConfiguration();
        APIKeyConfigurationDTO config = gatewayNotificationConfig.getApiKeyConfiguration();
        int queueSize = config.getQueueSize();
        this.batchSize = config.getBatchSize();
        this.batchIntervalMillis = config.getBatchIntervalMillis();
        this.batchProcessorMinThread = config.getBatchProcessorMinThread();
        this.batchProcessorMaxThread = config.getBatchProcessorMaxThread();
        this.batchProcessorKeepAlive = config.getBatchProcessorKeepAlive();
        this.batchProcessorQueueSize = config.getBatchProcessorQueueSize();
        this.notificationsEnabled = gatewayNotificationConfig.isEnabled();
        this.retryDuration = config.getRetryDuration();
        this.maxRetryCount = config.getMaxRetryCount();
        this.retryProgressionFactor = config.getRetryProgressionFactor();
        this.usageBatchScheduler = createSchedulerExecutor();
        this.usageBatchProcessorExecutor = createThreadPoolExecutor();
        currentUsageBatch = new LinkedBlockingQueue<>(queueSize);
        log.debug("Starting periodic batch processing for API key usage notifications");
        startPeriodicBatchProcessing();
    }

    private ScheduledExecutorService createSchedulerExecutor() {
        return Executors.newSingleThreadScheduledExecutor(createThreadFactory("ApiKeyUsageBatchScheduler"));
    }

    private ThreadPoolExecutor createThreadPoolExecutor() {
        return new ThreadPoolExecutor(batchProcessorMinThread, batchProcessorMaxThread, batchProcessorKeepAlive,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(batchProcessorQueueSize),
                createThreadFactory("ApiKeyUsageBatchProcessor"), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    private ThreadFactory createThreadFactory(String namePrefix) {
        final AtomicInteger threadNumber = new AtomicInteger(1);
        return r -> {
            Thread thread = new Thread(r, namePrefix + "-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> log.error(
                    "Uncaught exception in thread '" + t.getName() + "' of OpaqueApiKeyPublisher", e));
            return thread;
        };
    }

    private void startPeriodicBatchProcessing() {
        if (!notificationsEnabled) {
            return;
        }
        usageBatchScheduler.scheduleAtFixedRate(this::processPeriodicUsageBatch, batchIntervalMillis,
                batchIntervalMillis, TimeUnit.MILLISECONDS);
    }

    private void processPeriodicUsageBatch() {
        if (!currentUsageBatch.isEmpty()) {
            synchronized (usageBatchLock) {
                processCurrentUsageBatch();
            }
        }
    }

    private void processCurrentUsageBatch() {
        if (currentUsageBatch.isEmpty()) {
            return;
        }

        List<APIKeyUsageEvent> batchToProcess = new ArrayList<>();
        currentUsageBatch.drainTo(batchToProcess, batchSize);
        try {
            usageBatchProcessorExecutor.execute(() -> {
                try {
                    processBatchedUsageNotifications(batchToProcess);
                } catch (RuntimeException e) {
                    log.error("Unexpected error while processing API key usage batch notification", e);
                }
            });
            if (log.isDebugEnabled()) {
                log.debug(String.format("Submitted batch of %d API key usage jobs. Active threads: %d, queue size: %d",
                        batchToProcess.size(), usageBatchProcessorExecutor.getActiveCount(),
                        usageBatchProcessorExecutor.getQueue().size()));
            }
        } catch (RejectedExecutionException e) {
            log.error("Failed to submit API key usage batch due to thread pool rejection. Batch size: " +
                    batchToProcess.size() + ". Error: " + e.getMessage());
            currentUsageBatch.addAll(batchToProcess);
        }
    }

    private void processBatchedUsageNotifications(List<APIKeyUsageEvent> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return;
        }
        try {
            String threadName = Thread.currentThread().getName();
            if (log.isDebugEnabled()) {
                log.debug(String.format("[%s] Processing batch of %d API-Key Usage notifications", threadName,
                        jobs.size()));
            }
            String jsonPayload = new Gson().toJson(jobs);

            EventHubConfigurationDto config =
                    ServiceReferenceHolder.getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
            String serviceURLStr = config.getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP).concat("/notify");
            URL url = new URL(serviceURLStr);

            HttpClient httpClient = APIUtil.getHttpClient(url.getPort(), url.getProtocol());

            HttpPost request = new HttpPost(serviceURLStr);
            request.setHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT, APIConstants.AUTHORIZATION_BASIC + new String(
                    Base64.encodeBase64(
                            (config.getUsername() + APIConstants.DELEM_COLON + config.getPassword()).getBytes(
                                    StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
            request.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            request.setHeader(APIConstants.KeyManager.KEY_MANAGER_TYPE_HEADER,
                    APIConstants.NotificationEvent.API_KEY_USAGE_EVENT);
            request.setEntity(new StringEntity(jsonPayload, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(request, httpClient,
                    retryDuration, maxRetryCount, retryProgressionFactor)) {
                if (log.isDebugEnabled()) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    log.debug("Received response for API key usage batch notification. Status code: " + statusCode +
                            ", Response body: " + responseBody);

                }
            }
        } catch (IOException | APIManagementException e) {
            log.error("Error occurred while calling batched deployment status notification", e);
        }
    }

    public static OpaqueApiKeyPublisher getInstance() {
        if (opaqueApiKeyPublisher == null) {
            synchronized (OpaqueApiKeyPublisher.class) {
                if (opaqueApiKeyPublisher == null) {
                    opaqueApiKeyPublisher = new OpaqueApiKeyPublisher();
                }
            }
        }
        return opaqueApiKeyPublisher;
    }

    /**
     * Shutdown publisher resources and clear pending usage batch jobs.
     */
    public synchronized void shutdownInstance() {
        if (!shutdownStarted) {
            shutdown();
        }
    }

    /**
     * Shutdown usage batch scheduler and executor.
     */
    private void shutdown() {
        shutdownStarted = true;
        if (log.isDebugEnabled()) {
            log.debug("Shutting down OpaqueApiKeyPublisher usage batch processors");
        }

        usageBatchScheduler.shutdown();
        synchronized (usageBatchLock) {
            processCurrentUsageBatch();
        }
        usageBatchProcessorExecutor.shutdown();
        currentUsageBatch.clear();

        try {
            if (!usageBatchScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                usageBatchScheduler.shutdownNow();
            }
            if (!usageBatchProcessorExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                usageBatchProcessorExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            usageBatchScheduler.shutdownNow();
            usageBatchProcessorExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Publish API key usage events
     * @param apiKeyUsageEvent API key usage event to publish
     */
    public void publishApiKeyUsageEvents(APIKeyUsageEvent apiKeyUsageEvent) {

        if (shutdownStarted || !notificationsEnabled || apiKeyUsageEvent == null) {
            return;
        }

        if (!currentUsageBatch.offer(apiKeyUsageEvent)) {
            log.error(String.format("API key usage notification queue is full. Current queue size: %d",
                    currentUsageBatch.size()));
            return;
        }

        if (currentUsageBatch.size() >= batchSize) {
            synchronized (usageBatchLock) {
                if (currentUsageBatch.size() >= batchSize) {
                    processCurrentUsageBatch();
                }
            }
        }
    }

}
