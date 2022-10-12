/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.apk.apimgt.rest.api.store.v1.common.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.model.API;
import org.wso2.apk.apimgt.api.APIConsumer;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.model.APIIdentifier;
import org.wso2.apk.apimgt.api.model.Application;
import org.wso2.apk.apimgt.api.model.SubscribedAPI;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.AdditionalSubscriptionInfoListDTO;
import org.wso2.apk.apimgt.rest.api.store.v1.dto.PaginationDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Util class for AdditionalSubscription Info Mapping
 */
public class AdditionalSubscriptionInfoMappingUtil {

    private static final Log log = LogFactory.getLog(AdditionalSubscriptionInfoMappingUtil.class);

    /**
     * Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param limit         max number of objects returned
     * @param offset        starting index
     * @param organization  identifier of the organization
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     * @throws APIManagementException if error occurred when creating AdditionalSubscriptionInfoListDTO
     */
    public static AdditionalSubscriptionInfoListDTO fromAdditionalSubscriptionInfoListToDTO(
            List<SubscribedAPI> subscriptions, Integer limit, Integer offset, String organization)
            throws APIManagementException {

        AdditionalSubscriptionInfoListDTO additionalSubscriptionInfoListDTO = new AdditionalSubscriptionInfoListDTO();
        List<AdditionalSubscriptionInfoDTO> additionalSubscriptionInfoDTOs =
                additionalSubscriptionInfoListDTO.getList();
        if (additionalSubscriptionInfoDTOs == null) {
            additionalSubscriptionInfoDTOs = new ArrayList<>();
            additionalSubscriptionInfoListDTO.setList(additionalSubscriptionInfoDTOs);
        }

        //Identifying the proper start and end indexes
        int size = subscriptions.size();
        if (size > 0) {
            int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
            int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

            for (int i = start; i <= end; i++) {
                try {
                    SubscribedAPI subscription = subscriptions.get(i);
                    additionalSubscriptionInfoDTOs.add(fromAdditionalSubscriptionInfoToDTO(subscription, organization));
                } catch (APIManagementException e) {
                    log.error("Error while obtaining additional info of subscriptions", e);
                }
            }
        }

        // Set count for list
        additionalSubscriptionInfoListDTO.setCount(additionalSubscriptionInfoDTOs.size());
        return additionalSubscriptionInfoListDTO;
    }

    /**
     * Converts a AdditionalSubscriptionInfo object into AdditionalSubscriptionInfoDTO
     *
     * @param subscription SubscribedAPI object
     * @param organization Identifier of the organization
     * @return SubscriptionDTO corresponds to SubscribedAPI object
     */
    public static AdditionalSubscriptionInfoDTO fromAdditionalSubscriptionInfoToDTO(SubscribedAPI subscription,
            String organization) throws APIManagementException {
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        AdditionalSubscriptionInfoDTO additionalSubscriptionInfoDTO = new AdditionalSubscriptionInfoDTO();
        additionalSubscriptionInfoDTO.setSubscriptionId(subscription.getUUID());
        APIIdentifier apiId = subscription.getAPIIdentifier();
        API api = null;

        if (apiId != null) {
            api = apiConsumer.getLightweightAPIByUUID(apiId.getUUID(), organization);
        }

        Application application = subscription.getApplication();
        if (api != null) {
            additionalSubscriptionInfoDTO.setApiId(api.getUuid());
            // Set Application information
            application = apiConsumer.getApplicationByUUID(application.getUUID());
            additionalSubscriptionInfoDTO.setApplicationId(subscription.getApplication().getUUID());
            additionalSubscriptionInfoDTO.setApplicationName(application.getName());
        }

        return additionalSubscriptionInfoDTO;
    }

    /**
     * Sets pagination urls for a AdditionalSubscriptionInfoListDTO object given pagination parameters and url
     * parameters
     *
     * @param additionalSubscriptionInfoListDTO a AdditionalSubscriptionInfoListDTO object
     * @param apiId                             uuid/id of API
     * @param groupId                           group id of the applications to be returned
     * @param limit                             max number of objects returned
     * @param offset                            starting index
     * @param size                              max offset
     */
    public static void setPaginationParams(AdditionalSubscriptionInfoListDTO additionalSubscriptionInfoListDTO,
            String apiId, String groupId, int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil.getSubscriptionPaginatedURLForAPIId(
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), apiId, groupId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil.getSubscriptionPaginatedURLForAPIId(
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), apiId, groupId);
        }

        PaginationDTO pagination = new PaginationDTO();
        pagination.setOffset(offset);
        pagination.setLimit(limit);
        pagination.setNext(paginatedNext);
        pagination.setPrevious(paginatedPrevious);
        pagination.setTotal(size);
        additionalSubscriptionInfoListDTO.setPagination(pagination);
    }

}
