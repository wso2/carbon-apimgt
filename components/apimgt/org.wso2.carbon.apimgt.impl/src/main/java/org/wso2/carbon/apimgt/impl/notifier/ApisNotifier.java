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
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.APIEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * The default API notification service implementation in which API creation, update, delete and LifeCycle change
 * events are published to gateway.
 */
public class ApisNotifier implements Notifier {
    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        try {
            APIEvent apiEvent = (APIEvent) event;
            byte[] bytesEncoded = Base64.encodeBase64(new Gson().toJson(apiEvent).getBytes());
            Object[] objects = new Object[]{apiEvent.getType(), apiEvent.getTimeStamp(), new String(bytesEncoded)};
            org.wso2.carbon.databridge.commons.Event payload = new org.wso2.carbon.databridge.commons.Event(
                    APIConstants.NOTIFICATION_STREAM_ID, System.currentTimeMillis(),
                    null, null, objects);
            APIUtil.publishEventToEventHub(null, payload);
            return true;
        } catch (Exception e) {
            throw new NotifierException(e);
        }
    }

    @Override
    public String getType() {
        return APIConstants.NotifierType.API.name();
    }
}
