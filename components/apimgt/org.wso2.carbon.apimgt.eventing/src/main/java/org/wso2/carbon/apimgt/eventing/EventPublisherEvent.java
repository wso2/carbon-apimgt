/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.eventing;

import org.wso2.carbon.databridge.commons.Event;

import java.util.Arrays;
import java.util.Objects;

/**
 * Event class for eventing.
 */
public class EventPublisherEvent extends Event {

    /**
     * The event that needs to be logged in case if it is different from the payload.
     */
    private String loggingEvent;

    public EventPublisherEvent(String streamId, long timeStamp, Object[] payloadDataArray) {
        super(streamId, timeStamp, null, null, payloadDataArray);
    }

    public EventPublisherEvent(java.lang.String streamId, long timeStamp, java.lang.Object[] payloadDataArray,
                               String loggingEvent) {
        this(streamId, timeStamp, payloadDataArray);
        this.loggingEvent = loggingEvent;
    }

    @Override
    public String toString() {
        if (Objects.isNull(loggingEvent)) {
            loggingEvent = (Arrays.asList(this.getPayloadData())).toString();
        }
        return "\nEvent{\n  payloadData" + "=" + loggingEvent + "\n}\n";
    }
}
