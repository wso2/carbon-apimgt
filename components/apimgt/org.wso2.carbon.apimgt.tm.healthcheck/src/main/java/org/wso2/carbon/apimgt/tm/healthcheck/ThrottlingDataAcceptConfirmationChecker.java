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
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAgentConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointAuthenticationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointConfigurationException;
import org.wso2.carbon.databridge.agent.exception.DataEndpointException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Stage 2 accept confirmation for {@link ControlPlaneConnectionId#THROTTLING_DATA} (9611, plain) and
 * {@link ControlPlaneConnectionId#THROTTLING_DATA_SECURE} (9711, SSL/auth) - Research &sect;3.
 *
 * <p>Both ports are exercised through a single, private, short-lived {@link DataPublisher} instance (this
 * bundle's own - it cannot reach the Gateway bundle's shared production publisher, since they are now separate
 * OSGi bundles). Constructing/logging in to that instance against the auth URL group <b>is</b> 9711's
 * accept-confirmation step; 9611's accept-confirmation ({@code tryPublish}) can only run once that login has
 * succeeded (data-model.md validation rule: 9611 depends on 9711's login, never the reverse).</p>
 *
 * <p><b>Bounded by {@code connect.timeout.ms} (FR-006)</b>: constructing a {@code DataPublisher} only
 * <i>submits</i> its login/connection work to an internal background executor and returns immediately -
 * {@code DataEndpoint.initialize()} calls {@code connect()}, which hands off to a single-thread executor rather
 * than blocking - so a hang during that background login cannot otherwise surface as a hang in this call.
 * {@link #createDataPublisher(String, String, String, String)} is therefore run on a short-lived helper thread
 * bounded by {@code connectTimeoutMs} via {@link Future#get(long, TimeUnit)}, so this method itself can never
 * block past that bound even if the underlying construction call were ever changed to block synchronously.
 * The subsequent {@link #publishMarkerEvent(DataPublisher)} call (9611's own accept-confirmation) is bounded the
 * same way, on its own short-lived helper thread - a Control Plane that accepts the 9711 login but hangs during
 * {@code tryPublish} can therefore no longer block this call past the configured bound either (Convergence
 * T032).</p>
 *
 * <p><b>Known, unverified assumption (research.md &sect;3 "Open question")</b>: this relies on
 * {@code DataPublisher#tryPublish(Event)} propagating a Control-Plane-side rejection (e.g. its event queue being
 * full) back to the caller synchronously, rather than only enqueuing into a local client-side buffer and
 * reporting success regardless. That has not been confirmed against a live Control Plane in this workspace; if
 * it turns out {@code tryPublish} cannot surface Control-Plane-side saturation, 9611 would need a different
 * accept-confirmation signal. (Tracked separately - see the Convergence follow-up this bounded-time fix left
 * open: whether construction succeeding, and {@code tryPublish} returning {@code true}, are actually reliable
 * evidence of a Control-Plane-side outcome given {@code DataPublisher}'s asynchronous connection model, rather
 * than just confirming a local client-side buffer accepted the event.)</p>
 */
public class ThrottlingDataAcceptConfirmationChecker {

    private static final Log log = LogFactory.getLog(ThrottlingDataAcceptConfirmationChecker.class);

    /**
     * Names the single helper thread used to bound each {@link #createDataPublisher(String, String, String,
     * String)} call, and marks it a daemon so a construction call that never returns (the helper thread is
     * best-effort interrupted via {@link ExecutorService#shutdownNow()} but may not actually stop) can never
     * keep the JVM alive on its own.
     */
    private static final ThreadFactory LOGIN_THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "tm-healthcheck-throttling-data-login");
        thread.setDaemon(true);
        return thread;
    };

    /**
     * Names the single helper thread used to bound each {@link #publishMarkerEvent(DataPublisher)} call, and
     * marks it a daemon for the same reason as {@link #LOGIN_THREAD_FACTORY}.
     */
    private static final ThreadFactory PUBLISH_THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "tm-healthcheck-throttling-data-publish");
        thread.setDaemon(true);
        return thread;
    };

    /**
     * The {@code DataPublisher} transport type the Traffic Manager's own throttling data publisher already uses
     * by default for these two ports (see {@code ThrottleProperties.DataPublisher}'s {@code "Binary"} default in
     * carbon-apimgt) - {@code EventHubConfigurationDto} carries no separate "type" field of its own, since 9611
     * is always the binary receiver port.
     */
    private static final String DATA_PUBLISHER_TYPE = "Binary";

    /**
     * A dedicated stream ID for this feature's marker event, distinct from the real
     * {@code org.wso2.throttle.request.stream} the Gateway's production publisher uses - so that this probe can
     * never be mistaken for, or silently fold into, an actual throttling decision even if it were ever processed
     * downstream (contracts/health-checker-spi.md "Marker payload", FR-002b).
     */
    private static final String MARKER_STREAM_ID = "org.wso2.carbon.apimgt.tm.healthcheck.marker:1.0.0";

    /**
     * Runs stage 2 for both 9611 and 9711 together, since they share one login step.
     *
     * @param throttlingDataStage1       9611's stage-1 (port-responsiveness) result
     * @param throttlingDataSecureStage1 9711's stage-1 (port-responsiveness) result
     * @param receiverUrlGroup           the live {@code receiverUrlGroup} to log in against (9611 side)
     * @param authUrlGroup               the live {@code authUrlGroup} to log in against (9711 side)
     * @param username                   the Event Hub username to authenticate with
     * @param password                   the Event Hub password to authenticate with
     * @param connectTimeoutMs           the bounded timeout, in milliseconds, that the login step must not
     *                                   exceed (FR-006)
     * @return final results for both connections - never {@code null}
     */
    public ThrottlingDataAcceptConfirmationResult check(ConnectionCheckResult throttlingDataStage1,
                                                          ConnectionCheckResult throttlingDataSecureStage1,
                                                          String receiverUrlGroup, String authUrlGroup,
                                                          String username, String password, int connectTimeoutMs) {

        if (!throttlingDataSecureStage1.isUp()) {
            // 9711 itself is not even port-responsive - logging in would only fail the same way again, and
            // 9611's accept-confirmation can never run without a successful 9711 login. Report both connections
            // using their own stage-1 result alone.
            return new ThrottlingDataAcceptConfirmationResult(throttlingDataStage1, throttlingDataSecureStage1);
        }

        ExecutorService loginExecutor = Executors.newSingleThreadExecutor(LOGIN_THREAD_FACTORY);
        DataPublisher dataPublisher = null;
        try {
            Future<DataPublisher> loginFuture = loginExecutor.submit(
                    () -> createDataPublisher(receiverUrlGroup, authUrlGroup, username, password));
            // Constructing a DataPublisher performs the TLS handshake and login against authUrlGroup itself -
            // reaching the line below without an exception IS 9711's accept-confirmation success.
            dataPublisher = loginFuture.get(connectTimeoutMs, TimeUnit.MILLISECONDS);
            ConnectionCheckResult throttlingDataSecureResult =
                    ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA_SECURE);
            ConnectionCheckResult throttlingDataResult =
                    confirmThrottlingData(dataPublisher, throttlingDataStage1, connectTimeoutMs);
            return new ThrottlingDataAcceptConfirmationResult(throttlingDataResult, throttlingDataSecureResult);
        } catch (TimeoutException e) {
            // The login did not complete within connectTimeoutMs - report both connections as timed out rather
            // than letting this call block past the configured bound (FR-006). The helper thread above is
            // best-effort interrupted in the finally block; it is a daemon thread regardless, so it can never
            // keep the JVM alive if the underlying library call never actually returns.
            log.warn("Control Plane throttling data connection (secure) login did not complete within "
                    + connectTimeoutMs + "ms");
            ConnectionCheckResult timedOutResult =
                    ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA_SECURE,
                            FailureReason.TIMED_OUT);
            return new ThrottlingDataAcceptConfirmationResult(
                    ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA, FailureReason.TIMED_OUT),
                    timedOutResult);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ConnectionCheckResult interruptedResult =
                    ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA_SECURE,
                            FailureReason.TIMED_OUT, e.getMessage());
            return new ThrottlingDataAcceptConfirmationResult(
                    ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA, FailureReason.TIMED_OUT,
                            e.getMessage()),
                    interruptedResult);
        } catch (ExecutionException e) {
            // Any login failure (auth rejected, TLS rejected post-connect, etc.) - 9611 could not be exercised
            // either, since it depends on this same login, so it falls back to its own stage-1 result.
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.warn("Control Plane throttling data connection (secure) login failed: " + cause.getMessage());
            ConnectionCheckResult throttlingDataSecureResult = ConnectionCheckResult.down(
                    ControlPlaneConnectionId.THROTTLING_DATA_SECURE, FailureReason.AUTHENTICATION_FAILED,
                    cause.getMessage());
            return new ThrottlingDataAcceptConfirmationResult(throttlingDataStage1, throttlingDataSecureResult);
        } finally {
            loginExecutor.shutdownNow();
            shutdownQuietly(dataPublisher);
        }
    }

    /**
     * Factory seam for constructing the private {@code DataPublisher} instance, overridable so a test can
     * substitute a slow/blocking implementation to exercise the bounded-time wrapper in {@link #check} without
     * depending on {@code DataPublisher}'s own real (asynchronous) connection/login behavior.
     *
     * @param receiverUrlGroup the live {@code receiverUrlGroup} to log in against (9611 side)
     * @param authUrlGroup     the live {@code authUrlGroup} to log in against (9711 side)
     * @param username         the Event Hub username to authenticate with
     * @param password         the Event Hub password to authenticate with
     * @return a newly constructed {@code DataPublisher}
     * @throws DataEndpointAgentConfigurationException see {@link DataPublisher}'s constructor
     * @throws DataEndpointException see {@link DataPublisher}'s constructor
     * @throws DataEndpointConfigurationException see {@link DataPublisher}'s constructor
     * @throws DataEndpointAuthenticationException see {@link DataPublisher}'s constructor
     * @throws TransportException see {@link DataPublisher}'s constructor
     */
    protected DataPublisher createDataPublisher(String receiverUrlGroup, String authUrlGroup, String username,
            String password) throws DataEndpointAgentConfigurationException, DataEndpointException,
            DataEndpointConfigurationException, DataEndpointAuthenticationException, TransportException {

        return new DataPublisher(DATA_PUBLISHER_TYPE, receiverUrlGroup, authUrlGroup, username, password);
    }

    /**
     * Runs 9611's own accept-confirmation ({@code tryPublish}), but only if 9611's stage 1 already succeeded -
     * otherwise 9611 is reported using that stage-1 result alone (data-model.md validation rule).
     *
     * <p>Bounded by {@code connectTimeoutMs} (FR-006, Convergence T032): {@link #publishMarkerEvent(DataPublisher)}
     * is run on its own short-lived helper thread via {@link Future#get(long, TimeUnit)}, the same pattern used
     * for the login step above, so a Control Plane that accepts the 9711 login but then hangs on the 9611
     * publish cannot block this call past the configured bound.</p>
     */
    private ConnectionCheckResult confirmThrottlingData(DataPublisher dataPublisher,
                                                          ConnectionCheckResult throttlingDataStage1,
                                                          int connectTimeoutMs) {

        if (!throttlingDataStage1.isUp()) {
            return throttlingDataStage1;
        }
        ExecutorService publishExecutor = Executors.newSingleThreadExecutor(PUBLISH_THREAD_FACTORY);
        try {
            Future<Boolean> publishFuture = publishExecutor.submit(() -> publishMarkerEvent(dataPublisher));
            boolean accepted = publishFuture.get(connectTimeoutMs, TimeUnit.MILLISECONDS);
            return accepted
                    ? ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA)
                    : ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA,
                            FailureReason.REJECTED_BY_CONTROL_PLANE);
        } catch (TimeoutException e) {
            // The publish did not complete within connectTimeoutMs - report this connection as timed out rather
            // than letting this call block past the configured bound (FR-006). The helper thread above is
            // best-effort interrupted in the finally block; it is a daemon thread regardless, so it can never
            // keep the JVM alive if the underlying library call never actually returns.
            log.warn("Control Plane throttling data connection did not accept the health-check marker event "
                    + "within " + connectTimeoutMs + "ms");
            return ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA, FailureReason.TIMED_OUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA, FailureReason.TIMED_OUT,
                    e.getMessage());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.warn("Control Plane throttling data connection rejected the health-check marker event: "
                    + cause.getMessage());
            return ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA,
                    FailureReason.REJECTED_BY_CONTROL_PLANE, cause.getMessage());
        } finally {
            publishExecutor.shutdownNow();
        }
    }

    /**
     * Factory seam for the actual marker-event publish call, overridable so a test can substitute a
     * slow/blocking implementation to exercise the bounded-time wrapper in
     * {@link #confirmThrottlingData(DataPublisher, ConnectionCheckResult, int)} without depending on
     * {@code DataPublisher#tryPublish(Event)}'s own real (asynchronous) behavior.
     *
     * @param dataPublisher the already-logged-in, private {@code DataPublisher} instance to publish through
     * @return {@code true} if the Control Plane accepted the marker event, {@code false} otherwise
     * @throws Exception if the publish attempt itself fails
     */
    protected boolean publishMarkerEvent(DataPublisher dataPublisher) throws Exception {

        Event markerEvent = new Event(MARKER_STREAM_ID, System.currentTimeMillis(), null, null, null);
        return dataPublisher.tryPublish(markerEvent);
    }

    private void shutdownQuietly(DataPublisher dataPublisher) {

        if (dataPublisher == null) {
            return;
        }
        try {
            dataPublisher.shutdown();
        } catch (DataEndpointException e) {
            log.warn("Error shutting down the health-check DataPublisher instance: " + e.getMessage());
        }
    }

    /**
     * The final stage-2 results for both throttling-data connections, returned together since one login step
     * determines both.
     */
    public static final class ThrottlingDataAcceptConfirmationResult {

        private final ConnectionCheckResult throttlingData;
        private final ConnectionCheckResult throttlingDataSecure;

        public ThrottlingDataAcceptConfirmationResult(ConnectionCheckResult throttlingData,
                                                        ConnectionCheckResult throttlingDataSecure) {

            this.throttlingData = throttlingData;
            this.throttlingDataSecure = throttlingDataSecure;
        }

        public ConnectionCheckResult getThrottlingData() {

            return throttlingData;
        }

        public ConnectionCheckResult getThrottlingDataSecure() {

            return throttlingDataSecure;
        }
    }
}
