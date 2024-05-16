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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit Test cases related to  SumAttributeAggregator with the Reset extension
 */
public class SumAttributeAggregatorWithResetTestCase {

    private static final Log log = LogFactory.getLog(SumAttributeAggregatorWithResetTestCase.class);
    private AtomicInteger atomicEventCount;

    @Before
    public void init() {
        atomicEventCount = new AtomicInteger(0);
    }

    @Test
    public void SumAggregatorTestWithoutReset() throws InterruptedException {
        log.info("SumAggregator Test #1 : Without setting Reset");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('SumAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price float);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.timeBatch(5 sec) " +
                "select throttler:sum(price) as sum " +
                "group by symbol " +
                "having sum > 4 " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);

        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    atomicEventCount.addAndGet(inEvents.length);
                    if (atomicEventCount.get() == 2) {
                        junit.framework.Assert.assertEquals(5D, inEvents[0].getData(0));
                        junit.framework.Assert.assertEquals(6D, inEvents[1].getData(0));
                    }
                }
            }
        });

        execPlanRunTime.start();
        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 1F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 2F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 2F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"APIM", 3F});
        execPlanRunTime.shutdown();
        junit.framework.Assert.assertEquals(2, atomicEventCount.intValue());
    }

    @Test
    public void SumAggregatorTestWithResetSetToFalse() throws InterruptedException {
        log.info("SumAggregator Test #2 : Setting Reset value to be false");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('SumAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price float);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.timeBatch(5 sec) " +
                "select throttler:sum(price, false) as sum " +
                "group by symbol " +
                "having sum > 4 " +
                "insert all events into outputStream;";

        ExecutionPlanRuntime execPlanRunTime = siddhiManager.createExecutionPlanRuntime(execPlan);

        execPlanRunTime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                if (inEvents != null) {
                    atomicEventCount.addAndGet(inEvents.length);
                    if (atomicEventCount.get() == 2) {
                        junit.framework.Assert.assertEquals(5D, inEvents[0].getData(0));
                        junit.framework.Assert.assertEquals(6D, inEvents[1].getData(0));
                    }
                }
            }
        });

        execPlanRunTime.start();
        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 1F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 2F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"WSO2", 2F});
        Thread.sleep(1000);
        inputHandler.send(new Object[]{"APIM", 3F});
        Thread.sleep(2000);
        inputHandler.send(new Object[]{"APIM", 3F});
        execPlanRunTime.shutdown();
        junit.framework.Assert.assertEquals(2, atomicEventCount.intValue());
    }

    @Test
    public void SumAggregatorTestWithResetSetToTrue() throws InterruptedException {
        log.info("SumAggregator Test #3 : Setting Reset value to be true");

        SiddhiManager siddhiManager = new SiddhiManager();

        String execPlan = "" +
                "@Plan:name('SumAggregatorTests') " +
                "" +
                "define stream cseEventStream (symbol string, price float);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream#window.timeBatch(2 sec) " +
                "select symbol, ifThenElse(price == -1F,throttler:sum(price, true),throttler:sum(price, false)) as sum " +
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
                        junit.framework.Assert.assertEquals(3D, inEvents[0].getData(1));
                    } else if (atomicEventCount.get() == 2) {
                        junit.framework.Assert.assertEquals("WSO2", inEvents[0].getData(0));
                        junit.framework.Assert.assertEquals(6D, inEvents[0].getData(1));
                        junit.framework.Assert.assertEquals("APIM", inEvents[1].getData(0));
                        junit.framework.Assert.assertEquals(0D, inEvents[1].getData(1));
                    }
                }
            }
        });

        execPlanRunTime.start();
        InputHandler inputHandler = execPlanRunTime.getInputHandler("cseEventStream");
        inputHandler.send(new Object[]{"WSO2", 1F});
        Thread.sleep(400);
        inputHandler.send(new Object[]{"WSO2", 2F});
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
}
