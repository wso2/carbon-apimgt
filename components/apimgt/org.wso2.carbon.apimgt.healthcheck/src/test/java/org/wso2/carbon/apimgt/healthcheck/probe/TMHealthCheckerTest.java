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
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * {@link TMHealthChecker#checkHealth()} never throws - every case below reads its result straight out of the
 * returned {@link Properties}, including the {@code DOWN} cases (see the class Javadoc for why).
 */
public class TMHealthCheckerTest {

    // Deliberately different ports from NODE_2 - see buildProperties' own Javadoc caveat: two nodes sharing a
    // port would collide on the same endpoint.<port> key, which reportsOnlyTheLastNodeWhenTwoShareAPort below
    // tests on its own.
    private static final ProbeEndpoint NODE_1 = ProbeEndpoint.throttleReceiver("apim.throttling", "tm-node-1", 9612,
            "tm-node-1", 9712, "admin", "admin");
    private static final ProbeEndpoint NODE_2 = ProbeEndpoint.throttleReceiver("apim.throttling", "tm-node-2", 9613,
            "tm-node-2", 9713, "admin", "admin");
    private static final ProbeEndpoint BROKER_PLAIN = ProbeEndpoint.messageBroker("apim.throttling", "tm-node-1",
            5673, false, new Properties());
    private static final ProbeEndpoint BROKER_SSL = ProbeEndpoint.messageBroker("apim.throttling", "tm-node-1", 8673,
            true, new Properties());

    @Test
    public void reportsUpWhenEveryReceiverAndBrokerPortPasses() {

        TMHealthChecker checker = newChecker(Arrays.asList(NODE_1, NODE_2), Arrays.asList(BROKER_PLAIN, BROKER_SSL),
                new HashMap<>());

        Properties properties = checker.checkHealth();

        Assert.assertEquals("UP", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.endpoint.9612.status"));
        Assert.assertEquals("apim.throttling",
                properties.getProperty("tm.throttle.receiver.endpoint.9613.source"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.endpoint.5673.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.endpoint.8673.status"));
        // The resolved address is never put on the response as its own value - only the port, as part of the
        // key name.
        Assert.assertNull(properties.getProperty("tm.throttle.receiver.endpoint.9612"));
        Assert.assertNull(properties.getProperty("tm.message.broker.endpoint.5673"));
    }

    @Test
    public void reportsDegradedWhenOnlySomeAddressesOfACheckFail() {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(BROKER_SSL.getUrl(), ProbeStage.TLS_FAILED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Arrays.asList(BROKER_PLAIN, BROKER_SSL), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("DEGRADED", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("TM_HC_TLS_FAILED",
                properties.getProperty("tm.message.broker.endpoint.8673.error.code"));
    }

    @Test
    public void reportsAuthStatusSeparatelyFromTheReceiverEndpointsOwnStatus() {

        // Node 1: everything passes, including login. Node 2: login (9711/9713) itself fails.
        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(NODE_2.getUrl(), ProbeStage.LOGIN_FAILED);
        TMHealthChecker checker = newChecker(Arrays.asList(NODE_1, NODE_2), Collections.emptyList(), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.endpoint.9612.auth.status"));
        // Node 2's login itself is what failed, so the publish side was never attempted - UNKNOWN, not DOWN,
        // and the real reason (the login failure) is under .auth.error.code, not the top-level .error.code.
        Assert.assertEquals("UNKNOWN", properties.getProperty("tm.throttle.receiver.endpoint.9613.status"));
        Assert.assertEquals("TM_HC_NOT_ATTEMPTED",
                properties.getProperty("tm.throttle.receiver.endpoint.9613.error.code"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.endpoint.9613.auth.status"));
        Assert.assertEquals("TM_HC_LOGIN_FAILED",
                properties.getProperty("tm.throttle.receiver.endpoint.9613.auth.error.code"));
        // No resolved address is ever put on the response, including the 9711 auth side.
        Assert.assertNull(properties.getProperty("tm.throttle.receiver.endpoint.9612.auth.url"));
        Assert.assertNull(properties.getProperty("tm.throttle.receiver.endpoint.9613.auth.url"));
    }

    @Test
    public void reportsAuthStatusUpWhenOnlyThePublishStepFailedNotLogin() {

        // 9711's login succeeds, only the 9611-equivalent publish fails - .status must still be DOWN, but
        // .auth.status UP.
        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(NODE_1.getUrl(), ProbeStage.PUBLISH_FAILED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1), Collections.emptyList(), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.endpoint.9612.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.endpoint.9612.auth.status"));
    }

    @Test
    public void reportsOnlyTheLastNodeWhenTwoShareAPort() {

        // Two different hosts, same receiver port - the documented collision case (see buildProperties'
        // Javadoc): both would be keyed as endpoint.9612, so only the one resolved last survives.
        ProbeEndpoint firstOnPort9612 = ProbeEndpoint.throttleReceiver("apim.throttling", "tm-node-1", 9612,
                "tm-node-1", 9712, "admin", "admin");
        ProbeEndpoint secondOnPort9612 = ProbeEndpoint.throttleReceiver("apim.throttling", "tm-node-2", 9612,
                "tm-node-2", 9712, "admin", "admin");
        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(secondOnPort9612.getUrl(), ProbeStage.PUBLISH_FAILED);
        TMHealthChecker checker = newChecker(Arrays.asList(firstOnPort9612, secondOnPort9612),
                Collections.emptyList(), failures);

        Properties properties = checker.checkHealth();

        // Only one "endpoint.9612" entry exists in the response at all - the second node's result (DOWN) wins,
        // not the first node's (which would have been UP), since it was resolved after and so processed after.
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.endpoint.9612.status"));
    }

    @Test
    public void reportsDegradedNotDownWhenOneCheckFailsCompletelyButTheOtherStillPasses() {

        // The broker's only configured address fails, but the receiver still works - overall that is "some, not
        // all, addresses down" (DEGRADED), even though the broker's own status is DOWN on its own.
        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.PORT_CLOSED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("TM_HC_PORT_CLOSED",
                properties.getProperty("tm.message.broker.endpoint.5673.error.code"));
        Assert.assertEquals("Port is not reachable",
                properties.getProperty("tm.message.broker.endpoint.5673.error.message"));
    }

    @Test
    public void reportsDownWithoutThrowingWhenEveryAddressInBothChecksFails() {

        Map<String, ProbeStage> failures = new HashMap<>();
        failures.put(NODE_1.getUrl(), ProbeStage.LOGIN_FAILED);
        failures.put(BROKER_PLAIN.getUrl(), ProbeStage.NOT_RECEIVED);
        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), failures);

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.health.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.status"));
        // Login itself failed, so this is UNKNOWN/NOT_ATTEMPTED, not DOWN/LOGIN_FAILED - the real reason is under
        // .auth.error.code instead (see reportsAuthStatusSeparatelyFromTheReceiverEndpointsOwnStatus).
        Assert.assertEquals("UNKNOWN", properties.getProperty("tm.throttle.receiver.endpoint.9612.status"));
        Assert.assertEquals("TM_HC_NOT_ATTEMPTED",
                properties.getProperty("tm.throttle.receiver.endpoint.9612.error.code"));
        Assert.assertEquals("TM_HC_LOGIN_FAILED",
                properties.getProperty("tm.throttle.receiver.endpoint.9612.auth.error.code"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("TM_HC_NOT_RECEIVED",
                properties.getProperty("tm.message.broker.endpoint.5673.error.code"));
    }

    @Test
    public void reportsDownWithoutThrowingWhenNoAddressCanBeResolved() {

        // No endpoint was ever resolved at all - a different, more fundamental problem than a login failure, so
        // this must keep reporting CONFIG_UNRESOLVED/DOWN, never be mistaken for the NOT_ATTEMPTED/UNKNOWN case
        // above just because both happen to carry authSucceeded=false.
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

    @Test
    public void reportsTimeoutInsteadOfHangingAndDoesNotStartASecondStuckProbe() throws InterruptedException {

        CountDownLatch release = new CountDownLatch(1);
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
                        // Like a socket stuck in connect(), ignore interrupts until the test releases it.
                        long giveUpAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
                        while (release.getCount() > 0 && System.nanoTime() < giveUpAt) {
                            try {
                                release.await(10, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException ignored) {
                                // Keep blocking, as a real socket call would.
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
            Assert.assertTrue("second call should not wait",
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) < 150);
        } finally {
            release.countDown();
        }
    }

    @Test
    public void reportsTheRemainingReceiverThreadsAndStaysUpWhileAnyAreLeft() {

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
    public void explainsInPlainWordsWhichPartIsDownWhenOnlyPartlyWorking() {

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
    public void explainsThatNothingWorksWhenEverythingIsDown() {

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

    @Test
    public void reportsTheReceiverDownWhenAPoolHasNoThreadsLeftEvenIfTheProbePassed() {

        TMHealthChecker checker = newChecker(Collections.singletonList(NODE_1),
                Collections.singletonList(BROKER_PLAIN), new HashMap<>(),
                ReceiverThreadPoolMonitorTest.fixedMonitor(100, 100, 100, 0));

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.threads.status"));
        Assert.assertEquals("0", properties.getProperty("tm.throttle.receiver.threads.tcp.remaining"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.status"));
        // Same rule as every other check: one part down while the broker still works is DEGRADED overall.
        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.status"));
        Assert.assertEquals("No free slots on the TCP pool.",
                properties.getProperty("tm.throttle.receiver.threads.message"));
        Assert.assertTrue(properties.getProperty("tm.throttle.receiver.message")
                .contains("new Gateways cannot connect"));
    }

    @Test
    public void reportsThreadsUnknownWithoutChangingAnythingElseWhenPoolSizesCannotBeRead() {

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
     * The stuck receiver fails, but the broker (not stuck) still passes, so the overall result is {@code DEGRADED}
     * - never a thrown exception.
     */
    private static void assertReceiverTimesOut(TMHealthChecker checker) {

        Properties properties = checker.checkHealth();

        Assert.assertEquals("DEGRADED", properties.getProperty("tm.health.status"));
        Assert.assertEquals("DOWN", properties.getProperty("tm.throttle.receiver.status"));
        Assert.assertEquals("TM_HC_TIMEOUT",
                properties.getProperty("tm.throttle.receiver.endpoint.9612.error.code"));
        Assert.assertEquals("UP", properties.getProperty("tm.message.broker.status"));
    }

    private static TMHealthChecker newChecker(List<ProbeEndpoint> receivers, List<ProbeEndpoint> brokers,
                                              Map<String, ProbeStage> failures) {

        return newChecker(receivers, brokers, failures, new ReceiverThreadPoolMonitor(configName -> null));
    }

    private static TMHealthChecker newChecker(List<ProbeEndpoint> receivers, List<ProbeEndpoint> brokers,
                                              Map<String, ProbeStage> failures,
                                              ReceiverThreadPoolMonitor threadPoolMonitor) {

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
                        // Simulates which phase failed the same way the real client's probeThrottleReceiver does:
                        // a login-phase stage means 9711 itself failed, anything else means login (9711) passed
                        // and only the publish (9611) afterwards failed.
                        boolean authSucceeded = failure != ProbeStage.LOGIN_FAILED && failure != ProbeStage.TLS_FAILED;
                        return ProbeResult.failure(endpoint, failure, "simulated", 5, authSucceeded);
                    }
                };
            }
        };
        checker.init(config(null));
        return checker;
    }

    private static EndpointResolver fixedResolver(List<ProbeEndpoint> receivers, List<ProbeEndpoint> brokers) {

        return new EndpointResolver(() -> null, () -> 0) {

            @Override
            public List<ProbeEndpoint> resolveThrottleReceivers() {

                return receivers;
            }

            @Override
            public List<ProbeEndpoint> resolveMessageBrokers(boolean includeSslPort) {

                return brokers;
            }
        };
    }

    private static HealthCheckerConfig config(String probeTimeoutMs) {

        Properties properties = new Properties();
        if (probeTimeoutMs != null) {
            properties.setProperty(TMHealthChecker.PROBE_TIMEOUT_MS_PROPERTY, probeTimeoutMs);
        }
        return new HealthCheckerConfig(TMHealthChecker.CHECKER_NAME, 6, true, properties);
    }
}
