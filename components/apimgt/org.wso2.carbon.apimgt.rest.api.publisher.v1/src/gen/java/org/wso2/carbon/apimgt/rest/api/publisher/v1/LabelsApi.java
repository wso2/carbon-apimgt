package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.LabelsApiServiceImpl;
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
@Path("/labels")

@Api(description = "the labels API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class LabelsApi  {

  @Context MessageContext securityContext;

LabelsApiService delegate = new LabelsApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all registered Labels", notes = "Get all registered Labels ", response = LabelListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Label Collection" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Labels returned ", response = LabelListDTO.class) })
    public Response labelsGet() throws APIManagementException{
        return delegate.labelsGet(securityContext);
    }
}
