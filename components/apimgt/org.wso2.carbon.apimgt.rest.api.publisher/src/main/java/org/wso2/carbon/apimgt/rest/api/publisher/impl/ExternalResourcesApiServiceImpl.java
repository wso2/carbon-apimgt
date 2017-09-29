package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.ServiceDiscoverer;
import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.impl.KubernetesServiceDiscoverer;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;

public class ExternalResourcesApiServiceImpl extends ExternalResourcesApiService {
    private static final Logger log = LoggerFactory.getLogger(ExternalResourcesApiServiceImpl.class);

    /**
     * Retrieve all service endpoints via service discovery
     *
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header
     * @param request         msf4j request object
     * @return A list of service endpoints avaliable in the cluster
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response externalResourcesServicesGet(String ifNoneMatch, String ifModifiedSince,
                                                 Request request) throws NotFoundException {
        try{
            EndPointListDTO endPointListDTO = new EndPointListDTO();
            ServiceDiscoverer serviceDiscoverer = KubernetesServiceDiscoverer.getInstance();
            if (serviceDiscoverer.isEnabled()) {
                String namespaceFilter = serviceDiscoverer.getNamespaceFilter();
                HashMap<String, String> criteriaFilter = serviceDiscoverer.getCriteriaFilter();
                List<Endpoint> discoveredEndpointList;
                if ( namespaceFilter ==null && criteriaFilter == null) {
                    discoveredEndpointList = serviceDiscoverer.listServices();
                } else if ( namespaceFilter !=null && criteriaFilter != null) {
                    discoveredEndpointList = serviceDiscoverer.listServices(namespaceFilter, criteriaFilter);
                } else if ( namespaceFilter != null ) {
                    discoveredEndpointList = serviceDiscoverer.listServices(namespaceFilter);
                } else {
                    discoveredEndpointList = serviceDiscoverer.listServices(criteriaFilter);
                }
                for (Endpoint endpoint : discoveredEndpointList) {
                    endPointListDTO.addListItem(MappingUtil.toEndPointDTO(endpoint));
                }
            }
            endPointListDTO.setCount(endPointListDTO.getList().size());
            return Response.ok().entity(endPointListDTO).build();
        } catch (IOException e) {
            String errorMessage = "Error while Converting Endpoint Security Details in Endpoint";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
            log.error(errorMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
        } catch (ServiceDiscoveryException e) {
            String errorMessage = "Error while Discovering Service Endpoints";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
