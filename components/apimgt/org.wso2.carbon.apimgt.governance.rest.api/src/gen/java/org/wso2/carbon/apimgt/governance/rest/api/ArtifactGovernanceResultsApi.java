package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultsSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.ArtifactGovernanceResultsApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.ArtifactGovernanceResultsApiServiceImpl;
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
@Path("/artifact-governance-results")

@Api(description = "the artifact-governance-results API")




public class ArtifactGovernanceResultsApi  {

  @Context MessageContext securityContext;

ArtifactGovernanceResultsApiService delegate = new ArtifactGovernanceResultsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves governance results of all artifacts", notes = "Retrieves governance results of all artifacts in the organization.", response = ArtifactGovernanceResultListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Governance Results",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with governance results.", response = ArtifactGovernanceResultListDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getArtifactGovernanceResults( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws GovernanceException{
        return delegate.getArtifactGovernanceResults(limit, offset, securityContext);
    }

    @GET
    @Path("/summary")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves the summary of governance results of all artifacts", notes = "Retrieves the summary of governance results of all artifacts in the organization.", response = ArtifactGovernanceResultsSummaryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Governance Results",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with governance results summary.", response = ArtifactGovernanceResultsSummaryDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getArtifactGovernanceResultsSummary() throws GovernanceException{
        return delegate.getArtifactGovernanceResultsSummary(securityContext);
    }

    @GET
    @Path("/{artifactId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve governance results for a specific artifact", notes = "Retrieve governance results associated with a specific artifact including APIs within the organization using its unique UUID.", response = ArtifactGovernanceResultDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Governance Results" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with governance results for the specified artifact.", response = ArtifactGovernanceResultDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getGovernanceResultsByArtifactId(@ApiParam(value = "**UUID** of the Artifact. ",required=true) @PathParam("artifactId") String artifactId) throws GovernanceException{
        return delegate.getGovernanceResultsByArtifactId(artifactId, securityContext);
    }
}
