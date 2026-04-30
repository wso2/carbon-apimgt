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

package org.wso2.carbon.apimgt.notification;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiKeyMgtDAO;
import org.wso2.carbon.apimgt.impl.handlers.EventHandler;
import org.wso2.carbon.apimgt.notification.event.APIKeyUsageEvent;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class implements to handle API key usage related notification events.
 */
public class APIKeyEventHandler implements EventHandler {
    private static final Log log = LogFactory.getLog(APIKeyEventHandler.class);

    private static final int API_KEY_USAGE_DB_BATCH_SIZE = 100;
    private static final long API_KEY_USAGE_DB_BATCH_INTERVAL_MILLIS = 5000L;
    private static final AtomicBoolean flushInProgress = new AtomicBoolean(false);
    private static final ConcurrentHashMap<String, Optional<Timestamp>> pendingUsageUpdates = new ConcurrentHashMap<>();
    private final ScheduledExecutorService periodicBatchProcessor;

    public APIKeyEventHandler() {
        periodicBatchProcessor = createSchedulerExecutor();
        periodicBatchProcessor.scheduleAtFixedRate(APIKeyEventHandler::flushPendingUsageUpdates,
                API_KEY_USAGE_DB_BATCH_INTERVAL_MILLIS, API_KEY_USAGE_DB_BATCH_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);

    }

    private static void flushPendingUsageUpdates() {
        Map<String, Timestamp> updatesToFlush;
        if (!flushInProgress.compareAndSet(false, true)) {
            return;
        }

        try {
            updatesToFlush = drainPendingUsageUpdates();
            if (updatesToFlush.isEmpty()) {
                return;
            }

            try {
                ApiKeyMgtDAO.getInstance().updateAPIKeyUsageBatch(updatesToFlush);
                if (log.isDebugEnabled()) {
                    log.debug("Flushed API key usage DB batch with size: " + updatesToFlush.size());
                }
            } catch (APIManagementException | RuntimeException e) {
                log.error("Error occurred while flushing API key usage DB batch", e);
                updatesToFlush.forEach(
                        (apiKeyHash, timestamp) -> pendingUsageUpdates.merge(apiKeyHash, Optional.ofNullable(timestamp),
                                APIKeyEventHandler::selectLatestTimestamp));
            }
        } finally {
            flushInProgress.set(false);
        }
    }

    private static Optional<Timestamp> selectLatestTimestamp(Optional<Timestamp> existingTimestamp,
                                                             Optional<Timestamp> incomingTimestamp) {

        if (!incomingTimestamp.isPresent()) {
            return existingTimestamp;
        }
        if (!existingTimestamp.isPresent()) {
            return incomingTimestamp;
        }
        return incomingTimestamp.get().after(existingTimestamp.get()) ? incomingTimestamp : existingTimestamp;
    }

    private static Map<String, Timestamp> drainPendingUsageUpdates() {
        Map<String, Timestamp> updatesToFlush = new HashMap<>();
        pendingUsageUpdates.forEach((apiKeyHash, timestamp) -> {
            if (pendingUsageUpdates.remove(apiKeyHash, timestamp)) {
                updatesToFlush.put(apiKeyHash, timestamp.orElse(null));
            }
        });
        return updatesToFlush;
    }

    private static ScheduledExecutorService createSchedulerExecutor() {
        final AtomicInteger threadNumber = new AtomicInteger(1);
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r, "APIKeyUsageDBBatchProcessor-" + threadNumber.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    /**
     * Static inner class for TypeToken to avoid anonymous inner class warning.
     */
    private static class APIKeyUsageEventListType extends TypeToken<List<APIKeyUsageEvent>> {
    }

    @Override
    public boolean handleEvent(String event, Map<String, List<String>> headers) throws APIManagementException {
        List<APIKeyUsageEvent> apiKeyUsageEvents;
        try {
            apiKeyUsageEvents = new Gson().fromJson(event, new APIKeyUsageEventListType().getType());
        } catch (com.google.gson.JsonSyntaxException e) {
            log.error("Failed to parse API key usage event JSON", e);
            return false;
        }
        if (apiKeyUsageEvents == null || apiKeyUsageEvents.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Received empty or null API key usage event list");
            }
            return true;
        }
        for (APIKeyUsageEvent apiKeyUsageEvent : apiKeyUsageEvents) {
            Timestamp lastUsedTimestamp = new Timestamp(apiKeyUsageEvent.getLastAccessTime());
            if (StringUtils.isNotBlank(apiKeyUsageEvent.getApikeyHash())) {
                pendingUsageUpdates.merge(apiKeyUsageEvent.getApikeyHash(), Optional.of(lastUsedTimestamp),
                        APIKeyEventHandler::selectLatestTimestamp);
                if (pendingUsageUpdates.size() >= API_KEY_USAGE_DB_BATCH_SIZE) {
                    if (log.isDebugEnabled()) {
                        log.debug("Batch size threshold reached, triggering flush");
                    }
                    flushPendingUsageUpdates();
                }
            } else {
                log.warn("Received API key usage event with empty apiKeyHash.");
            }
        }
        return true;
    }

    public void shutdownScheduler() {
        periodicBatchProcessor.shutdown();
        flushPendingUsageUpdates();
        try {
            if (!periodicBatchProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                periodicBatchProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            periodicBatchProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public String getType() {
        return APIConstants.NotificationEvent.API_KEY_USAGE_EVENT;
    }
}
