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
 *  servicesList list of services in the cluster
 *
 *  */

public class ServiceDiscoveryMappingUtil {


//    public static void ( ServiceDiscoveriesInfoListDTO serviceListDTO,ServiceDiscoveryEndpoints endpointObj) {
//        String serviceName;  String serviceURL ;
//        Map properties ;  String proDetails;
//
//         List<Services> services;
//
//        services = endpointObj.getServices();
//        List<ServiceDiscoveriesInfoDTO> list = ServiceDiscoveryMappingUtil.fromServiceListToServiceDiscoveriesInfoDTOList(endpointObj);
//        serviceListDTO.setList(list);
//        serviceListDTO.setType(endpointObj.getType());
//        serviceListDTO.setCount(services.size());
//
//
//
//
//    }
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
        paginatedNext = RestApiUtil.getServiceDiscoveryPaginatedServices(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                        paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
    }

    PaginationDTO paginationDTO = CommonMappingUtil
            .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
    if (serviceListDTO instanceof ServiceDiscoveriesInfoListDTO) {
        ((ServiceDiscoveriesInfoListDTO)serviceListDTO).setPagination(paginationDTO);
    }
//    else if (serviceListDTO instanceof APIListExpandedDTO) {
//        ((APIListExpandedDTO)serviceListDTO).setPagination(paginationDTO);
//    }
}
    public static void listToDTO(ServiceDiscoveriesInfoListDTO serviceListDTO, ServiceDiscoveryEndpoints endpointObj) {
        String serviceName;  String serviceURL ;
        Map properties ;  String proDetails;

        List<Services> services;

        services = endpointObj.getServices();
        List<ServiceDiscoveriesInfoDTO> list = ServiceDiscoveryMappingUtil.fromServiceListToServiceDiscoveriesInfoDTOList(endpointObj);
        serviceListDTO.setList(list);
        serviceListDTO.setType(endpointObj.getType());
        serviceListDTO.setCount(services.size());
    }


    //fromServiceToServiceDTO
    public static ServiceDiscoveriesInfoDTO fromServicesToServiceDiscoveriesInfoDTO(Services servicesObj){
        ServiceDiscoveriesInfoDTO serviceDTO = new ServiceDiscoveriesInfoDTO();
        serviceDTO.setServiceName(servicesObj.getServiceName());
        serviceDTO.setServiceURL(servicesObj.getServiceURL());
        serviceDTO.setProperties(servicesObj.getProperties());
        return serviceDTO;

    }
    //fromServiceListTOServicesListDTO
    public static List<ServiceDiscoveriesInfoDTO> fromServiceListToServiceDiscoveriesInfoDTOList(ServiceDiscoveryEndpoints endpointsObj){
       List< ServiceDiscoveriesInfoDTO> serviceListDTO = new ArrayList<>();
        List<Services> services = endpointsObj.getServices();
        List<ServiceDiscoveriesInfoDTO> list = new ArrayList<>();
        for(Services s : services){
            ServiceDiscoveriesInfoDTO serviceDTO = fromServicesToServiceDiscoveriesInfoDTO(s);
            list.add(serviceDTO);
        }
        //serviceListDTO.setList(list);
        return list;
    }


//    public static void setPaginationParamsToTypes(Object typeListDTO, int offset, int limit, int size) {
//
//        //acquiring pagination parameters and setting pagination urls
//        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
//        String paginatedPrevious = "";
//        String paginatedNext = "";
//
//        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
//            paginatedPrevious = RestApiUtil
//                    .getServiceDiscoveryPaginatedServices(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
//                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
//        }
//
//        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
//            paginatedNext = RestApiUtil.getServiceDiscoveryPaginatedServices(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
//                    paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
//        }
//
//        PaginationDTO paginationDTO = CommonMappingUtil
//                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
//        if (typeListDTO instanceof ServiceDiscoverySystemTypeListDTO) {
//            ((ServiceDiscoverySystemTypeListDTO)typeListDTO).setPagination(paginationDTO);
//        }
////    else if (serviceListDTO instanceof APIListExpandedDTO) {
////        ((APIListExpandedDTO)serviceListDTO).setPagination(paginationDTO);
////    }
//    }

    public static void typeListToDTO(ServiceDiscoverySystemTypeListDTO typeListDTO, List<String> types) {


        List<TypeInfoDTO> list = ServiceDiscoveryMappingUtil.fromTypeListToServiceDiscoverySystemTypeDTOList(types);
        typeListDTO.setList(list);
        typeListDTO.setCount(list.size());
    }

    //fromServiceToServiceDTO
    public static TypeInfoDTO fromTypesToTypeInfoDTO(String type){
//        ServiceDiscoveryEndpoints endpointsObj = new ServiceDiscoveryEndpoints();
        TypeInfoDTO typeDTO = new TypeInfoDTO();
        typeDTO.setName(type);
        return typeDTO;

    }
    //fromServiceListTOServicesListDTO
    public static List<TypeInfoDTO> fromTypeListToServiceDiscoverySystemTypeDTOList(List<String> types){
        List<TypeInfoDTO> list = new ArrayList<>();
        for(String s : types){
            TypeInfoDTO typeDTO = fromTypesToTypeInfoDTO(s);
            list.add(typeDTO);
        }
        //serviceListDTO.setList(list);
        return list;
    }





}
