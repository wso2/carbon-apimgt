package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.containermgt.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.ServiceDiscoveryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import java.util.*;
import javax.ws.rs.core.Response;



public class ServiceDiscoveryApiServiceImpl implements ServiceDiscoveryApiService {

    private static final Log log = LogFactory.getLog(ServiceDiscoveryApiServiceImpl.class);

    ServiceDiscoveryEndpoints subEndpointObj = new ServiceDiscoveryEndpoints();

    /**
     * Query available services in the given type cluster
     * @param type service discovery system type
     * @return the serviceListDTO, list of services
     *
     */

    public Response serviceDiscoveryEndpointsTypeGet(String type, Integer limit, Integer offset,
                                                     MessageContext messageContext)  {
        ServiceDiscoveriesInfoListDTO serviceListDTO = new ServiceDiscoveriesInfoListDTO();

        //setting default type values if they are not set
        type = type != null ? type : ContainerBasedConstants.SYSTEM_TYPE;
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try{
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();

            subEndpointObj = apiProvider.getServiceDiscoveryEndpoints(type,username,offset,limit);
            int length = apiProvider.getNumberOfAllServices(type);
            ServiceDiscoveryMappingUtil.setPaginationParams(serviceListDTO,  offset, limit, length);
            ServiceDiscoveryMappingUtil.listToDTO(serviceListDTO,subEndpointObj);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.ok().entity(serviceListDTO).build();
    }

    /**
     * Query available types of service discovery system
     * @return the typeListDTO , list of types
     *
     */

    public Response serviceDiscoveryEndpointsGet(Integer limit, Integer offset, MessageContext messageContext)  {
        ServiceDiscoverySystemTypeListDTO typeListDTO = new ServiceDiscoverySystemTypeListDTO();
        APIProvider apiProvider = null;
        try {
            apiProvider = RestApiUtil.getLoggedInUserProvider();
        } catch (APIManagementException e) {
            String errorMessage = "Error occured while retriving the tenant details";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);

        }
        String username = RestApiUtil.getLoggedInUsername();
        List<String> types = new ArrayList<>();
        try {
            types = apiProvider.getServiceDiscoveryTypes(username);
        } catch (UserStoreException e) {
            String errorMessage = "Error occured while retriving the user name";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (RegistryException e) {
            String errorMessage = "Error occurred while retrieving the user details from registry";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error occured while parsing the value";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        ServiceDiscoveryMappingUtil.typeListToDTO(typeListDTO,types);

        return Response.ok().entity(typeListDTO).build();
    }
}
