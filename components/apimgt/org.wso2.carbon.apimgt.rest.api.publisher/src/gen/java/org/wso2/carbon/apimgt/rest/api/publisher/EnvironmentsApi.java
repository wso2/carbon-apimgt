package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.EnvironmentsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/environments")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/environments", description = "the environments API")
public class EnvironmentsApi  {

   private final EnvironmentsApiService delegate = EnvironmentsApiServiceFactory.getEnvironmentsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all gateway environments", notes = "This operation can be used to retrieve the list of gateway environments available.\n", response = EnvironmentListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nEnvironment list is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested API does not exist.\n") })

    public Response environmentsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. Using the **UUID** in the API call is recommended.\nThe combination of the provider of the API, name of the API and the version is also accepted as a valid API I.\nShould be formatted as **provider-name-version**.\n",required=true) @QueryParam("apiId") @Encoded String apiId)
    {
    return delegate.environmentsGet(apiId);
    }
}

