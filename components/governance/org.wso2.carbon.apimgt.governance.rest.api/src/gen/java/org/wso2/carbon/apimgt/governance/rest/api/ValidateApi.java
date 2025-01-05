package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernanceValidationRequestDTO;
import org.wso2.carbon.apimgt.governance.rest.api.ValidateApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.ValidateApiServiceImpl;
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
@Path("/validate")

@Api(description = "the validate API")




public class ValidateApi  {

  @Context MessageContext securityContext;

ValidateApiService delegate = new ValidateApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Validate governance compliance", notes = "Validates the compliance of an artifact with the governance policies.", response = ArtifactGovernanceResultDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_validation", description = "Validate governance policies")
        })
    }, tags={ "Governance Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Governance check successful.", response = ArtifactGovernanceResultDTO.class),
        @ApiResponse(code = 204, message = "No Content. No governance check triggered in the background.", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response validateGovernanceCompliance(@ApiParam(value = "JSON object containing the details of the artifact to be checked." ,required=true) GovernanceValidationRequestDTO governanceValidationRequestDTO) throws GovernanceException{
        return delegate.validateGovernanceCompliance(governanceValidationRequestDTO, securityContext);
    }
}
