/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductStatus;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.*;

public class APIProductMappingUtil {

    /**
     * Converts a List object of APIProducts into a DTO
     *
     * @param apiProductList List of APIProducts
     * @param limit   maximum number of APIProducts returns
     * @param offset  starting index
     * @return APIProductListDTO object containing APIProductDTOs
     */
    public static APIProductListDTO fromAPIProductListToDTO(List<APIProduct> apiProductList, int offset, int limit) {
        APIProductListDTO apiProductListDTO = new APIProductListDTO();
        List<APIProductInfoDTO> apiProductInfoDTOs = apiProductListDTO.getList();
        if (apiProductInfoDTOs == null) {
            apiProductInfoDTOs = new ArrayList<>();
            apiProductListDTO.setList(apiProductInfoDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < apiProductList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= apiProductList.size() - 1 ? offset + limit - 1 : apiProductList.size() - 1;
        for (int i = start; i <= end; i++) {
            apiProductInfoDTOs.add(fromAPIToInfoDTO(apiProductList.get(i)));
        }
        apiProductListDTO.setCount(apiProductInfoDTOs.size());
        return apiProductListDTO;
    }

    /**
     * Creates a minimal DTO representation of an APIProduct object
     *
     * @param apiProduct APIProduct object
     * @return a minimal representation DTO
     */
    public static APIProductInfoDTO fromAPIToInfoDTO(APIProduct apiProduct) {
        APIProductInfoDTO apiProductInfoDTO = new APIProductInfoDTO();
        apiProductInfoDTO.setDescription(apiProduct.getDescription());
        apiProductInfoDTO.setId(apiProduct.getUUID());
        APIProductIdentifier apiProductId = apiProduct.getId();
        apiProductInfoDTO.setName(apiProductId.getApiProductName());
        apiProductInfoDTO.setVersion(apiProductId.getVersion());
        String providerName = apiProduct.getId().getProviderName();
        apiProductInfoDTO.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        apiProductInfoDTO.setStatus(apiProduct.getStatus().toString());
        return apiProductInfoDTO;
    }

    /**
     * Sets pagination urls for a APIListDTO object given pagination parameters and url parameters
     *
     * @param apiProductListDTO a APIListDTO object
     * @param query      search condition
     * @param limit      max number of objects returned
     * @param offset     starting index
     * @param size       max offset
     */
    public static void setPaginationParams(APIProductListDTO apiProductListDTO, String query, int offset,
                                           int limit, int size) {
        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getAPIProductPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getAPIProductPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        apiProductListDTO.setNext(paginatedNext);
        apiProductListDTO.setPrevious(paginatedPrevious);
    }

    public static APIProduct fromDTOtoAPIProduct(APIProductDTO dto, String provider) throws APIManagementException {
        String providerEmailDomainReplaced = APIUtil.replaceEmailDomain(provider);

        // The provider name that is coming from the body is not honored for now.
        // Later we can use it by checking admin privileges of the user.
        APIProductIdentifier apiId = new APIProductIdentifier(providerEmailDomainReplaced, dto.getName(), dto.getVersion());
        APIProduct model = new APIProduct(apiId);

        model.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            model.setStatus(mapStatusFromDTOToAPIProduct(dto.getStatus()));
        }

        if (dto.getSubscriptionAvailability() != null) {
            model.setSubscriptionAvailability(
                    mapSubscriptionAvailabilityFromDTOtoAPIProduct(dto.getSubscriptionAvailability()));
        }

        if (dto.getSubscriptionAvailableTenants() != null) {
            model.setSubscriptionAvailableTenants(StringUtils.join(dto.getSubscriptionAvailableTenants(), ","));
        }

        if (dto.getTags() != null) {
            Set<String> apiTags = new HashSet<>(dto.getTags());
            model.addTags(apiTags);
        }

        Set<Tier> apiTiers = new HashSet<>();
        List<String> tiersFromDTO = dto.getThrottlingTier();
        for (String tier : tiersFromDTO) {
            apiTiers.add(new Tier(tier));
        }
        model.addAvailableTiers(apiTiers);

        model.setVisibility(mapVisibilityFromDTOtoAPIProduct(dto.getVisibility()));
        if (dto.getVisibleRoles() != null) {
            String visibleRoles = StringUtils.join(dto.getVisibleRoles(), ',');
            model.setVisibleRoles(visibleRoles);
        }

        if (dto.getVisibleTenants() != null) {
            String visibleTenants = StringUtils.join(dto.getVisibleTenants(), ',');
            model.setVisibleTenants(visibleTenants);
        }

        APIBusinessInformationDTO apiBusinessInformationDTO = dto.getBusinessInformation();
        if (apiBusinessInformationDTO != null) {
            model.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
            model.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
            model.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
            model.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        }

        return model;
    }


    public static APIProductDTO fromAPIProducttoDTO(APIProduct model) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

        APIProductDTO dto = new APIProductDTO();
        dto.setName(model.getId().getApiProductName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUUID());
        dto.setDescription(model.getDescription());

        if (!StringUtils.isBlank(model.getThumbnailUrl())) {
            dto.setThumbnailUri(getThumbnailUri(model.getUUID()));
        }

        dto.setStatus(model.getStatus().getStatus());

        String subscriptionAvailability = model.getSubscriptionAvailability();
        if (subscriptionAvailability != null) {
            dto.setSubscriptionAvailability(mapSubscriptionAvailabilityFromAPIProductToDTO(subscriptionAvailability));
        }

        if (model.getSubscriptionAvailableTenants() != null) {
            dto.setSubscriptionAvailableTenants(Arrays.asList(model.getSubscriptionAvailableTenants().split(",")));
        }

        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        dto.setThrottlingTiers(tiersToReturn);

        dto.setVisibility(mapVisibilityFromAPIProductToDTO(model.getVisibility()));

        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }

        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);

        return dto;
    }

    private static APIProductStatus mapStatusFromDTOToAPIProduct(String status) {
        // switch case statements are not working as APIStatus.<STATUS>.toString() or APIStatus.<STATUS>.getStatus()
        //  is not a constant
        if (status.equals(APIProductStatus.BLOCKED.toString())) {
            return APIProductStatus.BLOCKED;
        } else if (status.equals(APIProductStatus.CREATED.toString())) {
            return APIProductStatus.CREATED;
        } else if (status.equals(APIProductStatus.PUBLISHED.toString())) {
            return APIProductStatus.PUBLISHED;
        } else if (status.equals(APIProductStatus.DEPRECATED.toString())) {
            return APIProductStatus.DEPRECATED;
        } else {
            return null; // how to handle this?
        }
    }

    private static String mapSubscriptionAvailabilityFromDTOtoAPIProduct(
            APIProductDTO.SubscriptionAvailabilityEnum subscriptionAvailability) {
        switch (subscriptionAvailability) {
            case current_tenant:
                return APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT;
            case all_tenants:
                return APIConstants.SUBSCRIPTION_TO_ALL_TENANTS;
            case specific_tenants:
                return APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS;
            default:
                return null; // how to handle this? 500 or 400
        }

    }

    private static APIProductDTO.SubscriptionAvailabilityEnum mapSubscriptionAvailabilityFromAPIProductToDTO(
            String subscriptionAvailability) {

        switch (subscriptionAvailability) {
            case APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT :
                return APIProductDTO.SubscriptionAvailabilityEnum.current_tenant;
            case APIConstants.SUBSCRIPTION_TO_ALL_TENANTS :
                return APIProductDTO.SubscriptionAvailabilityEnum.all_tenants;
            case APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS :
                return APIProductDTO.SubscriptionAvailabilityEnum.specific_tenants;
            default:
                return null; // how to handle this?
        }

    }

    private static String mapVisibilityFromDTOtoAPIProduct(APIProductDTO.VisibilityEnum visibility) {
        switch (visibility) {
            case PUBLIC:
                return APIConstants.API_GLOBAL_VISIBILITY;
            case PRIVATE:
                return APIConstants.API_PRIVATE_VISIBILITY;
            case RESTRICTED:
                return APIConstants.API_RESTRICTED_VISIBILITY;
            case CONTROLLED:
                return APIConstants.API_CONTROLLED_VISIBILITY;
            default:
                return null; // how to handle this?
        }
    }

    private static APIProductDTO.VisibilityEnum mapVisibilityFromAPIProductToDTO(String visibility) {
        switch (visibility) { //public, private,controlled, restricted
            case APIConstants.API_GLOBAL_VISIBILITY :
                return APIProductDTO.VisibilityEnum.PUBLIC;
            case APIConstants.API_PRIVATE_VISIBILITY :
                return APIProductDTO.VisibilityEnum.PRIVATE;
            case APIConstants.API_RESTRICTED_VISIBILITY :
                return APIProductDTO.VisibilityEnum.RESTRICTED;
            case APIConstants.API_CONTROLLED_VISIBILITY :
                return APIProductDTO.VisibilityEnum.CONTROLLED;
            default:
                return null; // how to handle this?
        }
    }

    private static String getThumbnailUri (String uuid) {
        return RestApiConstants.RESOURCE_PATH_PRODUCT_THUMBNAIL.replace(RestApiConstants.PRODUCTID_PARAM, uuid);
    }
}
