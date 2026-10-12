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
import org.wso2.carbon.apimgt.tm.healthcheck.ThrottlingDataAcceptConfirmationChecker.ThrottlingDataAcceptConfirmationResult;
import org.wso2.carbon.healthcheck.api.core.AbstractHealthChecker;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Reports whether the Traffic Manager can reach, and actually use, the Control Plane on every connection
 * its Event Hub dependency relies on: the throttling-data connections (9611, 9711), the message broker
 * connections (5672, and 8672 when TLS messaging is configured), and the management/REST connection.
 *
 * <p>Registered as an OSGi {@code HealthChecker} service by
 * {@link org.wso2.carbon.apimgt.tm.healthcheck.internal.TMHealthCheckServiceComponent} and picked up
 * automatically by the existing, unmodified carbon-health-check {@code GET /health} endpoint - see
 * {@code contracts/health-checker-spi.md} for the full behavioral contract this class implements.</p>
 *
 */
public class TMControlPlaneConnectivityHealthChecker extends AbstractHealthChecker {

    private static final Log log = LogFactory.getLog(TMControlPlaneConnectivityHealthChecker.class);

    /**
     * Prefix shared by every {@code Properties} key this checker returns, e.g.
     * {@code TMControlPlaneConnectivity.status}, {@code TMControlPlaneConnectivity.9611.status}
     * (contracts/health-checker-spi.md "Properties keys returned on success").
     */
    private static final String RESPONSE_KEY_PREFIX = "TMControlPlaneConnectivity.";

    private static final String OVERALL_STATUS_KEY = RESPONSE_KEY_PREFIX + "status";
    private static final String STATUS_SUFFIX = ".status";
    private static final String LABEL_SUFFIX = ".label";
    private static final String FAILURE_REASON_SUFFIX = ".failureReason";

    private static final String OVERALL_STATUS_HEALTHY = "HEALTHY";
    private static final String OVERALL_STATUS_DEGRADED = "DEGRADED";
    private static final String OVERALL_STATUS_UNHEALTHY = "UNHEALTHY";

    /**
     * The fixed name this checker registers under. This MUST match the {@code name} attribute of this
     * checker's {@code <HealthChecker>} block in {@code health-check-config.xml} exactly, since that is
     * the join key {@code HealthCheckConfigParser} uses to bind the XML configuration (timeout, enabled,
     * order) to this particular service instance.
     */
    private static final String CHECKER_NAME = "TMControlPlaneConnectivityHealthChecker";

    /**
     * The {@code <Property>} key, under this checker's own {@code <HealthChecker>} block, that configures
     * the bounded per-connection connect timeout.
     */
    private static final String CONNECT_TIMEOUT_MS_PROPERTY = "connect.timeout.ms";

    /**
     * Human-readable stage label used only in the internal {@code WARN} log (Research &sect;5) - never in the
     * {@code checkHealth()} return value.
     */
    private static final String STAGE_PORT_RESPONSIVENESS = "port responsiveness";

    /**
     * Human-readable stage label used only in the internal {@code WARN} log, for a connection that passed
     * stage 1 but failed its accept-confirmation step.
     */
    private static final String STAGE_ACCEPT_CONFIRMATION = "accept confirmation";

    /**
     * Default connect timeout, in milliseconds, used when {@link #CONNECT_TIMEOUT_MS_PROPERTY} is absent
     * or malformed in configuration.
     */
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;

    /**
     * How many multiples of {@link #connectTimeoutMs} the overall, cross-group defensive bound in
     * {@link #runGroupsConcurrently(List)} allows for, on top of each group's own internal bounding (Convergence
     * T031, SC-004). The throttling-data group is the slowest single group - two sequential stage-1 checks plus
     * one bounded stage-2 login/publish, each up to {@code connectTimeoutMs} - so this must comfortably exceed
     * that group's own worst case; it is a last-resort safety net, not the expected path.
     */
    private static final int OVERALL_BOUND_MULTIPLIER = 4;

    /**
     * Names the per-group helper threads used by {@link #runGroupsConcurrently(List)}, and marks them daemon so
     * a group that fails to honour its own internal bound for any reason can never keep the JVM alive.
     */
    private static final ThreadFactory CHECK_THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "tm-healthcheck-connection-group");
        thread.setDaemon(true);
        return thread;
    };

    private int connectTimeoutMs = DEFAULT_CONNECT_TIMEOUT_MS;

    @Override
    public String getName() {

        return CHECKER_NAME;
    }

    @Override
    public void init(HealthCheckerConfig healthCheckerConfig) {

        super.init(healthCheckerConfig);
        connectTimeoutMs = readConnectTimeoutMs(healthCheckerConfig);
    }

    /**
     * Reads {@link #CONNECT_TIMEOUT_MS_PROPERTY} out of the given configuration, falling back to
     * {@link #DEFAULT_CONNECT_TIMEOUT_MS} (with a log message) when the property is absent or not a
     * valid integer - mirroring the fallback-with-log pattern carbon-health-check's own
     * {@code DataSourceHealthChecker.setHealthyPoolLimit()} uses for a malformed value.
     *
     * @param healthCheckerConfig this checker's parsed configuration
     * @return the connect timeout to use, in milliseconds
     */
    private int readConnectTimeoutMs(HealthCheckerConfig healthCheckerConfig) {

        Properties properties = healthCheckerConfig.getProperties();
        String configuredValue = properties == null ? null : properties.getProperty(CONNECT_TIMEOUT_MS_PROPERTY);
        if (configuredValue == null) {
            return DEFAULT_CONNECT_TIMEOUT_MS;
        }
        try {
            return Integer.parseInt(configuredValue.trim());
        } catch (NumberFormatException e) {
            log.info("Invalid value configured for '" + CONNECT_TIMEOUT_MS_PROPERTY + "': '" + configuredValue
                    + "' - falling back to the default of " + DEFAULT_CONNECT_TIMEOUT_MS + "ms");
            return DEFAULT_CONNECT_TIMEOUT_MS;
        }
    }

    /**
     * Returns the bounded per-connection connect timeout, in milliseconds, that every port-responsiveness
     * and accept-confirmation check must honour (FR-006). This bound is an internal safety value only and
     * must never itself appear in the response returned by {@link #checkHealth()} (FR-013).
     *
     * @return the configured connect timeout in milliseconds
     */
    protected int getConnectTimeoutMs() {

        return connectTimeoutMs;
    }

    /**
     * Factory seam for {@link ControlPlaneConnectionConfigResolver}, overridable so a test can substitute a
     * mock instead of reading live OSGi-bound configuration - mirrors the protected-method testability pattern
     * {@code DataSourceHealthChecker} already uses for its own dependencies.
     *
     * @return a new config resolver
     */
    protected ControlPlaneConnectionConfigResolver createConfigResolver() {

        return new ControlPlaneConnectionConfigResolver();
    }

    /**
     * Factory seam for {@link PortResponsivenessChecker}, overridable for the same reason as
     * {@link #createConfigResolver()} - lets a test substitute a mock instead of opening real sockets.
     *
     * @return a new port-responsiveness checker
     */
    protected PortResponsivenessChecker createPortResponsivenessChecker() {

        return new PortResponsivenessChecker();
    }

    /**
     * Factory seam for {@link ThrottlingDataAcceptConfirmationChecker}, overridable for the same reason as
     * {@link #createConfigResolver()} - lets a test substitute a mock instead of opening a real
     * {@code DataPublisher}.
     *
     * @return a new throttling-data accept-confirmation checker
     */
    protected ThrottlingDataAcceptConfirmationChecker createThrottlingDataAcceptConfirmationChecker() {

        return new ThrottlingDataAcceptConfirmationChecker();
    }

    /**
     * Factory seam for {@link MessageBrokerAcceptConfirmationChecker}, overridable for the same reason as
     * {@link #createConfigResolver()} - lets a test substitute a mock instead of opening a real JMS connection.
     *
     * @return a new message broker accept-confirmation checker
     */
    protected MessageBrokerAcceptConfirmationChecker createMessageBrokerAcceptConfirmationChecker() {

        return new MessageBrokerAcceptConfirmationChecker();
    }

    /**
     * Factory seam for {@link ManagementApiAcceptConfirmationChecker}, overridable for the same reason as
     * {@link #createConfigResolver()} - lets a test substitute a mock instead of making a real HTTPS call.
     *
     * @return a new management-API accept-confirmation checker
     */
    protected ManagementApiAcceptConfirmationChecker createManagementApiAcceptConfirmationChecker() {

        return new ManagementApiAcceptConfirmationChecker();
    }

    @Override
    public Properties checkHealth() throws BadHealthException {

        ControlPlaneConnectionConfigResolver configResolver = createConfigResolver();
        PortResponsivenessChecker portResponsivenessChecker = createPortResponsivenessChecker();

        List<ConnectionCheckGroup> groups = new ArrayList<>();
        groups.add(new ConnectionCheckGroup(
                Arrays.asList(ControlPlaneConnectionId.THROTTLING_DATA,
                        ControlPlaneConnectionId.THROTTLING_DATA_SECURE),
                () -> checkThrottlingDataConnections(configResolver, portResponsivenessChecker)));
        groups.add(new ConnectionCheckGroup(
                Collections.singletonList(ControlPlaneConnectionId.MESSAGE_BROKER),
                () -> Collections.singletonList(checkMessageBrokerConnection(configResolver,
                        portResponsivenessChecker, ControlPlaneConnectionId.MESSAGE_BROKER))));
        if (isTlsMessagingEnabled(configResolver)) {
            groups.add(new ConnectionCheckGroup(
                    Collections.singletonList(ControlPlaneConnectionId.MESSAGE_BROKER_SECURE),
                    () -> Collections.singletonList(checkMessageBrokerConnection(configResolver,
                            portResponsivenessChecker, ControlPlaneConnectionId.MESSAGE_BROKER_SECURE))));
        }
        groups.add(new ConnectionCheckGroup(
                Collections.singletonList(ControlPlaneConnectionId.MANAGEMENT_API),
                () -> Collections.singletonList(checkManagementConnection(configResolver,
                        portResponsivenessChecker))));

        List<ConnectionCheckResult> applicableResults = runGroupsConcurrently(groups);

        logApplicableFailures(applicableResults, configResolver);

        String overallStatus = deriveOverallStatus(applicableResults);

        if (OVERALL_STATUS_UNHEALTHY.equals(overallStatus)) {
            throw buildBadHealthException(applicableResults);
        }
        return buildProperties(overallStatus, applicableResults);
    }

    /**
     * Runs each of the given per-connection-group checks concurrently, one per helper thread, so the aggregate
     * worst-case latency of {@link #checkHealth()} is bounded by the single slowest group rather than the sum
     * of every group's own bounded time (Convergence T031, SC-004) - previously these groups ran strictly
     * sequentially, so a poll with multiple simultaneously-failing connections could take as long as the sum of
     * all of them (up to roughly 7-9x {@code connect.timeout.ms} in the worst case across all groups).
     *
     * <p>Each group already self-bounds its own worst-case time via {@code connect.timeout.ms} (stage 1's
     * Socket/TLS connect timeout, and the stage-2 bounded executors added for T026/T027); the shared deadline
     * computed here is purely defense-in-depth against a group that, for any reason, fails to honour that bound
     * itself, so {@link #checkHealth()} can still never hang indefinitely overall.</p>
     */
    private List<ConnectionCheckResult> runGroupsConcurrently(List<ConnectionCheckGroup> groups) {

        ExecutorService checkExecutor = Executors.newFixedThreadPool(groups.size(), CHECK_THREAD_FACTORY);
        try {
            List<Future<List<ConnectionCheckResult>>> futures = new ArrayList<>(groups.size());
            for (ConnectionCheckGroup group : groups) {
                futures.add(checkExecutor.submit(group.check));
            }
            long deadlineNanos = System.nanoTime()
                    + TimeUnit.MILLISECONDS.toNanos((long) connectTimeoutMs * OVERALL_BOUND_MULTIPLIER);

            List<ConnectionCheckResult> results = new ArrayList<>();
            for (int i = 0; i < groups.size(); i++) {
                results.addAll(awaitGroupResult(groups.get(i), futures.get(i), deadlineNanos));
            }
            return results;
        } finally {
            checkExecutor.shutdownNow();
        }
    }

    /**
     * Awaits one group's result, bounded by the time remaining until {@code deadlineNanos} (a single shared
     * deadline across every group in this invocation, not a fresh bound per group - so the groups running
     * concurrently cannot sum their individual bounds back into a sequential-length wait).
     */
    private List<ConnectionCheckResult> awaitGroupResult(ConnectionCheckGroup group,
            Future<List<ConnectionCheckResult>> future, long deadlineNanos) {

        long remainingNanos = Math.max(0, deadlineNanos - System.nanoTime());
        try {
            return future.get(remainingNanos, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            log.warn("Control Plane connection check group " + group.connectionIds + " did not complete within "
                    + "the overall bounded time - reporting it as timed out");
            return downAll(group.connectionIds, FailureReason.TIMED_OUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return downAll(group.connectionIds, FailureReason.TIMED_OUT);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.warn("Unexpected error checking " + group.connectionIds + ": " + cause.getMessage());
            return downAll(group.connectionIds, FailureReason.UNREACHABLE);
        }
    }

    private List<ConnectionCheckResult> downAll(List<ControlPlaneConnectionId> connectionIds, String failureReason) {

        List<ConnectionCheckResult> results = new ArrayList<>(connectionIds.size());
        for (ControlPlaneConnectionId connectionId : connectionIds) {
            results.add(ConnectionCheckResult.down(connectionId, failureReason));
        }
        return results;
    }

    /**
     * Pairs one independent, concurrently-runnable per-connection-group check with the connection IDs it is
     * responsible for, so a defensive timeout/error in {@link #awaitGroupResult} can still report the correct
     * IDs as {@code DOWN} without the check itself having run to completion.
     */
    private static final class ConnectionCheckGroup {

        private final List<ControlPlaneConnectionId> connectionIds;
        private final Callable<List<ConnectionCheckResult>> check;

        private ConnectionCheckGroup(List<ControlPlaneConnectionId> connectionIds,
                Callable<List<ConnectionCheckResult>> check) {

            this.connectionIds = connectionIds;
            this.check = check;
        }
    }

    /**
     * Runs both stages for 9611 and 9711 together, since one 9711 login determines both results. Any unexpected
     * error is caught here so it is reported as both connections being {@code DOWN} rather than propagating
     * (contracts/health-checker-spi.md "Postconditions").
     */
    private List<ConnectionCheckResult> checkThrottlingDataConnections(
            ControlPlaneConnectionConfigResolver configResolver,
            PortResponsivenessChecker portResponsivenessChecker) {

        try {
            ConnectionCheckResult throttlingDataStage1 = checkPortResponsiveness(configResolver,
                    portResponsivenessChecker, ControlPlaneConnectionId.THROTTLING_DATA);
            ConnectionCheckResult throttlingDataSecureStage1 = checkPortResponsiveness(configResolver,
                    portResponsivenessChecker, ControlPlaneConnectionId.THROTTLING_DATA_SECURE);

            ThrottlingDataAcceptConfirmationResult result = createThrottlingDataAcceptConfirmationChecker().check(
                    throttlingDataStage1, throttlingDataSecureStage1,
                    configResolver.getThrottlingDataReceiverUrlGroup(),
                    configResolver.getThrottlingDataAuthUrlGroup(),
                    configResolver.getEventHubUsername(), configResolver.getEventHubPassword(), connectTimeoutMs);

            List<ConnectionCheckResult> results = new ArrayList<>();
            results.add(result.getThrottlingData());
            results.add(result.getThrottlingDataSecure());
            return results;
        } catch (Throwable t) {
            log.warn("Unexpected error checking the Control Plane throttling data connections: " + t.getMessage());
            List<ConnectionCheckResult> results = new ArrayList<>();
            results.add(ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA,
                    FailureReason.UNREACHABLE, t.getMessage()));
            results.add(ConnectionCheckResult.down(ControlPlaneConnectionId.THROTTLING_DATA_SECURE,
                    FailureReason.UNREACHABLE, t.getMessage()));
            return results;
        }
    }

    private ConnectionCheckResult checkMessageBrokerConnection(ControlPlaneConnectionConfigResolver configResolver,
            PortResponsivenessChecker portResponsivenessChecker, ControlPlaneConnectionId connectionId) {

        try {
            ConnectionCheckResult stage1Result = checkPortResponsiveness(configResolver, portResponsivenessChecker,
                    connectionId);
            return createMessageBrokerAcceptConfirmationChecker().check(connectionId,
                    configResolver.getJmsConnectionParameters(), stage1Result, connectTimeoutMs);
        } catch (Throwable t) {
            log.warn("Unexpected error checking " + connectionId + ": " + t.getMessage());
            return ConnectionCheckResult.down(connectionId, FailureReason.UNREACHABLE, t.getMessage());
        }
    }

    private ConnectionCheckResult checkManagementConnection(ControlPlaneConnectionConfigResolver configResolver,
            PortResponsivenessChecker portResponsivenessChecker) {

        try {
            ConnectionCheckResult stage1Result = checkPortResponsiveness(configResolver, portResponsivenessChecker,
                    ControlPlaneConnectionId.MANAGEMENT_API);
            return createManagementApiAcceptConfirmationChecker().check(configResolver.getManagementServiceUrl(),
                    configResolver.getEventHubUsername(), configResolver.getEventHubPassword(), connectTimeoutMs,
                    stage1Result);
        } catch (Throwable t) {
            log.warn("Unexpected error checking the Control Plane management connection: " + t.getMessage());
            return ConnectionCheckResult.down(ControlPlaneConnectionId.MANAGEMENT_API, FailureReason.UNREACHABLE,
                    t.getMessage());
        }
    }

    /**
     * Resolves the live connection details for {@code connectionId} and runs stage 1 against them. A
     * connection that cannot currently be resolved from configuration (e.g. a malformed URL) is treated the
     * same as a failed stage-1 check, never as an exception (contracts/health-checker-spi.md "Postconditions").
     */
    private ConnectionCheckResult checkPortResponsiveness(ControlPlaneConnectionConfigResolver configResolver,
            PortResponsivenessChecker portResponsivenessChecker, ControlPlaneConnectionId connectionId) {

        ResolvedConnectionConfig config = configResolver.resolve(connectionId);
        if (config == null) {
            return ConnectionCheckResult.down(connectionId, FailureReason.UNREACHABLE);
        }
        return portResponsivenessChecker.check(config, connectTimeoutMs);
    }

    private boolean isTlsMessagingEnabled(ControlPlaneConnectionConfigResolver configResolver) {

        try {
            return configResolver.isTlsMessagingEnabled();
        } catch (Throwable t) {
            log.warn("Unexpected error checking whether TLS messaging is configured: " + t.getMessage());
            return false;
        }
    }

    /**
     * Logs every {@code DOWN} connection in {@code applicableResults} at {@code WARN}, once per failed poll -
     * not only on a healthy-to-unhealthy state transition - so a flapping connection still leaves a trail
     * (Research &sect;5, FR-009). This is purely an internal diagnostic log: none of the detail logged here
     * (resolved host/port, raw error detail) is ever placed on the {@code checkHealth()} return value
     * (FR-013, FR-014).
     */
    private void logApplicableFailures(List<ConnectionCheckResult> applicableResults,
            ControlPlaneConnectionConfigResolver configResolver) {

        for (ConnectionCheckResult result : applicableResults) {
            if (!result.isUp()) {
                logFailure(result, configResolver);
            }
        }
    }

    /**
     * Logs one individual connection failure, naming the checker, the connection's plain-language label, which
     * stage failed (port responsiveness vs. accept confirmation, derived from the failure reason - Research
     * &sect;5), the resolved host/port re-resolved fresh for this log line (never cached - FR-012), and any raw
     * underlying error detail captured along the way.
     */
    private void logFailure(ConnectionCheckResult result, ControlPlaneConnectionConfigResolver configResolver) {

        ControlPlaneConnectionId connectionId = result.getConnectionId();
        String stage = isStage1FailureReason(result.getFailureReason())
                ? STAGE_PORT_RESPONSIVENESS : STAGE_ACCEPT_CONFIRMATION;
        ResolvedConnectionConfig config = resolveQuietlyForLogging(configResolver, connectionId);
        String target = config == null ? "unresolved" : config.getHost() + ":" + config.getPort();
        String rawDetail = result.getRawDetail();

        log.warn(CHECKER_NAME + ": Control Plane connection is DOWN - " + connectionId.getLabel() + " ["
                + connectionId + "], stage=" + stage + ", target=" + target + ", failureReason="
                + result.getFailureReason() + (rawDetail == null ? "" : ", detail=" + rawDetail));
    }

    /**
     * Distinguishes a stage-1 (port-responsiveness) failure reason from a stage-2 (accept-confirmation) one,
     * purely for the internal log's benefit - the two stage-1 reasons ({@link FailureReason#UNREACHABLE},
     * {@link FailureReason#TIMED_OUT}, {@link FailureReason#CONNECTION_REFUSED}) are produced only by
     * {@link PortResponsivenessChecker}; the other two ({@link FailureReason#REJECTED_BY_CONTROL_PLANE},
     * {@link FailureReason#AUTHENTICATION_FAILED}) only by a stage-2 accept-confirmation checker.
     */
    private boolean isStage1FailureReason(String failureReason) {

        return FailureReason.UNREACHABLE.equals(failureReason) || FailureReason.TIMED_OUT.equals(failureReason)
                || FailureReason.CONNECTION_REFUSED.equals(failureReason);
    }

    /**
     * Re-resolves the live connection details for {@code connectionId}, for this log line only - never cached
     * from the earlier stage-1 resolution (FR-012). Swallows any error so that a logging failure can never
     * affect {@code checkHealth()}'s own outcome.
     */
    private ResolvedConnectionConfig resolveQuietlyForLogging(ControlPlaneConnectionConfigResolver configResolver,
            ControlPlaneConnectionId connectionId) {

        try {
            return configResolver.resolve(connectionId);
        } catch (Throwable t) {
            log.warn("Could not re-resolve " + connectionId + " for the failure log entry: " + t.getMessage());
            return null;
        }
    }

    /**
     * Pure function over the applicable connections' statuses (data-model.md, FR-003, FR-005): all {@code UP} ->
     * {@code HEALTHY}; some (but not all) {@code DOWN} -> {@code DEGRADED}; all {@code DOWN} -> {@code
     * UNHEALTHY}.
     */
    private String deriveOverallStatus(List<ConnectionCheckResult> applicableResults) {

        int upCount = 0;
        for (ConnectionCheckResult result : applicableResults) {
            if (result.isUp()) {
                upCount++;
            }
        }
        if (upCount == applicableResults.size()) {
            return OVERALL_STATUS_HEALTHY;
        }
        if (upCount == 0) {
            return OVERALL_STATUS_UNHEALTHY;
        }
        return OVERALL_STATUS_DEGRADED;
    }

    /**
     * Builds the {@code Properties} bag returned when {@code overallStatus} is {@code HEALTHY} or {@code
     * DEGRADED} (contracts/health-checker-spi.md "Properties keys returned on success").
     */
    private Properties buildProperties(String overallStatus, List<ConnectionCheckResult> applicableResults) {

        Properties properties = new Properties();
        properties.setProperty(OVERALL_STATUS_KEY, overallStatus);
        for (ConnectionCheckResult result : applicableResults) {
            String keyPrefix = RESPONSE_KEY_PREFIX + result.getConnectionId().getResponseKeySegment();
            properties.setProperty(keyPrefix + STATUS_SUFFIX, result.isUp() ? "UP" : "DOWN");
            properties.setProperty(keyPrefix + LABEL_SUFFIX, result.getConnectionId().getLabel());
            if (!result.isUp()) {
                properties.setProperty(keyPrefix + FAILURE_REASON_SUFFIX, result.getFailureReason());
            }
        }
        return properties;
    }

    /**
     * Builds the {@code BadHealthException} thrown when every applicable connection is {@code DOWN}
     * (contracts/health-checker-spi.md "Error codes used in the UNHEALTHY (thrown) path").
     */
    private BadHealthException buildBadHealthException(List<ConnectionCheckResult> applicableResults) {

        List<HealthCheckError> errors = new ArrayList<>();
        for (ConnectionCheckResult result : applicableResults) {
            String errorCode = "HC_TM_" + result.getConnectionId().getErrorCodeSegment();
            errors.add(new HealthCheckError(errorCode, result.getConnectionId().getLabel(),
                    result.getFailureReason()));
        }
        return new BadHealthException(CHECKER_NAME + ": total Control Plane connectivity loss", errors);
    }
}
