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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API key usage listener to handle API key usage events.
 */
public class APIKeyUsageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(APIKeyUsageListener.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int API_KEY_USAGE_DB_BATCH_SIZE = 100;
    private static final long API_KEY_USAGE_DB_BATCH_INTERVAL_MILLIS = 5000L;
    private static final Object batchLock = new Object();
    private static final Map<String, Timestamp> pendingUsageUpdates = new HashMap<>();
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
                        boolean shouldFlush = false;
                        synchronized (batchLock) {
                            Timestamp existingTimestamp = pendingUsageUpdates.get(apiKeyHash);
                            if (existingTimestamp == null || (lastUsedTimestamp != null
                                    && lastUsedTimestamp.after(existingTimestamp))) {
                                pendingUsageUpdates.put(apiKeyHash, lastUsedTimestamp);
                            }
                            shouldFlush = pendingUsageUpdates.size() >= API_KEY_USAGE_DB_BATCH_SIZE;
                        }
                        if (shouldFlush) {
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

        Map<String, Timestamp> updatesToFlush;
        synchronized (batchLock) {
            if (pendingUsageUpdates.isEmpty()) {
                return;
            }
            updatesToFlush = new HashMap<>(pendingUsageUpdates);
            pendingUsageUpdates.clear();
        }

        try {
            ApiKeyMgtDAO.getInstance().updateAPIKeyUsageBatch(updatesToFlush);
            if (log.isDebugEnabled()) {
                log.debug("Flushed API key usage DB batch with size: " + updatesToFlush.size());
            }
        } catch (APIManagementException e) {
            log.error("Error occurred while flushing API key usage DB batch", e);
            synchronized (batchLock) {
                for (Map.Entry<String, Timestamp> entry : updatesToFlush.entrySet()) {
                    String apiKeyHash = entry.getKey();
                    Timestamp incomingTimestamp = entry.getValue();
                    Timestamp existingTimestamp = pendingUsageUpdates.get(apiKeyHash);
                    if (existingTimestamp == null || (incomingTimestamp != null
                            && incomingTimestamp.after(existingTimestamp))) {
                        pendingUsageUpdates.put(apiKeyHash, incomingTimestamp);
                    }
                }
            }
        }
    }
}



