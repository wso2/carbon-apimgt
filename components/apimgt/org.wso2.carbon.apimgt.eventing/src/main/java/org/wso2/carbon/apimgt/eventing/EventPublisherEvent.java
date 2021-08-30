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

/**
 * Event class for eventing.
 */
public class EventPublisherEvent extends Event {

    public EventPublisherEvent(java.lang.String streamId, long timeStamp, java.lang.Object[] metaDataArray,
                 java.lang.Object[] correlationDataArray, java.lang.Object[] payloadDataArray) {
        super(streamId, timeStamp, metaDataArray, correlationDataArray, payloadDataArray);
    }
}
