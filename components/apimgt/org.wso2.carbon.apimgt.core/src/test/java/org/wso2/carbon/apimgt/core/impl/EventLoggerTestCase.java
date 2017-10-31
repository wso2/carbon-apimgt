/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.Event;

/**
 * Test class for EventLogger
 */
public class EventLoggerTestCase {

    @Test(description = "Test capture event")
    public void testCaptureEvent() {
        EventLogger eventLogger = new EventLogger();
        String username = "user1";
        eventLogger.captureEvent(Event.API_CREATION, username, null, null);
    }

    @Test(description = "Test capture event when event is null", expectedExceptions = IllegalArgumentException.class)
    public void testCaptureEventWhenEventNull() {
        EventLogger eventLogger = new EventLogger();
        eventLogger.captureEvent(null, "user1", null, null);
    }

    @Test(description = "Test capture event when user name is null", expectedExceptions = IllegalArgumentException
            .class)
    public void testCaptureEventWhenUserNameNull() {
        EventLogger eventLogger = new EventLogger();
        eventLogger.captureEvent(Event.API_CREATION, null, null, null);
    }
}
