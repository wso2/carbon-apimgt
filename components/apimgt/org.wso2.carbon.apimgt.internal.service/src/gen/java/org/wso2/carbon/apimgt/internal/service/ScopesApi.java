package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ScopesListDTO;
import org.wso2.carbon.apimgt.internal.service.ScopesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ScopesApiServiceImpl;
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
@Path("/scopes")

@Api(description = "the scopes API")

@Produces({ "application/json" })


public class ScopesApi  {

  @Context MessageContext securityContext;

ScopesApiService delegate = new ScopesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all scopes.", notes = "This will provide access to the Scopes created in database. ", response = ScopesListDTO.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of Scopes in Database.", response = ScopesListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response scopesGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  Scope Key ")  @QueryParam("scopeKey") String scopeKey) throws APIManagementException{
        return delegate.scopesGet(xWSO2Tenant, scopeKey, securityContext);
    }
}
