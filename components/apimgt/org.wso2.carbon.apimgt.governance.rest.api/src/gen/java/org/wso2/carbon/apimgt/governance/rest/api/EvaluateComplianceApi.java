package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.governance.rest.api.EvaluateComplianceApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.EvaluateComplianceApiServiceImpl;
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
@Path("/evaluate-compliance")

@Api(description = "the evaluate-compliance API")




public class EvaluateComplianceApi  {

  @Context MessageContext securityContext;

EvaluateComplianceApiService delegate = new EvaluateComplianceApiServiceImpl();


    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Evaluate governance compliance", notes = "Evaluates the compliance of an artifact with the governance policies of the organization.", response = ArtifactGovernanceResultDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_evaluate_compliance", description = "Evaluate governance compliance")
        })
    }, tags={ "Governance Evaluation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Governance check successful.", response = ArtifactGovernanceResultDTO.class),
        @ApiResponse(code = 204, message = "No Content. No governance check triggered in the background.", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response evaluateCompliance(@Multipart(value = "artifactId")  String artifactId, @Multipart(value = "artifactType")  String artifactType, @Multipart(value = "governableState")  String governableState,  @Multipart(value = "targetFile", required = false) InputStream targetFileInputStream, @Multipart(value = "targetFile" , required = false) Attachment targetFileDetail) throws GovernanceException{
        return delegate.evaluateCompliance(artifactId, artifactType, governableState, targetFileInputStream, targetFileDetail, securityContext);
    }
}
