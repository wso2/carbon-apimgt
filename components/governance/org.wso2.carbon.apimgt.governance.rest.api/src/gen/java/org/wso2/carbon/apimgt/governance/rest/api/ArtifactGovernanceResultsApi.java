package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultsDTO;
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
    @Path("/{artifactId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve governance results for a specific artifact", notes = "Retrieve governance results associated with a specific artifact including APIs within the organization using its unique ID.", response = ArtifactGovernanceResultDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Governance Results",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with governance results for the specified artifact.", response = ArtifactGovernanceResultDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getGovernanceResultsByArtifactId(@ApiParam(value = "The unique identifier of the artifact/api.",required=true) @PathParam("artifactId") String artifactId) throws GovernanceException{
        return delegate.getGovernanceResultsByArtifactId(artifactId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves governance results of all artifacts", notes = "Retrieves governance results of all artifacts in the organization.", response = ArtifactGovernanceResultsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Governance Results" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with governance results.", response = ArtifactGovernanceResultsDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getGovernanceResultsForAllArtifacts() throws GovernanceException{
        return delegate.getGovernanceResultsForAllArtifacts(securityContext);
    }
}
