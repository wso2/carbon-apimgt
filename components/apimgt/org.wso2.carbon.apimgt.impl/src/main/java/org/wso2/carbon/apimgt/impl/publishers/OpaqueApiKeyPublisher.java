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

package org.wso2.carbon.apimgt.impl.publishers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration;
import org.wso2.carbon.apimgt.impl.dto.GatewayNotificationConfiguration.DeploymentAcknowledgementConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.token.OpaqueAPIKeyNotifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
 * Publisher class to notify api key info
 */
public class OpaqueApiKeyPublisher {

    private static final Log log = LogFactory.getLog(OpaqueApiKeyPublisher.class);

    private static OpaqueApiKeyPublisher opaqueApiKeyPublisher = null;
    private static final BlockingQueue<Properties> currentUsageBatch = new LinkedBlockingQueue<>();
    private static final Object usageBatchLock = new Object();
    private OpaqueAPIKeyNotifier opaqueApiKeyNotifier;
    private final ThreadPoolExecutor usageBatchProcessorExecutor;
    private final ScheduledExecutorService usageBatchScheduler;
    private volatile boolean shutdownStarted;
    private final int batchSize;
    private final long batchIntervalMillis;
    private final int batchProcessorMinThread;
    private final int batchProcessorMaxThread;
    private final long batchProcessorKeepAlive;
    private final int batchProcessorQueueSize;
    private final boolean notificationsEnabled;

    private OpaqueApiKeyPublisher() {

        opaqueApiKeyNotifier = ServiceReferenceHolder.getInstance().getOpaqueApiKeyNotifier();
        GatewayNotificationConfiguration gatewayNotificationConfig = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getGatewayNotificationConfiguration();
        DeploymentAcknowledgementConfiguration config = gatewayNotificationConfig.getDeploymentAcknowledgement();
        this.batchSize = config.getBatchSize();
        this.batchIntervalMillis = config.getBatchIntervalMillis();
        this.batchProcessorMinThread = config.getBatchProcessorMinThread();
        this.batchProcessorMaxThread = config.getBatchProcessorMaxThread();
        this.batchProcessorKeepAlive = config.getBatchProcessorKeepAlive();
        this.batchProcessorQueueSize = config.getBatchProcessorQueueSize();
        this.notificationsEnabled = gatewayNotificationConfig.isEnabled();

        this.usageBatchScheduler = createSchedulerExecutor();
        this.usageBatchProcessorExecutor = createThreadPoolExecutor();

        if (opaqueApiKeyNotifier != null) {
            log.debug("Opaque API key notifier initialized");
            log.debug("Starting periodic batch processing for API key usage notifications");
            startPeriodicBatchProcessing();
        } else {
            log.warn("Opaque API key notifier is not initialized. Realtime notifications will be disabled.");
        }
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

        List<Properties> batchToProcess = new ArrayList<>();
        currentUsageBatch.drainTo(batchToProcess);
        try {
            usageBatchProcessorExecutor.submit(() -> processBatchedUsageNotifications(batchToProcess));
            if (log.isDebugEnabled()) {
                log.debug(String.format("Submitted batch of %d API key usage jobs. Active threads: %d, queue size: %d",
                        batchToProcess.size(), usageBatchProcessorExecutor.getActiveCount(),
                        usageBatchProcessorExecutor.getQueue().size()));
            }
        } catch (RejectedExecutionException e) {
            log.error("Failed to submit API key usage batch due to thread pool rejection. Batch size: "
                    + batchToProcess.size(), e);
            currentUsageBatch.addAll(batchToProcess);
        }
    }

    private void processBatchedUsageNotifications(List<Properties> jobs) {
        if (jobs == null || jobs.isEmpty() || opaqueApiKeyNotifier == null) {
            return;
        }
        for (Properties job : jobs) {
            try {
                opaqueApiKeyNotifier.sendLastUsedTimeOnRealtime(job);
            } catch (Exception e) {
                log.error("Error while sending batched API key usage notification", e);
            }
        }
    }

    public static synchronized OpaqueApiKeyPublisher getInstance() {

        if (opaqueApiKeyPublisher == null) {
            opaqueApiKeyPublisher = new OpaqueApiKeyPublisher();
        }
        return opaqueApiKeyPublisher;
    }

    /**
     * Shutdown publisher resources and clear pending usage batch jobs.
     */
    public static synchronized void shutdownInstance() {
        if (opaqueApiKeyPublisher != null) {
            opaqueApiKeyPublisher.shutdown();
            opaqueApiKeyPublisher = null;
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
     * @param properties
     */
    public void publishApiKeyUsageEvents(Properties properties) {

        if (shutdownStarted || opaqueApiKeyNotifier == null || !notificationsEnabled || properties == null) {
            return;
        }

        Properties usageEvent = new Properties();
        usageEvent.putAll(properties);
        if (!currentUsageBatch.offer(usageEvent)) {
            log.error(String.format("API key usage notification queue is full. Current queue size: %d, max capacity: %d",
                    currentUsageBatch.size(), Integer.MAX_VALUE));
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

    /**
     * Publish API key info events
     * @param properties
     */
    public void publishApiKeyInfoEvents(Properties properties) {

        if (opaqueApiKeyNotifier != null) {
            opaqueApiKeyNotifier.sendApiKeyInfoOnRealtime(properties);
        }
    }

    /**
     * Publish API key association info events
     * @param properties
     */
    public void publishApiKeyAssociationInfoEvents(Properties properties) {

        if (opaqueApiKeyNotifier != null) {
            opaqueApiKeyNotifier.sendApiKeyAssociationInfoOnRealtime(properties);
        }
    }
}
