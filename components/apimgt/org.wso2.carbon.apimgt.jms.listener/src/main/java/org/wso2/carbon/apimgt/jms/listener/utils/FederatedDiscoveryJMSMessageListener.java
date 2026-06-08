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

package org.wso2.carbon.apimgt.jms.listener.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.FederatedDiscoverySyncEvent;
import org.wso2.carbon.apimgt.impl.federatedDiscovery.FederatedDiscoveryTaskStore;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.lang.reflect.Type;

/**
 * JMS {@link MessageListener} that keeps every CP node's in-memory discovery task store
 * synchronized across the cluster.
 *
 * <p>When Node A accepts a {@code POST /federated-apis/discover} request it publishes a
 * {@code FederatedDiscoverySyncEvent} to the JMS {@code notification} topic.  All other
 * nodes receive the event here and replicate the state change into their own
 * {@code TASK_STORE} / {@code ACTIVE_TASK_BY_ENV} maps so that a subsequent
 * {@code GET /federated-apis/status/{taskId}} poll can be served by any node.
 *
 * <p>The originating node is identified via {@code originNodeId}; its own messages are
 * silently skipped (the local maps were already updated by the same call that triggered
 * the publish).
 *
 * <p>Message format: JMS {@link TextMessage} on the {@code notification} topic, same
 * envelope as all other WSO2 notification events:
 * <pre>
 *   { "event": { "payloadData": [ eventType, timestamp, base64EncodedPayload ] } }
 * </pre>
 * The payload is a Base64-encoded Gson serialization of {@link FederatedDiscoverySyncEvent}.
 */
public class FederatedDiscoveryJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(FederatedDiscoveryJMSMessageListener.class);

    /** Event type discriminator written by {@code FederatedDiscoveryNotifier}. */
    private static final String FEDERATED_DISCOVERY_SYNC =
            APIConstants.EventType.FEDERATED_DISCOVERY_SYNC.toString();

    @Override
    public void onMessage(Message message) {
        try {
            if (message == null) {
                log.warn("Dropping null JMS message in FederatedDiscoveryJMSMessageListener");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("FederatedDiscoveryJMSMessageListener received message: " + message);
            }

            Topic jmsDestination = (Topic) message.getJMSDestination();
            if (!(message instanceof TextMessage)) {
                log.warn("Unsupported JMS message type in FederatedDiscoveryJMSMessageListener: "
                        + message.getClass());
                return;
            }

            if (!JMSConstants.TOPIC_NOTIFICATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                return; // Safety guard — only process notification topic messages
            }

            String textMessage = ((TextMessage) message).getText();
            JsonNode payloadData = new ObjectMapper()
                    .readTree(textMessage)
                    .path(APIConstants.EVENT_PAYLOAD)
                    .path(APIConstants.EVENT_PAYLOAD_DATA);

            String eventType = payloadData.get(APIConstants.EVENT_TYPE).asText();
            if (!FEDERATED_DISCOVERY_SYNC.equals(eventType)) {
                return; // Not our event; let other listeners on this topic handle it
            }

            String encodedPayload = payloadData.get(APIConstants.EVENT_PAYLOAD).asText();
            byte[] decoded = Base64.decodeBase64(encodedPayload);
            String eventJson = new String(decoded);

            FederatedDiscoverySyncEvent event =
                    new Gson().fromJson(eventJson, FederatedDiscoverySyncEvent.class);

            handleDiscoverySync(event);

        } catch (JMSException | JsonProcessingException e) {
            log.error("Error processing JMS message in FederatedDiscoveryJMSMessageListener", e);
        }
    }

    /**
     * Applies the state change carried by the event to the local task store.
     *
     * <p>If the event originates from <em>this</em> node the local maps were already updated
     * by the originating code path, so we skip to avoid duplicate writes / log noise.
     */
    private void handleDiscoverySync(FederatedDiscoverySyncEvent event) {
        String thisNodeId = System.getProperty("carbon.id", "");
        if (thisNodeId.equals(event.getOriginNodeId())) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping own federated-discovery-sync event for task: " + event.getTaskId());
            }
            return;
        }

        log.info("Received federated-discovery-sync event: taskId=" + event.getTaskId()
                + " status=" + event.getStatus() + " from node=" + event.getOriginNodeId());

        switch (event.getStatus()) {
            case "PENDING":
                FederatedDiscoveryTaskStore.applyRemoteTaskPending(
                        event.getTaskId(), event.getEnvKey(), event.getOrganization());
                break;
            case "COMPLETED":
                FederatedDiscoveryTaskStore.applyRemoteTaskCompleted(
                        event.getTaskId(), event.getResult());
                break;
            case "FAILED":
                FederatedDiscoveryTaskStore.applyRemoteTaskFailed(
                        event.getTaskId(), event.getErrorMessage());
                break;
            default:
                log.warn("Unknown status in federated-discovery-sync event: " + event.getStatus());
        }
    }
}
