/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.concurrent.atomic.AtomicInteger;

public class ThrottlingTimeLengthWindowTestCase {

    private static final Log log = LogFactory.getLog(ThrottlingTimeLengthWindowTestCase.class);
    private int inEventCount;
    private int removeEventCount;
    private boolean eventArrived;
    private AtomicInteger count = new AtomicInteger();


    @Before
    public void init() {
        inEventCount = 0;
        removeEventCount = 0;
        eventArrived = false;
        count.set(0);

    }

    @Test
    public void throttleTimeLengthWindowTest1() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String requestStream = "" +
                "define stream RequestStream (messageID string, isEligible bool, throttleKey string);";
        String query = "" +
                "@info(name = 'query1') " +
                "from RequestStream#throttler:timeLength(10 sec,0, 2) " +
                "select throttleKey, isThrottled, expiryTimeStamp group by throttleKey " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(requestStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    for (Event event : inEvents) {
                        switch (count.incrementAndGet()) {
                            case 1:
                            case 2:
                            case 4:
                                Assert.assertEquals(false, event.getData(1));
                                break;
                            case 3:
                                Assert.assertEquals(true, event.getData(1));
                                break;
                            default:
                                Assert.fail("Received more than expected number of events. Expected maximum : 4," +
                                        "Received : " + count.get());
                        }
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("RequestStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"message123", true, "message123:1234"});
        inputHandler.send(new Object[]{"message123", true, "message123:1234"});
        inputHandler.send(new Object[]{"message123", true, "message123:1234"});
        inputHandler.send(new Object[]{"message456", true, "message456:1234"});
        Assert.assertEquals(4, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }


    @Test
    public void throttleTimeLengthWindowTest2() throws InterruptedException {

        SiddhiManager siddhiManager = new SiddhiManager();

        String requestStream = "" +
                "define stream RequestStream (messageID string, isEligible bool, throttleKey string);";
        String query = "" +
                "@info(name = 'query1') " +
                "from RequestStream#throttler:timeLength(5 sec,0, 3) " +
                "select throttleKey, isThrottled, expiryTimeStamp group by throttleKey " +
                "insert all events into outputStream ;";

        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(requestStream + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    inEventCount = inEventCount + inEvents.length;
                    for (Event event : inEvents) {
                        switch (count.incrementAndGet()) {
                            case 1:
                            case 2:
                            case 3:
                            case 5:
                                Assert.assertEquals(false, event.getData(1));
                                break;
                            case 4:
                                Assert.assertEquals(true, event.getData(1));
                                break;
                            default:
                                Assert.fail("Received more than expected number of events. Expected maximum : 4," +
                                        "Received : " + count.get());
                        }
                    }
                }
                eventArrived = true;
            }

        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("RequestStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"message123", true, "message123:1234"});
        inputHandler.send(new Object[]{"message123", true, "message123:1234"});
        inputHandler.send(new Object[]{"message123", true, "message123:1234"});
        inputHandler.send(new Object[]{"message123", true, "message123:1234"});
        Thread.sleep(6000);
        inputHandler.send(new Object[]{"message123", true, "message123:1234"});
        Thread.sleep(1000);
        Assert.assertEquals(5, inEventCount);
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();

    }

}
