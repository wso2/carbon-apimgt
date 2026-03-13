package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.BatchDeploymentsRequestDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentsResponseDTO;
import org.wso2.carbon.apimgt.internal.service.DeploymentsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.DeploymentsApiServiceImpl;
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
@Path("/deployments")

@Api(description = "the deployments API")




public class DeploymentsApi  {

  @Context MessageContext securityContext;

DeploymentsApiService delegate = new DeploymentsApiServiceImpl();


    @POST
    @Path("/batch")
    @Consumes({ "application/json" })
    @Produces({ "application/x-tar+gzip", "application/json" })
    @ApiOperation(value = "Batch fetch deployment content as TAR.GZ", notes = "Returns deployment content (platform api.yaml) for the given deployment IDs as a single application/x-tar+gzip archive. Used by the API Platform gateway for startup sync. Requires api-key header. ", response = File.class, tags={ "Gateway Internal APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "TAR.GZ archive of deployment content.", response = File.class),
        @ApiResponse(code = 401, message = "Missing or invalid api-key.", response = ErrorDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response deploymentsBatchPost(@ApiParam(value = "" ,required=true) BatchDeploymentsRequestDTO batchDeploymentsRequest) throws APIManagementException{
        return delegate.deploymentsBatchPost(batchDeploymentsRequest, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List deployments for platform gateway", notes = "Returns the list of deployments (API revisions) for the authenticated platform gateway. Used by the API Platform gateway at startup for deployment sync. Requires api-key header. Optional query 'since' (ISO8601) returns only deployments updated after that time. ", response = GatewayDeploymentsResponseDTO.class, tags={ "Gateway Internal APIs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of deployments for this gateway.", response = GatewayDeploymentsResponseDTO.class),
        @ApiResponse(code = 401, message = "Missing or invalid api-key.", response = ErrorDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response deploymentsGet( @ApiParam(value = "Return only deployments updated at or after this timestamp (ISO8601 string).")  @QueryParam("since") String since) throws APIManagementException{
        return delegate.deploymentsGet(since, securityContext);
    }
}
