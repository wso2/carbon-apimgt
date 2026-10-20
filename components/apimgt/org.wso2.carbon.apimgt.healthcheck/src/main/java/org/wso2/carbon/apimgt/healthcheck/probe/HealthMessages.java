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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * Adds a plain-English {@code .message} next to the overall status and each part's status, so the response can be
 * read by someone who does not know WSO2's internals: what the part does for the Gateways, and what it means for
 * them when it is not {@code UP}. Only adds keys - every existing key and value stays exactly as it is.
 *
 * <p>Must run after every status in the response is final, since each message describes its status.</p>
 */
final class HealthMessages {

    static final String MESSAGE_SUFFIX = "message";

    private static final String OVERALL_PREFIX = "tm.health.";
    private static final String RECEIVER_PREFIX = CheckType.THROTTLE_RECEIVER.getKeyPrefix() + ".";
    private static final String BROKER_PREFIX = CheckType.MESSAGE_BROKER.getKeyPrefix() + ".";
    private static final String THREADS_PREFIX = RECEIVER_PREFIX + "threads.";

    private static final String RECEIVER_NAME = "rate-limit event receiver";
    private static final String BROKER_NAME = "message broker";

    private HealthMessages() {

    }

    static void add(Properties properties, List<ProbeResult> results, List<ThreadPoolUsage> threadPools) {

        String receiverStatus = properties.getProperty(RECEIVER_PREFIX + "status");
        String brokerStatus = properties.getProperty(BROKER_PREFIX + "status");
        String threadsStatus = properties.getProperty(THREADS_PREFIX + "status");

        properties.setProperty(OVERALL_PREFIX + MESSAGE_SUFFIX,
                overallMessage(properties.getProperty(OVERALL_PREFIX + "status"), receiverStatus, brokerStatus));
        properties.setProperty(RECEIVER_PREFIX + MESSAGE_SUFFIX,
                receiverMessage(receiverStatus, threadsStatus, results));
        properties.setProperty(BROKER_PREFIX + MESSAGE_SUFFIX, brokerMessage(brokerStatus, results));
        if (threadsStatus != null) {
            properties.setProperty(THREADS_PREFIX + MESSAGE_SUFFIX, threadsMessage(threadsStatus, threadPools));
        }
    }

    private static String overallMessage(String status, String receiverStatus, String brokerStatus) {

        if (TMHealthChecker.STATUS_UP.equals(status)) {
            return "Working normally.";
        }
        if (TMHealthChecker.STATUS_DOWN.equals(status)) {
            return "Down: no usage events in, no rate-limit decisions out.";
        }
        List<String> problems = new ArrayList<>();
        if (!TMHealthChecker.STATUS_UP.equals(receiverStatus)) {
            problems.add(RECEIVER_NAME);
        }
        if (!TMHealthChecker.STATUS_UP.equals(brokerStatus)) {
            problems.add(BROKER_NAME);
        }
        if (problems.isEmpty()) {
            return "Working, but responding slowly.";
        }
        return "Partly working. Problem: " + String.join(" and ", problems) + ".";
    }

    private static String receiverMessage(String status, String threadsStatus, List<ProbeResult> results) {

        Set<Integer> ports = new TreeSet<>();
        Set<Integer> loginPorts = new TreeSet<>();
        for (ProbeResult result : results) {
            if (result.getCheckType() == CheckType.THROTTLE_RECEIVER && result.getEndpoint() != null) {
                ports.add(result.getEndpoint().getPort());
                loginPorts.add(result.getEndpoint().getAuthPort());
            }
        }
        if (TMHealthChecker.STATUS_DOWN.equals(threadsStatus)) {
            return "No free connection slots - new Gateways cannot connect.";
        }
        if (ports.isEmpty()) {
            return "Not configured.";
        }
        String where = " (" + portList(ports) + ", login " + numbers(loginPorts) + ")";
        if (TMHealthChecker.STATUS_UP.equals(status)) {
            return "Gateways can send usage events" + where + ".";
        }
        if (TMHealthChecker.STATUS_DEGRADED.equals(status)) {
            return "Some addresses failing or slow" + where + ".";
        }
        return "Gateways cannot send usage events" + where + ".";
    }

    private static String brokerMessage(String status, List<ProbeResult> results) {

        Set<Integer> ports = new TreeSet<>();
        for (ProbeResult result : results) {
            if (result.getCheckType() == CheckType.MESSAGE_BROKER && result.getEndpoint() != null) {
                ports.add(result.getEndpoint().getPort());
            }
        }
        if (ports.isEmpty()) {
            return "Not configured.";
        }
        String where = " (" + portList(ports) + ")";
        if (TMHealthChecker.STATUS_UP.equals(status)) {
            return "Rate-limit decisions reach Gateways" + where + ".";
        }
        if (TMHealthChecker.STATUS_DEGRADED.equals(status)) {
            return "Some broker ports failing or slow" + where + ".";
        }
        return "Rate-limit decisions do not reach Gateways" + where + ".";
    }

    private static String threadsMessage(String status, List<ThreadPoolUsage> threadPools) {

        if (TMHealthChecker.STATUS_UNKNOWN.equals(status)) {
            return "Could not count free connection slots.";
        }
        List<String> counts = new ArrayList<>();
        List<String> full = new ArrayList<>();
        for (ThreadPoolUsage usage : threadPools) {
            if (!usage.isKnown()) {
                continue;
            }
            String pool = usage.getPool().toUpperCase(Locale.ROOT);
            counts.add(usage.getFree() + "/" + usage.getSize() + " " + pool);
            if (usage.getFree() == 0) {
                full.add(pool);
            }
        }
        if (!full.isEmpty()) {
            return "No free slots on the " + String.join(" and ", full) + " pool.";
        }
        return String.join(", ", counts) + " slots free.";
    }

    private static String portList(Set<Integer> ports) {

        return (ports.size() == 1 ? "port " : "ports ") + numbers(ports);
    }

    private static String numbers(Set<Integer> ports) {

        List<String> values = new ArrayList<>();
        for (Integer port : ports) {
            values.add(String.valueOf(port));
        }
        return String.join(", ", values);
    }
}
