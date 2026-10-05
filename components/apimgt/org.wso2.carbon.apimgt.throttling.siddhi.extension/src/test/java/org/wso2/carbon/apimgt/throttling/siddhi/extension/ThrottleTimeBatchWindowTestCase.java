/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.throttling.siddhi.extension;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class ThrottleTimeBatchWindowTestCase {
    private static final Log log = LogFactory.getLog(ThrottleTimeBatchWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;
    private Event lastRemoveEvent;
    private Event lastCurrentEvent;


    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;

    }

    @Test
    public void throttleTimeWindowBatchTest1() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#throttler:timeBatch(5 sec) " +
                "select symbol,sum(price) as sumPrice,volume, expiryTimeStamp " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                } else if(removeEvents != null){
                    removeEventCount = removeEventCount + removeEvents.length;

                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        Thread.sleep(500);
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(6000);
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(6000);
        Assert.assertEquals(4, inEventCount);
        Assert.assertEquals(0, removeEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void throttleTimeWindowBatchTest2() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                "@info(name = 'query1') " +
                "from cseEventStream#throttler:timeBatch(5 sec , 0) " +
                "select symbol,sum(price) as sumPrice,volume, expiryTimeStamp " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                } else if(removeEvents != null){
                    removeEventCount = removeEventCount + removeEvents.length;
                    lastRemoveEvent = removeEvents[removeEvents.length - 1];
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(10000);
        Assert.assertEquals(2, inEventCount);
        Assert.assertNull(lastRemoveEvent);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void throttleTimeWindowBatchShouldResetAggregatesOnWindowExpiry() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String requestStream = "" +
                "define stream RequestStream (throttleKey string, messageSize long);";
        String query = "" +
                "@info(name = 'query1') " +
                "from RequestStream#throttler:timeBatch(2 sec , 0) " +
                "select throttleKey, count(throttleKey) as requestCount, sum(messageSize) as bandwidth, " +
                "expiryTimeStamp group by throttleKey " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(requestStream + query);
        List<Event> currentEvents = new CopyOnWriteArrayList<>();
        CountDownLatch firstWindowEventsArrived = new CountDownLatch(2);
        CountDownLatch postExpiryEventArrived = new CountDownLatch(1);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event event : inEvents) {
                        currentEvents.add(event);
                        if (currentEvents.size() <= 2) {
                            firstWindowEventsArrived.countDown();
                        } else {
                            postExpiryEventArrived.countDown();
                        }
                    }
                }
                eventArrived = true;
            }
        });

        try {
            InputHandler inputHandler = executionPlanRuntime.getInputHandler("RequestStream");
            executionPlanRuntime.start();
            inputHandler.send(new Object[]{"app1", 100L});
            inputHandler.send(new Object[]{"app1", 200L});
            Assert.assertTrue("Timed out waiting for first window events",
                    firstWindowEventsArrived.await(5, TimeUnit.SECONDS));

            long expiryTime = (Long) currentEvents.get(1).getData()[3];
            long waitTime = expiryTime - System.currentTimeMillis() + 500;
            if (waitTime > 0) {
                Thread.sleep(waitTime);
            }

            inputHandler.send(new Object[]{"app1", 50L});
            Assert.assertTrue("Timed out waiting for post-expiry event",
                    postExpiryEventArrived.await(5, TimeUnit.SECONDS));

            Event firstEventAfterReset = currentEvents.get(currentEvents.size() - 1);
            Assert.assertEquals("app1", firstEventAfterReset.getData()[0]);
            Assert.assertEquals(1L, firstEventAfterReset.getData()[1]);
            Assert.assertEquals(50L, firstEventAfterReset.getData()[2]);
            Assert.assertTrue(eventArrived);
        } finally {
            executionPlanRuntime.shutdown();
        }
    }

    /**
     * Verifies that getThreadLocalWindowExpiry() returns null when called outside of a
     * ThrottleStreamProcessor dispatch — the ThreadLocal must never leak across event batches.
     */
    @Test
    public void getThreadLocalWindowExpiryIsNullOutsideProcessing() {
        Assert.assertNull(
                "ThreadLocal window expiry must be null outside of ThrottleStreamProcessor.process()",
                ThrottleStreamProcessor.getThreadLocalWindowExpiry());
    }

    /**
     * Verifies that all events belonging to the same time-batch window share an identical
     * expiryTimeStamp. The window boundary is fixed once the first event arrives, so every
     * subsequent event in the same batch must carry the same expiry value.
     */
    @Test
    public void windowExpiryTimestampIsConsistentForAllEventsInSameBatch() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String requestStream = "define stream RequestStream (throttleKey string, messageSize long);";
        String query = "@info(name = 'query1') " +
                "from RequestStream#throttler:timeBatch(2 sec , 0) " +
                "select throttleKey, count(throttleKey) as requestCount, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(requestStream + query);
        List<Long> expiryTimestamps = new CopyOnWriteArrayList<>();
        CountDownLatch eventsArrived = new CountDownLatch(3);

        runtime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        expiryTimestamps.add((Long) e.getData()[2]);
                        eventsArrived.countDown();
                    }
                }
            }
        });

        try {
            InputHandler inputHandler = runtime.getInputHandler("RequestStream");
            runtime.start();
            inputHandler.send(new Object[]{"key1", 100L});
            inputHandler.send(new Object[]{"key2", 200L});
            inputHandler.send(new Object[]{"key3", 300L});
            Assert.assertTrue("Timed out waiting for events", eventsArrived.await(6, TimeUnit.SECONDS));
            Assert.assertEquals("All events in the same window must have the same expiryTimeStamp",
                    1, expiryTimestamps.stream().distinct().count());
        } finally {
            runtime.shutdown();
        }
    }

    @Test
    public void throttleTimeWindowBatchTest3() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String cseEventStream = "" +
                                "define stream cseEventStream (symbol string, price float, volume int);";
        String query = "" +
                       "@info(name = 'query1') " +
                       "from cseEventStream#throttler:timeBatch(1 min , 0) " +
                       "select symbol,sum(price) as sumPrice,volume, expiryTimeStamp " +
                       "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(cseEventStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    lastCurrentEvent = inEvents[inEvents.length - 1];
                } else if(removeEvents != null){
                    removeEventCount = removeEventCount + removeEvents.length;
                    lastRemoveEvent = removeEvents[removeEvents.length - 1];
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("cseEventStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 1});
        Thread.sleep(121000);
        inputHandler.send(new Object[]{"IBM", 700f, 0});
        Assert.assertEquals(3, inEventCount);
        Assert.assertTrue("Event expiry time is not valid for the current batch" , (Long) (lastCurrentEvent.getData()[3]) >= System.currentTimeMillis());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

    // -----------------------------------------------------------------------
    // GROUP BY tests with multiple distinct throttle keys — i.e. multiple applications
    // and/or multiple API resources sharing one policy query, the normal production shape.
    // The tests above only ever exercise a single key per window; the ones below confirm
    // the window-boundary reset and expiry-notification behavior generalizes correctly
    // across several keys at once, including consecutive rollovers and an idle window.
    // -----------------------------------------------------------------------

    /**
     * Multiple applications AND multiple APIs/resources within one of those applications,
     * all sharing a single policy query. Every key exceeds the limit in window 1. Window 2
     * sends exactly one fresh request per key; if the window-boundary reset only zeroed one
     * key instead of every key sharing the query, the un-reset keys would show an inflated
     * count in window 2 instead of 1.
     */
    @Test
    public void resetBroadcastZeroesEveryKey_multipleAppsAndMultipleApisPerApp() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, count(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        runtime.start();
        InputHandler in = runtime.getInputHandler("cseEventStream");

        // Two applications, and two distinct API resources within "App1" — mirrors a
        // resource-level throttleKey shape (appId + resourceKey combined).
        String[] keys = {"App1_ResourceA", "App1_ResourceB", "App2_ResourceA"};
        int limit = 3;
        for (String key : keys) {
            for (int i = 1; i <= limit + 2; i++) {
                in.send(new Object[]{key, key + "-msg" + i});
            }
        }
        Thread.sleep(4000); // cross the 3s boundary

        // Window 2: exactly one fresh request per key.
        for (String key : keys) {
            in.send(new Object[]{key, key + "-window2-msg1"});
        }
        Thread.sleep(500);
        runtime.shutdown();

        for (String key : keys) {
            Assert.assertEquals("Key " + key + " should show count=1 in window 2 if the reset "
                            + "truly zeroed it — a higher value means it carried over from window 1",
                    Long.valueOf(1L), lastCountPerKey.get(key));
        }
    }

    /**
     * ThrottleStreamProcessor captures only the FIRST event of the window (whichever key it
     * happens to belong to) as the RESET placeholder — it has no concept of "throttleKey" at
     * all, since group-by routing happens downstream of it. The placeholder's own identity is
     * then irrelevant: GroupByAggregationAttributeExecutor's RESET handling broadcasts to every
     * key in its map regardless of what data the placeholder itself carries.
     *
     * This test proves that directly: the identical 3-key scenario is run twice, with the send
     * order swapped so a DIFFERENT key becomes the captured placeholder each time (Siddhi
     * captures strictly by arrival order, so whichever key is sent first is guaranteed to be
     * the one captured). If the reset's correctness depended on which specific key's data ended
     * up as the placeholder, swapping the order would change the outcome for at least one key.
     * It must not — every key must reset identically in both runs.
     */
    @Test
    public void resetBroadcastOutcomeIsIndependentOfWhichKeyBecomesTheCapturedPlaceholder()
            throws InterruptedException {
        Map<String, Long> resultsWhenAppFirst = runMultiKeyResetScenario(
                new String[]{"AppFirst", "AppMiddle", "AppLast"});
        Map<String, Long> resultsWhenLastFirst = runMultiKeyResetScenario(
                new String[]{"AppLast", "AppMiddle", "AppFirst"});

        for (String key : new String[]{"AppFirst", "AppMiddle", "AppLast"}) {
            Assert.assertEquals("Key " + key + " must reset correctly when it was NOT the "
                            + "captured placeholder (AppFirst was sent first, so AppFirst was "
                            + "captured this run) — got: " + resultsWhenAppFirst,
                    Long.valueOf(1L), resultsWhenAppFirst.get(key));
            Assert.assertEquals("Key " + key + " must reset IDENTICALLY when a DIFFERENT key "
                            + "(AppLast) was the captured placeholder instead — got: "
                            + resultsWhenLastFirst,
                    Long.valueOf(1L), resultsWhenLastFirst.get(key));
        }
    }

    /**
     * Runs the same 3-key, single-window, over-the-limit-then-reset scenario used throughout
     * this file, but sends the keys in the given order — the first key in {@code sendOrder} is
     * guaranteed to be the one ThrottleStreamProcessor captures as its RESET placeholder for
     * this window, since capture is strictly first-arrival, not key-based.
     */
    private Map<String, Long> runMultiKeyResetScenario(String[] sendOrder) throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, count(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        runtime.start();
        InputHandler in = runtime.getInputHandler("cseEventStream");

        for (String key : sendOrder) {
            for (int i = 1; i <= 4; i++) {
                in.send(new Object[]{key, key + "-msg" + i});
            }
        }
        Thread.sleep(4000); // cross the 3s boundary

        for (String key : sendOrder) {
            in.send(new Object[]{key, key + "-window2-msg1"});
        }
        Thread.sleep(500);
        runtime.shutdown();

        return lastCountPerKey;
    }

    /**
     * Explicit "multiple APIs under one application" scenario using resource-level-shaped
     * keys (mirroring str:concat(resourceKey,'_default') from throttle_policy_template_
     * resource_default.xml): one application calling two different API resources, each with
     * its own independent limit and its own independent reset — confirms no cross-resource
     * contamination when both are reset by the same window boundary.
     */
    @Test
    public void multipleApiResourcesUnderSameApplication_independentCountingAndReset() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, count(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        try {
            runtime.start();
            InputHandler in = runtime.getInputHandler("cseEventStream");

            String getPetKey = "api123_GET_/pet_default";
            String postPetKey = "api123_POST_/pet_default";

            // GET is hit twice, POST is hit five times — independent volumes on the same app's API.
            in.send(new Object[]{getPetKey, "get-1"});
            in.send(new Object[]{getPetKey, "get-2"});
            for (int i = 1; i <= 5; i++) {
                in.send(new Object[]{postPetKey, "post-" + i});
            }
            Assert.assertEquals(Long.valueOf(2L), lastCountPerKey.get(getPetKey));
            Assert.assertEquals(Long.valueOf(5L), lastCountPerKey.get(postPetKey));

            Thread.sleep(4000); // cross the boundary
            in.send(new Object[]{getPetKey, "get-window2-1"});
            in.send(new Object[]{postPetKey, "post-window2-1"});
            Thread.sleep(500);

            Assert.assertEquals("GET resource should start fresh at 1 in window 2, not 3 (2+1 carried over)",
                    Long.valueOf(1L), lastCountPerKey.get(getPetKey));
            Assert.assertEquals("POST resource should start fresh at 1 in window 2, not 6 (5+1 carried over)",
                    Long.valueOf(1L), lastCountPerKey.get(postPetKey));
        } finally {
            runtime.shutdown();
        }
    }

    /**
     * Three consecutive windows, three keys. Verifies the window-boundary reset correctly
     * zeroes every key on EVERY rollover, not just the first — a mechanism that only worked
     * once would be a much narrower bug than one that never worked, so this is worth
     * confirming explicitly rather than assuming window 1->2 behavior generalizes.
     */
    @Test
    public void multipleConsecutiveWindows_allKeysResetOnEveryRollover() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, count(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        try {
            runtime.start();
            InputHandler in = runtime.getInputHandler("cseEventStream");
            String[] keys = {"AppA", "AppB", "AppC"};

            // Window 1: heavy traffic (3, 4, 5 requests respectively).
            for (int k = 0; k < keys.length; k++) {
                for (int i = 0; i <= k + 2; i++) {
                    in.send(new Object[]{keys[k], keys[k] + "-w1-" + i});
                }
            }
            Thread.sleep(4000); // 1000ms margin past the 3000ms boundary, consistent with the rest of this file

            // Window 2: exactly one request per key — must read back as 1, not accumulated.
            for (String key : keys) {
                in.send(new Object[]{key, key + "-w2"});
            }
            Assert.assertEquals(Long.valueOf(1L), lastCountPerKey.get("AppA"));
            Assert.assertEquals(Long.valueOf(1L), lastCountPerKey.get("AppB"));
            Assert.assertEquals(Long.valueOf(1L), lastCountPerKey.get("AppC"));
            Thread.sleep(4000);

            // Window 3: exactly one request per key again — must STILL read back as 1, proving
            // the second rollover reset correctly too, not just the first.
            for (String key : keys) {
                in.send(new Object[]{key, key + "-w3"});
            }

            Assert.assertEquals("AppA should reset correctly on the SECOND rollover too",
                    Long.valueOf(1L), lastCountPerKey.get("AppA"));
            Assert.assertEquals("AppB should reset correctly on the SECOND rollover too",
                    Long.valueOf(1L), lastCountPerKey.get("AppB"));
            Assert.assertEquals("AppC should reset correctly on the SECOND rollover too",
                    Long.valueOf(1L), lastCountPerKey.get("AppC"));
        } finally {
            runtime.shutdown();
        }
    }

    /**
     * An idle window with zero traffic sandwiched between two active windows. Confirms this
     * self-corrects the moment traffic resumes — the next active window's own boundary reset
     * (triggered by ITS traffic) still zeroes everything correctly, with no stale carryover
     * from the window before the gap.
     */
    @Test
    public void idleWindowInTheMiddle_selfCorrectsOnceTrafficResumes() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, count(messageID) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        try {
            runtime.start();
            InputHandler in = runtime.getInputHandler("cseEventStream");

            // Window 1: traffic for AppX and AppY.
            for (int i = 0; i < 4; i++) {
                in.send(new Object[]{"AppX", "w1-" + i});
            }
            for (int i = 0; i < 2; i++) {
                in.send(new Object[]{"AppY", "w1-" + i});
            }
            Assert.assertEquals(Long.valueOf(4L), lastCountPerKey.get("AppX"));
            Assert.assertEquals(Long.valueOf(2L), lastCountPerKey.get("AppY"));

            // Window 2: completely idle — no traffic from anyone. Sleep through two full
            // window lengths so window 2 elapses with zero events.
            Thread.sleep(7000);

            // Window 3 (or later): traffic resumes. Both keys must start fresh at 1, not
            // carry over their window-1 accumulated values (4 and 2 respectively).
            in.send(new Object[]{"AppX", "resumed"});
            in.send(new Object[]{"AppY", "resumed"});

            Assert.assertEquals("AppX must not carry over its pre-idle-gap count", Long.valueOf(1L),
                    lastCountPerKey.get("AppX"));
            Assert.assertEquals("AppY must not carry over its pre-idle-gap count", Long.valueOf(1L),
                    lastCountPerKey.get("AppY"));
        } finally {
            runtime.shutdown();
        }
    }

    /**
     * Mirrors throttle_policy_template_resource.xml's shape: an extra "evaluatedConditions"
     * column alongside throttleKey/count/expiryTimeStamp, and resource-level-style keys
     * (resourceKey + "_" + condition). Confirms the extra column doesn't interfere with
     * multi-key reset correctness.
     */
    @Test
    public void resourceLevelStyleQueryWithExtraColumn_multiKeyResetStillCorrect() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string, evaluatedConditions string);" +
                "@info(name = 'q1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, count(messageID) as cnt, expiryTimeStamp, evaluatedConditions " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime runtime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        runtime.addCallback("q1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        runtime.start();
        InputHandler in = runtime.getInputHandler("cseEventStream");

        String key1 = "resourceA_GET_condition1";
        String key2 = "resourceA_POST_condition2";
        for (int i = 0; i < 6; i++) {
            in.send(new Object[]{key1, key1 + "-" + i, "W10="});
        }
        for (int i = 0; i < 3; i++) {
            in.send(new Object[]{key2, key2 + "-" + i, "W10="});
        }
        Thread.sleep(4000); // 1000ms margin past the 3000ms boundary, consistent with the rest of this file

        in.send(new Object[]{key1, "after", "W10="});
        in.send(new Object[]{key2, "after", "W10="});
        runtime.shutdown();

        Assert.assertEquals("Resource/condition key 1 should reset correctly despite the extra column",
                Long.valueOf(1L), lastCountPerKey.get(key1));
        Assert.assertEquals("Resource/condition key 2 should reset correctly despite the extra column",
                Long.valueOf(1L), lastCountPerKey.get(key2));
    }

}
