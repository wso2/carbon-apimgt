package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.EndpointCertificatesApiServiceImpl;
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
@Path("/endpoint-certificates")

@Api(description = "the endpoint-certificates API")

@Produces({ "application/json" })


public class EndpointCertificatesApi  {

  @Context MessageContext securityContext;

EndpointCertificatesApiService delegate = new EndpointCertificatesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all the endpoint certificates for given alias.", notes = "This will provide access to ", response = Void.class, tags={ "Retrieving Runtime artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "List of endpoint certificates", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response endpointCertificatesGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   Alias ")  @QueryParam("alias") String alias) throws APIManagementException{
        return delegate.endpointCertificatesGet(xWSO2Tenant, alias, securityContext);
    }
}
