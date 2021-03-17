package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.impl.LabelsApiServiceImpl;
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




public class LabelsApi  {

  @Context MessageContext securityContext;

LabelsApiService delegate = new LabelsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all registered Labels", notes = "Get all Registered Labels ", response = LabelListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:label_read", description = "Retrieve microgateway labels")
        })
    }, tags={ "Label Collection",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Labels returned ", response = LabelListDTO.class) })
    public Response labelsGet() throws APIManagementException{
        return delegate.labelsGet(securityContext);
    }

    @DELETE
    @Path("/{labelId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Label", notes = "Delete a Label by label Id ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:label_manage", description = "Manage microgateway labels")
        })
    }, tags={ "Label",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Label successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response labelsLabelIdDelete(@ApiParam(value = "Label UUID ",required=true) @PathParam("labelId") String labelId) throws APIManagementException{
        return delegate.labelsLabelIdDelete(labelId, securityContext);
    }

    @PUT
    @Path("/{labelId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Label", notes = "Update a Label by label Id ", response = LabelDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:label_manage", description = "Manage microgateway labels")
        })
    }, tags={ "Label",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Label updated. ", response = LabelDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response labelsLabelIdPut(@ApiParam(value = "Label UUID ",required=true) @PathParam("labelId") String labelId, @ApiParam(value = "Label object with updated information " ,required=true) LabelDTO labelDTO) throws APIManagementException{
        return delegate.labelsLabelIdPut(labelId, labelDTO, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a Label", notes = "Add a new gateway label ", response = LabelDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations"),
            @AuthorizationScope(scope = "apim:label_manage", description = "Manage microgateway labels")
        })
    }, tags={ "Label" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. ", response = LabelDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class) })
    public Response labelsPost(@ApiParam(value = "Label object that should to be added " ,required=true) LabelDTO labelDTO) throws APIManagementException{
        return delegate.labelsPost(labelDTO, securityContext);
    }
}
