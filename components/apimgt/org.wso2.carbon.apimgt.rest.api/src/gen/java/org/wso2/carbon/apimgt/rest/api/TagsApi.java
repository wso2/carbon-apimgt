package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.dto.*;
import org.wso2.carbon.apimgt.rest.api.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.factories.TagsApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/tags")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/tags", description = "the tags API")
public class TagsApi  {

   private final TagsApiService delegate = TagsApiServiceFactory.getTagsApi();

    @GET
    
    
    
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of tags", response = Void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. tag list is returned."),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource."),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist."),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported") })

    public Response tagsGet(@ApiParam(value = "Media types acceptable for the response. Should denote XML or JSON, default is JSON."  )@HeaderParam("Accept") String accept,
    @ApiParam(value = "Validator for conditional requests; based on ETag."  )@HeaderParam("If-None-Match") String ifNoneMatch,
    @ApiParam(value = "**Search condition**.\n\nYou can search in attributes by using **\"attribute:\"** modifier.\n\nSupported attribute modifiers are [**apiName,version**]\n\nEg. \"apiName:phoneVerification\" will match if the API Name is phoneVerification.\n\nIf no attribute modifier is found search will match the given query string against Tag Name.\n") @QueryParam("query") String query)
    {
    return delegate.tagsGet(accept,ifNoneMatch,query);
    }
}

