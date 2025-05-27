package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetValidationResultDTO;
import org.wso2.carbon.apimgt.governance.rest.api.PolicyEvaluationApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.PolicyEvaluationApiServiceImpl;
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
@Path("/policy-evaluation")

@Api(description = "the policy-evaluation API")




public class PolicyEvaluationApi  {

  @Context MessageContext securityContext;

PolicyEvaluationApiService delegate = new PolicyEvaluationApiServiceImpl();


    @POST
    @Path("/api")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve rule violation results for a specific API", notes = "Retrieves the rule violation results after checking if the API complies with governance rulesets.", response = RulesetValidationResultDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_result_read", description = "Read governance results")
        })
    }, tags={ "Artifact Compliance" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with governance rule violation results for specified API.", response = RulesetValidationResultDTO.class),
        @ApiResponse(code = 400, message = "Bad request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getPolicyEvaluationByAPI(@Multipart(value = "artifactType", required = false)  String artifactType, @Multipart(value = "ruleCategory", required = false)  String ruleCategory, @Multipart(value = "ruleType", required = false)  String ruleType,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "label", required = false)  String label) throws APIMGovernanceException{
        return delegate.getPolicyEvaluationByAPI(artifactType, ruleCategory, ruleType, fileInputStream, fileDetail, label, securityContext);
    }
}
