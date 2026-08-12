/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit Test cases related to  CountAttributeAggregator with the Reset extension
 */
public class CountAttributeAggregatorWithResetTestCase {
    private static final Log log = LogFactory.getLog(CountAttributeAggregatorWithResetTestCase.class);
    private AtomicInteger atomicEventCount;

    @Before
    public void init() {
        atomicEventCount = new AtomicInteger(0);
    }

    @Test
    public void CountAggregatorTestWithoutReset() throws InterruptedException {
        log.info("CountAggregator Test #1 : Without setting Reset");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('CountAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price float);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.timeBatch(5 sec) " +
                "select throttler:count(price) as count " +
                "group by symbol " +
                "having count > 2 " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);

        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    atomicEventCount.addAndGet(inEvents.length);
                    if (atomicEventCount.get() == 1) {
                        junit.framework.Assert.assertEquals(3L, inEvents[0].getData(0));
                    }
                }
            }
        });

        execPlanRunTime.start();
        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 0F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 0F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 3F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"APIM", 3F});
        execPlanRunTime.shutdown();
        junit.framework.Assert.assertEquals(1, atomicEventCount.intValue());
    }

    @Test
    public void CountAggregatorTestWithResetSetToFalse() throws InterruptedException {
        log.info("CountAggregator Test #2 : Setting Reset value to be false");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('CountAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price float);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.timeBatch(5 sec) " +
                "select throttler:count(price, false) as count " +
                "group by symbol " +
                "having count > 2 " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);

        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    atomicEventCount.addAndGet(inEvents.length);
                    if (atomicEventCount.get() == 1) {
                        junit.framework.Assert.assertEquals(3L, inEvents[0].getData(0));
                    }
                }
            }
        });

        execPlanRunTime.start();
        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 0F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 0F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 3F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"APIM", 3F});
        execPlanRunTime.shutdown();
        junit.framework.Assert.assertEquals(1, atomicEventCount.intValue());
    }

    @Test
    public void CountAggregatorTestWithResetSetToTrue() throws InterruptedException {
        log.info("CountAggregator Test #3 : Setting Reset value to be true");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('CountAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price float);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.timeBatch(2 sec) " +
                "select symbol, ifThenElse(price == -1F, throttler:count(price, true),throttler:count(price, false)) as count " +
                "group by symbol " +
                "insert all events into outputStream;";


        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);

        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                atomicEventCount.set(0);
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    atomicEventCount.addAndGet(inEvents.length);
                    if (atomicEventCount.get() == 1) {
                        junit.framework.Assert.assertEquals("APIM", inEvents[0].getData(0));
                        junit.framework.Assert.assertEquals(1L, inEvents[0].getData(1));
                    } else if (atomicEventCount.get() == 2) {
                        junit.framework.Assert.assertEquals("WSO2", inEvents[0].getData(0));
                        junit.framework.Assert.assertEquals(3L, inEvents[0].getData(1));
                        junit.framework.Assert.assertEquals("APIM", inEvents[1].getData(0));
                        junit.framework.Assert.assertEquals(0L, inEvents[1].getData(1));
                    }
                }
            }
        });

        execPlanRunTime.start();
        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 0F});
        Thread.sleep(400);
        inputHandler.send(new Object[]{"WSO2", 0F});
        Thread.sleep(400);
        inputHandler.send(new Object[]{"APIM", 2F});
        Thread.sleep(400);
        inputHandler.send(new Object[]{"WSO2", 3F});
        Thread.sleep(400);
        inputHandler.send(new Object[]{"APIM", -1F});
        Thread.sleep(800);
        junit.framework.Assert.assertEquals(2, atomicEventCount.intValue());
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(2000);
        execPlanRunTime.shutdown();
        junit.framework.Assert.assertEquals(1, atomicEventCount.intValue());
    }

    // -----------------------------------------------------------------------
    // GROUP BY tests with multiple distinct throttle keys sharing the reset flag column —
    // the tests above only ever exercise a single "symbol"/"APIM" key at a time. The tests
    // below confirm the reset flag on throttler:count(price, reset) is properly SCOPED to
    // only the key whose own event carried the flag, and doesn't disturb sibling keys
    // sharing the same query/window.
    // -----------------------------------------------------------------------

    /**
     * The admin-triggered reset-flag mechanism (throttler:count(messageID, resetFlag), the
     * 2-arg form used by app.xml) must be properly SCOPED to only the key whose event
     * actually carried the reset flag. Resetting "App1_ResourceA" must not touch
     * "App1_ResourceB" or "App2_ResourceA"'s independently-accumulated counts.
     */
    @Test
    public void adminFlagResetOnlyAffectsTargetedKey_otherAppsAndApisUntouched() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string, reset bool);" +
                "@info(name = 'query1') " +
                "from cseEventStream#throttler:timeBatch(10000) " +
                "select throttleKey, throttler:count(messageID, reset) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        execPlanRunTime.start();
        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        // All three keys accumulate to 3.
        String[] keys = {"App1_ResourceA", "App1_ResourceB", "App2_ResourceA"};
        for (String key : keys) {
            for (int i = 1; i <= 3; i++) {
                inputHandler.send(new Object[]{key, key + "-msg" + i, false});
            }
        }
        junit.framework.Assert.assertEquals(Long.valueOf(3L), lastCountPerKey.get("App1_ResourceA"));
        junit.framework.Assert.assertEquals(Long.valueOf(3L), lastCountPerKey.get("App1_ResourceB"));
        junit.framework.Assert.assertEquals(Long.valueOf(3L), lastCountPerKey.get("App2_ResourceA"));

        // Admin resets ONLY "App1_ResourceA".
        inputHandler.send(new Object[]{"App1_ResourceA", "admin-reset-msg", true});
        // A follow-up real request for the reset key should now start counting from 0 -> 1.
        inputHandler.send(new Object[]{"App1_ResourceA", "post-reset-msg", false});

        execPlanRunTime.shutdown();

        junit.framework.Assert.assertEquals("The targeted key should reflect the reset (0) then one new "
                + "increment (1)", Long.valueOf(1L), lastCountPerKey.get("App1_ResourceA"));
        junit.framework.Assert.assertEquals("A different resource under the SAME app must be untouched",
                Long.valueOf(3L), lastCountPerKey.get("App1_ResourceB"));
        junit.framework.Assert.assertEquals("A different app entirely must be untouched",
                Long.valueOf(3L), lastCountPerKey.get("App2_ResourceA"));
    }

    /**
     * Admin-triggered flag reset on one key in the middle of a window, followed later by the
     * natural window-boundary rollover — confirms the two mechanisms compose correctly: the
     * admin reset takes effect immediately and scoped to its own key, the later rollover
     * still correctly resets every key (including ones untouched by the admin reset), and
     * neither corrupts the other.
     */
    @Test
    public void adminFlagResetMidWindow_thenNaturalBoundaryLater_bothApplyCorrectly() throws InterruptedException {
        SiddhiManager siddhiManager = new SiddhiManager();
        String query = "" +
                "define stream cseEventStream (throttleKey string, messageID string, reset bool);" +
                "@info(name = 'query1') " +
                "from cseEventStream#throttler:timeBatch(3000) " +
                "select throttleKey, throttler:count(messageID, reset) as cnt, expiryTimeStamp " +
                "group by throttleKey " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(query);
        Map<String, Long> lastCountPerKey = new ConcurrentHashMap<>();
        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    for (Event e : inEvents) {
                        lastCountPerKey.put((String) e.getData(0), (Long) e.getData(1));
                    }
                }
            }
        });
        execPlanRunTime.start();
        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");

        // Both keys accumulate to 4 mid-window.
        for (int i = 0; i < 4; i++) {
            inputHandler.send(new Object[]{"AppM", "m-" + i, false});
        }
        for (int i = 0; i < 4; i++) {
            inputHandler.send(new Object[]{"AppN", "n-" + i, false});
        }
        junit.framework.Assert.assertEquals(Long.valueOf(4L), lastCountPerKey.get("AppM"));
        junit.framework.Assert.assertEquals(Long.valueOf(4L), lastCountPerKey.get("AppN"));

        // Admin resets ONLY AppM, mid-window, well before the natural 3s boundary.
        inputHandler.send(new Object[]{"AppM", "admin-reset", true});
        inputHandler.send(new Object[]{"AppM", "post-admin-reset", false});
        junit.framework.Assert.assertEquals("AppM should be at 1 right after its own admin reset + one "
                + "new event", Long.valueOf(1L), lastCountPerKey.get("AppM"));
        junit.framework.Assert.assertEquals("AppN must be untouched by AppM's admin reset",
                Long.valueOf(4L), lastCountPerKey.get("AppN"));

        // Let the natural window boundary fire.
        Thread.sleep(3500);

        // Fresh window: both keys must start at 1, regardless of the mid-window admin reset
        // history — AppM from its post-admin-reset baseline of 1, AppN from its pre-boundary
        // baseline of 4, both reset by the SAME rollover.
        inputHandler.send(new Object[]{"AppM", "new-window", false});
        inputHandler.send(new Object[]{"AppN", "new-window", false});
        execPlanRunTime.shutdown();

        junit.framework.Assert.assertEquals("AppM should be fresh at 1 in the new window",
                Long.valueOf(1L), lastCountPerKey.get("AppM"));
        junit.framework.Assert.assertEquals("AppN should be fresh at 1 in the new window too",
                Long.valueOf(1L), lastCountPerKey.get("AppN"));
    }
}
