package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.ResourceEndpoint;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourceEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourceEndpointListDTO;

import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping APIM core resource endpoints related objects into REST API resource endpoints
 * related DTOs.
 */
public class ResourceEndpointMappingUtil {

    public static ResourceEndpoint fromDTOtoResourceEndpoint(ResourceEndpointDTO endpointDTO) {
        ResourceEndpoint resourceEndpoint = new ResourceEndpoint();
        resourceEndpoint.setId(endpointDTO.getId());
        resourceEndpoint
                .setEndpointType(ResourceEndpoint.EndpointType.valueOf(endpointDTO.getEndpointType().toString()));
        resourceEndpoint.setName(endpointDTO.getName());
        resourceEndpoint.setUrl(endpointDTO.getUrl());
        resourceEndpoint.setSecurityConfig(endpointDTO.getSecurityConfig());
        resourceEndpoint.setGeneralConfig(endpointDTO.getGeneralConfig());
        return resourceEndpoint;
    }

    public static ResourceEndpointDTO fromResourceEndpointToDTO(ResourceEndpoint endpoint) {
        ResourceEndpointDTO endpointDTO = new ResourceEndpointDTO();
        endpointDTO.setId(endpoint.getId());
        endpointDTO
                .setEndpointType(ResourceEndpointDTO.EndpointTypeEnum.valueOf(endpoint.getEndpointType().toString()));
        endpointDTO.setName(endpoint.getName());
        endpointDTO.setUrl(endpoint.getUrl());
        endpointDTO.setSecurityConfig(endpoint.getSecurityConfig());
        endpointDTO.setGeneralConfig(endpoint.getGeneralConfig());
        return endpointDTO;
    }

    public static ResourceEndpointListDTO fromResourceEndpointListToDTO(List<ResourceEndpoint> endpointList, int offset,
            int limit) {
        ResourceEndpointListDTO endpointListDTO = new ResourceEndpointListDTO();
        List<ResourceEndpointDTO> resourceEndpointDTOS = endpointListDTO.getList();

        if (endpointList != null || !endpointList.isEmpty()) {
            int size = endpointList.size();
            int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
            int end = Math.min(offset + limit - 1, size - 1);
            for (int i = start; i <= end; i++) {
                resourceEndpointDTOS.add(fromResourceEndpointToDTO(endpointList.get(i)));
            }
        }
        endpointListDTO.setCount(resourceEndpointDTOS.size());

        return endpointListDTO;
    }

    /**
     * Sets pagination urls for a ResourceEndpointListDTO given pagination parameters and url parameters.
     *
     * @param endpointListDTO   a ResourceEndpointListDTO object
     * @param limit             max number of objects returned
     * @param offset            starting index
     * @param size              max offset
     */

    public static void setPaginationParams(ResourceEndpointListDTO endpointListDTO, String apiId, int limit, int offset,
            int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getResourceEndpointsPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), apiId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getResourceEndpointsPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), apiId);
        }
        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        endpointListDTO.setPagination(paginationDTO);
    }
}
