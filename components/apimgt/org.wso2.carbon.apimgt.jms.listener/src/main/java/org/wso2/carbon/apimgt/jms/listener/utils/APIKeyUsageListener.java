/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.jms.listener.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiKeyMgtDAO;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.sql.Timestamp;
import java.util.HashMap;
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
 * API key usage listener to handle API key usage events.
 */
public class APIKeyUsageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(APIKeyUsageListener.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int API_KEY_USAGE_DB_BATCH_SIZE = 100;
    private static final long API_KEY_USAGE_DB_BATCH_INTERVAL_MILLIS = 5000L;
    private static final ConcurrentHashMap<String, Optional<Timestamp>> pendingUsageUpdates =
            new ConcurrentHashMap<>();
    private static final AtomicBoolean flushInProgress = new AtomicBoolean(false);
    private static final ScheduledExecutorService periodicBatchProcessor = createSchedulerExecutor();

    static {
        periodicBatchProcessor.scheduleAtFixedRate(APIKeyUsageListener::flushPendingUsageUpdates,
                API_KEY_USAGE_DB_BATCH_INTERVAL_MILLIS,
                API_KEY_USAGE_DB_BATCH_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS);
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
     * Flushes pending API key usage updates and shuts down scheduler resources.
     */
    public static void shutdown() {
        flushPendingUsageUpdates();
        periodicBatchProcessor.shutdown();
        try {
            if (!periodicBatchProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                periodicBatchProcessor.shutdownNow();
            }
        } catch (InterruptedException e) {
            periodicBatchProcessor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        // Final flush to catch any re-queued entries from failed batches
        flushPendingUsageUpdates();
    }

    @Override
    public void onMessage(Message message) {

        if (log.isDebugEnabled()) {
            log.debug("API Key Usage JMS message received");
        }

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    // Navigate to payloadData
                    JsonNode payloadData = objectMapper.readTree(textMessage).path("event").path("payloadData");
                    JsonNode apiKeyHashNode = payloadData.path(APIConstants.NotificationEvent.API_KEY_HASH);
                    JsonNode lastUsedTimeNode = payloadData.path(APIConstants.NotificationEvent.LAST_USED_TIME);
                    String apiKeyHash = apiKeyHashNode.asText(null);
                    Long epoch = lastUsedTimeNode.isMissingNode() || lastUsedTimeNode.isNull()
                            ? null : lastUsedTimeNode.asLong();
                    Timestamp lastUsedTimestamp = epoch != null ? new Timestamp(epoch) : null;

                    if (apiKeyHash != null && !apiKeyHash.isEmpty()) {
                        pendingUsageUpdates.merge(apiKeyHash, Optional.ofNullable(lastUsedTimestamp),
                                APIKeyUsageListener::selectLatestTimestamp);
                        if (pendingUsageUpdates.size() >= API_KEY_USAGE_DB_BATCH_SIZE) {
                            flushPendingUsageUpdates();
                        }
                    } else {
                        log.warn("Received API key usage event with empty apiKeyHash.");
                    }
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver.");
            }
        } catch (JMSException | JsonProcessingException e) {
            log.error("Error occurred when processing the API key usage message ", e);
        }
    }

    private static void flushPendingUsageUpdates() {

        if (!flushInProgress.compareAndSet(false, true)) {
            return;
        }

        try {
            Map<String, Timestamp> updatesToFlush = drainPendingUsageUpdates();
            if (updatesToFlush.isEmpty()) {
                return;
            }

            try {
                ApiKeyMgtDAO.getInstance().updateAPIKeyUsageBatch(updatesToFlush);
                if (log.isDebugEnabled()) {
                    log.debug("Flushed API key usage DB batch with size: " + updatesToFlush.size());
                }
            } catch (APIManagementException e) {
                log.error("Error occurred while flushing API key usage DB batch", e);
                updatesToFlush.forEach((apiKeyHash, timestamp) -> pendingUsageUpdates.merge(
                        apiKeyHash, Optional.ofNullable(timestamp), APIKeyUsageListener::selectLatestTimestamp));
            }
        } finally {
            flushInProgress.set(false);
        }
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
}
