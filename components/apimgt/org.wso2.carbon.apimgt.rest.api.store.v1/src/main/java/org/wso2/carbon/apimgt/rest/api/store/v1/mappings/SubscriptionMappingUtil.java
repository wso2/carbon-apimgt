/*
 *
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
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is responsible for mapping APIM core subscription related objects into REST API subscription related DTOs
 */
public class SubscriptionMappingUtil {

    private static final Log log = LogFactory.getLog(SubscriptionMappingUtil.class);

    /**
     * Converts a SubscribedAPI object into SubscriptionDTO
     *
     * @param subscription SubscribedAPI object
     * @param organization Identifier of the organization
     * @return SubscriptionDTO corresponds to SubscribedAPI object
     */
    public static SubscriptionDTO fromSubscriptionToDTO(SubscribedAPI subscription, String organization)
            throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        APIInfoDTO apiInfo;
        Identifier apiId = subscription.getIdentifier();
        ApiTypeWrapper apiTypeWrapper;
        try {
            apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(subscription.getIdentifier().getUUID(), organization);
            subscriptionDTO.setApiId(subscription.getIdentifier().getUUID());
            Set<String> deniedTiers = apiConsumer.getDeniedTiers(organization);
            Map<String, Tier> tierMap = APIUtil.getTiers(organization);
            if (apiTypeWrapper.isAPIProduct()) {
                apiInfo = APIMappingUtil.fromAPIToInfoDTO(apiTypeWrapper.getApiProduct(), organization);
                APIMappingUtil.setThrottlePoliciesAndMonetization(apiTypeWrapper.getApiProduct(), apiInfo,
                        deniedTiers, tierMap);
            } else {
                apiInfo = APIMappingUtil.fromAPIToInfoDTO(apiTypeWrapper.getApi());
                APIMappingUtil.setThrottlePoliciesAndMonetization(apiTypeWrapper.getApi(), apiInfo, deniedTiers,
                        tierMap);
            }
            subscriptionDTO.setApiInfo(apiInfo);
        } catch (APIManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("User :" + username + " does not have access to the API " + apiId);
            }
            apiInfo = new APIInfoDTO();
            apiInfo.setName(apiId.getName());
            apiInfo.setVersion(apiId.getVersion());
            subscriptionDTO.setApiInfo(apiInfo);
        }
        Application application = subscription.getApplication();
        application = apiConsumer.getLightweightApplicationByUUID(application.getUUID());
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(SubscriptionDTO.StatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setThrottlingPolicy(subscription.getTier().getName());
        subscriptionDTO.setRequestedThrottlingPolicy(subscription.getRequestedTier().getName());
        ApplicationInfoDTO applicationInfoDTO = ApplicationMappingUtil.fromApplicationToInfoDTO(application);
        subscriptionDTO.setApplicationInfo(applicationInfoDTO);
        return subscriptionDTO;
    }

    public static SubscriptionDTO fromSubscriptionToDTO(SubscribedAPI subscription, ApiTypeWrapper apiTypeWrapper,
                                                        String organization)
            throws APIManagementException {

        SubscriptionDTO subscriptionDTO = new SubscriptionDTO();
        subscriptionDTO.setSubscriptionId(subscription.getUUID());
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        Set<String> deniedTiers = apiConsumer.getDeniedTiers(organization);
        Map<String,Tier> tierMap = APIUtil.getTiers(organization);
        if (apiTypeWrapper != null && !apiTypeWrapper.isAPIProduct()) {
            API api = apiTypeWrapper.getApi();
            subscriptionDTO.setApiId(api.getUUID());
            APIInfoDTO apiInfo = APIMappingUtil.fromAPIToInfoDTO(api);
            APIMappingUtil.setThrottlePoliciesAndMonetization(api, apiInfo, deniedTiers, tierMap);
            subscriptionDTO.setApiInfo(apiInfo);
        } else {
            APIProduct apiProduct = apiTypeWrapper.getApiProduct();
            subscriptionDTO.setApiId(apiProduct.getUuid());
            APIInfoDTO apiInfo = APIMappingUtil.fromAPIToInfoDTO(apiProduct, organization);
            APIMappingUtil.setThrottlePoliciesAndMonetization(apiProduct, apiInfo, deniedTiers, tierMap);
            subscriptionDTO.setApiInfo(apiInfo);
        }
        Application application = subscription.getApplication();
        subscriptionDTO.setApplicationId(subscription.getApplication().getUUID());
        subscriptionDTO.setStatus(SubscriptionDTO.StatusEnum.valueOf(subscription.getSubStatus()));
        subscriptionDTO.setThrottlingPolicy(subscription.getTier().getName());
        subscriptionDTO.setRequestedThrottlingPolicy(subscription.getRequestedTier().getName());

        ApplicationInfoDTO applicationInfoDTO = ApplicationMappingUtil.fromApplicationToInfoDTO(application);
        subscriptionDTO.setApplicationInfo(applicationInfoDTO);

        return subscriptionDTO;
    }

    /**
     * Converts a List object of SubscribedAPIs into a DTO
     *
     * @param subscriptions a list of SubscribedAPI objects
     * @param limit         max number of objects returned
     * @param offset        starting index
     * @param organization  identifier of the organization
     * @return SubscriptionListDTO object containing SubscriptionDTOs
     */
    public static SubscriptionListDTO fromSubscriptionListToDTO(List<SubscribedAPI> subscriptions, Integer limit,
                                                                Integer offset, String organization) throws APIManagementException {

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
            try {
                SubscribedAPI subscription = subscriptions.get(i);
                subscriptionDTOs.add(fromSubscriptionToDTO(subscription, organization));
            } catch (APIManagementException e) {
                log.error("Error while obtaining api metadata", e);
            }
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

        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getSubscriptionPaginatedURLForAPIId(
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), apiId, groupId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
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
}
