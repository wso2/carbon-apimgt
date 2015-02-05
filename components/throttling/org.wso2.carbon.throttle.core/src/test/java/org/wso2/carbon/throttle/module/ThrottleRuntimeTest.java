/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.throttle.module;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.wso2.carbon.throttle.core.Throttle;
import org.wso2.carbon.throttle.core.ThrottleConstants;
import org.wso2.carbon.throttle.core.ThrottleException;
import org.wso2.carbon.throttle.module.utils.StatCollector;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThrottleRuntimeTest {

    public static final int INIT_DELAY_IN_MILLI_SECS = 10;
    public static final int PERIOD_IN_MILLI_SECS = 100;
    public static final int TOTAL_DURATION_IN_SECS = 25;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);


    public void testThrottleRuntimeOnlyRole() throws Exception {
        StatCollector.enableStatCollection = true;
        TestThrottleHandler throttleHandler = new TestThrottleHandler();
        Throttle throttle = ThrottleTestFactory.getThrottle(ThrottleTestFactory.roleBaseOnlyPolicy);
//        Throttle throttle = ThrottleTestFactory.getThrottle(ThrottleTestFactory.roleBaseOnlyPolicy_2);
//        Throttle throttle = ThrottleTestFactory.getThrottle(ThrottleTestFactory.roleBasePolicy_WithIP);
//        Throttle throttle = ThrottleTestFactory.getThrottle(ThrottleTestFactory.roleBasePolicy_WithGlobalCLimit);
//        Throttle throttle = ThrottleTestFactory.getThrottle(ThrottleTestFactory.modulePolicy);

        //create throttle simulator
        ThrottleRequestSimulator simulator = new ThrottleRequestSimulator(throttleHandler, throttle);

         //schedule job
        final ScheduledFuture<?> scheduleHandle =
                scheduler.scheduleAtFixedRate(simulator, INIT_DELAY_IN_MILLI_SECS, PERIOD_IN_MILLI_SECS, TimeUnit.MILLISECONDS);
        scheduler.schedule(new Runnable() {
            public void run() {
                scheduleHandle.cancel(true);
                StatCollector.displayStats(ThrottleConstants.ROLE_BASE);
                StatCollector.displayStats(ThrottleConstants.IP_BASE);
                StatCollector.flushStats();
                scheduler.shutdown();
            }
        }, TOTAL_DURATION_IN_SECS, TimeUnit.SECONDS);


    }

    public static void main(String[] args) throws Exception {
        new ThrottleRuntimeTest().testThrottleRuntimeOnlyRole();
    }

    private static class ThrottleRequestSimulator implements Runnable{

        private Throttle throttle;
        private TestThrottleHandler throttleHandler;

        public ThrottleRequestSimulator(TestThrottleHandler throttleHandler, Throttle throttle) {
            this.throttleHandler = throttleHandler;
            this.throttle = throttle;
        }

        public void run() {
            MessageContext ctxt = new TestMessageContext();
            try {
                throttleHandler.process(throttle, ctxt);
            } catch (ThrottleException e) {
//                e.printStackTrace();
            } catch (AxisFault axisFault) {
//                axisFault.printStackTrace();
            }
        }
    }

}
