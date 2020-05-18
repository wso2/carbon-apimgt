package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MonetizationUsagePublishInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PublishStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.MonetizationApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.MonetizationApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

import io.swagger.annotations.*;
import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.*;
@Path("/monetization")

@Api(description = "the monetization API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class MonetizationApi  {

  @Context MessageContext securityContext;

MonetizationApiService delegate = new MonetizationApiServiceImpl();


    @POST
    @Path("/publish-usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Publish Usage Records", notes = "Publish Usage Records of Monetized APIs ", response = PublishStatusDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:monetization_usage_publish", description = "Retrieve and publish Monetization related usage records")
        })
    }, tags={ "Monetization (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Usage records successfully published.", response = PublishStatusDTO.class),
        @ApiResponse(code = 202, message = "Request is sucessfully accepted for processing.", response = PublishStatusDTO.class),
        @ApiResponse(code = 404, message = "Not Found. ", response = PublishStatusDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = PublishStatusDTO.class) })
    public Response monetizationPublishUsagePost() throws APIManagementException{
        return delegate.monetizationPublishUsagePost(securityContext);
    }

    @GET
    @Path("/publish-usage/status")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the status of Monetization usage publisher", notes = "Get the status of Monetization usage publisher ", response = MonetizationUsagePublishInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:monetization_usage_publish", description = "Retrieve and publish Monetization related usage records")
        })
    }, tags={ "Monetization (Collection)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Status returned ", response = MonetizationUsagePublishInfoDTO.class) })
    public Response monetizationPublishUsageStatusGet() throws APIManagementException{
        return delegate.monetizationPublishUsageStatusGet(securityContext);
    }
}
