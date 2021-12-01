/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.notifier;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.wso2.carbon.apimgt.eventing.EventPublisherEvent;
import org.wso2.carbon.apimgt.eventing.EventPublisherType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * Abstract class to handle common implementations related to notifiers
 */
public abstract class AbstractNotifier implements Notifier {

    protected void publishEventToEventHub(Event event) {
        byte[] bytesEncoded = Base64.encodeBase64(new Gson().toJson(event).getBytes());
        Object[] objects = new Object[]{event.getType(), event.getTimeStamp(), new String(bytesEncoded)};
        //Decoded event string to be logged in the case of failures and debugging
        String loggingEvent = event.toString();
        EventPublisherEvent notificationEvent = new EventPublisherEvent(APIConstants.NOTIFICATION_STREAM_ID,
                System.currentTimeMillis(), objects, loggingEvent);
        APIUtil.publishEvent(EventPublisherType.NOTIFICATION, notificationEvent, loggingEvent);
    }
}
