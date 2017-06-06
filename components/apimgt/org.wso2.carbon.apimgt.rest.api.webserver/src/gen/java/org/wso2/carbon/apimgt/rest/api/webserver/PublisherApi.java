package org.wso2.carbon.apimgt.rest.api.webserver;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.webserver.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.webserver.factories.PublisherApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.webserver.PublisherApi",
    service = Microservice.class,
    immediate = true
)
@Path(".*")
@Consumes({ "application/json" })
//@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the publisher API")
public class PublisherApi implements Microservice  {
   private final PublisherApiService delegate = PublisherApiServiceFactory.getPublisherApi();

    @GET

    @Consumes({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get file", notes = "Get file as requested ", response = File.class, tags={ "Application Policies", })
    @io.swagger.annotations.ApiResponses(value = {
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK", response = File.class),

        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = File.class),

        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = File.class) })
    public Response publisherGet(@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.publisherGet(accept,ifNoneMatch,ifModifiedSince, request);
    }


}
