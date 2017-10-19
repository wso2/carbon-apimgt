package org.wso2.carbon.apimgt.rest.api.store;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.store.factories.LabelInfoApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
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
    name = "org.wso2.carbon.apimgt.rest.api.store.LabelInfoApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/store/v1.[\\d]+/label-info")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/label-info")
@io.swagger.annotations.Api(description = "the label-info API")
public class LabelInfoApi implements Microservice  {
   private final LabelInfoApiService delegate = LabelInfoApiServiceFactory.getLabelInfoApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get label information based on the label name", notes = "This operation can be used to retrieve the information of the labels ", response = LabelListDTO.class, tags={ "Label (Collection)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Label list is returned. ", response = LabelListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = LabelListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = LabelListDTO.class) })
    public Response labelInfoGet(@ApiParam(value = "Defines comma separated list of labels ",required=true) @QueryParam("labels") String labels
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.labelInfoGet(labels,ifNoneMatch,ifModifiedSince, request);
    }
}
