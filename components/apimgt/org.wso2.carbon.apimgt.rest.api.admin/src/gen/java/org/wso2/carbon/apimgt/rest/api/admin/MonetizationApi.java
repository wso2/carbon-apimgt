package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.MonetizationApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.MonetizationApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.PublishStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.MonetizationUsagePublishInfoDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/monetization")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/monetization", description = "the monetization API")
public class MonetizationApi  {

   private final MonetizationApiService delegate = MonetizationApiServiceFactory.getMonetizationApi();

    @POST
    @Path("/publish-usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Publish Usage Records", notes = "Publish Usage Records of Monetized APIs\n", response = PublishStatusDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Usage records successfully published."),
        
        @io.swagger.annotations.ApiResponse(code = 202, message = "Request is sucessfully accepted for processing."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error") })

    public Response monetizationPublishUsagePost()
    {
    return delegate.monetizationPublishUsagePost();
    }
    @GET
    @Path("/publish-usage/status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get the status of Monetization usage publisher", notes = "Get the status of Monetization usage publisher\n", response = MonetizationUsagePublishInfoDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nStatus returned\n") })

    public Response monetizationPublishUsageStatusGet()
    {
    return delegate.monetizationPublishUsageStatusGet();
    }
}

