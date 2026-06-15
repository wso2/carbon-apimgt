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
        Assert.assertEquals(0, removeEventCount);
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


}
