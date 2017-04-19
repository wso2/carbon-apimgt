package org.wso2.carbon.apimgt.rest.api.store;

import io.swagger.annotations.ApiParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.store.factories.LabelInfoApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.store.LabelInfoApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/store/v1.[\\d]+/label-info")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the label-info API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-07T10:04:16.863+05:30")
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
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
, @Context Request request)
    throws NotFoundException {
        return delegate.labelInfoGet(labels,accept,ifNoneMatch,ifModifiedSince, request);
    }
}
