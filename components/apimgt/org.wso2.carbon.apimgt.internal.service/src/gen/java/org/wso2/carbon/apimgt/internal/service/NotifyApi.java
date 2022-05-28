package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.NotifyApiService;
import org.wso2.carbon.apimgt.internal.service.impl.NotifyApiServiceImpl;
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
@Path("/notify")

@Api(description = "the notify API")

@Produces({ "application/json" })


public class NotifyApi  {

  @Context MessageContext securityContext;

NotifyApiService delegate = new NotifyApiServiceImpl();


    @POST
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Push notification events into nodes.", notes = "This pushes events to the other nodes. ", response = Object.class, tags={ "Notification" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Event Received success", response = Object.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response notifyPost(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("X-WSO2-KEY-MANAGER") String xWSO2KEYMANAGER, @ApiParam(value = "Notification event payload" ) String body) throws APIManagementException{
        return delegate.notifyPost(xWSO2KEYMANAGER, body, securityContext);
    }
}
