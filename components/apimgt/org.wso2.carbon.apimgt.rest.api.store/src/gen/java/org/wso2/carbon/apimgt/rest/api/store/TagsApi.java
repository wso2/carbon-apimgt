package org.wso2.carbon.apimgt.rest.api.store;

import io.swagger.annotations.ApiParam;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagListDTO;
import org.wso2.carbon.apimgt.rest.api.store.factories.TagsApiServiceFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/tags")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/tags", description = "the tags API")
public class TagsApi  {

   private final TagsApiService delegate = TagsApiServiceFactory.getTagsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all tags\n", notes = "This operation can be used to retrieve a list of tags that are already added to APIs.\n", response = TagListDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nTag list is returned.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.\nEmpty body because the client has already the latest version of the requested resource (Will be supported in future).\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported\n") })

    public Response tagsGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit") Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset") Integer offset,
    @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be\n  retirieved from.\n"  )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,
    @ApiParam(value = "Media types acceptable for the response. Default is application/json.\n"  , defaultValue="application/json")@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved\nvariant of the resourec.\n"  )@HeaderParam("If-None-Match") String ifNoneMatch)
    {
    return delegate.tagsGet(limit,offset,xWSO2Tenant,accept,ifNoneMatch);
    }

    public String tagsGetGetLastUpdatedTime(Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch)
    {
        return delegate.tagsGetGetLastUpdatedTime(limit,offset,xWSO2Tenant,accept,ifNoneMatch);
    }
}

