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

package org.wso2.carbon.apimgt.rest.api.store.utils.mappings;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** This class is responsible for mapping APIM core subscription related objects into REST API subscription related DTOs 
 *
 */
public class SubscriptionMappingUtil {

    /** Converts a SubscribedAPI object into SubscriptionDTO
     *
     * @param subscription SubscribedAPI object
     * @return SubscriptionDTO corresponds to SubscribedAPI object
     */
    public static SubscriptionDTO fromSubscriptionToDTO(SubscribedAPI subscription) throws UnsupportedEncodingException {
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        APIIdentifier apiId = subscription.getApiId();
        APIIdentifier apiIdEmailReplacedBack = new APIIdentifier(APIUtil.replaceEmailDomainBack(apiId.getProviderName
                ()).replace(RestApiConstants.API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER),
                URLEncoder.encode(apiId.getApiName(), RestApiConstants.CHARSET).replace(RestApiConstants
                        .API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER),apiId.getVersion().
                replace(RestApiConstants.API_ID_DELIMITER, RestApiConstants.URL_ENCODED_API_ID_DELIMITER));
        subscriptionDTO.setApiIdentifier(apiIdEmailReplacedBack.toString());
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(SubscriptionDTO.StatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setTier(subscription.getTier().getName());
        return subscriptionDTO;
    }

    /** 
     * Converts a SubscriptionDTO object into SubscribedAPI
     * 
     * @param subscriptionDTO SubscriptionDTO object
     * @param username usename of the logged in user
     * @param requestedTenantDomain tenant domain that the API (uuid) of the subscription belongs to
     * @return SubscribedAPI object that corresponds to the SubscriptionDTO
     * @throws APIManagementException
     */
    public static SubscribedAPI fromDTOToSubscription(SubscriptionDTO subscriptionDTO, String username,
            String requestedTenantDomain) throws APIManagementException, UnsupportedEncodingException {

        APIIdentifier apiIdentifier = APIMappingUtil
                .getAPIIdentifierFromApiIdOrUUID(subscriptionDTO.getApiIdentifier(), requestedTenantDomain);
        Subscriber subscriber = new Subscriber(username);
        SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, apiIdentifier);
        subscribedAPI.setTier(new Tier(subscriptionDTO.getTier()));
        subscribedAPI.setApplication(new Application(subscriptionDTO.getApplicationId()));
        return subscribedAPI;
    }

    /** Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param limit max number of objects returned
     * @param offset starting index
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     */
    public static SubscriptionListDTO fromSubscriptionListToDTO(List<SubscribedAPI> subscriptions, Integer limit,
            Integer offset) throws UnsupportedEncodingException {

        SubscriptionListDTO subscriptionListDTO = new SubscriptionListDTO();
        List<SubscriptionDTO> subscriptionDTOs = subscriptionListDTO.getList();
        if (subscriptionDTOs == null) {
            subscriptionDTOs = new ArrayList<>();
            subscriptionListDTO.setList(subscriptionDTOs);
        }

        //identifying the proper start and end indexes
        int size =subscriptions.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit -1 : size - 1;

        for (int i = start; i <= end; i++) {
            SubscribedAPI subscription = subscriptions.get(i);
            subscriptionDTOs.add(fromSubscriptionToDTO(subscription));
        }

        subscriptionListDTO.setCount(subscriptionDTOs.size());
        return subscriptionListDTO;
    }

    /** Sets pagination urls for a SubscriptionListDTO object given pagination parameters and url parameters for APIId
     *
     * @param subscriptionListDTO a SubscriptionListDTO object
     * @param apiId uuid/id of API
     * @param groupId group id of the applications to be returned
     * @param limit max number of objects returned
     * @param offset starting index
     * @param size max offset
     */
    public static void setPaginationParamsForAPIId(SubscriptionListDTO subscriptionListDTO, String apiId,
            String groupId, Integer limit, Integer offset, Integer size) {

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

    /** Sets pagination urls for a SubscriptionListDTO object given pagination parameters and url parameters 
     * for application id
     *
     * @param subscriptionListDTO a SubscriptionListDTO object
     * @param applicationId uuid of the application
     * @param limit max number of objects returned
     * @param offset starting index
     * @param size max offset
     */
    public static void setPaginationParamsForApplicationId(SubscriptionListDTO subscriptionListDTO,
            String applicationId, Integer limit, Integer offset, Integer size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getSubscriptionPaginatedURLForApplicationId(
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), applicationId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getSubscriptionPaginatedURLForApplicationId(
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), applicationId);
        }

        subscriptionListDTO.setNext(paginatedNext);
        subscriptionListDTO.setPrevious(paginatedPrevious);
    }
}