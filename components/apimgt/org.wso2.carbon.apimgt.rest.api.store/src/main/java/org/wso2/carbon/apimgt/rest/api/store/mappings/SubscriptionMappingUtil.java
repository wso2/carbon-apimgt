/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionMappingUtil {

    /** Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param limit max number of objects returned
     * @param offset starting index
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     */
    public static SubscriptionListDTO fromSubscriptionListToDTO(List<Subscription> subscriptions, Integer limit,
            Integer offset) {

        SubscriptionListDTO subscriptionListDTO = new SubscriptionListDTO();
        List<SubscriptionDTO> subscriptionDTOs = subscriptionListDTO.getList();
        if (subscriptionDTOs == null) {
            subscriptionDTOs = new ArrayList<>();
            subscriptionListDTO.setList(subscriptionDTOs);
        }

        //identifying the proper start and end indexes
        int size = subscriptions.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit -1 : size - 1;

        for (int i = start; i <= end; i++) {
            Subscription subscription = subscriptions.get(i);
            subscriptionDTOs.add(fromSubscriptionToDTO(subscription));
        }

        subscriptionListDTO.setCount(subscriptionDTOs.size());
        return subscriptionListDTO;
    }

    /** Converts a SubscribedAPI object into SubscriptionDTO
     *
     * @param subscription SubscribedAPI object
     * @return SubscriptionDTO corresponds to SubscribedAPI object
     */
    public static SubscriptionDTO fromSubscriptionToDTO(Subscription subscription) {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getId());
        if (subscription.getApi() != null) {
            subscriptionDTO.setApiIdentifier(subscription.getApi().getId());
            subscriptionDTO.setApiName(subscription.getApi().getName());
            subscriptionDTO.setApiVersion(subscription.getApi().getVersion());
        }
        if (subscription.getApplication() != null) {
            subscriptionDTO.setApplicationId(subscription.getApplication().getId());
        }
        subscriptionDTO.setPolicy(subscription.getPolicy().getPolicyName());
        subscriptionDTO.setLifeCycleStatus(SubscriptionDTO.LifeCycleStatusEnum.valueOf(subscription.getStatus()
                .toString()));
        return subscriptionDTO;
    }
}
