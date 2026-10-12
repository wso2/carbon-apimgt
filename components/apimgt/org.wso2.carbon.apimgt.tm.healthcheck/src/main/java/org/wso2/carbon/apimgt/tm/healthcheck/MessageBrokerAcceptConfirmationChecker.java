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

package org.wso2.carbon.apimgt.tm.healthcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.factory.JMSConnectionFactory;

import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Stage 2 accept confirmation for {@link ControlPlaneConnectionId#MESSAGE_BROKER} (5672) and
 * {@link ControlPlaneConnectionId#MESSAGE_BROKER_SECURE} (8672, when applicable) - Research &sect;3.
 *
 * <p>Opens a short-lived, authenticated JMS/AMQP producer using the same {@code jmsConnectionParameters} the
 * Traffic Manager's own consumer code already uses ({@code JMSTransportHandler}), publishes exactly one
 * throwaway marker message to a dedicated health-check topic, and closes the connection immediately. No
 * consumer is created and none is expected - the message is intentionally lost, not a defect.</p>
 *
 * <p>A dedicated topic (rather than the real throttle-data topic) is used so this probe can never be mistaken
 * for, or folded into, real throttling/notification traffic even if a consumer happened to exist for it
 * (contracts/health-checker-spi.md "Marker payload", FR-002b).</p>
 *
 * <p><b>Bounded by {@code connect.timeout.ms} (FR-006)</b>: unlike {@code DataPublisher}'s asynchronous
 * connection model (see {@link ThrottlingDataAcceptConfirmationChecker}), {@link JMSConnectionFactory#
 * getConnection()} and the raw {@code javax.jms} API it delegates to perform a genuinely synchronous, blocking
 * connect/session/producer handshake with no timeout parameter of their own - a Control Plane broker that
 * accepts the TCP/TLS connect but never completes that handshake would otherwise hang this call indefinitely.
 * {@link #publishMarkerMessage(Properties)} is therefore run on a short-lived helper thread bounded by
 * {@code connectTimeoutMs} via {@link Future#get(long, TimeUnit)}.</p>
 */
public class MessageBrokerAcceptConfirmationChecker {

    private static final Log log = LogFactory.getLog(MessageBrokerAcceptConfirmationChecker.class);

    private static final String CONNECTION_FACTORY_NAME = "TMHealthCheckMarkerProducer";
    private static final String MARKER_TOPIC_NAME = "tmHealthCheckMarkerTopic";

    /**
     * Names the single helper thread used to bound each {@link #publishMarkerMessage(Properties)} call, and
     * marks it a daemon so a connect/publish call that never returns (the helper thread is best-effort
     * interrupted via {@link ExecutorService#shutdownNow()} but may not actually stop) can never keep the JVM
     * alive on its own.
     */
    private static final ThreadFactory PUBLISH_THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "tm-healthcheck-message-broker-publish");
        thread.setDaemon(true);
        return thread;
    };

    /**
     * Runs stage 2 for one message broker connection (5672 or 8672).
     *
     * @param connectionId           which connection this is for - used only to label the returned result
     * @param jmsConnectionParameters the live JMS connection-factory properties to connect with
     * @param stage1Result           this connection's stage-1 (port-responsiveness) result
     * @param connectTimeoutMs       the bounded timeout, in milliseconds, that the connect-and-publish
     *                               sequence must not exceed (FR-006)
     * @return {@code stage1Result} unchanged if stage 1 did not succeed; otherwise a fresh {@code UP}/{@code
     *         DOWN} result from attempting the marker publish
     */
    public ConnectionCheckResult check(ControlPlaneConnectionId connectionId, Properties jmsConnectionParameters,
                                        ConnectionCheckResult stage1Result, int connectTimeoutMs) {

        if (!stage1Result.isUp()) {
            return stage1Result;
        }
        if (jmsConnectionParameters == null) {
            return ConnectionCheckResult.down(connectionId, FailureReason.UNREACHABLE);
        }

        ExecutorService publishExecutor = Executors.newSingleThreadExecutor(PUBLISH_THREAD_FACTORY);
        try {
            Future<Void> publishFuture = publishExecutor.submit(() -> {
                publishMarkerMessage(jmsConnectionParameters);
                return null;
            });
            publishFuture.get(connectTimeoutMs, TimeUnit.MILLISECONDS);
            return ConnectionCheckResult.up(connectionId);
        } catch (TimeoutException e) {
            // The connect-and-publish sequence did not complete within connectTimeoutMs - report this
            // connection as timed out rather than letting this call block past the configured bound (FR-006).
            // The helper thread above is best-effort interrupted in the finally block; it is a daemon thread
            // regardless, so it can never keep the JVM alive if the underlying JMS call never actually returns
            // (its own finally block, inside publishMarkerMessage, still closes the connection/factory
            // whenever - if ever - that call eventually unblocks).
            log.warn("Control Plane message broker connection did not accept the health-check marker message "
                    + "within " + connectTimeoutMs + "ms");
            return ConnectionCheckResult.down(connectionId, FailureReason.TIMED_OUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ConnectionCheckResult.down(connectionId, FailureReason.TIMED_OUT, e.getMessage());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.warn("Control Plane message broker connection rejected the health-check marker message: "
                    + cause.getMessage());
            return ConnectionCheckResult.down(connectionId, FailureReason.REJECTED_BY_CONTROL_PLANE,
                    cause.getMessage());
        } finally {
            publishExecutor.shutdownNow();
        }
    }

    /**
     * Factory seam for the connect-and-publish sequence, overridable so a test can substitute a slow/blocking
     * implementation to exercise the bounded-time wrapper in {@link #check} without depending on a real JMS
     * provider's own connect/handshake behavior. Closes the connection and stops the connection factory
     * deterministically on every path, including the exception path - matching the short-lived resource
     * pattern used throughout this feature (FR-007).
     *
     * @param jmsConnectionParameters the live JMS connection-factory properties to connect with
     * @throws Exception if the connection, session, producer, or send itself fails
     */
    protected void publishMarkerMessage(Properties jmsConnectionParameters) throws Exception {

        JMSConnectionFactory connectionFactory =
                new JMSConnectionFactory(toHashtable(jmsConnectionParameters), CONNECTION_FACTORY_NAME);
        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            Session session = connectionFactory.getSession(connection);
            Destination destination = connectionFactory.getDestination(MARKER_TOPIC_NAME);
            MessageProducer producer = connectionFactory.getMessageProducer(connection, session, destination);
            try {
                producer.send(session.createMessage());
            } finally {
                producer.close();
            }
        } finally {
            closeQuietly(connection);
            connectionFactory.stop();
        }
    }

    /**
     * Converts the {@code Properties} bag {@code EventHubConfigurationDto} carries into the
     * {@code Hashtable<String, String>} {@link JMSConnectionFactory} requires - the same conversion
     * {@code JMSTransportHandler} already performs for the Traffic Manager's own consumer connection.
     */
    private Hashtable<String, String> toHashtable(Properties properties) {

        Hashtable<String, String> parameters = new Hashtable<>();
        for (String name : properties.stringPropertyNames()) {
            parameters.put(name, properties.getProperty(name));
        }
        return parameters;
    }

    private void closeQuietly(Connection connection) {

        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (JMSException e) {
            log.warn("Error closing the health-check JMS connection: " + e.getMessage());
        }
    }
}
