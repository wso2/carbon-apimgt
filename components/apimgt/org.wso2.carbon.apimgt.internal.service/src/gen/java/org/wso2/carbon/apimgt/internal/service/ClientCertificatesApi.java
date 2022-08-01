package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.ClientCertificatesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ClientCertificatesApiServiceImpl;
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
@Path("/client-certificates")

@Api(description = "the client-certificates API")

@Produces({ "application/json" })


public class ClientCertificatesApi  {

  @Context MessageContext securityContext;

ClientCertificatesApiService delegate = new ClientCertificatesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all the client certificates for given parameters.", notes = "This will provide access to ", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of client certificates", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response clientCertificatesGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   UUID ")  @QueryParam("uuid") String uuid,  @ApiParam(value = "**Search condition**.   Serial Number ")  @QueryParam("serialNumber") String serialNumber,  @ApiParam(value = "**Search condition**.   Application Id ")  @QueryParam("applicationId") Integer applicationId) throws APIManagementException{
        return delegate.clientCertificatesGet(xWSO2Tenant, uuid, serialNumber, applicationId, securityContext);
    }
}
