package org.wso2.carbon.apimgt.rest.api.gateway.v1;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.LocalEntryDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.LocalEntryApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.impl.LocalEntryApiServiceImpl;
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
@Path("/local-entry")

@Api(description = "the local-entry API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class LocalEntryApi  {

  @Context MessageContext securityContext;

LocalEntryApiService delegate = new LocalEntryApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Local Entry from the storage", notes = "This operation is used to get local entry from the storage.If the Tenant domain is not provided carbon.super will be picked as the Tenant domain. ", response = LocalEntryDTO.class, tags={ "Get API Artifacts" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Local entry for the API successfully retrieved from the storage. ", response = LocalEntryDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response localEntryGet( @NotNull @ApiParam(value = "Name of the API ",required=true)  @QueryParam("apiName") String apiName,  @NotNull @ApiParam(value = "version of the API ",required=true)  @QueryParam("version") String version,  @ApiParam(value = "Tenant Domain of the API ")  @QueryParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.localEntryGet(apiName, version, tenantDomain, securityContext);
    }
}
