package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EmailDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.BotDataApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.BotDataApiServiceImpl;
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
@Path("/botData")

@Api(description = "the botData API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class BotDataApi  {

  @Context MessageContext securityContext;

BotDataApiService delegate = new BotDataApiServiceImpl();


    @POST
    @Path("/addEmail")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an Email", notes = "Here we can use this to configure email ", response = EmailDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:bot_data", description = "Manage emails")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Email List updated. ", response = EmailDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class) })
    public Response botDataAddEmailPost(@ApiParam(value = "A email " ,required=true) EmailDTO body) throws APIManagementException{
        return delegate.botDataAddEmailPost(body, securityContext);
    }

    @DELETE
    @Path("/deleteEmail")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an configured email.", notes = "Delete an configured email from DB by pasing uuid. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:bot_data", description = "Manage emails")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Email successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response botDataDeleteEmailDelete( @NotNull @ApiParam(value = "Pass the uuid to remove the email ",required=true)  @QueryParam("uuid") String uuid) throws APIManagementException{
        return delegate.botDataDeleteEmailDelete(uuid, securityContext);
    }

    @GET
    @Path("/getEmailList")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all configured email list ", notes = "Get all email list which configured to trigger for BotData api email alert ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:bot_data", description = "Manage emails")
        })
    }, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Successful. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request.messageID Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Data does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response botDataGetEmailListGet( @ApiParam(value = "Pass the tenantDomain to get the email list and if not passed it will get from the logged user. ")  @QueryParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.botDataGetEmailListGet(tenantDomain, securityContext);
    }
}
