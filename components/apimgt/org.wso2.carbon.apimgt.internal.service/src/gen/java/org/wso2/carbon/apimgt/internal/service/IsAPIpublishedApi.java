package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.IsAPIpublishedApiService;
import org.wso2.carbon.apimgt.internal.service.impl.IsAPIpublishedApiServiceImpl;
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
@Path("/isAPIpublished")

@Api(description = "the isAPIpublished API")

@Produces({ "application/json" })


public class IsAPIpublishedApi  {

  @Context MessageContext securityContext;

IsAPIpublishedApiService delegate = new IsAPIpublishedApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Check whether the API is published or not", notes = "This will provide access to synapse artifacts in database. ", response = Boolean.class, tags={ "Checking for APIs" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Whether the API Published or not", response = Boolean.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response isAPIpublishedGet( @ApiParam(value = "**Search condition**.   Api ID ")  @QueryParam("apiId") String apiId) throws APIManagementException{
        return delegate.isAPIpublishedGet(apiId, securityContext);
    }
}
