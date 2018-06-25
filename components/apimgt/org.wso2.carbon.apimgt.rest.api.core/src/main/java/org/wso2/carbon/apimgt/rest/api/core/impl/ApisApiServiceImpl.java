package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-26T10:56:28.057+05:30")
public class ApisApiServiceImpl extends ApisApiService {

    private APIMgtAdminService apiMgtAdminService;

    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImpl.class);

    public ApisApiServiceImpl(APIMgtAdminService apiMgtAdminService) {
        this.apiMgtAdminService = apiMgtAdminService;
    }

    /**
     * Retrieve API's gateway configuration
     *
     * @param apiId   UUID of the API
     * @param request msf4j request object
     * @return 200 OK if the opration was successful
     * @throws NotFoundException If failed to retrieve API gateway configuration
     */
    @Override
    public Response apisApiIdGatewayConfigGet(String apiId, String accept,
                                              Request request) throws NotFoundException {

        try {
            String apiGatewayConfig = null;
            apiGatewayConfig = apiMgtAdminService.getAPIGatewayServiceConfig(apiId);

            if (apiGatewayConfig != null) {
                return Response.ok().entity(apiGatewayConfig).build();
            } else {
                String msg = "API is not found with apiId : " + apiId;
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).header(HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving gateway config of API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).header(HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON).entity(errorDTO).build();
        }
    }

    /**
     * Retrieve a list of APIs with given gateway labels and status.
     *
     * @param labels Gateway labels
     * @param status Lifecycle status
     * @param request msf4j request object
     * @return 200 OK if the opration was successful
     * @throws NotFoundException If failed to retrieve APIs
     */
    @Override
    public Response apisGet(String labels, String status, Request request) throws NotFoundException {
        APIListDTO apiListDTO;
        try {
            if (labels != null && !labels.isEmpty()) {
                String[] gatewayLabels = labels.split(",");
                List<String> labelList = new ArrayList<String>(Arrays.asList(gatewayLabels));

                if (status != null && !status.isEmpty()) {
                    apiListDTO = MappingUtil.toAPIListDTO(apiMgtAdminService.getAPIsByStatus(labelList, status));
                    return Response.ok().entity(apiListDTO).build();
                } else {
                    apiListDTO = MappingUtil.toAPIListDTO(apiMgtAdminService.getAPIsByGatewayLabel(labelList));
                    return Response.ok().entity(apiListDTO).build();
                }
            } else {
                apiListDTO = new APIListDTO();
                return Response.ok().entity(apiListDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
