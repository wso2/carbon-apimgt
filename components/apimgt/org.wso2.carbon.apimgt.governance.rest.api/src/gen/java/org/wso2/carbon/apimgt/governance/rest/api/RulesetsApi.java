package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RulesetListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.RulesetsApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.RulesetsApiServiceImpl;
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
@Path("/rulesets")

@Api(description = "the rulesets API")




public class RulesetsApi  {

  @Context MessageContext securityContext;

RulesetsApiService delegate = new RulesetsApiServiceImpl();


    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new ruleset.", notes = "Creates a new ruleset in the user's organization.", response = RulesetInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_manage", description = "Manage governance rulesets")
        })
    }, tags={ "Rulesets",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Ruleset created successfully.", response = RulesetInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response createRuleset(@Multipart(value = "name")  String name,  @Multipart(value = "rulesetContent") InputStream rulesetContentInputStream, @Multipart(value = "rulesetContent" ) Attachment rulesetContentDetail, @Multipart(value = "ruleType")  String ruleType, @Multipart(value = "artifactType")  String artifactType, @Multipart(value = "description", required = false)  String description, @Multipart(value = "ruleCategory", required = false)  String ruleCategory, @Multipart(value = "documentationLink", required = false)  String documentationLink, @Multipart(value = "provider", required = false)  String provider) throws APIMGovernanceException{
        return delegate.createRuleset(name, rulesetContentInputStream, rulesetContentDetail, ruleType, artifactType, description, ruleCategory, documentationLink, provider, securityContext);
    }

    @DELETE
    @Path("/{rulesetId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Deletes a specific ruleset.", notes = "Deletes an existing ruleset identified by the rulesetId.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_manage", description = "Manage governance rulesets")
        })
    }, tags={ "Rulesets",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "OK. Ruleset deleted successfully.", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response deleteRuleset(@ApiParam(value = "**UUID** of the Ruleset. ",required=true) @PathParam("rulesetId") String rulesetId) throws APIMGovernanceException{
        return delegate.deleteRuleset(rulesetId, securityContext);
    }

    @GET
    @Path("/{rulesetId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves details of a specific ruleset.", notes = "Retrieves details of the ruleset identified by the rulesetId.", response = RulesetInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_read", description = "Read governance rulesets")
        })
    }, tags={ "Rulesets",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Ruleset details retrieved successfully.", response = RulesetInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getRulesetById(@ApiParam(value = "**UUID** of the Ruleset. ",required=true) @PathParam("rulesetId") String rulesetId) throws APIMGovernanceException{
        return delegate.getRulesetById(rulesetId, securityContext);
    }

    @GET
    @Path("/{rulesetId}/content")
    
    @Produces({ "application/x-yaml", "application/json" })
    @ApiOperation(value = "Retrieves the content of a specific ruleset.", notes = "Retrieves the content of the ruleset identified by the rulesetId.", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_read", description = "Read governance rulesets")
        })
    }, tags={ "Rulesets",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Ruleset content retrieved successfully.", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getRulesetContent(@ApiParam(value = "**UUID** of the Ruleset. ",required=true) @PathParam("rulesetId") String rulesetId) throws APIMGovernanceException{
        return delegate.getRulesetContent(rulesetId, securityContext);
    }

    @GET
    @Path("/{rulesetId}/usage")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves the policy usage of a specific ruleset.", notes = "Retrieves the list of policies using the ruleset identified by the rulesetId.", response = String.class, responseContainer = "List", authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_read", description = "Read governance rulesets")
        })
    }, tags={ "Rulesets",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Ruleset usage retrieved successfully.", response = String.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getRulesetUsage(@ApiParam(value = "**UUID** of the Ruleset. ",required=true) @PathParam("rulesetId") String rulesetId) throws APIMGovernanceException{
        return delegate.getRulesetUsage(rulesetId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves a list of rulesets.", notes = "Returns a list of all rulesets associated with the requested organization.", response = RulesetListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_read", description = "Read governance rulesets")
        })
    }, tags={ "Rulesets",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with a list of rulesets.", response = RulesetListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getRulesets( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "You can search for rulesets using the following format:    - \"query=name:{NAME}\" searches rulesets by name.   - \"query=artifactType:{ARTIFACT_TYPE}\" searches rulesets by artifact type.   - \"query=ruleType:{RULE_TYPE}\" searches rulesets by rule type.  You can combine multiple attributes to search for rulesets:   - \"query=name:{NAME} artifactType:{ARTIFACT_TYPE} ruleType:{RULE_TYPE}\" searches rulesets by name, artifact type, and rule type.  Remember to use URL encoding if your client does not support it (e.g., curl). ")  @QueryParam("query") String query) throws APIMGovernanceException{
        return delegate.getRulesets(limit, offset, query, securityContext);
    }

    @PUT
    @Path("/{rulesetId}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a specific ruleset.", notes = "Updates the details of the ruleset identified by the `rulesetId`.", response = RulesetInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_rule_manage", description = "Manage governance rulesets")
        })
    }, tags={ "Rulesets" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Ruleset updated successfully.", response = RulesetInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response updateRulesetById(@ApiParam(value = "**UUID** of the Ruleset. ",required=true) @PathParam("rulesetId") String rulesetId, @Multipart(value = "name")  String name,  @Multipart(value = "rulesetContent") InputStream rulesetContentInputStream, @Multipart(value = "rulesetContent" ) Attachment rulesetContentDetail, @Multipart(value = "ruleType")  String ruleType, @Multipart(value = "artifactType")  String artifactType, @Multipart(value = "description", required = false)  String description, @Multipart(value = "ruleCategory", required = false)  String ruleCategory, @Multipart(value = "documentationLink", required = false)  String documentationLink, @Multipart(value = "provider", required = false)  String provider) throws APIMGovernanceException{
        return delegate.updateRulesetById(rulesetId, name, rulesetContentInputStream, rulesetContentDetail, ruleType, artifactType, description, ruleCategory, documentationLink, provider, securityContext);
    }
}
