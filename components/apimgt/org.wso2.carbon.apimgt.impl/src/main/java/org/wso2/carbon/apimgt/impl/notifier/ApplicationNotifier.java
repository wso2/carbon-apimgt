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
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

/**
 * The default Application notification service implementation in which Application creation, update and delete
 * events are published to gateway.
 */
public class ApplicationNotifier implements Notifier {
    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        try {
            ApplicationEvent appEvent = (ApplicationEvent) event;
            byte[] bytesEncoded = Base64.encodeBase64(new Gson().toJson(appEvent).getBytes());
            Object[] objects = new Object[]{appEvent.getType(), appEvent.getTimeStamp(), new String(bytesEncoded)};
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
        return APIConstants.NotifierType.APPLICATION.name();
    }
}
