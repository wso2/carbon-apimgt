package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ExternalResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

import java.util.List;
import javax.ws.rs.core.Response;

public class ExternalResourcesApiServiceImpl extends ExternalResourcesApiService {
    private static final Logger log = LoggerFactory.getLogger(ExternalResourcesApiServiceImpl.class);

    /**
     * Retrieve all service endpoints after service discovery
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
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            List<Endpoint> endpointList = apiPublisher.discoverServiceEndpoints();
            EndPointListDTO endPointListDTO = new EndPointListDTO();
            for (Endpoint endpoint : endpointList) {
                endPointListDTO.addListItem(MappingUtil.toEndPointDTO(endpoint));
            }
            endPointListDTO.setCount(endPointListDTO.getList().size());
            return Response.ok().entity(endPointListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while discovering service endpoints";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
