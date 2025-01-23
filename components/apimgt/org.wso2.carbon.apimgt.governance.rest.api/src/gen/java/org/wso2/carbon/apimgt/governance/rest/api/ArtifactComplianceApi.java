package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceDetailsDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.ArtifactComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.ArtifactComplianceApiServiceImpl;
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
@Path("/artifact-compliance")

@Api(description = "the artifact-compliance API")




public class ArtifactComplianceApi  {

  @Context MessageContext securityContext;

ArtifactComplianceApiService delegate = new ArtifactComplianceApiServiceImpl();


    @GET
    @Path("/{artifactId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve compliance details for a specific artifact", notes = "Retrieve compliance details associated with a specific artifact including APIs within the organization using its unique UUID.", response = ArtifactComplianceDetailsDTO.class, authorizations = {
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
    public Response getArtifactComplianceByArtifactId(@ApiParam(value = "**UUID** of the Artifact. ",required=true) @PathParam("artifactId") String artifactId) throws GovernanceException{
        return delegate.getArtifactComplianceByArtifactId(artifactId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves compliance of all artifacts of a certain type", notes = "Retrieves compliance of all artifacts of a certain type within the organization.", response = ArtifactComplianceListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Artifact Compliance",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with artifact compliance results.", response = ArtifactComplianceListDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getArtifactComplianceForAllArtifacts( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Type of the artifact. ", allowableValues="API", defaultValue="API") @DefaultValue("API") @QueryParam("artifactType") String artifactType) throws GovernanceException{
        return delegate.getArtifactComplianceForAllArtifacts(limit, offset, artifactType, securityContext);
    }

    @GET
    @Path("/summary")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves the summary of compliance of all artifacts of a certain type", notes = "Retrieves the summary of compliance of all artifacts of a certain type within the organization.", response = ArtifactComplianceSummaryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Artifact Compliance" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with compliance summary.", response = ArtifactComplianceSummaryDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getArtifactComplianceSummary( @ApiParam(value = "Type of the artifact. ", allowableValues="API", defaultValue="API") @DefaultValue("API") @QueryParam("artifactType") String artifactType) throws GovernanceException{
        return delegate.getArtifactComplianceSummary(artifactType, securityContext);
    }
}
