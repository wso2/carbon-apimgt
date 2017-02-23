package org.wso2.carbon.apimgt.rest.api.publisher;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.LabelsApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.publisher.LabelsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1/labels")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the labels API")
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-23T18:39:28.727+05:30")
public class LabelsApi implements Microservice  {
   private final LabelsApiService delegate = LabelsApiServiceFactory.getLabelsApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all labels", notes = "This operation can be used to retrieve the list of labels available. ", response = LabelListDTO.class, tags={ "Label (Collection)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Label list is returned. ", response = LabelListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = LabelListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = LabelListDTO.class) })
    public Response labelsGet(@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
,@ApiParam(value = "Validator for API Minor Version " , defaultValue="1.0")@HeaderParam("Minor-Version") String minorVersion
)
    throws NotFoundException {
        return delegate.labelsGet(accept,ifNoneMatch,ifModifiedSince,minorVersion);
    }
}
