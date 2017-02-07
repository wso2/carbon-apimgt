package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.factories.PoliciesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.PoliciesApi",
    service = Microservice.class,
    immediate = true
)

@Path("/api/am/publisher/v0.10/policies")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the policies API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-03T20:31:12.997+05:30")
public class PoliciesApi implements Microservice  {
   private final PoliciesApiService delegate = PoliciesApiServiceFactory.getPoliciesApi();

    @GET
    @Path("/{tierLevel}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all policies", notes = "This operation can be used to list the available policies for a given policy level. Tier level should be specified as a path parameter and should be one of `api`, `application` and `resource`. ", response = TierListDTO.class, tags={ "Throttling Tier (Collection)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. List of policies returned. ", response = TierListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = TierListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = TierListDTO.class) })
    public Response policiesTierLevelGet(@ApiParam(value = "List API or Application or Resource type policies. ",required=true, allowableValues="api, application, resource") @PathParam("tierLevel") String tierLevel
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
,@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
)
    throws NotFoundException {
        return delegate.policiesTierLevelGet(tierLevel,limit,offset,accept,ifNoneMatch);
    }

    @GET
    @Path("/{tierLevel}/{tierName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get details of a policy", notes = "This operation can be used to retrieve details of a single policy by specifying the policy level and policy name. ", response = TierDTO.class, tags={ "Throttling Tier (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Tier returned ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested Tier does not exist. ", response = TierDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = TierDTO.class) })
    public Response policiesTierLevelTierNameGet(@ApiParam(value = "Tier name ",required=true) @PathParam("tierName") String tierName
,@ApiParam(value = "List API or Application or Resource type policies. ",required=true, allowableValues="api, application, resource") @PathParam("tierLevel") String tierLevel
,@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
)
    throws NotFoundException {
        return delegate.policiesTierLevelTierNameGet(tierName,tierLevel,accept,ifNoneMatch,ifModifiedSince);
    }

    @POST
    @Path("/update-permission")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update policy permission", notes = "This operation can be used to update policy permissions which controls access for the particular policy based on the subscribers' roles. ", response = TierDTO.class, responseContainer = "List", tags={ "Throttling Tier (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successfully updated policy permissions ", response = TierDTO.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = TierDTO.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = TierDTO.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested policy does not exist. ", response = TierDTO.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = TierDTO.class, responseContainer = "List") })
    public Response policiesUpdatePermissionPost(@ApiParam(value = "Name of the policy ",required=true) @QueryParam("tierName") String tierName
,@ApiParam(value = "List API or Application or Resource type policies. ",required=true, allowableValues="api, application, resource") @QueryParam("tierLevel") String tierLevel
,@ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header. " )@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince
,@ApiParam(value = "" ) TierPermissionDTO permissions
)
    throws NotFoundException {
        return delegate.policiesUpdatePermissionPost(tierName,tierLevel,ifMatch,ifUnmodifiedSince,permissions);
    }
}
