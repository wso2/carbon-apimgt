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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Checkers built with {@link #newChecker} use {@code fail.on.status = NONE}, so even {@code DOWN} results can be
 * read from the returned {@link Properties}.
 */
public class TMHealthCheckerTest {

    private static final ProbeEndpoint NODE_1 = ProbeEndpoint.throttleReceiver("health_checker", "tm-node-1", 9612,
            "tm-node-1", 9712, "admin", "admin");
    private static final ProbeEndpoint NODE_2 = ProbeEndpoint.throttleReceiver("health_checker", "tm-node-2", 9613,
            "tm-node-2", 9713, "admin", "admin");
    private static final ProbeEndpoint BROKER_PLAIN = ProbeEndpoint.messageBroker("health_checker", "tm-node-1",
            5673, false, "admin", "admin", new Properties());
    private static final ProbeEndpoint BROKER_SSL = ProbeEndpoint.messageBroker("health_checker", "tm-node-1", 8673,
            true, "admin", "admin", new Properties());

    @Test
    public void reportsUpWhenEveryReceiverAndBrokerPortPasses() throws BadHealthException {

        TMHealthChecker checker = newChecker(Arrays.asList(NODE_1, NODE_2), Arrays.asList(BROKER_PLAIN, BROKER_SSL),
                new HashMap<>());

        Properties properties = checker.checkHealth();

        Assert.assertEquals("UP", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.endpoint.receiver.status"));
        Assert.assertEquals("health_checker",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.source"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.endpoint.amqp.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.endpoint.amqps.status"));
        Assert.assertEquals("9612", properties.getProperty("tm.throttle.receiver.endpoint.receiver.port"));
        Assert.assertEquals("9712", properties.getProperty("tm.throttle.receiver.endpoint.receiver.auth.port"));
        Assert.assertEquals("9613", properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.port"));
        Assert.assertEquals("5673", properties.getProperty("tm.message.broker.endpoint.amqp.port"));
        Assert.assertEquals("8673", properties.getProperty("tm.message.broker.endpoint.amqps.port"));
        for (String key : properties.stringPropertyNames()) {
            Assert.assertFalse(key + " = " + properties.getProperty(key),
                    properties.getProperty(key).contains("tm-node-"));
        }
    }

    /**
     * The receiver and broker are never resolved or probed, and only the overall and thread pool keys are reported.
     */
    @Test
    public void reportsOnlyOverallAndThreadPoolKeysWhenEndpointChecksAreDisabled() throws BadHealthException {

        AtomicInteger probes = new AtomicInteger();
        TMHealthChecker checker = endpointChecksDisabled(probes, "DEGRADED",
                ReceiverThreadPoolMonitorTest.fixedMonitor(100, 100, 98, 0));

        Properties properties = checker.checkHealth();

        Assert.assertEquals(0, probes.get());
        Assert.assertEquals("UP", properties.getProperty("tm.health.status"));
        Assert.assertEquals("Working normally.", properties.getProperty("tm.health.message"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.threads.status"));
        Assert.assertEquals("2", properties.getProperty("tm.throttle.receiver.threads.tcp.remaining"));
        Assert.assertNotNull(properties.getProperty("tm.throttle.receiver.threads.message"));
        for (String key : properties.stringPropertyNames()) {
            Assert.assertTrue(key, key.startsWith("tm.health.") || key.startsWith("tm.throttle.receiver.threads."));
        }
    }

    @Test
    public void stillFailsOnExhaustedReceiverThreadsWhenEndpointChecksAreDisabled() {

        AtomicInteger probes = new AtomicInteger();
        TMHealthChecker checker = endpointChecksDisabled(probes, "DEGRADED",
                ReceiverThreadPoolMonitorTest.fixedMonitor(100, 100, 100, 0));

        try {
            checker.checkHealth();
            Assert.fail("expected a BadHealthException");
        } catch (BadHealthException e) {
            Assert.assertEquals("TM_HC_DEGRADED", e.getErrors().get(0).getErrorCode());
            Assert.assertEquals("tm.health.status", e.getErrors().get(0).getErrorDescription());
        }
        Assert.assertEquals(0, probes.get());
    }

    /**
     * A probe that finds every probe thread busy is rejected, so a pile-up of stuck probes answers 503 rather than
     * growing without limit.
     */
    @Test
    public void failsWithPoolExhaustedWhenEveryProbeThreadIsBusy() throws Exception {

        CountDownLatch release = new CountDownLatch(1);
        TMHealthChecker checker = new TMHealthChecker(fixedResolver(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN))) {

            @Override
            protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

                return new ProbeClient(streamId, receiveTimeoutMs) {

                    @Override
                    public ProbeResult probe(ProbeEndpoint endpoint) {

                        try {
                            release.await(10, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return ProbeResult.success(endpoint, 5);
                    }
                };
            }
        };
        HealthCheckerConfig config = config(null);
        config.getProperties().setProperty(TMHealthChecker.MAX_PROBE_THREADS_PROPERTY, "1");
        config.getProperties().setProperty(TMHealthChecker.FAIL_ON_STATUS_PROPERTY, "DEGRADED");
        checker.init(config);

        try {
            checker.checkHealth();
            Assert.fail("expected a BadHealthException");
        } catch (BadHealthException e) {
            assertHasError(e.getErrors(), "TM_HC_PROBE_POOL_EXHAUSTED", "tm.message.broker.endpoint.amqp");
            Assert.assertEquals("tm.health.status", e.getErrors().get(0).getErrorDescription());
        } finally {
            release.countDown();
        }
    }

    @Test
    public void resolvesAddressesFromItsOwnHealthCheckerProperties() throws BadHealthException {

        List<Properties> seen = new ArrayList<>();
        TMHealthChecker checker = new TMHealthChecker(new EndpointResolver(() -> 0) {

            @Override
            public List<ProbeEndpoint> resolveThrottleReceivers(Properties config) {

                seen.add(config);
                return Collections.emptyList();
            }

            @Override
            public List<ProbeEndpoint> resolveMessageBrokers(Properties config, boolean includeSslPort) {

                seen.add(config);
                return Collections.emptyList();
            }
        });
        HealthCheckerConfig config = config(null);
        config.getProperties().setProperty(EndpointResolver.RECEIVER_URLS_PROPERTY, "tcp://tm-node-1:9611");
        checker.init(config);

        checker.checkHealth();

        Assert.assertEquals(2, seen.size());
        for (Properties properties : seen) {
            Assert.assertEquals("tcp://tm-node-1:9611",
                    properties.getProperty(EndpointResolver.RECEIVER_URLS_PROPERTY));
        }
    }

    /**
     * Keys are named by role, not port, so one alert rule fits every node.
     */
    @Test
    public void reportsUnderTheSameKeysWhateverThePortOffset() throws BadHealthException {

        Properties offset0 = newChecker(Collections.singletonList(ProbeEndpoint.throttleReceiver("health_checker",
                        "tm-a", 9611, "tm-a", 9711, "admin", "admin")),
                Arrays.asList(broker("tm-a", 5672, false), broker("tm-a", 8672, true)),
                new HashMap<>()).checkHealth();
        Properties offset3 = newChecker(Collections.singletonList(ProbeEndpoint.throttleReceiver("health_checker",
                        "tm-b", 9614, "tm-b", 9714, "admin", "admin")),
                Arrays.asList(broker("tm-b", 5675, false), broker("tm-b", 8675, true)),
                new HashMap<>()).checkHealth();

        Assert.assertEquals(offset0.stringPropertyNames(), offset3.stringPropertyNames());
        Assert.assertEquals("5675", offset3.getProperty("tm.message.broker.endpoint.amqp.port"));
    }

    @Test
    public void reportsDegradedWhenOnlySomeAddressesOfACheckFail() throws BadHealthException {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(BROKER_SSL.getUrl(), ProbeStage.TLS_FAILED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Arrays.asList(BROKER_PLAIN, BROKER_SSL), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("DEGRADED", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("TM_HC_TLS_FAILED",
                properties.getProperty("tm.message.broker.endpoint.amqps.error.code"));
    }

    /**
     * When login fails, publish is reported as not attempted and the real reason goes under {@code .auth.error.code}.
     */
    @Test
    public void reportsAuthStatusSeparatelyFromTheReceiverEndpointsOwnStatus() throws BadHealthException {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(NODE_2.getUrl(), ProbeStage.LOGIN_FAILED);
        TMHealthChecker checker = newChecker(Arrays.asList(NODE_1, NODE_2), Collections.emptyList(), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.endpoint.receiver.auth.status"));
        Assert.assertEquals("UNKNOWN", properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.status"));
        Assert.assertEquals("TM_HC_NOT_ATTEMPTED",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.error.code"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.auth.status"));
        Assert.assertEquals("TM_HC_LOGIN_FAILED",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.auth.error.code"));
        Assert.assertNull(properties.getProperty("tm.throttle.receiver.endpoint.receiver.auth.url"));
        Assert.assertNull(properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.auth.url"));
    }

    @Test
    public void reportsAuthStatusUpWhenOnlyThePublishStepFailedNotLogin() throws BadHealthException {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(NODE_1.getUrl(), ProbeStage.PUBLISH_FAILED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1), Collections.emptyList(), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.endpoint.receiver.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.endpoint.receiver.auth.status"));
    }

    /**
     * A healthy node must not inherit the error keys of an earlier failed node on the same port.
     */
    @Test
    public void reportsEachNodeSeparatelyWhenTwoShareAPort() throws BadHealthException {

        ProbeEndpoint firstOnPort9612 = ProbeEndpoint.throttleReceiver("health_checker", "tm-node-1", 9612,
                "tm-node-1", 9712, "admin", "admin");
        ProbeEndpoint secondOnPort9612 = ProbeEndpoint.throttleReceiver("health_checker", "tm-node-2", 9612,
                "tm-node-2", 9712, "admin", "admin");
        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(firstOnPort9612.getUrl(), ProbeStage.PUBLISH_FAILED);
        TMHealthChecker checker = newChecker(Arrays.asList(firstOnPort9612, secondOnPort9612),
                Collections.emptyList(), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.endpoint.receiver.status"));
        Assert.assertEquals("TM_HC_PUBLISH_FAILED",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver.error.code"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.status"));
        Assert.assertNull("a healthy node's entry must not inherit another node's stale error keys",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.error.code"));
        Assert.assertNull(properties.getProperty("tm.throttle.receiver.endpoint.receiver_2.error.message"));
    }

    @Test
    public void reportsDegradedNotDownWhenOneCheckFailsCompletelyButTheOtherStillPasses() throws BadHealthException {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.PORT_CLOSED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("TM_HC_PORT_CLOSED",
                properties.getProperty("tm.message.broker.endpoint.amqp.error.code"));
        Assert.assertEquals("Port is not reachable",
                properties.getProperty("tm.message.broker.endpoint.amqp.error.message"));
    }

    @Test
    public void reportsDownWhenEveryAddressInBothChecksFails() throws BadHealthException {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(NODE_1.getUrl(), ProbeStage.LOGIN_FAILED);
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.NOT_RECEIVED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.health.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UNKNOWN", properties.getProperty("tm.throttle.receiver.endpoint.receiver.status"));
        Assert.assertEquals("TM_HC_NOT_ATTEMPTED",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver.error.code"));
        Assert.assertEquals("TM_HC_LOGIN_FAILED",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver.auth.error.code"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("TM_HC_NOT_RECEIVED",
                properties.getProperty("tm.message.broker.endpoint.amqp.error.code"));
    }

    @Test
    public void reportsDownWhenNoAddressCanBeResolved() throws BadHealthException {

        TMHealthChecker checker = newChecker(Collections.emptyList(), Collections.emptyList(), new HashMap<>());

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.health.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.endpoint.unresolved.status"));
        Assert.assertEquals("TM_HC_CONFIG_UNRESOLVED",
                properties.getProperty("tm.throttle.receiver.endpoint.unresolved.error.code"));
        Assert.assertNull("no real endpoint, so no auth sub-status either",
                properties.getProperty("tm.throttle.receiver.endpoint.unresolved.auth.status"));
        Assert.assertEquals("TM_HC_CONFIG_UNRESOLVED",
                properties.getProperty("tm.message.broker.endpoint.unresolved.error.code"));
    }

    /**
     * The second call waits on the shared stuck probe for its own timeout instead of starting another probe.
     */
    @Test
    public void reportsTimeoutInsteadOfHangingAndDoesNotStartASecondStuckProbe()
            throws InterruptedException, BadHealthException {

        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger receiverProbeInvocations = new AtomicInteger();
        TMHealthChecker checker = new TMHealthChecker(fixedResolver(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN))) {

            @Override
            protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

                return new ProbeClient(streamId, receiveTimeoutMs) {

                    @Override
                    public ProbeResult probe(ProbeEndpoint endpoint) {

                        if (endpoint.getCheckType() == CheckType.MESSAGE_BROKER) {
                            return ProbeResult.success(endpoint, 1);
                        }
                        receiverProbeInvocations.incrementAndGet();
                        long giveUpAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                        while (release.getCount() > 0 && System.nanoTime() < giveUpAt) {
                            try {
                                release.await(10, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException ignored) {
                            }
                        }
                        return ProbeResult.success(endpoint, 1);
                    }
                };
            }
        };
        checker.init(config("200"));

        try {
            assertReceiverTimesOut(checker);
            long start = System.nanoTime();
            assertReceiverTimesOut(checker);
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            Assert.assertTrue("second call should wait roughly its own timeout, not return instantly: "
                    + elapsedMs + "ms", elapsedMs >= 150);
            Assert.assertTrue("second call must not wait out the stuck probe itself: " + elapsedMs + "ms",
                    elapsedMs < 2000);
            Assert.assertEquals("a second stuck probe must not be started for the same address", 1,
                    receiverProbeInvocations.get());
        } finally {
            release.countDown();
        }
    }

    /**
     * Two overlapping callers of a healthy in-flight probe must both see its real result.
     */
    @Test
    public void sharesAnInFlightHealthyProbeInsteadOfFalselyReportingItAsTimedOutToAnOverlappingCaller()
            throws InterruptedException, BadHealthException {

        CountDownLatch probeEntered = new CountDownLatch(1);
        CountDownLatch releaseProbe = new CountDownLatch(1);
        AtomicInteger receiverProbeInvocations = new AtomicInteger();
        TMHealthChecker checker = new TMHealthChecker(fixedResolver(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN))) {

            @Override
            protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

                return new ProbeClient(streamId, receiveTimeoutMs) {

                    @Override
                    public ProbeResult probe(ProbeEndpoint endpoint) {

                        if (endpoint.getCheckType() == CheckType.MESSAGE_BROKER) {
                            return ProbeResult.success(endpoint, 1);
                        }
                        receiverProbeInvocations.incrementAndGet();
                        probeEntered.countDown();
                        try {
                            Assert.assertTrue("test setup: releaseProbe should arrive well inside the timeout",
                                    releaseProbe.await(2, TimeUnit.SECONDS));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return ProbeResult.success(endpoint, 1);
                    }
                };
            }
        };
        checker.init(config("2000"));

        AtomicReference<Properties> resultA = new AtomicReference<>();
        Thread callerA = new Thread(() -> {
            try {
                resultA.set(checker.checkHealth());
            } catch (BadHealthException e) {
                throw new AssertionError("a healthy probe must not fail the check", e);
            }
        });
        callerA.start();
        Assert.assertTrue("A's probe did not start in time", probeEntered.await(1, TimeUnit.SECONDS));

        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            releaseProbe.countDown();
        }).start();

        Properties resultB = checker.checkHealth();
        callerA.join(TimeUnit.SECONDS.toMillis(5));

        Assert.assertEquals("overlapping caller B must see the real, healthy result - not a false timeout",
                "UP", resultB.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UP", resultA.get().getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("only one real probe should have run for the shared address", 1,
                receiverProbeInvocations.get());
    }

    /**
     * Runs with the default {@code fail.on.status}, so it also shows that slowness alone throws nothing.
     */
    @Test
    public void reportsRespondingSlowlyNotAProblemWhenEveryProbeSucceedsButOneIsSlow() throws BadHealthException {

        Properties config = new Properties();
        config.setProperty(TMHealthChecker.SLOW_THRESHOLD_MS_PROPERTY, "100");
        TMHealthChecker checker = new TMHealthChecker(fixedResolver(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN))) {

            @Override
            protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

                return new ProbeClient(streamId, receiveTimeoutMs) {

                    @Override
                    public ProbeResult probe(ProbeEndpoint endpoint) {
                        long latencyMs = endpoint.getCheckType() == CheckType.THROTTLE_RECEIVER ? 500 : 1;
                        return ProbeResult.success(endpoint, latencyMs);
                    }
                };
            }
        };
        checker.init(new HealthCheckerConfig(TMHealthChecker.CHECKER_NAME, 6, true, config));

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("DEGRADED", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("Working, but responding slowly.", properties.getProperty("tm.health.message"));
    }

    @Test
    public void reportsTheRemainingReceiverThreadsAndStaysUpWhileAnyAreLeft() throws BadHealthException {

        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), new HashMap<>(),
                ReceiverThreadPoolMonitorTest.fixedMonitor(100, 100, 98, 0));

        Properties properties = checker.checkHealth();

        Assert.assertEquals("UP", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.threads.status"));
        Assert.assertEquals("2", properties.getProperty("tm.throttle.receiver.threads.tcp.remaining"));
        Assert.assertEquals("100", properties.getProperty("tm.throttle.receiver.threads.ssl.remaining"));
        Assert.assertEquals("2/100 TCP, 100/100 SSL slots free.",
                properties.getProperty("tm.throttle.receiver.threads.message"));
        Assert.assertEquals("Working normally.", properties.getProperty("tm.health.message"));
        Assert.assertEquals("Gateways can send usage events (port 9612, login 9712).",
                properties.getProperty("tm.throttle.receiver.message"));
        Assert.assertEquals("Rate-limit decisions reach Gateways (port 5673).",
                properties.getProperty("tm.message.broker.message"));
    }

    @Test
    public void explainsInPlainWordsWhichPartIsDownWhenOnlyPartlyWorking() throws BadHealthException {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.TIMEOUT);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("Partly working. Problem: message broker.", properties.getProperty("tm.health.message"));
        Assert.assertTrue(properties.getProperty("tm.message.broker.message")
                .startsWith("Rate-limit decisions do not reach Gateways"));
    }

    @Test
    public void explainsThatNothingWorksWhenEverythingIsDown() throws BadHealthException {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(NODE_1.getUrl(), ProbeStage.PORT_CLOSED);
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.PORT_CLOSED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.health.status"));
        Assert.assertTrue(properties.getProperty("tm.health.message").startsWith("Down:"));
        Assert.assertTrue(properties.getProperty("tm.throttle.receiver.message")
                .startsWith("Gateways cannot send usage events"));
    }

    /**
     * Login fails on an unreachable node, so publish is never attempted.
     */
    @Test
    public void reportsNotAttemptedOnTheReceiverWhenTheWholeThrottleManagerIsUnreachable() throws BadHealthException {

        TMHealthChecker checker = new TMHealthChecker(fixedResolver(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN))) {

            @Override
            protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

                return new ProbeClient(streamId, receiveTimeoutMs) {

                    @Override
                    public ProbeResult probe(ProbeEndpoint endpoint) {

                        if (endpoint.getCheckType() == CheckType.MESSAGE_BROKER) {
                            return ProbeResult.success(endpoint, 5);
                        }
                        return ProbeResult.failure(endpoint, ProbeStage.PORT_CLOSED, "simulated", 5, false);
                    }
                };
            }
        };
        checker.init(config(null));

        Properties properties = checker.checkHealth();

        Assert.assertEquals("UNKNOWN", properties.getProperty("tm.throttle.receiver.endpoint.receiver.status"));
        Assert.assertEquals("TM_HC_NOT_ATTEMPTED",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver.error.code"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.endpoint.receiver.auth.status"));
        Assert.assertEquals("TM_HC_PORT_CLOSED",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver.auth.error.code"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.status"));
    }

    @Test
    public void reportsTheReceiverDownWhenAPoolHasNoThreadsLeftEvenIfTheProbePassed() throws BadHealthException {

        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), new HashMap<>(),
                ReceiverThreadPoolMonitorTest.fixedMonitor(100, 100, 100, 0));

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.threads.status"));
        Assert.assertEquals("0", properties.getProperty("tm.throttle.receiver.threads.tcp.remaining"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("No free slots on the TCP pool.",
                properties.getProperty("tm.throttle.receiver.threads.message"));
        Assert.assertTrue(properties.getProperty("tm.throttle.receiver.message")
                .contains("new Gateways cannot connect"));
        Assert.assertEquals("Partly working. Problem: rate-limit event receiver.",
                properties.getProperty("tm.health.message"));
    }

    @Test
    public void reportsOverallDownNotDegradedWhenThePoolIsExhaustedAndTheBrokerIsAlsoDown() throws BadHealthException {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.PORT_CLOSED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures,
                ReceiverThreadPoolMonitorTest.fixedMonitor(100, 100, 100, 0));

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.threads.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.health.status"));
    }

    @Test
    public void reportsThreadsUnknownWithoutChangingAnythingElseWhenPoolSizesCannotBeRead() throws BadHealthException {

        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), new HashMap<>());

        Properties properties = checker.checkHealth();

        Assert.assertEquals("UNKNOWN", properties.getProperty("tm.throttle.receiver.threads.status"));
        Assert.assertEquals("TM_HC_THREAD_POOL_SIZE_UNKNOWN",
                properties.getProperty("tm.throttle.receiver.threads.error.code"));
        Assert.assertNull(properties.getProperty("tm.throttle.receiver.threads.tcp.remaining"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.health.status"));
    }

    /**
     * By default a fully down Traffic Manager throws, so {@code GET /health} answers 503 without any host name.
     */
    @Test
    public void throwsWithEveryFailureWhenDownByDefault() {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(NODE_1.getUrl(), ProbeStage.LOGIN_FAILED);
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.NOT_RECEIVED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures, new ReceiverThreadPoolMonitor(configName -> null),
                null);

        BadHealthException thrown = expectBadHealth(checker);

        List<HealthCheckError> errors = thrown.getErrors();
        Assert.assertEquals("TM_HC_DOWN", errors.get(0).getErrorCode());
        Assert.assertEquals("tm.health.status", errors.get(0).getErrorDescription());
        Assert.assertTrue(errors.get(0).getMessage().startsWith("Down:"));
        assertHasError(errors, "TM_HC_NOT_ATTEMPTED", "tm.throttle.receiver.endpoint.receiver");
        assertHasError(errors, "TM_HC_LOGIN_FAILED", "tm.throttle.receiver.endpoint.receiver.auth");
        assertHasError(errors, "TM_HC_NOT_RECEIVED", "tm.message.broker.endpoint.amqp");
        for (HealthCheckError error : errors) {
            Assert.assertFalse("no host name in a 503 either: " + error.getMessage(),
                    String.valueOf(error.getMessage()).contains("tm-node-1"));
        }
    }

    /**
     * Same rule as the framework's own checkers: one failed item fails the check, even while others still work.
     */
    @Test
    public void throwsOnAPartialFailureByDefault() {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.PORT_CLOSED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures, new ReceiverThreadPoolMonitor(configName -> null),
                null);

        BadHealthException thrown = expectBadHealth(checker);

        Assert.assertEquals("TM_HC_DEGRADED", thrown.getErrors().get(0).getErrorCode());
        assertHasError(thrown.getErrors(), "TM_HC_PORT_CLOSED", "tm.message.broker.endpoint.amqp");
    }

    @Test
    public void throwsByDefaultWhenTheReceiverHasNoThreadsLeftEvenIfEveryProbePassed() {

        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), new HashMap<>(),
                ReceiverThreadPoolMonitorTest.fixedMonitor(100, 100, 100, 0), null);

        BadHealthException thrown = expectBadHealth(checker);

        Assert.assertEquals("TM_HC_DEGRADED", thrown.getErrors().get(0).getErrorCode());
    }

    @Test
    public void throwsOnlyWhenEverythingFailedWhenFailOnStatusIsDown() throws BadHealthException {

        Map<String, ProbeStage> brokerDown = new HashMap<>();
        brokerDown.put(BROKER_PLAIN.getUrl(), ProbeStage.PORT_CLOSED);
        TMHealthChecker degraded = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), brokerDown,
                new ReceiverThreadPoolMonitor(configName -> null), "down");
        Assert.assertEquals("DEGRADED", degraded.checkHealth().getProperty("tm.health.status"));

        Map<String, ProbeStage> allDown = new HashMap<>(brokerDown);
        allDown.put(NODE_1.getUrl(), ProbeStage.PUBLISH_FAILED);
        TMHealthChecker down = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), allDown, new ReceiverThreadPoolMonitor(configName -> null),
                "down");
        Assert.assertEquals("TM_HC_DOWN", expectBadHealth(down).getErrors().get(0).getErrorCode());
    }

    @Test
    public void fallsBackToTheDefaultThresholdForAnInvalidFailOnStatus() {

        Map<String, ProbeStage> brokerDown = new HashMap<>();
        brokerDown.put(BROKER_PLAIN.getUrl(), ProbeStage.PORT_CLOSED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), brokerDown,
                new ReceiverThreadPoolMonitor(configName -> null), "sometimes");

        Assert.assertEquals("TM_HC_DEGRADED", expectBadHealth(checker).getErrors().get(0).getErrorCode());
    }

    /**
     * Each login is a real user store login, so retrying rejected credentials could lock the account.
     */
    @Test
    public void doesNotRetryALoginTheReceiverRejected() throws BadHealthException {

        AtomicInteger receiverProbes = new AtomicInteger();
        TMHealthChecker checker = countingReceiverChecker(receiverProbes,
                endpoint -> ProbeResult.credentialsRejected(endpoint, "simulated", 5), Collections.emptyMap());

        checker.checkHealth();
        Properties second = checker.checkHealth();

        Assert.assertEquals("the rejected login must not be attempted again", 1, receiverProbes.get());
        Assert.assertEquals("TM_HC_LOGIN_FAILED",
                second.getProperty("tm.throttle.receiver.endpoint.receiver.auth.error.code"));
        Assert.assertEquals("TM_HC_NOT_ATTEMPTED",
                second.getProperty("tm.throttle.receiver.endpoint.receiver.error.code"));
        Assert.assertEquals("DOWN", second.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UP", second.getProperty("tm.message.broker.status"));
    }

    @Test
    public void retriesARejectedLoginOnceTheRetryIntervalHasPassedAndForgetsItAfterASuccess()
            throws BadHealthException, InterruptedException {

        AtomicInteger receiverProbes = new AtomicInteger();
        TMHealthChecker checker = countingReceiverChecker(receiverProbes,
                endpoint -> receiverProbes.get() == 1 ? ProbeResult.credentialsRejected(endpoint, "simulated", 5)
                        : ProbeResult.success(endpoint, 5),
                Collections.singletonMap(TMHealthChecker.LOGIN_RETRY_INTERVAL_MS_PROPERTY, "50"));

        checker.checkHealth();
        Assert.assertEquals("still remembered within the interval, so reported without a new login", "DOWN",
                checker.checkHealth().getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals(1, receiverProbes.get());

        Thread.sleep(100);
        Assert.assertEquals("UP", checker.checkHealth().getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UP", checker.checkHealth().getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("retried once after the interval, then probed normally", 3, receiverProbes.get());
    }

    @Test
    public void alwaysRetriesALoginThatFailedOnTheNetwork() throws BadHealthException {

        AtomicInteger receiverProbes = new AtomicInteger();
        TMHealthChecker checker = countingReceiverChecker(receiverProbes,
                endpoint -> ProbeResult.failure(endpoint, ProbeStage.PORT_CLOSED, "simulated", 5, false),
                Collections.emptyMap());

        checker.checkHealth();
        checker.checkHealth();

        Assert.assertEquals("a network failure never reached the user store, so it is retried", 2,
                receiverProbes.get());
    }

    @Test
    public void reusesTheLastResultWithinTheCacheWindow() throws BadHealthException {

        AtomicInteger receiverProbes = new AtomicInteger();
        TMHealthChecker checker = countingReceiverChecker(receiverProbes, endpoint -> ProbeResult.success(endpoint, 5),
                Collections.singletonMap(TMHealthChecker.RESULT_CACHE_MS_PROPERTY, "60000"));

        Properties first = checker.checkHealth();
        first.setProperty("tm.health.status", "changed by a caller");
        Properties second = checker.checkHealth();

        Assert.assertEquals(1, receiverProbes.get());
        Assert.assertEquals("each caller gets its own copy", "UP", second.getProperty("tm.health.status"));
    }

    @Test
    public void reusesACachedFailureAsTheSame503() {

        AtomicInteger receiverProbes = new AtomicInteger();
        Map<String, String> settings = new HashMap<>();
        settings.put(TMHealthChecker.RESULT_CACHE_MS_PROPERTY, "60000");
        settings.put(TMHealthChecker.FAIL_ON_STATUS_PROPERTY, null);
        TMHealthChecker checker = countingReceiverChecker(receiverProbes,
                endpoint -> ProbeResult.failure(endpoint, ProbeStage.PUBLISH_FAILED, "simulated", 5, true), settings);

        BadHealthException first = expectBadHealth(checker);
        BadHealthException second = expectBadHealth(checker);

        Assert.assertEquals(1, receiverProbes.get());
        Assert.assertEquals(first.getErrors().size(), second.getErrors().size());
        assertHasError(second.getErrors(), "TM_HC_PUBLISH_FAILED", "tm.throttle.receiver.endpoint.receiver");
    }

    @Test
    public void checksAgainOnceTheCacheWindowHasPassed() throws BadHealthException, InterruptedException {

        AtomicInteger receiverProbes = new AtomicInteger();
        TMHealthChecker checker = countingReceiverChecker(receiverProbes, endpoint -> ProbeResult.success(endpoint, 5),
                Collections.singletonMap(TMHealthChecker.RESULT_CACHE_MS_PROPERTY, "50"));

        checker.checkHealth();
        Thread.sleep(100);
        checker.checkHealth();

        Assert.assertEquals(2, receiverProbes.get());
    }

    /**
     * The framework only catches {@link BadHealthException}, so anything else would turn /health into a bare 500.
     */
    @Test
    public void reportsDownInsteadOfLettingAnUnexpectedErrorEscape() {

        TMHealthChecker checker = new TMHealthChecker(failingResolver(
                new NoClassDefFoundError("org/wso2/carbon/databridge/agent/AgentHolder")));
        HealthCheckerConfig config = config(null);
        config.getProperties().remove(TMHealthChecker.FAIL_ON_STATUS_PROPERTY);
        checker.init(config);

        BadHealthException thrown = expectBadHealth(checker);

        Assert.assertEquals("TM_HC_DOWN", thrown.getErrors().get(0).getErrorCode());
        assertHasError(thrown.getErrors(), "TM_HC_UNEXPECTED_ERROR", "tm.health");
        for (HealthCheckError error : thrown.getErrors()) {
            Assert.assertFalse("the exception itself only goes to the log: " + error.getMessage(),
                    String.valueOf(error.getMessage()).contains("AgentHolder"));
        }
    }

    @Test
    public void reportsAnUnexpectedErrorAsDownWithoutThrowingWhenFailOnStatusIsNone() throws BadHealthException {

        TMHealthChecker checker = new TMHealthChecker(failingResolver(new IllegalStateException("simulated")));
        checker.init(config(null));

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.health.status"));
        Assert.assertEquals("TM_HC_UNEXPECTED_ERROR", properties.getProperty("tm.health.error.code"));
    }

    @Test
    public void defaultTimeoutsAnswerWithinATypicalLoadBalancerHealthCheckTimeout() {

        Assert.assertTrue("a whole check must finish under a typical 5s load balancer timeout",
                TMHealthChecker.DEFAULT_PROBE_TIMEOUT_MS < 5000);
    }

    private static TMHealthChecker endpointChecksDisabled(AtomicInteger probes, String failOnStatus,
                                                          ReceiverThreadPoolMonitor threadPoolMonitor) {

        TMHealthChecker checker = new TMHealthChecker(fixedResolver(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN)), threadPoolMonitor) {

            @Override
            protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

                return new ProbeClient(streamId, receiveTimeoutMs) {

                    @Override
                    public ProbeResult probe(ProbeEndpoint endpoint) {

                        probes.incrementAndGet();
                        return ProbeResult.success(endpoint, 5);
                    }
                };
            }
        };
        HealthCheckerConfig config = config(null);
        config.getProperties().setProperty(TMHealthChecker.FAIL_ON_STATUS_PROPERTY, failOnStatus);
        config.getProperties().setProperty(TMHealthChecker.CHECK_ENDPOINTS_PROPERTY, "false");
        checker.init(config);
        return checker;
    }

    private static ProbeEndpoint broker(String host, int port, boolean secure) {

        return ProbeEndpoint.messageBroker("health_checker", host, port, secure, "admin", "admin", new Properties());
    }

    private static EndpointResolver failingResolver(Throwable failure) {

        return new EndpointResolver(() -> 0) {

            @Override
            public List<ProbeEndpoint> resolveThrottleReceivers(Properties config) {

                if (failure instanceof Error) {
                    throw (Error) failure;
                }
                throw (RuntimeException) failure;
            }
        };
    }

    /**
     * @param settings checker properties to set on top of {@link #config}; a null value removes that property
     */
    private static TMHealthChecker countingReceiverChecker(AtomicInteger receiverProbes,
                                                           Function<ProbeEndpoint, ProbeResult> receiverOutcome,
                                                           Map<String, String> settings) {

        TMHealthChecker checker = new TMHealthChecker(fixedResolver(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN))) {

            @Override
            protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

                return new ProbeClient(streamId, receiveTimeoutMs) {

                    @Override
                    public ProbeResult probe(ProbeEndpoint endpoint) {

                        if (endpoint.getCheckType() == CheckType.MESSAGE_BROKER) {
                            return ProbeResult.success(endpoint, 5);
                        }
                        receiverProbes.incrementAndGet();
                        return receiverOutcome.apply(endpoint);
                    }
                };
            }
        };
        HealthCheckerConfig config = config(null);
        for (Map.Entry<String, String> setting : settings.entrySet()) {
            if (setting.getValue() == null) {
                config.getProperties().remove(setting.getKey());
            } else {
                config.getProperties().setProperty(setting.getKey(), setting.getValue());
            }
        }
        checker.init(config);
        return checker;
    }

    private static BadHealthException expectBadHealth(TMHealthChecker checker) {

        try {
            Properties properties = checker.checkHealth();
            Assert.fail("expected BadHealthException, but got tm.health.status="
                    + properties.getProperty("tm.health.status"));
            return null;
        } catch (BadHealthException e) {
            return e;
        }
    }

    private static void assertHasError(List<HealthCheckError> errors, String code, String description) {

        for (HealthCheckError error : errors) {
            if (code.equals(error.getErrorCode()) && description.equals(error.getErrorDescription())) {
                Assert.assertNotNull("error " + code + " should carry a message", error.getMessage());
                return;
            }
        }
        StringBuilder actual = new StringBuilder();
        for (HealthCheckError error : errors) {
            actual.append(error.getErrorCode()).append('@').append(error.getErrorDescription()).append(' ');
        }
        Assert.fail("no " + code + " error for " + description + "; got: " + actual);
    }

    /**
     * Asserts the stuck receiver timed out with an unknown login status while the broker still passed.
     */
    private static void assertReceiverTimesOut(TMHealthChecker checker) throws BadHealthException {

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("TM_HC_TIMEOUT",
                properties.getProperty("tm.throttle.receiver.endpoint.receiver.error.code"));
        Assert.assertNull(properties.getProperty("tm.throttle.receiver.endpoint.receiver.auth.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.status"));
    }

    private static TMHealthChecker newChecker(List<ProbeEndpoint> receivers, List<ProbeEndpoint> brokers,
                                              Map<String, ProbeStage> failures) {

        return newChecker(receivers, brokers, failures, new ReceiverThreadPoolMonitor(configName -> null));
    }

    private static TMHealthChecker newChecker(List<ProbeEndpoint> receivers, List<ProbeEndpoint> brokers,
                                              Map<String, ProbeStage> failures,
                                              ReceiverThreadPoolMonitor threadPoolMonitor) {

        return newChecker(receivers, brokers, failures, threadPoolMonitor, TMHealthChecker.FAIL_ON_NONE);
    }

    /**
     * Treats {@code LOGIN_FAILED} and {@code TLS_FAILED} as login failures and every other stage as a publish failure.
     *
     * @param failOnStatus the {@code fail.on.status} value, or null to leave it unset (the default threshold)
     */
    private static TMHealthChecker newChecker(List<ProbeEndpoint> receivers, List<ProbeEndpoint> brokers,
                                              Map<String, ProbeStage> failures,
                                              ReceiverThreadPoolMonitor threadPoolMonitor, String failOnStatus) {

        TMHealthChecker checker = new TMHealthChecker(fixedResolver(receivers, brokers), threadPoolMonitor) {

            @Override
            protected ProbeClient createProbeClient(String streamId, long receiveTimeoutMs) {

                return new ProbeClient(streamId, receiveTimeoutMs) {

                    @Override
                    public ProbeResult probe(ProbeEndpoint endpoint) {

                        ProbeStage failure = failures.get(endpoint.getUrl());
                        if (failure == null) {
                            return ProbeResult.success(endpoint, 5);
                        }
                        boolean authSucceeded = failure != ProbeStage.LOGIN_FAILED && failure != ProbeStage.TLS_FAILED;
                        return ProbeResult.failure(endpoint, failure, "simulated", 5, authSucceeded);
                    }
                };
            }
        };
        HealthCheckerConfig config = config(null);
        if (failOnStatus == null) {
            config.getProperties().remove(TMHealthChecker.FAIL_ON_STATUS_PROPERTY);
        } else {
            config.getProperties().setProperty(TMHealthChecker.FAIL_ON_STATUS_PROPERTY, failOnStatus);
        }
        checker.init(config);
        return checker;
    }

    private static EndpointResolver fixedResolver(List<ProbeEndpoint> receivers, List<ProbeEndpoint> brokers) {

        return new EndpointResolver(() -> 0) {

            @Override
            public List<ProbeEndpoint> resolveThrottleReceivers(Properties config) {

                return receivers;
            }

            @Override
            public List<ProbeEndpoint> resolveMessageBrokers(Properties config, boolean includeSslPort) {

                return brokers;
            }
        };
    }

    /**
     * With {@code fail.on.status = NONE}, so failure cases can still be read from the returned properties, and
     * {@code result.cache.ms = 0}, so every call runs a fresh check.
     */
    private static HealthCheckerConfig config(String probeTimeoutMs) {

        Properties properties = new Properties();
        properties.setProperty(TMHealthChecker.FAIL_ON_STATUS_PROPERTY, TMHealthChecker.FAIL_ON_NONE);
        properties.setProperty(TMHealthChecker.RESULT_CACHE_MS_PROPERTY, "0");
        if (probeTimeoutMs != null) {
            properties.setProperty(TMHealthChecker.PROBE_TIMEOUT_MS_PROPERTY, probeTimeoutMs);
        }
        return new HealthCheckerConfig(TMHealthChecker.CHECKER_NAME, 6, true, properties);
    }
}
