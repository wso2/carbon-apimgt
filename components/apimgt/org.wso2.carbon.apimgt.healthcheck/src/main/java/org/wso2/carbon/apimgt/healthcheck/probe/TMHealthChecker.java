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

package org.wso2.carbon.apimgt.healthcheck.probe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.healthcheck.internal.TMHealthCheckerDataHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.healthcheck.api.core.AbstractHealthChecker;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Carbon health check plug-in for the Traffic Manager. On every {@code GET /health} call it checks, at the same time:
 * <ul>
 *   <li>the throttle event receiver (9611 + offset) - sends one test event;</li>
 *   <li>the message broker (5672 + offset, and 8672 + offset for SSL) - sends one test message to itself;</li>
 *   <li>how many worker threads the throttle event receiver has left ({@link ReceiverThreadPoolMonitor}).</li>
 * </ul>
 * <p>{@link EndpointResolver} works out the addresses and {@link ProbeClient} probes each one.</p>
 * <p>Addresses come only from the Traffic Manager's own {@code [apim.throttling]} configuration, so the result does
 * not depend on the Control Plane or Gateways being up. Nothing is cached between calls.</p>
 *
 * <p>Result of one call - always returned as {@link Properties}, HTTP 200, never thrown as an exception:</p>
 * <ul>
 *   <li>every address passed: {@code UP} (or {@code DEGRADED} if one was slower than {@code slow.threshold.ms});</li>
 *   <li>some, but not all, addresses across both checks failed: {@code DEGRADED};</li>
 *   <li>every address across both checks failed (or none could be resolved): {@code DOWN}.</li>
 * </ul>
 * <p>This checker never throws {@code BadHealthException}, so {@code GET /health} always answers HTTP 200 for it;
 * the severity is only in the {@code tm.health.status} value. The {@code carbon-health-check} framework bundles
 * every registered checker (datasource, userstore, this one, ...) into one response and turns the <em>whole</em>
 * response into a non-2xx status if <em>any</em> one checker throws - so a thrown exception here would also hide
 * an unrelated checker's good result behind an error status. Reading {@code tm.throttle.receiver.status} and
 * {@code tm.message.broker.status} individually still tells a caller exactly which part, if any, is down.</p>
 *
 * <p>The whole call is limited by {@code probe.timeout.ms}, so {@code /health} can never hang.</p>
 */
public class TMHealthChecker extends AbstractHealthChecker {

    private static final Log log = LogFactory.getLog(TMHealthChecker.class);

    /**
     * Must match the {@code name} of this checker's {@code <HealthChecker>} block in {@code health-check-config.xml}.
     */
    public static final String CHECKER_NAME = "TMHealthChecker";

    static final String PROBE_TIMEOUT_MS_PROPERTY = "probe.timeout.ms";
    static final String SLOW_THRESHOLD_MS_PROPERTY = "slow.threshold.ms";
    static final String STREAM_ID_PROPERTY = "stream.id";
    static final String RECEIVE_TIMEOUT_MS_PROPERTY = "broker.receive.timeout.ms";
    static final String CHECK_SSL_PORT_PROPERTY = "broker.ssl.port.enabled";

    static final int DEFAULT_PROBE_TIMEOUT_MS = 10000;
    static final int DEFAULT_SLOW_THRESHOLD_MS = 2000;

    /**
     * Name and version of the marker stream this checker's test event is published to. Registered programmatically
     * by {@code TMHealthCheckerServiceComponent} on activation (and removed on deactivation), instead of a static
     * {@code eventstreams} resource file, so the stream only exists while this bundle is active.
     */
    public static final String MARKER_STREAM_NAME = "org.wso2.carbon.apimgt.healthcheck.marker";
    public static final String MARKER_STREAM_VERSION = "1.0.0";

    static final String DEFAULT_STREAM_ID = MARKER_STREAM_NAME + ":" + MARKER_STREAM_VERSION;
    static final int DEFAULT_RECEIVE_TIMEOUT_MS = 5000;

    static final String STATUS_UP = "UP";
    static final String STATUS_DEGRADED = "DEGRADED";
    static final String STATUS_DOWN = "DOWN";

    /**
     * Only ever used for a throttle receiver endpoint's own {@code .status}, when the 9711 login step failed - the
     * publish (9611) side was never attempted, so calling it {@code DOWN} with a "port not reachable"-style
     * message would be misleading. Still counted as a failure for {@code tm.throttle.receiver.status}/
     * {@code tm.health.status}, which only ever look at {@link ProbeResult#isSuccess()}, not this label.
     */
    static final String STATUS_UNKNOWN = "UNKNOWN";

    private static final String OVERALL_STATUS_KEY = "tm.health.status";
    private static final String THREADS_PREFIX = CheckType.THROTTLE_RECEIVER.getKeyPrefix() + ".threads.";
    private static final String THREADS_UNKNOWN_ERROR_CODE = "TM_HC_THREAD_POOL_SIZE_UNKNOWN";
    private static final String THREADS_UNKNOWN_ERROR_MESSAGE =
            "The throttle receiver's thread pool sizes could not be read";

    private static final ThreadFactory PROBE_THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "tm-healthcheck-probe");
        thread.setDaemon(true);
        return thread;
    };

    private final EndpointResolver endpointResolver;
    private final ReceiverThreadPoolMonitor threadPoolMonitor;

    /**
     * Addresses whose probe is still running. A new call does not start a second probe for the same address, so
     * stuck probes cannot pile up threads when {@code /health} is called often.
     */
    private final Set<String> probesInFlight = ConcurrentHashMap.newKeySet();

    private int probeTimeoutMs = DEFAULT_PROBE_TIMEOUT_MS;
    private int slowThresholdMs = DEFAULT_SLOW_THRESHOLD_MS;
    private String streamId = DEFAULT_STREAM_ID;
    private int receiveTimeoutMs = DEFAULT_RECEIVE_TIMEOUT_MS;
    private boolean checkSslPort = true;

    /**
     * Created with no arguments and registered as a {@code HealthChecker} OSGi service, the same way carbon health
     * check registers its own {@code DataSourceHealthChecker}. The Traffic Manager configuration is read from
     * {@link TMHealthCheckerDataHolder}, which this bundle's OSGi component fills in.
     */
    public TMHealthChecker() {

        this(new ReceiverThreadPoolMonitor(TMHealthCheckerDataHolder::getReceiverThreadPoolSize));
    }

    /**
     * @param threadPoolMonitor the same monitor that is registered as the JMX view of the receiver's thread pools
     */
    public TMHealthChecker(ReceiverThreadPoolMonitor threadPoolMonitor) {

        this(new EndpointResolver(TMHealthCheckerDataHolder::getTrafficManagerConfiguration, APIUtil::getPortOffset),
                threadPoolMonitor);
    }

    TMHealthChecker(EndpointResolver endpointResolver) {

        this(endpointResolver, new ReceiverThreadPoolMonitor(configName -> null));
    }

    TMHealthChecker(EndpointResolver endpointResolver, ReceiverThreadPoolMonitor threadPoolMonitor) {

        this.endpointResolver = endpointResolver;
        this.threadPoolMonitor = threadPoolMonitor;
    }

    @Override
    public String getName() {

        return CHECKER_NAME;
    }

    @Override
    public void init(HealthCheckerConfig healthCheckerConfig) {

        super.init(healthCheckerConfig);
        Properties properties = healthCheckerConfig == null ? null : healthCheckerConfig.getProperties();
        probeTimeoutMs = readInt(properties, PROBE_TIMEOUT_MS_PROPERTY, DEFAULT_PROBE_TIMEOUT_MS);
        slowThresholdMs = readInt(properties, SLOW_THRESHOLD_MS_PROPERTY, DEFAULT_SLOW_THRESHOLD_MS);
        receiveTimeoutMs = readInt(properties, RECEIVE_TIMEOUT_MS_PROPERTY, DEFAULT_RECEIVE_TIMEOUT_MS);
        String configuredStreamId = properties == null ? null : properties.getProperty(STREAM_ID_PROPERTY);
        streamId = configuredStreamId == null || configuredStreamId.trim().isEmpty()
                ? DEFAULT_STREAM_ID : configuredStreamId.trim();
        String sslPort = properties == null ? null : properties.getProperty(CHECK_SSL_PORT_PROPERTY);
        checkSslPort = sslPort == null || !"false".equalsIgnoreCase(sslPort.trim());
    }

    @Override
    public Properties checkHealth() {

        // Before probing, so this checker's own test connections are not counted as busy threads.
        List<ThreadPoolUsage> threadPools = threadPoolMonitor.snapshot();
        String threadsStatus = threadPoolMonitor.statusOf(threadPools);

        List<ProbeResult> results = new ArrayList<>();
        List<ProbeEndpoint> endpoints = new ArrayList<>();

        List<ProbeEndpoint> receivers = endpointResolver.resolveThrottleReceivers();
        if (receivers.isEmpty()) {
            results.add(ProbeResult.unresolved(CheckType.THROTTLE_RECEIVER,
                    "No receiver URL could be resolved from the throttling configuration"));
        }
        endpoints.addAll(receivers);

        List<ProbeEndpoint> brokers = endpointResolver.resolveMessageBrokers(checkSslPort);
        if (brokers.isEmpty()) {
            results.add(ProbeResult.unresolved(CheckType.MESSAGE_BROKER,
                    "No broker URL could be resolved from the throttling JMS configuration"));
        }
        endpoints.addAll(brokers);

        results.addAll(probeAll(endpoints));
        logFailures(results);
        if (STATUS_DOWN.equals(threadsStatus)) {
            log.warn(CHECKER_NAME + ": throttle receiver has no worker threads left - new Gateway connections "
                    + "will not be served");
        }

        String status = overallStatus(results);
        if (STATUS_DOWN.equals(threadsStatus) && STATUS_UP.equals(status)) {
            // The receiver cannot take new connections, so it is down even if this call's probe got through.
            status = STATUS_DEGRADED;
        }
        Properties properties = buildProperties(status, results);
        addThreadPools(properties, threadsStatus, threadPools);
        HealthMessages.add(properties, results, threadPools);
        return properties;
    }

    /**
     * Healthy answer, for example:
     * <pre>
     * tm.throttle.receiver.threads.status = UP
     * tm.throttle.receiver.threads.tcp.remaining = 100
     * tm.throttle.receiver.threads.ssl.remaining = 100
     * </pre>
     * {@code status} is {@code UP} while every pool has at least one thread left and {@code DOWN} once a pool has
     * none - which also makes {@code tm.throttle.receiver.status} {@code DOWN}. When the pool sizes cannot be read,
     * {@code status} is {@code UNKNOWN} with {@code .error.code}/{@code .error.message}, no counts are shown, and
     * nothing else in the response changes.
     */
    private void addThreadPools(Properties properties, String threadsStatus, List<ThreadPoolUsage> threadPools) {

        properties.setProperty(THREADS_PREFIX + "status", threadsStatus);
        if (STATUS_UNKNOWN.equals(threadsStatus)) {
            properties.setProperty(THREADS_PREFIX + "error.code", THREADS_UNKNOWN_ERROR_CODE);
            properties.setProperty(THREADS_PREFIX + "error.message", THREADS_UNKNOWN_ERROR_MESSAGE);
            return;
        }
        for (ThreadPoolUsage usage : threadPools) {
            if (usage.isKnown()) {
                properties.setProperty(THREADS_PREFIX + usage.getPool() + ".remaining",
                        String.valueOf(usage.getFree()));
            }
        }
        if (STATUS_DOWN.equals(threadsStatus)) {
            properties.setProperty(CheckType.THROTTLE_RECEIVER.getKeyPrefix() + ".status", STATUS_DOWN);
        }
    }

    /**
     * Probes every endpoint at the same time, each on its own helper thread, and waits at most
     * {@code probe.timeout.ms} in total. An endpoint that has not answered by then is reported as {@code TIMEOUT}.
     */
    private List<ProbeResult> probeAll(List<ProbeEndpoint> endpoints) {

        List<ProbeResult> results = new ArrayList<>(endpoints.size());
        if (endpoints.isEmpty()) {
            return results;
        }
        ProbeClient probeClient = createProbeClient(streamId, receiveTimeoutMs);
        ExecutorService executor = Executors.newFixedThreadPool(endpoints.size(), PROBE_THREAD_FACTORY);
        try {
            List<Future<ProbeResult>> futures = new ArrayList<>(endpoints.size());
            for (ProbeEndpoint endpoint : endpoints) {
                futures.add(startProbe(executor, probeClient, endpoint));
            }
            long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(probeTimeoutMs);
            for (int i = 0; i < endpoints.size(); i++) {
                results.add(awaitResult(endpoints.get(i), futures.get(i), deadlineNanos));
            }
            return results;
        } finally {
            // Stops waiting; a thread blocked in a network call ends on its own once that call times out.
            executor.shutdownNow();
        }
    }

    /**
     * @return the running probe, or {@code null} if a probe for this address is still running from an earlier call
     */
    private Future<ProbeResult> startProbe(ExecutorService executor, ProbeClient probeClient,
                                           ProbeEndpoint endpoint) {

        String key = endpoint.getUrl();
        if (!probesInFlight.add(key)) {
            return null;
        }
        try {
            return executor.submit(() -> {
                try {
                    return probeClient.probe(endpoint);
                } finally {
                    probesInFlight.remove(key);
                }
            });
        } catch (RuntimeException e) {
            probesInFlight.remove(key);
            throw e;
        }
    }

    private ProbeResult awaitResult(ProbeEndpoint endpoint, Future<ProbeResult> future, long deadlineNanos) {

        if (future == null) {
            return ProbeResult.failure(endpoint, ProbeStage.TIMEOUT,
                    "An earlier probe of this address has not finished yet", probeTimeoutMs);
        }
        try {
            long remainingNanos = Math.max(0, deadlineNanos - System.nanoTime());
            return future.get(remainingNanos, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            return ProbeResult.failure(endpoint, ProbeStage.TIMEOUT,
                    "No answer within " + probeTimeoutMs + " ms", probeTimeoutMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ProbeResult.failure(endpoint, ProbeStage.TIMEOUT, "Interrupted while waiting", probeTimeoutMs);
        } catch (ExecutionException e) {
            return ProbeResult.failure(endpoint, ProbeStage.UNEXPECTED_ERROR, String.valueOf(e.getCause()), 0);
        }
    }

    /**
     * Factory seam so a test can supply a fake probe client.
     */
    protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

        return new ProbeClient(streamId, receiveTimeoutMs);
    }

    /**
     * {@code DOWN} only if <b>every</b> address across both checks failed (or none could be resolved) - losing one
     * check entirely while the other still works is {@code DEGRADED}, not {@code DOWN}. {@code DEGRADED} if some
     * addresses failed or were slow; otherwise {@code UP}.
     */
    private String overallStatus(List<ProbeResult> results) {

        return statusFrom(results);
    }

    /**
     * The status of one check on its own, using the same rule as {@link #overallStatus}, but only over that
     * check's own addresses: {@code DOWN} if none of them passed, {@code DEGRADED} if some failed or were slow,
     * otherwise {@code UP}. Shown per-check in the response (e.g. {@code tm.throttle.receiver.status}) so a caller
     * can see exactly which part is down even when the overall status is only {@code DEGRADED}.
     */
    private String checkStatus(CheckType checkType, List<ProbeResult> results) {

        List<ProbeResult> own = new ArrayList<>();
        for (ProbeResult result : results) {
            if (result.getCheckType() == checkType) {
                own.add(result);
            }
        }
        return statusFrom(own);
    }

    /**
     * {@code DOWN} if nothing in {@code results} passed; {@code DEGRADED} if some passed and some did not, or one
     * was slower than {@code slow.threshold.ms}; {@code UP} if everything passed and nothing was slow.
     */
    private String statusFrom(List<ProbeResult> results) {

        int passed = 0;
        boolean slow = false;
        for (ProbeResult result : results) {
            if (result.isSuccess()) {
                passed++;
                slow = slow || result.getLatencyMs() > slowThresholdMs;
            }
        }
        if (passed == 0) {
            return STATUS_DOWN;
        }
        return passed < results.size() || slow ? STATUS_DEGRADED : STATUS_UP;
    }

    /**
     * Healthy answer, for example:
     * <pre>
     * tm.health.status = UP
     * tm.throttle.receiver.status = UP
     * tm.throttle.receiver.endpoint.9611.source = apim.throttling
     * tm.throttle.receiver.endpoint.9611.status = UP
     * tm.throttle.receiver.endpoint.9611.auth.status = UP
     * tm.message.broker.status = UP
     * tm.message.broker.endpoint.5672.status = UP
     * ...
     * </pre>
     * A failed address also gets {@code .error.code} and {@code .error.message}. A throttle receiver endpoint's
     * {@code .auth.status} (the 9711 login step) is extra detail only - it is never counted towards
     * {@code tm.health.status} or {@code tm.throttle.receiver.status}, which still mean exactly what they meant
     * before: whether a Gateway could get an event all the way through (login, then publish).
     *
     * <p>The resolved host/address is deliberately never put on the response as its own value, anywhere - only
     * the port number appears, as part of the key name.</p>
     *
     * <p>When the 9711 login step itself is what failed, publish (9611) was never even attempted - so that
     * endpoint's {@code .status} is {@code UNKNOWN} rather than {@code DOWN}, and its {@code .error.code} is
     * {@code TM_HC_NOT_ATTEMPTED} rather than the (misleading, since 9611 was never touched) real network error.
     * The real reason login failed moves to that same endpoint's {@code .auth.error.code}/
     * {@code .auth.error.message} instead, e.g.:</p>
     * <pre>
     * tm.throttle.receiver.endpoint.9611.status = UNKNOWN
     * tm.throttle.receiver.endpoint.9611.error.code = TM_HC_NOT_ATTEMPTED
     * tm.throttle.receiver.endpoint.9611.auth.status = DOWN
     * tm.throttle.receiver.endpoint.9611.auth.error.code = TM_HC_PORT_CLOSED
     * </pre>
     *
     * <p><b>Caveat:</b> {@code endpoint.<port>} is keyed by port number, not a running index. Two nodes behind a
     * load-balanced or failover address that share the same port would collide on this key, and only the last one
     * probed would survive in the response - acceptable for the common single-node-per-port case this is written
     * for, but worth knowing before relying on this response shape for a multi-node setup.</p>
     */
    private Properties buildProperties(String status, List<ProbeResult> results) {

        Properties properties = new Properties();
        properties.setProperty(OVERALL_STATUS_KEY, status);
        for (CheckType checkType : CheckType.values()) {
            String checkPrefix = checkType.getKeyPrefix() + ".";
            properties.setProperty(checkPrefix + "status", checkStatus(checkType, results));
            for (ProbeResult result : results) {
                if (result.getCheckType() != checkType) {
                    continue;
                }
                String key = result.getEndpoint() != null ? String.valueOf(result.getEndpoint().getPort())
                        : "unresolved";
                String prefix = checkPrefix + "endpoint." + key;
                if (result.getEndpoint() != null) {
                    properties.setProperty(prefix + ".source", result.getEndpoint().getSource());
                }
                // When 9711's own login is what failed, 9611's publish was never attempted at all - say so,
                // rather than reporting a network-level reason for a connection that was never touched. Guarded
                // by getEndpoint() != null so the unrelated "no address could be resolved at all" case (which
                // also carries authSucceeded=false, but for a completely different reason) keeps its own
                // CONFIG_UNRESOLVED message instead of being mislabeled as this.
                boolean notAttempted = checkType == CheckType.THROTTLE_RECEIVER && result.getEndpoint() != null
                        && !result.isSuccess() && !result.isAuthSucceeded();
                if (notAttempted) {
                    properties.setProperty(prefix + ".status", STATUS_UNKNOWN);
                    properties.setProperty(prefix + ".error.code", ProbeStage.NOT_ATTEMPTED.getErrorCode());
                    properties.setProperty(prefix + ".error.message", ProbeStage.NOT_ATTEMPTED.getMessage());
                } else if (result.isSuccess()) {
                    properties.setProperty(prefix + ".status", STATUS_UP);
                } else {
                    properties.setProperty(prefix + ".status", STATUS_DOWN);
                    properties.setProperty(prefix + ".error.code", result.getFailedStage().getErrorCode());
                    properties.setProperty(prefix + ".error.message", result.getFailedStage().getMessage());
                }
                if (checkType == CheckType.THROTTLE_RECEIVER && result.getEndpoint() != null) {
                    properties.setProperty(prefix + ".auth.status",
                            result.isAuthSucceeded() ? STATUS_UP : STATUS_DOWN);
                    if (!result.isAuthSucceeded()) {
                        // The real failure - it happened during login, so result.getFailedStage() is about 9711.
                        properties.setProperty(prefix + ".auth.error.code", result.getFailedStage().getErrorCode());
                        properties.setProperty(prefix + ".auth.error.message", result.getFailedStage().getMessage());
                    }
                }
            }
        }
        return properties;
    }

    private void logFailures(List<ProbeResult> results) {

        for (ProbeResult result : results) {
            if (!result.isSuccess()) {
                log.warn(CHECKER_NAME + ": " + result.getCheckType().getLabel() + " check failed - target="
                        + result.getTarget() + ", stage=" + result.getFailedStage() + ", detail="
                        + result.getDetail());
            }
        }
    }

    private static int readInt(Properties properties, String key, int defaultValue) {

        String value = properties == null ? null : properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid value '" + value + "' for health checker property '" + key + "', using "
                    + defaultValue);
            return defaultValue;
        }
    }
}
