package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryEndpoints;
import org.wso2.carbon.apimgt.api.model.Services;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Convert list of services to th serviceListDTO
 * servicesList list of services in the cluster
 * Convert the list of types to the typeListDTO
 *
 *  */

public class ServiceDiscoveryMappingUtil {

    /**
     * This method  set   pagination parameters
     *
     * @param limit max number of services returned
     * @param offset starting index
     * @param size total number of services
     * @param serviceListDTO  DTO object for list of services
     */
public static void setPaginationParams(Object serviceListDTO, int offset, int limit, int size) {

    //acquiring pagination parameters and setting pagination urls
    Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
    String paginatedPrevious = "";
    String paginatedNext = "";

    if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
        paginatedPrevious = RestApiUtil
                .getServiceDiscoveryPaginatedServices(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                        paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
    }

    if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
        paginatedNext = RestApiUtil.getServiceDiscoveryPaginatedServices
                (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                        paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
    }

    PaginationDTO paginationDTO = CommonMappingUtil
            .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
    if (serviceListDTO instanceof ServiceDiscoveriesInfoListDTO) {
        ((ServiceDiscoveriesInfoListDTO)serviceListDTO).setPagination(paginationDTO);
    }
}

    /**
     * This method  map endpointObj to serviceListDTO
     *
     * @param serviceListDTO  DTO object for list of services
     * @param endpointObj , all services details as object
     */
    public static void listToDTO(ServiceDiscoveriesInfoListDTO serviceListDTO, ServiceDiscoveryEndpoints endpointObj) {
        List<Services> services;

        services = endpointObj.getServices();
        List<ServiceDiscoveriesInfoDTO> list =
                ServiceDiscoveryMappingUtil.fromServiceListToServiceDiscoveriesInfoDTOList(endpointObj);
        serviceListDTO.setList(list);
        serviceListDTO.setType(endpointObj.getType());
        serviceListDTO.setCount(services.size());
    }



    /**
     * This method  map single service details to  serviceDTO
     * fromServiceToServiceDTO
     *
     * @param servicesObj service object
     * @return ServiceDiscoveriesInfoDTO object
     */
    public static ServiceDiscoveriesInfoDTO fromServicesToServiceDiscoveriesInfoDTO(Services servicesObj){
        ServiceDiscoveriesInfoDTO serviceDTO = new ServiceDiscoveriesInfoDTO();
        serviceDTO.setServiceName(servicesObj.getServiceName());
        serviceDTO.setServiceURL(servicesObj.getServiceURL());
        serviceDTO.setProperties(servicesObj.getProperties());
        return serviceDTO;

    }

    /**
     * This method  map list of  service  to  serviceListDTO list
     * fromServiceListTOServicesListDTO
     *
     * @return list of ServiceDiscoveriesInfoDTO object
     */
    public static List<ServiceDiscoveriesInfoDTO> fromServiceListToServiceDiscoveriesInfoDTOList
    (ServiceDiscoveryEndpoints endpointsObj){
       List< ServiceDiscoveriesInfoDTO> serviceListDTO = new ArrayList<>();
        List<Services> services = endpointsObj.getServices();
        for(Services s : services){
            ServiceDiscoveriesInfoDTO serviceDTO = fromServicesToServiceDiscoveriesInfoDTO(s);
            serviceListDTO.add(serviceDTO);
        }
        return serviceListDTO;
    }


    /**
     * This method  map list of  types  to  ServiceDiscoverySystemTypeListDTO list
     *
     */
    public static void typeListToDTO(ServiceDiscoverySystemTypeListDTO typeListDTO, List<String> types) {

        List<TypeInfoDTO> list = ServiceDiscoveryMappingUtil.fromTypeListToServiceDiscoverySystemTypeDTOList(types);
        typeListDTO.setList(list);
        typeListDTO.setCount(list.size());
    }

    /**
     * This method  map  type  to  TypeInfoDTO
     * fromtype to typeDTO
     *
     */
    public static TypeInfoDTO fromTypesToTypeInfoDTO(String type){
        TypeInfoDTO typeDTO = new TypeInfoDTO();
        typeDTO.setName(type);
        return typeDTO;

    }

    /**
     * This method  map  list of type  to  TypeInfoDTO list
     * from type list to TypeInfoDTO list
     *
     */
    public static List<TypeInfoDTO> fromTypeListToServiceDiscoverySystemTypeDTOList(List<String> types){
        List<TypeInfoDTO> list = new ArrayList<>();
        for(String s : types){
            TypeInfoDTO typeDTO = fromTypesToTypeInfoDTO(s);
            list.add(typeDTO);
        }
        return list;
    }





}
