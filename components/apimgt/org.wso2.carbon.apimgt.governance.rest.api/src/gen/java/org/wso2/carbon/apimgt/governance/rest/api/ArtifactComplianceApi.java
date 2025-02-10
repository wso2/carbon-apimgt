package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.ArtifactComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.ArtifactComplianceApiServiceImpl;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

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
@Path("/artifact-compliance")

@Api(description = "the artifact-compliance API")




public class ArtifactComplianceApi  {

  @Context MessageContext securityContext;

ArtifactComplianceApiService delegate = new ArtifactComplianceApiServiceImpl();


    @GET
    @Path("/api/{apiId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve compliance details for a specific API", notes = "Retrieve compliance details associated with a specific API using its unique UUID.", response = ArtifactComplianceDetailsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Artifact Compliance",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with compliance details for the specified artifact.", response = ArtifactComplianceDetailsDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getComplianceByAPIId(@ApiParam(value = "**UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIMGovernanceException{
        return delegate.getComplianceByAPIId(apiId, securityContext);
    }

    @GET
    @Path("/api")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves compliance of all API artifacts", notes = "Retrieves compliance of all API artifacts within the organization.", response = ArtifactComplianceListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Artifact Compliance",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with API compliance results.", response = ArtifactComplianceListDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getComplianceStatusListOfAPIs( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIMGovernanceException{
        return delegate.getComplianceStatusListOfAPIs(limit, offset, securityContext);
    }

    @GET
    @Path("/api/summary")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves the summary of compliance of all API artifacts", notes = "Retrieves the summary of compliance of all API artifacts within the organization.", response = ArtifactComplianceSummaryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Artifact Compliance",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with compliance summary.", response = ArtifactComplianceSummaryDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getComplianceSummaryForAPIs() throws APIMGovernanceException{
        return delegate.getComplianceSummaryForAPIs(securityContext);
    }

    @GET
    @Path("/api/{apiId}/ruleset-validation-results/{rulesetId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve ruleset validation results for a specific API", notes = "Retrieve ruleset validation results associated with a specific API using its unique UUID.", response = RulesetValidationResultDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Artifact Compliance" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with ruleset validation results for the specified API.", response = RulesetValidationResultDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getRulesetValidationResultsByAPIId(@ApiParam(value = "**UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "**UUID** of the Ruleset. ",required=true) @PathParam("rulesetId") String rulesetId) throws APIMGovernanceException{
        return delegate.getRulesetValidationResultsByAPIId(apiId, rulesetId, securityContext);
    }
}
