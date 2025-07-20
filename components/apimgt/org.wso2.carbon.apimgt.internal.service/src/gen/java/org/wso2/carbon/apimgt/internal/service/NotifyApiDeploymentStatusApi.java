package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentStatusAcknowledgmentDTO;
import org.wso2.carbon.apimgt.internal.service.dto.InlineResponse200DTO;
import org.wso2.carbon.apimgt.internal.service.NotifyApiDeploymentStatusApiService;
import org.wso2.carbon.apimgt.internal.service.impl.NotifyApiDeploymentStatusApiServiceImpl;
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
@Path("/notify-api-deployment-status")

@Api(description = "the notify-api-deployment-status API")




public class NotifyApiDeploymentStatusApi  {

  @Context MessageContext securityContext;

NotifyApiDeploymentStatusApiService delegate = new NotifyApiDeploymentStatusApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Acknowledge an API Revision Deployment", notes = "This endpoint is invoked by the Gateway to notify the Control Plane about the status of an API revision deployment or undeployment. ", response = InlineResponse200DTO.class, tags={ "Gateway Monitoring" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Deployment acknowledgement received successfully.", response = InlineResponse200DTO.class),
        @ApiResponse(code = 400, message = "Malformed deployment acknowledgment payload.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while processing deployment status.", response = ErrorDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error.", response = ErrorDTO.class) })
    public Response notifyApiDeploymentStatusPost(@ApiParam(value = "" ,required=true) GatewayDeploymentStatusAcknowledgmentDTO gatewayDeploymentStatusAcknowledgmentDTO) throws APIManagementException{
        return delegate.notifyApiDeploymentStatusPost(gatewayDeploymentStatusAcknowledgmentDTO, securityContext);
    }
}
