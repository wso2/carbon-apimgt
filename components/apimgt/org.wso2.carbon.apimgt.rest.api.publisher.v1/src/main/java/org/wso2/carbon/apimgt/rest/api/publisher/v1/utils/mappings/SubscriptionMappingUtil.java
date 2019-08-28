/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;
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
    public static SubscriptionDTO fromSubscriptionToDTO(SubscribedAPI subscription)
            throws APIManagementException {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        subscriptionDTO.setApplicationInfo(FromApplicationToApplicationInfoDTO(subscription.getApplication()));
        subscriptionDTO.setSubscriptionStatus(
                SubscriptionDTO.SubscriptionStatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setThrottlingPolicy(subscription.getTier().getName());

        return subscriptionDTO;
    }

    /**
     * Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param limit max number of objects returned
     * @param offset starting index
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     */
    public static SubscriptionListDTO fromSubscriptionListToDTO(List<SubscribedAPI> subscriptions, Integer limit,
                                                                Integer offset) throws APIManagementException {
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
     * Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param query query to filter subscriptions
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     */
    public static SubscriptionListDTO fromSubscriptionListToDTO(List<SubscribedAPI> subscriptions, String query)
            throws APIManagementException {
        SubscriptionListDTO subscriptionListDTO = new SubscriptionListDTO();
        List<SubscriptionDTO> subscriptionDTOs = subscriptionListDTO.getList();
        if (subscriptionDTOs == null) {
            subscriptionDTOs = new ArrayList<>();
            subscriptionListDTO.setList(subscriptionDTOs);
        }

        query = query.toLowerCase().trim();
        for (SubscribedAPI sub : subscriptions) {
            SubscriptionDTO subscription = fromSubscriptionToDTO(sub);
            if (subscription.getApplicationInfo().getName().toLowerCase().contains(query) ||
                    subscription.getApplicationInfo().getSubscriber().toLowerCase().contains(query) ||
                    subscription.getThrottlingPolicy().toLowerCase().contains(query)) {
                subscriptionDTOs.add(subscription);
            }
        }
        subscriptionListDTO.setCount(subscriptionDTOs.size());

        return subscriptionListDTO;
    }

    public static SubscriptionListDTO getPaginatedSubscriptions(SubscriptionListDTO subscriptionListDTO, Integer limit,
                                                                Integer offset) {
        SubscriptionListDTO paginatedSubscriptionListDTO = new SubscriptionListDTO();
        List<SubscriptionDTO> subscriptionDTOs = paginatedSubscriptionListDTO.getList();
        if (subscriptionDTOs == null) {
            subscriptionDTOs = new ArrayList<>();
            paginatedSubscriptionListDTO.setList(subscriptionDTOs);
        }

        //identifying the proper start and end indexes
        int size = subscriptionListDTO.getCount();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

        List<SubscriptionDTO> subscriptions = subscriptionListDTO.getList();

        for (int i = start; i <= end; i++) {
            subscriptionDTOs.add(subscriptions.get(i));
        }
        paginatedSubscriptionListDTO.setCount(subscriptionDTOs.size());

        return paginatedSubscriptionListDTO;
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

        PaginationDTO pagination = new PaginationDTO();
        pagination.setOffset(offset);
        pagination.setLimit(limit);
        pagination.setNext(paginatedNext);
        pagination.setPrevious(paginatedPrevious);
        pagination.setTotal(size);
        subscriptionListDTO.setPagination(pagination);
    }

    /**
     * Converts a UserApplicationAPIUsage[] array to a corresponding SubscriptionListDTO
     *
     * @param allApiUsage array of UserApplicationAPIUsage
     * @return a list of all subscriptions
     */
    public static List<SubscribedAPI> fromUserApplicationAPIUsageArrayToSubscribedAPIList(
            UserApplicationAPIUsage[] allApiUsage) {
        List<SubscribedAPI> subscribedAPIs = new ArrayList<>();

        for (UserApplicationAPIUsage usage : allApiUsage) {
            Collections.addAll(subscribedAPIs, usage.getApiSubscriptions());
        }

        return subscribedAPIs;
    }

    /**
     * Convert Application to an ApplicationInfoDTO
     *
     * @param application the application to be converted
     * @return ApplicationInfoDTO corresponding to the application
     * @throws APIManagementException If an error occurs when getting logged in provider or when getting lightweight API
     */
    private static ApplicationInfoDTO FromApplicationToApplicationInfoDTO(Application application)
            throws APIManagementException{
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

        application = apiProvider.getLightweightApplicationByUUID(application.getUUID());
        return ApplicationMappingUtil.fromApplicationToInfoDTO(application);
    }
}
