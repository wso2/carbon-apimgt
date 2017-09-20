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

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings;

import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ExtendedSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping APIM core subscription related objects into REST API subscription related DTOs
 */
public class SubscriptionMappingUtil {

    /**
     * Converts a SubscribedAPI object into SubscriptionDTO
     *
     * @param subscription SubscribedAPI object
     * @return SubscriptionDTO corresponds to SubscribedAPI object
     */
    public static SubscriptionDTO fromSubscriptionToDTO(SubscribedAPI subscription) {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        APIIdentifier apiId = subscription.getApiId();
        APIIdentifier apiIdEmailReplacedBack = new APIIdentifier(
                APIUtil.replaceEmailDomainBack(apiId.getProviderName()), apiId.getApiName(), apiId.getVersion());
        subscriptionDTO.setApiIdentifier(apiIdEmailReplacedBack.toString());
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(SubscriptionDTO.StatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setTier(subscription.getTier() == null ? null : subscription.getTier().getName());
        return subscriptionDTO;
    }

    /**
     * Converts a SubscribedAPI object into {@link ExtendedSubscriptionDTO}. Uses {@param workflowReferenceId}.
     *
     * @param subscription SubscribedAPI object
     * @param workflowReferenceId external workflow reference id
     * @return SubscriptionDTO corresponds to SubscribedAPI object
     */
    public static ExtendedSubscriptionDTO fromSubscriptionToExtendedSubscriptionDTO(SubscribedAPI subscription,
            String workflowReferenceId) {
        ExtendedSubscriptionDTO subscriptionDTO = new ExtendedSubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        APIIdentifier apiId = subscription.getApiId();
        APIIdentifier apiIdEmailReplacedBack = new APIIdentifier(
                APIUtil.replaceEmailDomainBack(apiId.getProviderName()), apiId.getApiName(), apiId.getVersion());
        subscriptionDTO.setApiIdentifier(apiIdEmailReplacedBack.toString());
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(SubscriptionDTO.StatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setTier(subscription.getTier() == null ? null : subscription.getTier().getName());
        if (workflowReferenceId != null && SubscriptionDTO.StatusEnum.ON_HOLD.name()
                .equals(subscription.getSubStatus())) {
            subscriptionDTO.setWorkflowId(workflowReferenceId);
        }
        return subscriptionDTO;
    }

    /**
     * Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param limit         max number of objects returned
     * @param offset        starting index
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     */
    public static SubscriptionListDTO fromSubscriptionListToDTO(List<SubscribedAPI> subscriptions, int limit,
            int offset) {
        SubscriptionListDTO subscriptionListDTO = new SubscriptionListDTO();
        List<SubscriptionDTO> subscriptionDTOs = subscriptionListDTO.getList();
        if (subscriptionDTOs == null) {
            subscriptionDTOs = new ArrayList<>();
            subscriptionListDTO.setList(subscriptionDTOs);
        }

        //identifying the proper start and end indexes
        int size = subscriptions.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

        for (int i = start; i <= end; i++) {
            SubscribedAPI subscription = subscriptions.get(i);
            subscriptionDTOs.add(fromSubscriptionToDTO(subscription));
        }
        subscriptionListDTO.setCount(subscriptionDTOs.size());
        return subscriptionListDTO;
    }

    /**
     * Sets pagination urls for a SubscriptionListDTO object given pagination parameters and url parameters
     *
     * @param subscriptionListDTO a SubscriptionListDTO object
     * @param apiId               uuid/id of API
     * @param groupId             group id of the applications to be returned
     * @param limit               max number of objects returned
     * @param offset              starting index
     * @param size                max offset
     */
    public static void setPaginationParams(SubscriptionListDTO subscriptionListDTO, String apiId,
            String groupId, int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getSubscriptionPaginatedURLForAPIId(
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), apiId, groupId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getSubscriptionPaginatedURLForAPIId(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), apiId, groupId);
        }

        subscriptionListDTO.setNext(paginatedNext);
        subscriptionListDTO.setPrevious(paginatedPrevious);
    }

    /**
     * Converts a UserApplicationAPIUsage[] array to a corresponding SubscriptionListDTO
     *
     * @param allApiUsage array of UserApplicationAPIUsage
     * @param limit       max number of objects returned
     * @param offset      starting index
     * @return a dto containing all subscriptions
     */
    public static SubscriptionListDTO fromUserApplicationAPIUsageArrayToDTO(UserApplicationAPIUsage[] allApiUsage,
            Integer limit, Integer offset) {
        List<SubscribedAPI> subscribedAPIs = new ArrayList<>();
        for (UserApplicationAPIUsage usage : allApiUsage) {
            Collections.addAll(subscribedAPIs, usage.getApiSubscriptions());
        }
        return fromSubscriptionListToDTO(subscribedAPIs, limit, offset);
    }
}