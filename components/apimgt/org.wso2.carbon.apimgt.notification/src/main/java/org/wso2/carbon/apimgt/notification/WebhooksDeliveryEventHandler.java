/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.notification;

import com.google.gson.Gson;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.WebhooksDAO;
import org.wso2.carbon.apimgt.impl.handlers.EventHandler;
import org.wso2.carbon.apimgt.notification.event.WebhooksDeliveryEvent;

import java.util.List;
import java.util.Map;

/**
 * This class implements to handle webhooks delivery status related notification events.
 */
public class WebhooksDeliveryEventHandler implements EventHandler {
    @Override
    public boolean handleEvent(String event, Map<String, List<String>> headers) throws APIManagementException {
        WebhooksDeliveryEvent deliveryEvent = new Gson().fromJson(event, WebhooksDeliveryEvent.class);
        WebhooksDAO.getInstance().updateDeliveryStatus(deliveryEvent.getApiUUID(), deliveryEvent.getAppID(),
                deliveryEvent.getTenantDomain(), deliveryEvent.getCallback(), deliveryEvent.getTopic(),
                deliveryEvent.getStatus());
        return true;
    }

    @Override
    public String getType() {
        return APIConstants.Webhooks.DELIVERY_EVENT_TYPE;
    }

}
