package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.HoneyPotApiService;
import org.wso2.carbon.apimgt.rest.api.admin.factories.HoneyPotApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.EmailDTO;

import java.sql.SQLException;
import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/honeyPot")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/honeyPot", description = "the honeyPot API")
public class HoneyPotApi  {

   private final HoneyPotApiService delegate = HoneyPotApiServiceFactory.getHoneyPotApi();

    @POST
    @Path("/addEmail")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Add an Email", notes = "Here we can use this to configure email\n", response = EmailDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nEmail List updated.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nThe resource to be updated does not exist.\n") })

    public Response honeyPotAddEmailPost(@ApiParam(value = "A email\n" ,required=true ) EmailDTO body) throws APIManagementException, SQLException {
    return delegate.honeyPotAddEmailPost(body);
    }
    @DELETE
    @Path("/deleteAlertData")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an alert data.", notes = "Get a alert record by the messageID and remove it from DB.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nResource successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met (Will be supported in future).\n") })

    public Response honeyPotDeleteAlertDataDelete(@ApiParam(value = "Pass the messageID to remove the record\n",required=true) @QueryParam("messageID")  String messageID) throws APIManagementException, SQLException {
    return delegate.honeyPotDeleteAlertDataDelete(messageID);
    }
    @DELETE
    @Path("/email")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete an configured email.", notes = "Delete an configured email from DB by pasing uuid.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nEmail successfully deleted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nResource to be deleted does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed.\nThe request has not been performed because one of the preconditions is not met.\n") })

    public Response honeyPotEmailDelete(@ApiParam(value = "Pass the uuid to remove the email\n",required=true) @QueryParam("uuid")  String uuid) throws APIManagementException, SQLException {
    return delegate.honeyPotEmailDelete(uuid);
    }
    @GET
    @Path("/getAlertData")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all Honeypot API alert data\n", notes = "Get all triggered Honeypot API alert data\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nExport Successful.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Data does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response honeyPotGetAlertDataGet() throws APIManagementException {
    return delegate.honeyPotGetAlertDataGet();
    }
    @GET
    @Path("/getEmailList")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all configured email list\n", notes = "Get all email list which configured to trigger for Honeypot api email alert\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nExport Successful.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.messageID\nInvalid request or validation error\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nRequested Data does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.\nThe requested media type is not supported\n") })

    public Response honeyPotGetEmailListGet(@ApiParam(value = "Pass the tenantDomain to get the email list and if not passed it will get from the logged user.\n") @QueryParam("tenantDomain")  String tenantDomain) throws APIManagementException {
    return delegate.honeyPotGetEmailListGet(tenantDomain);
    }
}

