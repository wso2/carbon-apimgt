package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayPayloadDTO;
import org.wso2.carbon.apimgt.internal.service.dto.NotifyGatewayStatusResponseDTO;
import org.wso2.carbon.apimgt.internal.service.NotifyGatewayApiService;
import org.wso2.carbon.apimgt.internal.service.impl.NotifyGatewayApiServiceImpl;
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
@Path("/notify-gateway")

@Api(description = "the notify-gateway API")




public class NotifyGatewayApi  {

  @Context MessageContext securityContext;

NotifyGatewayApiService delegate = new NotifyGatewayApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Gateway Register or Heartbeat", notes = "Accepts a payload indicating either a gateway registration or a heartbeat update. The `type` field distinguishes the operation. ", response = NotifyGatewayStatusResponseDTO.class, tags={ "Gateway Lifecycle" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response for registration or heartbeat.", response = NotifyGatewayStatusResponseDTO.class),
        @ApiResponse(code = 400, message = "Invalid payload", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while processing gateway status.", response = ErrorDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response notifyGatewayPost(@ApiParam(value = "" ,required=true) NotifyGatewayPayloadDTO notifyGatewayPayloadDTO) throws APIManagementException{
        return delegate.notifyGatewayPost(notifyGatewayPayloadDTO, securityContext);
    }
}
