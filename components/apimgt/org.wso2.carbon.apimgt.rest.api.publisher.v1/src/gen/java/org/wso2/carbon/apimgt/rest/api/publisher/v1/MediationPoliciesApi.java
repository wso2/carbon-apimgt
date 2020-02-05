package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.MediationPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.MediationPoliciesApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

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
@Path("/mediation-policies")

@Api(description = "the mediation-policies API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class MediationPoliciesApi  {

  @Context MessageContext securityContext;

MediationPoliciesApiService delegate = new MediationPoliciesApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all global level mediation policies ", notes = "This operation provides you a list of available all global level mediation policies. ", response = MediationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Global Mediation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of mediation policies is returned. ", response = MediationListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response getAllGlobalMediationPolicies( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "-Not supported yet-")  @QueryParam("query") String query, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAllGlobalMediationPolicies(limit, offset, query, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{mediationPolicyId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Downloadt specific global mediation policy", notes = "This operation can be used to download a particular global mediation policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Global Mediation Policy" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Mediation policy returned. ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested file does not exist. ", response = ErrorDTO.class) })
    public Response getGlobalMediationPolicyContent(@ApiParam(value = "Mediation policy Id ",required=true) @PathParam("mediationPolicyId") String mediationPolicyId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getGlobalMediationPolicyContent(mediationPolicyId, ifNoneMatch, securityContext);
    }
}
