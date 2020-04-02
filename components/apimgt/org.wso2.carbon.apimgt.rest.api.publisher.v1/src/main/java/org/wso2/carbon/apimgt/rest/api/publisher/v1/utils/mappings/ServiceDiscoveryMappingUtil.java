package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConf;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.containermgt.ServiceDiscoveryEndpoints;
import org.wso2.carbon.apimgt.impl.containermgt.Services;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

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





}
