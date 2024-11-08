package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.RulesetsApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.RulesetsApiServiceImpl;
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
@Path("/rulesets")

@Api(description = "the rulesets API")




public class RulesetsApi  {

  @Context MessageContext securityContext;

RulesetsApiService delegate = new RulesetsApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new ruleset.", notes = "Creates a new ruleset in the user's organization.", response = RulesetDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_manage", description = "Manage governance rulesets")
        })
    }, tags={ "Rulesets", "External",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Ruleset created successfully.", response = RulesetDTO.class),
        @ApiResponse(code = 400, message = "Client error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response createRuleset(@ApiParam(value = "JSON object containing the details of the new ruleset." ,required=true) RulesetDTO rulesetDTO) throws GovernanceException{
        return delegate.createRuleset(rulesetDTO, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves a list of rulesets.", notes = "Returns a list of all rulesets associated with the requested organization.", response = RulesetListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_read", description = "Read governance rulesets")
        })
    }, tags={ "Rulesets", "External" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with a list of rulesets.", response = RulesetListDTO.class),
        @ApiResponse(code = 400, message = "Client error.", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getRulesets() throws GovernanceException{
        return delegate.getRulesets(securityContext);
    }
}
