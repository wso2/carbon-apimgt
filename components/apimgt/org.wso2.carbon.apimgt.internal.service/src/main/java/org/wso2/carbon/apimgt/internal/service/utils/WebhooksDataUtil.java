/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.internal.service.utils;

import org.wso2.carbon.apimgt.api.model.webhooks.Subscription;
import org.wso2.carbon.apimgt.internal.service.dto.WebhooksSubscriptionDTO;
import org.wso2.carbon.apimgt.internal.service.dto.WebhooksSubscriptionsListDTO;

import java.util.ArrayList;
import java.util.List;

/*
The util class of the webhooks data
 */
public class WebhooksDataUtil {

    public static WebhooksSubscriptionsListDTO fromSubscriptionDataListToSubscriptionDtoList
            (List<Subscription> modelData) {

        WebhooksSubscriptionsListDTO subscriptionsListDTO = new WebhooksSubscriptionsListDTO();
        List<WebhooksSubscriptionDTO> subscriptionDTOS = new ArrayList<>();
        for (Subscription model : modelData) {
            subscriptionDTOS.add(fromSubscriptionModelToSubscriptionDto(model));
        }
        subscriptionsListDTO.setList(subscriptionDTOS);
        return subscriptionsListDTO;
    }

    private static WebhooksSubscriptionDTO fromSubscriptionModelToSubscriptionDto(Subscription model) {
        WebhooksSubscriptionDTO webhooksSubscriptionDTO = new WebhooksSubscriptionDTO();
        webhooksSubscriptionDTO.setApiUUID(model.getApiUuid());
        webhooksSubscriptionDTO.setAppID(model.getAppID());
        webhooksSubscriptionDTO.setCallbackURL(model.getCallback());
        webhooksSubscriptionDTO.setTopicName(model.getTopic());
        webhooksSubscriptionDTO.setSecret(model.getSecret());
        webhooksSubscriptionDTO.setExpiryTime(model.getExpiryTime());
        webhooksSubscriptionDTO.setApiContext(model.getApiContext());
        webhooksSubscriptionDTO.setApiVersion(model.getApiVersion());
        webhooksSubscriptionDTO.setTenantDomain(model.getTenantDomain());
        webhooksSubscriptionDTO.setTenantId(model.getTenantId());
        webhooksSubscriptionDTO.setTier(model.getTier());
        webhooksSubscriptionDTO.setApiTier(model.getApiTier());
        webhooksSubscriptionDTO.setApplicationTier(model.getApplicationTier());
        webhooksSubscriptionDTO.setSubscriberName(model.getSubscriberName());
        return webhooksSubscriptionDTO;
    }

}
