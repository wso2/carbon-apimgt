package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.AssessAPIComplianceRequestDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.AssessComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.AssessComplianceApiServiceImpl;
import org.wso2.carbon.apimgt.governance.api.error.GovernanceException;

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
@Path("/assess-compliance")

@Api(description = "the assess-compliance API")




public class AssessComplianceApi  {

  @Context MessageContext securityContext;

AssessComplianceApiService delegate = new AssessComplianceApiServiceImpl();


    @POST
    @Path("/api/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Assesses governance policy compliance for a specific api.", notes = "Initiates a compliance assessment for the specified api within the user's organization.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_compliance_manage", description = "Manage governance compliance tasks")
        })
    }, tags={ "Compliance Assessment", "Internal",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 202, message = "Accepted. Assessment initiated successfully.", response = Void.class),
        @ApiResponse(code = 400, message = "Client error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response assessAPICompliance(@ApiParam(value = "UUID of the api.",required=true) @PathParam("apiId") String apiId,  @NotNull @ApiParam(value = "Name of the organization.",required=true)  @QueryParam("organization") String organization, @ApiParam(value = "Labels associated with the api." ,required=true) AssessAPIComplianceRequestDTO assessAPIComplianceRequestDTO) throws GovernanceException{
        return delegate.assessAPICompliance(apiId, organization, assessAPIComplianceRequestDTO, securityContext);
    }

    @DELETE
    @Path("/api/{apiId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a specific api.", notes = "Delete a specified api within the user's organization.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_compliance_manage", description = "Manage governance compliance tasks")
        })
    }, tags={ "Compliance Assessment", "Internal" })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "OK. API deleted successfully.", response = Void.class),
        @ApiResponse(code = 400, message = "Client error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteAPI(@ApiParam(value = "UUID of the api.",required=true) @PathParam("apiId") String apiId,  @NotNull @ApiParam(value = "Name of the organization.",required=true)  @QueryParam("organization") String organization) throws GovernanceException{
        return delegate.deleteAPI(apiId, organization, securityContext);
    }
}
