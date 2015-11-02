/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.rest.api.dto.SubscriptionDTO;

public class SubscriptionMappingUtil {
    public static SubscriptionDTO fromSubscriptionToDTO(SubscribedAPI subscription) {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        subscriptionDTO.setApiId(subscription.getApiId().toString());
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(SubscriptionDTO.StatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setTier(subscription.getTier().getName());
        return subscriptionDTO;
    }

    public static SubscribedAPI fromDTOToSubscription(SubscriptionDTO subscription) {
        SubscribedAPI subscribedAPI = new SubscribedAPI(subscription.getSubscriptionId());
        subscribedAPI.setSubStatus(subscription.getStatus().toString());
        subscribedAPI.setTier(new Tier(subscription.getTier()));
        subscribedAPI.setApplication(new Application(subscription.getApplicationId()));
        //subscribedAPI.setAPIId(subscription.getApiId()); //todo need to add support in impl
        return subscribedAPI;
    }
}