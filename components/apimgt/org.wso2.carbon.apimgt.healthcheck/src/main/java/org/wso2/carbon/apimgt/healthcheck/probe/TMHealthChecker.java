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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.healthcheck.api.core.AbstractHealthChecker;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Traffic Manager health checker that probes the throttle event receiver, the message broker and the receiver's
 * worker thread pools in parallel, using the addresses and credentials in its own {@code [[health_checker]]}
 * properties (see {@link EndpointResolver}), so it does not depend on the API Manager configuration service.
 * A failure at or above {@code fail.on.status} (default {@code DEGRADED}) is thrown as {@link BadHealthException}
 * so {@code /health} returns HTTP 503, and each outcome is reused for {@code result.cache.ms} (default 5000).
 */
public class TMHealthChecker extends AbstractHealthChecker {

    private static final Log log = LogFactory.getLog(TMHealthChecker.class);

    /** Must match this checker's name in {@code health-check-config.xml}. */
    public static final String CHECKER_NAME = "TMHealthChecker";

    static final String PROBE_TIMEOUT_MS_PROPERTY = "probe.timeout.ms";
    static final String SLOW_THRESHOLD_MS_PROPERTY = "slow.threshold.ms";
    static final String STREAM_ID_PROPERTY = "stream.id";
    static final String RECEIVE_TIMEOUT_MS_PROPERTY = "broker.receive.timeout.ms";
    static final String CHECK_SSL_PORT_PROPERTY = "broker.ssl.port.enabled";
    static final String FAIL_ON_STATUS_PROPERTY = "fail.on.status";
    static final String FAIL_ON_NONE = "NONE";
    static final String LOGIN_RETRY_INTERVAL_MS_PROPERTY = "login.retry.interval.ms";
    static final String CHECK_ENDPOINTS_PROPERTY = "endpoint.checks.enabled";
    static final String MAX_PROBE_THREADS_PROPERTY = "probe.max.threads";
    static final int DEFAULT_MAX_PROBE_THREADS = 100;
    private static final long PROBE_THREAD_KEEP_ALIVE_SECONDS = 30;
 
    static final int DEFAULT_PROBE_TIMEOUT_MS = 1730;
    static final int DEFAULT_SLOW_THRESHOLD_MS = 2000;

    /** Marker stream for the test event, registered only while this bundle is active. */
    public static final String MARKER_STREAM_NAME = "org.wso2.carbon.apimgt.healthcheck.marker";
    public static final String MARKER_STREAM_VERSION = "1.0.0";

    static final String DEFAULT_STREAM_ID = MARKER_STREAM_NAME + ":" + MARKER_STREAM_VERSION;
    static final int DEFAULT_RECEIVE_TIMEOUT_MS = 1670;
    static final String RESULT_CACHE_MS_PROPERTY = "result.cache.ms";
    static final int DEFAULT_RESULT_CACHE_MS = 5000;

    static final String STATUS_UP = "UP";
    static final String STATUS_DEGRADED = "DEGRADED";
    static final String STATUS_DOWN = "DOWN";

    /** Receiver endpoint status when login failed and publish was never attempted; still counts as a failure. */
    static final String STATUS_UNKNOWN = "UNKNOWN";

    private static final String OVERALL_PREFIX = "tm.health";
    private static final String OVERALL_STATUS_KEY = OVERALL_PREFIX + ".status";
    private static final String OVERALL_MESSAGE_KEY = "tm.health.message";
    private static final String ERROR_CODE_SUFFIX = ".error.code";
    private static final String ERROR_MESSAGE_SUFFIX = ".error.message";
    private static final String THREADS_PREFIX = CheckType.THROTTLE_RECEIVER.getKeyPrefix() + ".threads.";
    private static final String THREADS_UNKNOWN_ERROR_CODE = "TM_HC_THREAD_POOL_SIZE_UNKNOWN";
    private static final String THREADS_UNKNOWN_ERROR_MESSAGE =
            "The throttle receiver's thread pool sizes could not be read";

    private static final ThreadFactory PROBE_THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "tm-healthcheck-probe");
        thread.setDaemon(true);
        return thread;
    };

    /**
     * Runs every probe. It has a fixed maximum size and no queue, so a probe that finds all threads busy (for
     * example with probes stuck behind a dropped connection) is rejected and the check fails with 503.
     */
    private volatile ThreadPoolExecutor probeExecutor = newProbeExecutor(DEFAULT_MAX_PROBE_THREADS);

    private final EndpointResolver endpointResolver;
    private final ReceiverThreadPoolMonitor threadPoolMonitor;

    /** Running probes by address, shared with overlapping calls so stuck probes cannot pile up threads. */
    private final ConcurrentHashMap<String, Future<ProbeResult>> probesInFlight = new ConcurrentHashMap<>();

    /** This checker's properties from {@code [[health_checker]]}, read on every check. */
    private volatile Properties checkerProperties = new Properties();

    /** Rejected receiver logins (see {@link #loginKey}) mapped to when they were rejected, in nano time. */
    private final ConcurrentHashMap<String, Long> rejectedLogins = new ConcurrentHashMap<>();

    private int probeTimeoutMs = DEFAULT_PROBE_TIMEOUT_MS;
    private int slowThresholdMs = DEFAULT_SLOW_THRESHOLD_MS;
    private String streamId = DEFAULT_STREAM_ID;
    private int receiveTimeoutMs = DEFAULT_RECEIVE_TIMEOUT_MS;
    private boolean checkSslPort = true;
    private boolean checkEndpoints = true;
    private String failOnStatus = STATUS_DEGRADED;
    private int loginRetryIntervalMs;
    private int resultCacheMs = DEFAULT_RESULT_CACHE_MS;

    private final Object refreshLock = new Object();
    private volatile Outcome lastOutcome;

    /** Last logged failure stage per address, so a failure is logged only when it starts or changes. */
    private final ConcurrentHashMap<String, ProbeStage> loggedFailures = new ConcurrentHashMap<>();
    private volatile boolean threadsExhaustedLogged;

    /**
     * @param threadPoolMonitor the monitor also registered as the JMX view of the receiver's thread pools
     */
    public TMHealthChecker(ReceiverThreadPoolMonitor threadPoolMonitor) {

        this(new EndpointResolver(APIUtil::getPortOffset), threadPoolMonitor);
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
        Properties copy = new Properties();
        if (properties != null) {
            copy.putAll(properties);
        }
        checkerProperties = copy;
        probeTimeoutMs = readInt(properties, PROBE_TIMEOUT_MS_PROPERTY, DEFAULT_PROBE_TIMEOUT_MS);
        slowThresholdMs = readInt(properties, SLOW_THRESHOLD_MS_PROPERTY, DEFAULT_SLOW_THRESHOLD_MS);
        receiveTimeoutMs = readInt(properties, RECEIVE_TIMEOUT_MS_PROPERTY, DEFAULT_RECEIVE_TIMEOUT_MS);
        String configuredStreamId = properties == null ? null : properties.getProperty(STREAM_ID_PROPERTY);
        streamId = configuredStreamId == null || configuredStreamId.trim().isEmpty()
                ? DEFAULT_STREAM_ID : configuredStreamId.trim();
        String sslPort = properties == null ? null : properties.getProperty(CHECK_SSL_PORT_PROPERTY);
        checkSslPort = sslPort == null || !"false".equalsIgnoreCase(sslPort.trim());
        String endpointChecks = properties == null ? null : properties.getProperty(CHECK_ENDPOINTS_PROPERTY);
        checkEndpoints = endpointChecks == null || !"false".equalsIgnoreCase(endpointChecks.trim());
        failOnStatus = readFailOnStatus(properties == null ? null : properties.getProperty(FAIL_ON_STATUS_PROPERTY));
        loginRetryIntervalMs = readInt(properties, LOGIN_RETRY_INTERVAL_MS_PROPERTY, 0);
        resultCacheMs = readInt(properties, RESULT_CACHE_MS_PROPERTY, DEFAULT_RESULT_CACHE_MS);
        rejectedLogins.clear();
        ThreadPoolExecutor previous = probeExecutor;
        probeExecutor = newProbeExecutor(Math.max(1, readInt(properties, MAX_PROBE_THREADS_PROPERTY,
                DEFAULT_MAX_PROBE_THREADS)));
        previous.shutdown();
        lastOutcome = null;
    }

    @Override
    public Properties checkHealth() throws BadHealthException {

        if (resultCacheMs <= 0) {
            return runCheck().replay();
        }
        Outcome outcome = freshOutcome();
        if (outcome == null) {
            synchronized (refreshLock) {
                outcome = freshOutcome();
                if (outcome == null) {
                    outcome = runCheck();
                    lastOutcome = outcome;
                }
            }
        }
        return outcome.replay();
    }

    private Outcome freshOutcome() {

        Outcome outcome = lastOutcome;
        return outcome != null && System.nanoTime() - outcome.completedAtNanos
                < TimeUnit.MILLISECONDS.toNanos(resultCacheMs) ? outcome : null;
    }

    /**
     * Reports any unexpected throwable as {@code DOWN}, since the framework only catches {@link BadHealthException}
     * and anything else would break the whole {@code /health} response; details go only to the log.
     */
    private Outcome runCheck() {

        try {
            return Outcome.of(check());
        } catch (BadHealthException e) {
            return Outcome.of(e);
        } catch (RuntimeException | Error e) {
            log.error(CHECKER_NAME + ": the check could not run, reporting the Traffic Manager as " + STATUS_DOWN, e);
            return checkCouldNotRun();
        }
    }

    private Outcome checkCouldNotRun() {

        Properties properties = new Properties();
        properties.setProperty(OVERALL_STATUS_KEY, STATUS_DOWN);
        properties.setProperty(OVERALL_MESSAGE_KEY, "Down: the health check could not run. See the server log.");
        properties.setProperty(OVERALL_PREFIX + ERROR_CODE_SUFFIX, ProbeStage.UNEXPECTED_ERROR.getErrorCode());
        properties.setProperty(OVERALL_PREFIX + ERROR_MESSAGE_SUFFIX, ProbeStage.UNEXPECTED_ERROR.getMessage());
        if (FAIL_ON_NONE.equals(failOnStatus)) {
            return Outcome.of(properties);
        }
        return Outcome.of(new BadHealthException("TM_HC_" + STATUS_DOWN,
                toHealthCheckErrors(STATUS_DOWN, properties)));
    }

    /** Thread pools are read before probing so this checker's own test connections are not counted as busy. */
    private Properties check() throws BadHealthException {

        List<ThreadPoolUsage> threadPools = threadPoolMonitor.snapshot();
        String threadsStatus = threadPoolMonitor.statusOf(threadPools);

        List<ProbeResult> results = new ArrayList<>();
        if (checkEndpoints) {
            results.addAll(probeEndpoints());
        }
        logChanges(results, threadsStatus);

        String status = checkEndpoints ? overallStatus(results) : STATUS_UP;
        if (STATUS_DOWN.equals(threadsStatus)) {
            status = checkEndpoints && STATUS_DOWN.equals(checkStatus(CheckType.MESSAGE_BROKER, results))
                    ? STATUS_DOWN : STATUS_DEGRADED;
        }
        Properties properties = buildProperties(status, results);
        addThreadPools(properties, threadsStatus, threadPools);
        HealthMessages.add(properties, results, threadPools, checkEndpoints);
        if (reachesFailureThreshold(status, results, threadsStatus)) {
            throw new BadHealthException("TM_HC_" + status, toHealthCheckErrors(status, properties));
        }
        return properties;
    }

    /** Resolves and probes the receiver and broker addresses; skipped when {@code endpoint.checks.enabled=false}. */
    private List<ProbeResult> probeEndpoints() {

        List<ProbeResult> results = new ArrayList<>();
        List<ProbeEndpoint> endpoints = new ArrayList<>();

        Properties config = checkerProperties;
        List<ProbeEndpoint> receivers = endpointResolver.resolveThrottleReceivers(config);
        if (receivers.isEmpty()) {
            results.add(ProbeResult.unresolved(CheckType.THROTTLE_RECEIVER,
                    "No receiver URL could be resolved from " + EndpointResolver.RECEIVER_URLS_PROPERTY));
        }
        endpoints.addAll(receivers);

        List<ProbeEndpoint> brokers = endpointResolver.resolveMessageBrokers(config, checkSslPort);
        if (brokers.isEmpty()) {
            results.add(ProbeResult.unresolved(CheckType.MESSAGE_BROKER,
                    "No broker URL could be resolved from " + EndpointResolver.BROKER_URLS_PROPERTY));
        }
        endpoints.addAll(brokers);

        results.addAll(probeAll(endpoints));
        return results;
    }

    /** A slow but working address never reaches the {@code DEGRADED} threshold; only real failures do. */
    private boolean reachesFailureThreshold(String status, List<ProbeResult> results, String threadsStatus) {

        if (FAIL_ON_NONE.equals(failOnStatus)) {
            return false;
        }
        if (STATUS_DOWN.equals(failOnStatus)) {
            return STATUS_DOWN.equals(status);
        }
        if (STATUS_DOWN.equals(threadsStatus)) {
            return true;
        }
        for (ProbeResult result : results) {
            if (!result.isSuccess()) {
                return true;
            }
        }
        return false;
    }

    /** The overall status error, then one error per {@code .error.code} key, described by its key prefix. */
    private static List<HealthCheckError> toHealthCheckErrors(String status, Properties properties) {

        List<HealthCheckError> errors = new ArrayList<>();
        errors.add(new HealthCheckError("TM_HC_" + status, properties.getProperty(OVERALL_MESSAGE_KEY),
                OVERALL_STATUS_KEY));
        Set<String> codeKeys = new TreeSet<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.endsWith(ERROR_CODE_SUFFIX)) {
                codeKeys.add(key);
            }
        }
        for (String codeKey : codeKeys) {
            String prefix = codeKey.substring(0, codeKey.length() - ERROR_CODE_SUFFIX.length());
            errors.add(new HealthCheckError(properties.getProperty(codeKey),
                    properties.getProperty(prefix + ERROR_MESSAGE_SUFFIX), prefix));
        }
        return errors;
    }

    /**
     * Adds the remaining thread count per pool; an exhausted pool also marks the receiver {@code DOWN}, and
     * unreadable pool sizes give {@code UNKNOWN} with an error instead of counts.
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
        if (checkEndpoints && STATUS_DOWN.equals(threadsStatus)) {
            properties.setProperty(CheckType.THROTTLE_RECEIVER.getKeyPrefix() + ".status", STATUS_DOWN);
        }
    }

    /**
     * Probes all endpoints in parallel, waiting at most {@code probe.timeout.ms} in total. Threads are not
     * interrupted on exit because a running probe may be shared with an overlapping caller.
     */
    private List<ProbeResult> probeAll(List<ProbeEndpoint> endpoints) {

        List<ProbeResult> results = new ArrayList<>(endpoints.size());
        if (endpoints.isEmpty()) {
            return results;
        }
        ProbeClient probeClient = createProbeClient(streamId, receiveTimeoutMs);
        ThreadPoolExecutor executor = probeExecutor;
        List<Future<ProbeResult>> futures = new ArrayList<>(endpoints.size());
        for (ProbeEndpoint endpoint : endpoints) {
            futures.add(startProbe(executor, probeClient, endpoint));
        }
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(probeTimeoutMs);
        for (int i = 0; i < endpoints.size(); i++) {
            results.add(awaitResult(endpoints.get(i), futures.get(i), deadlineNanos));
        }
        return results;
    }

    private static ThreadPoolExecutor newProbeExecutor(int maxThreads) {

        ThreadPoolExecutor executor = new ThreadPoolExecutor(maxThreads, maxThreads,
                PROBE_THREAD_KEEP_ALIVE_SECONDS, TimeUnit.SECONDS, new SynchronousQueue<>(), PROBE_THREAD_FACTORY);
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    /**
     * Starts a probe, or reuses one still running for the same address. A previously rejected receiver login is
     * answered from memory so repeated failed logins cannot lock the account.
     */
    private Future<ProbeResult> startProbe(ThreadPoolExecutor executor, ProbeClient probeClient,
                                           ProbeEndpoint endpoint) {

        String loginKey = endpoint.getCheckType() == CheckType.THROTTLE_RECEIVER ? loginKey(endpoint) : null;
        if (loginKey != null && isRejectedLoginStillRemembered(loginKey)) {
            FutureTask<ProbeResult> remembered = new FutureTask<>(() -> ProbeResult.credentialsRejected(endpoint,
                    endpoint.getAuthUrl() + " - the credentials were rejected earlier and are not retried, so "
                            + "repeated failed logins cannot lock the account", 0));
            remembered.run();
            return remembered;
        }
        String key = endpoint.getUrl();
        FutureTask<ProbeResult> task = new FutureTask<>(() -> {
            try {
                ProbeResult result = probeClient.probe(endpoint);
                if (loginKey != null) {
                    rememberLoginOutcome(loginKey, endpoint, result);
                }
                return result;
            } finally {
                probesInFlight.remove(key);
            }
        });
        Future<ProbeResult> existing = probesInFlight.putIfAbsent(key, task);
        if (existing != null) {
            return existing;
        }
        try {
            executor.execute(task);
            return task;
        } catch (RejectedExecutionException e) {
            probesInFlight.remove(key, task);
            FutureTask<ProbeResult> rejected = new FutureTask<>(() -> ProbeResult.failure(endpoint,
                    ProbeStage.PROBE_POOL_EXHAUSTED, "All " + executor.getMaximumPoolSize()
                            + " probe threads are busy with probes that have not finished", 0));
            rejected.run();
            return rejected;
        } catch (RuntimeException e) {
            probesInFlight.remove(key, task);
            throw e;
        }
    }

    private boolean isRejectedLoginStillRemembered(String loginKey) {

        Long rejectedAt = rejectedLogins.get(loginKey);
        if (rejectedAt == null) {
            return false;
        }
        return loginRetryIntervalMs <= 0
                || System.nanoTime() - rejectedAt < TimeUnit.MILLISECONDS.toNanos(loginRetryIntervalMs);
    }

    private void rememberLoginOutcome(String loginKey, ProbeEndpoint endpoint, ProbeResult result) {

        if (result.isCredentialsRejected()) {
            if (rejectedLogins.put(loginKey, System.nanoTime()) == null) {
                log.warn(CHECKER_NAME + ": the throttle receiver rejected the login of '" + endpoint.getUsername()
                        + "' on " + endpoint.getAuthUrl() + ". It is not retried "
                        + (loginRetryIntervalMs > 0 ? "for " + loginRetryIntervalMs + "ms" : "until restart")
                        + ", so the health check cannot lock that account. Check the " + CHECKER_NAME
                        + " username/password or receiver.username/receiver.password properties.");
            }
        } else if (Boolean.TRUE.equals(result.isAuthSucceeded())) {
            rejectedLogins.remove(loginKey);
        }
    }

    /** Auth address, username and a SHA-256 hash of the password, so the password itself is never held. */
    private static String loginKey(ProbeEndpoint endpoint) {

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(endpoint.getPassword()).getBytes(StandardCharsets.UTF_8));
            return endpoint.getAuthUrl() + '\n' + endpoint.getUsername() + '\n'
                    + Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** Reports only the root cause of a probe error, since an outer message can carry the connection string. */
    private ProbeResult awaitResult(ProbeEndpoint endpoint, Future<ProbeResult> future, long deadlineNanos) {

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
            return ProbeResult.failure(endpoint, ProbeStage.UNEXPECTED_ERROR,
                    String.valueOf(ProbeClient.rootCause(e.getCause())), 0);
        }
    }

    /** Overridable so tests can supply a fake probe client. */
    protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

        return new ProbeClient(streamId, receiveTimeoutMs, probeTimeoutMs);
    }

    /** {@code DOWN} only if every address across both checks failed, so losing one check alone is {@code DEGRADED}. */
    private String overallStatus(List<ProbeResult> results) {

        return statusFrom(results);
    }

    /** Same rule as {@link #overallStatus}, applied only to one check's own addresses. */
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
     * {@code DOWN} if nothing passed, {@code DEGRADED} if some failed or one was slower than
     * {@code slow.threshold.ms}, otherwise {@code UP}.
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
     * Writes one key prefix per address, named by role ({@code receiver}, {@code amqp}, {@code amqps}, then
     * {@code _2}, {@code _3} for more nodes); a receiver whose login failed is {@code UNKNOWN}/{@code NOT_ATTEMPTED}.
     */
    private Properties buildProperties(String status, List<ProbeResult> results) {

        Properties properties = new Properties();
        properties.setProperty(OVERALL_STATUS_KEY, status);
        if (!checkEndpoints) {
            return properties;
        }
        for (CheckType checkType : CheckType.values()) {
            String checkPrefix = checkType.getKeyPrefix() + ".";
            properties.setProperty(checkPrefix + "status", checkStatus(checkType, results));
            Set<String> usedKeys = new HashSet<>();
            for (ProbeResult result : results) {
                if (result.getCheckType() != checkType) {
                    continue;
                }
                ProbeEndpoint endpoint = result.getEndpoint();
                String baseKey = endpointRole(checkType, endpoint);
                String key = baseKey;
                for (int n = 2; !usedKeys.add(key); n++) {
                    key = baseKey + "_" + n;
                }
                String prefix = checkPrefix + "endpoint." + key;
                if (endpoint != null) {
                    properties.setProperty(prefix + ".source", endpoint.getSource());
                    properties.setProperty(prefix + ".port", String.valueOf(endpoint.getPort()));
                    if (checkType == CheckType.THROTTLE_RECEIVER) {
                        properties.setProperty(prefix + ".auth.port", String.valueOf(endpoint.getAuthPort()));
                    }
                }
                Boolean authSucceeded = result.isAuthSucceeded();
                boolean notAttempted = checkType == CheckType.THROTTLE_RECEIVER && result.getEndpoint() != null
                        && !result.isSuccess() && Boolean.FALSE.equals(authSucceeded);
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
                if (checkType == CheckType.THROTTLE_RECEIVER && result.getEndpoint() != null
                        && authSucceeded != null) {
                    properties.setProperty(prefix + ".auth.status", authSucceeded ? STATUS_UP : STATUS_DOWN);
                    if (!authSucceeded) {
                        properties.setProperty(prefix + ".auth.error.code", result.getFailedStage().getErrorCode());
                        properties.setProperty(prefix + ".auth.error.message", result.getFailedStage().getMessage());
                    }
                }
            }
        }
        return properties;
    }

    private static String endpointRole(CheckType checkType, ProbeEndpoint endpoint) {

        if (endpoint == null) {
            return "unresolved";
        }
        if (checkType == CheckType.THROTTLE_RECEIVER) {
            return "receiver";
        }
        return endpoint.isSecure() ? "amqps" : "amqp";
    }

    private void logChanges(List<ProbeResult> results, String threadsStatus) {

        for (ProbeResult result : results) {
            String key = result.getCheckType().getLabel() + " " + result.getTarget();
            if (!result.isSuccess()) {
                if (loggedFailures.put(key, result.getFailedStage()) != result.getFailedStage()) {
                    log.warn(CHECKER_NAME + ": " + result.getCheckType().getLabel() + " check failed - target="
                            + result.getTarget() + ", stage=" + result.getFailedStage() + ", detail="
                            + result.getDetail());
                }
            } else if (loggedFailures.remove(key) != null) {
                log.info(CHECKER_NAME + ": " + result.getCheckType().getLabel() + " check recovered - target="
                        + result.getTarget());
            }
        }
        boolean threadsExhausted = STATUS_DOWN.equals(threadsStatus);
        if (threadsExhausted != threadsExhaustedLogged) {
            threadsExhaustedLogged = threadsExhausted;
            if (threadsExhausted) {
                log.warn(CHECKER_NAME + ": throttle receiver has no worker threads left - new Gateway connections "
                        + "will not be served");
            } else {
                log.info(CHECKER_NAME + ": throttle receiver has free worker threads again");
            }
        }
    }

    /**
     * One check's outcome - its properties, or the failure it threw - replayed to every caller within
     * {@code result.cache.ms}. Each replay hands out its own copy, so no caller can change what the next one sees.
     */
    private static final class Outcome {

        private final long completedAtNanos = System.nanoTime();
        private final Properties properties;
        private final String errorCode;
        private final List<HealthCheckError> errors;

        private Outcome(Properties properties, String errorCode, List<HealthCheckError> errors) {

            this.properties = properties;
            this.errorCode = errorCode;
            this.errors = errors;
        }

        static Outcome of(Properties properties) {

            return new Outcome(properties, null, null);
        }

        static Outcome of(BadHealthException failure) {

            return new Outcome(null, failure.getErrorCode(), new ArrayList<>(failure.getErrors()));
        }

        Properties replay() throws BadHealthException {

            if (errors != null) {
                throw new BadHealthException(errorCode, new ArrayList<>(errors));
            }
            Properties copy = new Properties();
            copy.putAll(properties);
            return copy;
        }
    }

    private static String readFailOnStatus(String value) {

        if (value == null || value.trim().isEmpty()) {
            return STATUS_DEGRADED;
        }
        String normalized = value.trim().toUpperCase(Locale.ENGLISH);
        if (STATUS_DOWN.equals(normalized) || STATUS_DEGRADED.equals(normalized) || FAIL_ON_NONE.equals(normalized)) {
            return normalized;
        }
        log.warn("Invalid value '" + value + "' for health checker property '" + FAIL_ON_STATUS_PROPERTY
                + "' (expected DEGRADED, DOWN or NONE), using " + STATUS_DEGRADED);
        return STATUS_DEGRADED;
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
