package org.wso2.carbon.apimgt.rest.api.core;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ResourcesListDTO;
import org.wso2.carbon.apimgt.rest.api.core.factories.ResourcesApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.ResourcesApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/core/v1.[\\d]+/resources")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/resources")
@io.swagger.annotations.Api(description = "the resources API")
public class ResourcesApi implements Microservice  {
   private final ResourcesApiService delegate = ResourcesApiServiceFactory.getResourcesApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Return the list of resources of an API ", notes = "Return the list of subscriptions of an API, by proving API context and version. Response consist of API Context, API version, Consumer Key and Subscription Policy ", response = ResourcesListDTO.class, tags={ "Subscriptions of API", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. The list of Subscriptions. ", response = ResourcesListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ResourcesListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ResourcesListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ResourcesListDTO.class) })
    public Response resourcesGet(@ApiParam(value = "Context of the API. ") @QueryParam("apiContext") String apiContext
,@ApiParam(value = "Version of the API. ") @QueryParam("apiVersion") String apiVersion
,@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
, @Context Request request)
    throws NotFoundException {
        return delegate.resourcesGet(apiContext,apiVersion,accept, request);
    }
}
