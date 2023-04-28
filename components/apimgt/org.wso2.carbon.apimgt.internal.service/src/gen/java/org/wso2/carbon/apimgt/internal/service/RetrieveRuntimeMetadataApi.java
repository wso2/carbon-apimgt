package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.RetrieveRuntimeMetadataApiService;
import org.wso2.carbon.apimgt.internal.service.impl.RetrieveRuntimeMetadataApiServiceImpl;
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
@Path("/retrieve-runtime-metadata")

@Api(description = "the retrieve-runtime-metadata API")

@Produces({ "application/json" })


public class RetrieveRuntimeMetadataApi  {

  @Context MessageContext securityContext;

RetrieveRuntimeMetadataApiService delegate = new RetrieveRuntimeMetadataApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Metadata information for API runtimes for given Data-Plane/s", notes = "This will provide access to the deployment metadata for a given data-plane ID in json format ", response = Void.class, tags={ "Retrieving Runtime artifacts for Data-Planes" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Json file of runtime metadata", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response retrieveRuntimeMetadataGet( @ApiParam(value = "**Search condition**.   Data-Plane ID/s    To provide multiple Data-Plane IDs provide a \"|\" seperated list of IDs ")  @QueryParam("dataPlaneId") String dataPlaneId,  @ApiParam(value = "**Search condition**.   Gateway Accessibility type denotes whether the gateway environment is internal or external ")  @QueryParam("gatewayAccessibilityType") String gatewayAccessibilityType) throws APIManagementException{
        return delegate.retrieveRuntimeMetadataGet(dataPlaneId, gatewayAccessibilityType, securityContext);
    }
}
