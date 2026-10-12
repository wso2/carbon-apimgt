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

import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.tm.healthcheck.ThrottlingDataAcceptConfirmationChecker.ThrottlingDataAcceptConfirmationResult;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TMControlPlaneConnectivityHealthChecker}, covering quickstart.md Scenario A (every
 * applicable connection {@code UP}), Scenario C (every applicable connection {@code DOWN}) - User Story 1
 * (T015) - and Scenario B (one or more, but not all, applicable connections {@code DOWN}) - User Story 2
 * (T016, T017).
 *
 * <p>Real sockets/{@code DataPublisher}/JMS/HTTPS calls are never exercised here: the checker's stage-1/stage-2
 * collaborators are each replaced with a Mockito mock via the protected factory methods added in
 * {@link TMControlPlaneConnectivityHealthChecker} for exactly this purpose, so these tests exercise only this
 * checker's own control flow (resolving which connections apply, combining per-connection results,
 * {@code overallStatus} derivation, and the {@code Properties}/{@code BadHealthException} shape) - matching the
 * same mock-the-collaborators style {@code DataSourceHealthCheckerTest} already uses for
 * {@code DataSourceHealthChecker}.</p>
 */
public class TMControlPlaneConnectivityHealthCheckerTest {

    private ControlPlaneConnectionConfigResolver configResolver;
    private PortResponsivenessChecker portResponsivenessChecker;
    private ThrottlingDataAcceptConfirmationChecker throttlingDataAcceptConfirmationChecker;
    private MessageBrokerAcceptConfirmationChecker messageBrokerAcceptConfirmationChecker;
    private ManagementApiAcceptConfirmationChecker managementApiAcceptConfirmationChecker;

    @Before
    public void setUp() {

        configResolver = mock(ControlPlaneConnectionConfigResolver.class);
        portResponsivenessChecker = mock(PortResponsivenessChecker.class);
        throttlingDataAcceptConfirmationChecker = mock(ThrottlingDataAcceptConfirmationChecker.class);
        messageBrokerAcceptConfirmationChecker = mock(MessageBrokerAcceptConfirmationChecker.class);
        managementApiAcceptConfirmationChecker = mock(ManagementApiAcceptConfirmationChecker.class);

        // Every connection resolves to *some* non-null config - stage 1's outcome is stubbed separately per
        // test, so the exact host/port value here is never asserted on, only that resolution itself succeeds.
        when(configResolver.resolve(any(ControlPlaneConnectionId.class))).thenAnswer(
                invocation -> new ResolvedConnectionConfig(invocation.getArgument(0), "cp.example.internal", 1234,
                        false));
        // TLS messaging is off in these tests, so only the 4 unconditional connections apply (8672 is skipped
        // entirely, never reported DOWN - data-model.md validation rule).
        when(configResolver.isTlsMessagingEnabled()).thenReturn(false);
    }

    /**
     * Builds the checker under test with every collaborator swapped for the mocks above.
     */
    private TMControlPlaneConnectivityHealthChecker createChecker() {

        return new TMControlPlaneConnectivityHealthChecker() {

            @Override
            protected ControlPlaneConnectionConfigResolver createConfigResolver() {

                return configResolver;
            }

            @Override
            protected PortResponsivenessChecker createPortResponsivenessChecker() {

                return portResponsivenessChecker;
            }

            @Override
            protected ThrottlingDataAcceptConfirmationChecker createThrottlingDataAcceptConfirmationChecker() {

                return throttlingDataAcceptConfirmationChecker;
            }

            @Override
            protected MessageBrokerAcceptConfirmationChecker createMessageBrokerAcceptConfirmationChecker() {

                return messageBrokerAcceptConfirmationChecker;
            }

            @Override
            protected ManagementApiAcceptConfirmationChecker createManagementApiAcceptConfirmationChecker() {

                return managementApiAcceptConfirmationChecker;
            }
        };
    }

    /**
     * Scenario A: every applicable connection {@code UP} -> {@code checkHealth()} returns normally with
     * {@code TMControlPlaneConnectivity.status=HEALTHY} and every connection's {@code .status=UP}, no
     * {@code failureReason} keys present.
     */
    @Test
    public void testAllConnectionsUpReportsHealthy() throws Exception {

        stubAllConnections(true);

        Properties result = createChecker().checkHealth();

        assertEquals("HEALTHY", result.getProperty("TMControlPlaneConnectivity.status"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.9611.status"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.9711.status"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.5672.status"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.management.status"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.9611.failureReason"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.9711.failureReason"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.5672.failureReason"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.management.failureReason"));
        // 8672 is not applicable (TLS messaging disabled in setUp()) - must be entirely absent, not DOWN.
        assertNull(result.getProperty("TMControlPlaneConnectivity.8672.status"));
    }

    /**
     * Scenario C: every applicable connection {@code DOWN} -> {@code checkHealth()} throws
     * {@code BadHealthException} with exactly one {@code HealthCheckError} per applicable connection, using the
     * {@code HC_TM_*} codes from T014.
     */
    @Test
    public void testAllConnectionsDownThrowsBadHealthExceptionWithOneErrorPerConnection() {

        stubAllConnections(false);

        try {
            createChecker().checkHealth();
            fail("Expected BadHealthException when every applicable connection is DOWN");
        } catch (BadHealthException e) {
            List<HealthCheckError> errors = e.getErrors();
            assertEquals(4, errors.size());

            Set<String> codes = new HashSet<>();
            for (HealthCheckError error : errors) {
                codes.add(error.getErrorCode());
                assertNotNull(error.getMessage());
                assertNotNull(error.getErrorDescription());
            }
            assertTrue(codes.contains("HC_TM_9611"));
            assertTrue(codes.contains("HC_TM_9711"));
            assertTrue(codes.contains("HC_TM_5672"));
            assertTrue(codes.contains("HC_TM_MGMT"));
        }
    }

    /**
     * Scenario B (stage-1 half): a single connection (5672) failing a plain network-level connect, while every
     * other applicable connection stays {@code UP} - {@code checkHealth()} must report {@code DEGRADED} and name
     * only 5672 as {@code DOWN}, with a stage-1-style {@code failureReason} (T016).
     */
    @Test
    public void testSingleConnectionDownViaStage1NetworkBlockReportsOnlyThatConnectionDegraded() throws Exception {

        stubAllConnections(true);
        ConnectionCheckResult messageBrokerDown = ConnectionCheckResult.down(ControlPlaneConnectionId.MESSAGE_BROKER,
                FailureReason.TIMED_OUT);
        when(portResponsivenessChecker.check(
                argThat(config -> config != null
                        && config.getConnectionId() == ControlPlaneConnectionId.MESSAGE_BROKER),
                anyInt())).thenReturn(messageBrokerDown);
        // A real MessageBrokerAcceptConfirmationChecker never attempts stage 2 once stage 1 has failed - it just
        // returns the stage-1 result unchanged, so the mock here is told to do the same.
        when(messageBrokerAcceptConfirmationChecker.check(eq(ControlPlaneConnectionId.MESSAGE_BROKER), any(), any(),
                anyInt())).thenReturn(messageBrokerDown);

        Properties result = createChecker().checkHealth();

        assertEquals("DEGRADED", result.getProperty("TMControlPlaneConnectivity.status"));
        assertEquals("DOWN", result.getProperty("TMControlPlaneConnectivity.5672.status"));
        assertEquals(FailureReason.TIMED_OUT, result.getProperty("TMControlPlaneConnectivity.5672.failureReason"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.9611.status"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.9611.failureReason"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.9711.status"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.9711.failureReason"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.management.status"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.management.failureReason"));
    }

    /**
     * Scenario B (stage-2 half): that same single connection (5672) now passes stage 1 but is rejected at
     * stage 2, while every other applicable connection stays {@code UP} - {@code checkHealth()} must still report
     * {@code DEGRADED}, naming only 5672 {@code DOWN}, but now with {@code failureReason = Rejected by Control
     * Plane} instead of a stage-1 reason (T016).
     */
    @Test
    public void testSingleConnectionDownViaStage2RejectionReportsOnlyThatConnectionDegraded() throws Exception {

        stubAllConnections(true);
        when(messageBrokerAcceptConfirmationChecker.check(eq(ControlPlaneConnectionId.MESSAGE_BROKER), any(), any(),
                anyInt())).thenReturn(ConnectionCheckResult.down(ControlPlaneConnectionId.MESSAGE_BROKER,
                        FailureReason.REJECTED_BY_CONTROL_PLANE));

        Properties result = createChecker().checkHealth();

        assertEquals("DEGRADED", result.getProperty("TMControlPlaneConnectivity.status"));
        assertEquals("DOWN", result.getProperty("TMControlPlaneConnectivity.5672.status"));
        assertEquals(FailureReason.REJECTED_BY_CONTROL_PLANE,
                result.getProperty("TMControlPlaneConnectivity.5672.failureReason"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.9611.status"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.9711.status"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.management.status"));
    }

    /**
     * Scenario B (multi-failure case): two (but not all four) applicable connections - 9611 and the management
     * connection - fail simultaneously, each for a different reason, while 9711 and 5672 stay {@code UP}.
     * {@code checkHealth()} must still report {@code DEGRADED} (never {@code UNHEALTHY}, since not every
     * connection is down) and must name each failing connection individually with its own correct
     * {@code failureReason}, leaving the healthy ones unaffected (T016, FR-004, FR-005).
     */
    @Test
    public void testMultipleConnectionsDownSimultaneouslyAreEachNamedIndividually() throws Exception {

        stubAllConnections(true);
        when(throttlingDataAcceptConfirmationChecker.check(any(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(new ThrottlingDataAcceptConfirmationResult(
                        ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA,
                                FailureReason.REJECTED_BY_CONTROL_PLANE),
                        ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA_SECURE)));
        when(managementApiAcceptConfirmationChecker.check(any(), any(), any(), anyInt(), any())).thenReturn(
                ConnectionCheckResult.down(ControlPlaneConnectionId.MANAGEMENT_API, FailureReason.CONNECTION_REFUSED));

        Properties result = createChecker().checkHealth();

        assertEquals("DEGRADED", result.getProperty("TMControlPlaneConnectivity.status"));
        assertEquals("DOWN", result.getProperty("TMControlPlaneConnectivity.9611.status"));
        assertEquals(FailureReason.REJECTED_BY_CONTROL_PLANE,
                result.getProperty("TMControlPlaneConnectivity.9611.failureReason"));
        assertEquals("DOWN", result.getProperty("TMControlPlaneConnectivity.management.status"));
        assertEquals(FailureReason.CONNECTION_REFUSED,
                result.getProperty("TMControlPlaneConnectivity.management.failureReason"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.9711.status"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.9711.failureReason"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.5672.status"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.5672.failureReason"));
    }

    /**
     * Hardening (T017): confirms the 9611-depends-on-9711-login edge case at the orchestrator level - when the
     * (mocked) throttling-data stage-2 step reports 9711 {@code DOWN} (its login failed) while 9611's own result
     * is {@code UP}, {@code checkHealth()} must pass both results through unchanged rather than letting 9711's
     * failure "contaminate" 9611's independently-determined status (data-model.md validation rule).
     */
    @Test
    public void testThrottlingDataStatusIsNeverOverriddenByThrottlingDataSecureFailure() throws Exception {

        stubAllConnections(true);
        when(throttlingDataAcceptConfirmationChecker.check(any(), any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(new ThrottlingDataAcceptConfirmationResult(
                        ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA),
                        ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA_SECURE,
                                FailureReason.AUTHENTICATION_FAILED)));

        Properties result = createChecker().checkHealth();

        assertEquals("DEGRADED", result.getProperty("TMControlPlaneConnectivity.status"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.9611.status"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.9611.failureReason"));
        assertEquals("DOWN", result.getProperty("TMControlPlaneConnectivity.9711.status"));
        assertEquals(FailureReason.AUTHENTICATION_FAILED,
                result.getProperty("TMControlPlaneConnectivity.9711.failureReason"));
    }

    /**
     * Hardening (T017): confirms the same 9611-depends-on-9711 edge case directly against the real (unmocked)
     * {@link ThrottlingDataAcceptConfirmationChecker}, exercising its actual early-return branch - the one path
     * through that class reachable without opening a real {@code DataPublisher} connection - rather than only
     * through a mock standing in for it.
     */
    @Test
    public void testThrottlingDataAcceptConfirmationCheckerSkipsLoginWhenSecureStage1Failed() {

        ThrottlingDataAcceptConfirmationChecker realChecker = new ThrottlingDataAcceptConfirmationChecker();
        ConnectionCheckResult throttlingDataStage1 = ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA);
        ConnectionCheckResult throttlingDataSecureStage1 = ConnectionCheckResult.down(
                ControlPlaneConnectionId.THROTTLING_DATA_SECURE, FailureReason.TIMED_OUT);

        ThrottlingDataAcceptConfirmationResult result = realChecker.check(throttlingDataStage1,
                throttlingDataSecureStage1, "tcp://localhost:9611", "ssl://localhost:9711", "admin", "admin", 3000);

        // No login was attempted (9711 was not even port-responsive) - both results come back as the exact same
        // stage-1 objects passed in, proving neither was replaced or mutated.
        assertSame(throttlingDataStage1, result.getThrottlingData());
        assertSame(throttlingDataSecureStage1, result.getThrottlingDataSecure());
    }

    /**
     * Convergence (T026, FR-006): {@code DataPublisher} construction only submits its login to an internal
     * background executor and returns immediately (confirmed against the real
     * {@code org.wso2.carbon.databridge.agent} source - {@code DataEndpoint.initialize()} hands off to a
     * single-thread executor rather than blocking), so a hang during that background login could not otherwise
     * surface as a hang in {@link ThrottlingDataAcceptConfirmationChecker#check}. This test substitutes a
     * deliberately slow {@link ThrottlingDataAcceptConfirmationChecker#createDataPublisher} via the factory seam
     * added for exactly this purpose, so the bounded-time wrapper itself is exercised deterministically -
     * without depending on {@code DataPublisher}'s own real (asynchronous) connection/login behavior, which
     * this test must not assume anything further about (see the class-level Javadoc's "Open question").
     */
    @Test
    public void testThrottlingDataLoginReturnsWithinBoundedTimeAndReportsTimedOutWhenLoginHangs() throws Exception {

        ThrottlingDataAcceptConfirmationChecker slowLoginChecker = new ThrottlingDataAcceptConfirmationChecker() {

            @Override
            protected org.wso2.carbon.databridge.agent.DataPublisher createDataPublisher(String receiverUrlGroup,
                    String authUrlGroup, String username, String password) {

                try {
                    Thread.sleep(10_000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                throw new AssertionError("Should have been abandoned once connect.timeout.ms elapsed");
            }
        };
        ConnectionCheckResult throttlingDataStage1 = ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA);
        ConnectionCheckResult throttlingDataSecureStage1 =
                ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA_SECURE);
        int shortTimeoutMs = 300;

        long startNanos = System.nanoTime();
        ThrottlingDataAcceptConfirmationResult result = slowLoginChecker.check(throttlingDataStage1,
                throttlingDataSecureStage1, "tcp://localhost:9611", "ssl://localhost:9711", "admin", "admin",
                shortTimeoutMs);
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

        assertFalse(result.getThrottlingData().isUp());
        assertEquals(FailureReason.TIMED_OUT, result.getThrottlingData().getFailureReason());
        assertFalse(result.getThrottlingDataSecure().isUp());
        assertEquals(FailureReason.TIMED_OUT, result.getThrottlingDataSecure().getFailureReason());
        assertTrue("Expected a bounded return (<" + (shortTimeoutMs + BOUND_MARGIN_MS) + "ms), took " + elapsedMs
                + "ms", elapsedMs < shortTimeoutMs + BOUND_MARGIN_MS);
    }

    /**
     * Convergence (T032, FR-006): unlike {@code DataPublisher} construction/login (bounded by T026 above),
     * {@code DataPublisher#tryPublish(Event)} itself previously ran with no timeout applied at all - a Control
     * Plane that accepted the 9711 login but then hung during the 9611 publish could still block
     * {@link ThrottlingDataAcceptConfirmationChecker#check} indefinitely. This test substitutes a deliberately
     * slow {@link ThrottlingDataAcceptConfirmationChecker#publishMarkerEvent} via the factory seam added for
     * exactly this purpose, so the bounded-time wrapper itself is exercised deterministically - without
     * depending on {@code DataPublisher}'s own real (asynchronous) publish behavior.
     */
    @Test
    public void testThrottlingDataPublishReturnsWithinBoundedTimeAndReportsTimedOutWhenPublishHangs()
            throws Exception {

        ThrottlingDataAcceptConfirmationChecker slowPublishChecker = new ThrottlingDataAcceptConfirmationChecker() {

            @Override
            protected org.wso2.carbon.databridge.agent.DataPublisher createDataPublisher(String receiverUrlGroup,
                    String authUrlGroup, String username, String password) {

                return mock(org.wso2.carbon.databridge.agent.DataPublisher.class);
            }

            @Override
            protected boolean publishMarkerEvent(org.wso2.carbon.databridge.agent.DataPublisher dataPublisher)
                    throws Exception {

                Thread.sleep(10_000L);
                throw new AssertionError("Should have been abandoned once connect.timeout.ms elapsed");
            }
        };
        ConnectionCheckResult throttlingDataStage1 = ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA);
        ConnectionCheckResult throttlingDataSecureStage1 =
                ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA_SECURE);
        int shortTimeoutMs = 300;

        long startNanos = System.nanoTime();
        ThrottlingDataAcceptConfirmationResult result = slowPublishChecker.check(throttlingDataStage1,
                throttlingDataSecureStage1, "tcp://localhost:9611", "ssl://localhost:9711", "admin", "admin",
                shortTimeoutMs);
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

        assertTrue(result.getThrottlingDataSecure().isUp());
        assertFalse(result.getThrottlingData().isUp());
        assertEquals(FailureReason.TIMED_OUT, result.getThrottlingData().getFailureReason());
        assertTrue("Expected a bounded return (<" + (shortTimeoutMs + BOUND_MARGIN_MS) + "ms), took " + elapsedMs
                + "ms", elapsedMs < shortTimeoutMs + BOUND_MARGIN_MS);
    }

    /**
     * Hardening (T017): with TLS messaging enabled, 8672 becomes applicable and must count toward
     * {@code overallStatus} like any other connection - a healthy 8672 alongside the other four healthy
     * connections must still report {@code HEALTHY}, not silently drop 8672 from consideration.
     */
    @Test
    public void testMessageBrokerSecureCountsTowardHealthyStatusWhenTlsMessagingEnabled() throws Exception {

        stubAllConnections(true);
        when(configResolver.isTlsMessagingEnabled()).thenReturn(true);

        Properties result = createChecker().checkHealth();

        assertEquals("HEALTHY", result.getProperty("TMControlPlaneConnectivity.status"));
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.8672.status"));
    }

    /**
     * Hardening (T017): with TLS messaging enabled, an 8672 failure must be named individually and must pull
     * {@code overallStatus} down to {@code DEGRADED} - it must never be treated as {@code DOWN} by omission when
     * disabled, but it must also never be silently ignored when it legitimately fails while enabled.
     */
    @Test
    public void testMessageBrokerSecureFailureDegradesStatusWhenTlsMessagingEnabled() throws Exception {

        stubAllConnections(true);
        when(configResolver.isTlsMessagingEnabled()).thenReturn(true);
        when(messageBrokerAcceptConfirmationChecker.check(eq(ControlPlaneConnectionId.MESSAGE_BROKER_SECURE), any(),
                any(), anyInt())).thenReturn(ConnectionCheckResult.down(ControlPlaneConnectionId.MESSAGE_BROKER_SECURE,
                        FailureReason.REJECTED_BY_CONTROL_PLANE));

        Properties result = createChecker().checkHealth();

        assertEquals("DEGRADED", result.getProperty("TMControlPlaneConnectivity.status"));
        assertEquals("DOWN", result.getProperty("TMControlPlaneConnectivity.8672.status"));
        assertEquals(FailureReason.REJECTED_BY_CONTROL_PLANE,
                result.getProperty("TMControlPlaneConnectivity.8672.failureReason"));
        // The plain 5672 sibling is a separate connection with its own result - 8672 failing must not affect it.
        assertEquals("UP", result.getProperty("TMControlPlaneConnectivity.5672.status"));
        assertNull(result.getProperty("TMControlPlaneConnectivity.5672.failureReason"));
    }

    /**
     * Scenario D (stage-1 half, T018): a stage-1 target that accepts the TCP connection but never completes
     * the TLS handshake (simulating a slow/non-responding target without depending on any real external
     * network path) must still make {@link PortResponsivenessChecker#check} return within a bounded time -
     * governed by the configured {@code connect.timeout.ms} - reporting the connection {@code DOWN} with
     * {@code failureReason = Timed out}, rather than blocking indefinitely (FR-006, SC-004).
     */
    @Test
    public void testStage1SlowNonRespondingTargetReturnsWithinBoundedTimeAndReportsDown() throws Exception {

        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            Thread nonRespondingServer = new Thread(() -> {
                try (Socket accepted = serverSocket.accept()) {
                    Thread.sleep(10_000L);
                } catch (Exception e) {
                    // The accepting thread exiting early (e.g. the test's own socket closing first) is fine -
                    // the client-side bound under test does not depend on this server thread's own outcome.
                }
            });
            nonRespondingServer.setDaemon(true);
            nonRespondingServer.start();

            ResolvedConnectionConfig config = new ResolvedConnectionConfig(
                    ControlPlaneConnectionId.THROTTLING_DATA_SECURE, "localhost", serverSocket.getLocalPort(), true);
            int shortTimeoutMs = 300;

            long startNanos = System.nanoTime();
            ConnectionCheckResult result = new PortResponsivenessChecker().check(config, shortTimeoutMs);
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

            assertFalse(result.isUp());
            assertEquals(FailureReason.TIMED_OUT, result.getFailureReason());
            assertTrue("Expected a bounded return (<" + (shortTimeoutMs + BOUND_MARGIN_MS) + "ms), took " + elapsedMs
                    + "ms", elapsedMs < shortTimeoutMs + BOUND_MARGIN_MS);
        }
    }

    /**
     * Scenario D (stage-2 half, T018): a stage-2 (management connection) target that accepts the HTTP
     * connection but never writes a response must still make
     * {@link ManagementApiAcceptConfirmationChecker#check} return within a bounded time, reporting the
     * connection {@code DOWN} with {@code failureReason = Rejected by Control Plane}, rather than blocking
     * indefinitely (FR-006, SC-004).
     */
    @Test
    public void testStage2SlowNonRespondingTargetReturnsWithinBoundedTimeAndReportsDown() throws Exception {

        try (ServerSocket serverSocket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            Thread nonRespondingServer = new Thread(() -> {
                try (Socket accepted = serverSocket.accept()) {
                    Thread.sleep(10_000L);
                } catch (Exception e) {
                    // See the comment in the stage-1 test above - this thread's own outcome is not under test.
                }
            });
            nonRespondingServer.setDaemon(true);
            nonRespondingServer.start();

            String serviceUrl = "http://localhost:" + serverSocket.getLocalPort();
            ConnectionCheckResult stage1Up = ConnectionCheckResult.up(ControlPlaneConnectionId.MANAGEMENT_API);
            int shortTimeoutMs = 300;

            long startNanos = System.nanoTime();
            ConnectionCheckResult result = new ManagementApiAcceptConfirmationChecker().check(serviceUrl, "admin",
                    "admin", shortTimeoutMs, stage1Up);
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

            assertFalse(result.isUp());
            assertEquals(FailureReason.REJECTED_BY_CONTROL_PLANE, result.getFailureReason());
            assertTrue("Expected a bounded return (<" + (shortTimeoutMs + BOUND_MARGIN_MS) + "ms), took " + elapsedMs
                    + "ms", elapsedMs < shortTimeoutMs + BOUND_MARGIN_MS);
        }
    }

    /**
     * Convergence (T027, FR-006): {@code JMSConnectionFactory}/the raw {@code javax.jms} API it delegates to
     * perform a genuinely synchronous, blocking connect/session/producer handshake with no timeout of their
     * own, so a Control Plane broker that accepts the TCP/TLS connect but hangs during that handshake could
     * otherwise block {@link MessageBrokerAcceptConfirmationChecker#check} indefinitely. This test substitutes
     * a deliberately slow {@link MessageBrokerAcceptConfirmationChecker#publishMarkerMessage} via the factory
     * seam added for exactly this purpose, so the bounded-time wrapper itself is exercised deterministically -
     * without depending on a real JMS provider's own connect/handshake behavior.
     */
    @Test
    public void testMessageBrokerPublishReturnsWithinBoundedTimeAndReportsTimedOutWhenPublishHangs()
            throws Exception {

        MessageBrokerAcceptConfirmationChecker slowPublishChecker = new MessageBrokerAcceptConfirmationChecker() {

            @Override
            protected void publishMarkerMessage(Properties jmsConnectionParameters) throws Exception {

                Thread.sleep(10_000L);
                throw new AssertionError("Should have been abandoned once connect.timeout.ms elapsed");
            }
        };
        ConnectionCheckResult stage1Up = ConnectionCheckResult.up(ControlPlaneConnectionId.MESSAGE_BROKER);
        int shortTimeoutMs = 300;

        long startNanos = System.nanoTime();
        ConnectionCheckResult result = slowPublishChecker.check(ControlPlaneConnectionId.MESSAGE_BROKER,
                new Properties(), stage1Up, shortTimeoutMs);
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

        assertFalse(result.isUp());
        assertEquals(FailureReason.TIMED_OUT, result.getFailureReason());
        assertTrue("Expected a bounded return (<" + (shortTimeoutMs + BOUND_MARGIN_MS) + "ms), took " + elapsedMs
                + "ms", elapsedMs < shortTimeoutMs + BOUND_MARGIN_MS);
    }

    /**
     * Generous margin added on top of the configured timeout when asserting a "bounded time" return, to absorb
     * test-environment scheduling jitter without weakening the actual property under test: the call must not
     * hang indefinitely, not that it must return in exactly {@code connect.timeout.ms}.
     */
    private static final long BOUND_MARGIN_MS = 5_000L;

    /**
     * Stubs every collaborator to report every connection either all {@code UP} or all {@code DOWN}.
     */
    private void stubAllConnections(boolean up) {

        if (up) {
            when(portResponsivenessChecker.check(any(ResolvedConnectionConfig.class), anyInt())).thenAnswer(
                    invocation -> ConnectionCheckResult.up(
                            ((ResolvedConnectionConfig) invocation.getArgument(0)).getConnectionId()));
            when(throttlingDataAcceptConfirmationChecker.check(any(), any(), any(), any(), any(), any(), anyInt()))
                    .thenReturn(new ThrottlingDataAcceptConfirmationResult(
                            ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA),
                            ConnectionCheckResult.up(ControlPlaneConnectionId.THROTTLING_DATA_SECURE)));
            when(messageBrokerAcceptConfirmationChecker.check(any(), any(), any(), anyInt())).thenAnswer(
                    invocation -> ConnectionCheckResult.up(invocation.getArgument(0)));
            when(managementApiAcceptConfirmationChecker.check(any(), any(), any(), anyInt(), any())).thenReturn(
                    ConnectionCheckResult.up(ControlPlaneConnectionId.MANAGEMENT_API));
        } else {
            when(portResponsivenessChecker.check(any(ResolvedConnectionConfig.class), anyInt())).thenAnswer(
                    invocation -> ConnectionCheckResult.down(
                            ((ResolvedConnectionConfig) invocation.getArgument(0)).getConnectionId(),
                            FailureReason.TIMED_OUT));
            when(throttlingDataAcceptConfirmationChecker.check(any(), any(), any(), any(), any(), any(), anyInt()))
                    .thenReturn(new ThrottlingDataAcceptConfirmationResult(
                            ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA,
                                    FailureReason.TIMED_OUT),
                            ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA_SECURE,
                                    FailureReason.TIMED_OUT)));
            when(messageBrokerAcceptConfirmationChecker.check(any(), any(), any(), anyInt())).thenAnswer(
                    invocation -> ConnectionCheckResult.down(invocation.getArgument(0), FailureReason.TIMED_OUT));
            when(managementApiAcceptConfirmationChecker.check(any(), any(), any(), anyInt(), any())).thenReturn(
                    ConnectionCheckResult.down(ControlPlaneConnectionId.MANAGEMENT_API, FailureReason.TIMED_OUT));
        }
    }
}
