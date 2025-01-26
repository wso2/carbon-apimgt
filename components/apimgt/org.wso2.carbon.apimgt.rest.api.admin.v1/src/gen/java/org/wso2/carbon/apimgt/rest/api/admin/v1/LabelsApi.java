package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelUsageDTO;
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


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new Label", notes = "Add a new Label ", response = LabelDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Label (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. ", response = LabelDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class) })
    public Response createLabel(@ApiParam(value = "Label object that should to be added " ,required=true) LabelDTO labelDTO) throws APIManagementException{
        return delegate.createLabel(labelDTO, securityContext);
    }

    @DELETE
    @Path("/{labelId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Label", notes = "Delete a Label by label id ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Label (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Label successfully deleted. ", response = Void.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response deleteLabel(@ApiParam(value = "Label UUID ",required=true) @PathParam("labelId") String labelId) throws APIManagementException{
        return delegate.deleteLabel(labelId, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Labels", notes = "Get all Labels ", response = LabelListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Labels (Collection)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Labels returned ", response = LabelListDTO.class) })
    public Response getAllLabels() throws APIManagementException{
        return delegate.getAllLabels(securityContext);
    }

    @GET
    @Path("/{labelId}/usage")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a Label Usage", notes = "Retrieve a single Label Usage. We should provide the Id of the Label as a path parameter. ", response = LabelUsageDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Label (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Label Usage returned ", response = LabelUsageDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response getLabelUsage(@ApiParam(value = "Label UUID ",required=true) @PathParam("labelId") String labelId) throws APIManagementException{
        return delegate.getLabelUsage(labelId, securityContext);
    }

    @PUT
    @Path("/{labelId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Label", notes = "Update a Label by label id ", response = LabelDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:admin", description = "Manage all admin operations")
        })
    }, tags={ "Label (Individual)" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Label updated. ", response = LabelDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Specified resource already exists.", response = ErrorDTO.class) })
    public Response updateLabel(@ApiParam(value = "Label UUID ",required=true) @PathParam("labelId") String labelId, @ApiParam(value = "Label object with updated information " ,required=true) LabelDTO labelDTO) throws APIManagementException{
        return delegate.updateLabel(labelId, labelDTO, securityContext);
    }
}
