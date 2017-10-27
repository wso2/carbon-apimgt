package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.impl.ServiceDiscoverer;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.ServiceDiscoveryImplConfig;
import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.impl.ServiceDiscoveryConfigBuilder;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ExternalResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointListDTO;

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
     * Retrieve all service endpoints by iterating through all service discovery impl classes
     *
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header
     * @param request         msf4j request object
     * @return A list of service endpoints available in the cluster(s)
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response externalResourcesServicesGet(String ifNoneMatch, String ifModifiedSince,
                                                 Request request) throws NotFoundException {
        try{
            ServiceDiscoveryConfigurations serviceDiscoveryConfig = ServiceDiscoveryConfigBuilder
                    .getServiceDiscoveryConfiguration();

            // If service discovery not enabled - do not proceed
            if (!serviceDiscoveryConfig.isServiceDiscoveryEnabled()) {
                String errorMessage = "Service discovery is not enabled";
                return buildInternalServerErrorResponse(errorMessage, null);
            }

            EndPointListDTO endPointListDTO = new EndPointListDTO();

            List<ServiceDiscoveryImplConfig> implConfigsList = serviceDiscoveryConfig.getImplementationsList();
            for (ServiceDiscoveryImplConfig implConfig : implConfigsList) {
                //Every implConfig has two elements. The implClass and the implParameters.

                //Get the implClass instance
                String implClassName = implConfig.getImplClass();
                Class implClazz = Class.forName(implClassName);

                //Pass the implParameters to the above instance
                ServiceDiscoverer serviceDiscoverer = (ServiceDiscoverer) implClazz.newInstance();
                serviceDiscoverer.init(implConfig.getImplParameters());

                //The .init() method above sets the filtering parameters to the serviceDiscoverer instance, if provided.
                //Let's check whether those filtering parameters : "namespace" and/or "criteria" are set,
                //and call ServiceDiscoverer#listServices method accordingly
                String namespaceFilter = serviceDiscoverer.getNamespaceFilter();
                HashMap<String, String> criteriaFilter = serviceDiscoverer.getCriteriaFilter();
                List<Endpoint> discoveredEndpointList;

                //both not set
                if (namespaceFilter == null && criteriaFilter == null) {
                    discoveredEndpointList = serviceDiscoverer.listServices();

                //both set
                } else if (namespaceFilter != null && criteriaFilter != null) {
                    discoveredEndpointList = serviceDiscoverer.listServices(namespaceFilter, criteriaFilter);

                //only "namespace" is set
                } else if (namespaceFilter != null) {
                    discoveredEndpointList = serviceDiscoverer.listServices(namespaceFilter);

                //remaining -> only "criteria" is set
                } else {
                    discoveredEndpointList = serviceDiscoverer.listServices(criteriaFilter);
                }

                // Notice, the discoveredEndpointList is the list returned by ServiceDiscoverer#listServices method
                // Hence before moving on to the next implClass, let's add those Endpoints to endPointListDTO
                for (Endpoint endpoint : discoveredEndpointList) {
                    endPointListDTO.addListItem(MappingUtil.toEndPointDTO(endpoint));
                }
            }

            //Count is set after adding endpoints returned by all the implClasses
            endPointListDTO.setCount(endPointListDTO.getList().size());
            return Response.ok().entity(endPointListDTO).build();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            String errorMessage = "Error while loading service discovery impl class";
            return buildInternalServerErrorResponse(errorMessage, e);
        } catch (IOException e) {
            String errorMessage = "Error while Converting Endpoint Security Details in Endpoint";
            return buildInternalServerErrorResponse(errorMessage, e);
        } catch (ServiceDiscoveryException e) {
            String errorMessage = "Error while Discovering Service Endpoints";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    private Response buildInternalServerErrorResponse(String errorMessage, Exception e){
        ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorMessage, 900313L, errorMessage);
        if ( e == null ) {
            log.error(errorMessage);
        } else {
            log.error(errorMessage, e);
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorDTO).build();
    }
}
