package org.wso2.carbon.apimgt.governance.rest.api;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.impl.TemplatesApiServiceImpl;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/templates")
@Api(description = "the templates API")
public class TemplatesApi {

    @Context
    MessageContext securityContext;

    TemplatesApiService delegate = new TemplatesApiServiceImpl();

    @POST
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a Devportal Governance template.",
            notes = "Creates a Devportal Governance template in the requested organization.",
            response = DevportalGovernanceTemplateDTO.class, authorizations = {
            @Authorization(value = "OAuth2Security", scopes = {
                    @AuthorizationScope(scope = "apim:gov_template_manage",
                            description = "Manage Devportal Governance templates")
            })
    }, tags = { "Devportal Governance Templates" })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "OK. Template created successfully.",
                    response = DevportalGovernanceTemplateDTO.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response createDevportalGovernanceTemplate(
            @ApiParam(value = "Devportal Governance template payload.", required = true)
            @Valid DevportalGovernanceTemplateDTO templateDTO) throws APIMGovernanceException {

        return delegate.createDevportalGovernanceTemplate(templateDTO, securityContext);
    }

    @GET
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve Devportal Governance templates.",
            notes = "Returns Devportal Governance templates in the requested organization.",
            response = DevportalGovernanceTemplateListDTO.class, authorizations = {
            @Authorization(value = "OAuth2Security", scopes = {
                    @AuthorizationScope(scope = "apim:gov_template_read",
                            description = "Read Devportal Governance templates")
            })
    }, tags = { "Devportal Governance Templates" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. Successful response with a list of templates.",
                    response = DevportalGovernanceTemplateListDTO.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getDevportalGovernanceTemplates(
            @ApiParam(value = "Maximum size of resource array to return.", defaultValue = "25")
            @DefaultValue("25") @QueryParam("limit") Integer limit,
            @ApiParam(value = "Starting point within the complete list of items qualified.", defaultValue = "0")
            @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIMGovernanceException {

        return delegate.getDevportalGovernanceTemplates(limit, offset, securityContext);
    }

    @GET
    @Path("/default")
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve the default Devportal Governance template.",
            notes = "Returns the fallback Devportal Governance template for the requested organization.",
            response = DevportalGovernanceTemplateDTO.class, authorizations = {
            @Authorization(value = "OAuth2Security", scopes = {
                    @AuthorizationScope(scope = "apim:gov_template_read",
                            description = "Read Devportal Governance templates")
            })
    }, tags = { "Devportal Governance Templates" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. Default template retrieved successfully.",
                    response = DevportalGovernanceTemplateDTO.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getDefaultDevportalGovernanceTemplate() throws APIMGovernanceException {

        return delegate.getDefaultDevportalGovernanceTemplate(securityContext);
    }

    @GET
    @Path("/{templateId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve a Devportal Governance template.",
            notes = "Retrieves the Devportal Governance template identified by the templateId.",
            response = DevportalGovernanceTemplateDTO.class, authorizations = {
            @Authorization(value = "OAuth2Security", scopes = {
                    @AuthorizationScope(scope = "apim:gov_template_read",
                            description = "Read Devportal Governance templates")
            })
    }, tags = { "Devportal Governance Templates" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. Template retrieved successfully.",
                    response = DevportalGovernanceTemplateDTO.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getDevportalGovernanceTemplateById(
            @ApiParam(value = "UUID of the template.", required = true) @PathParam("templateId")
            String templateId) throws APIMGovernanceException {

        return delegate.getDevportalGovernanceTemplateById(templateId, securityContext);
    }

    @PUT
    @Path("/{templateId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a Devportal Governance template.",
            notes = "Updates the Devportal Governance template identified by the templateId.",
            response = DevportalGovernanceTemplateDTO.class, authorizations = {
            @Authorization(value = "OAuth2Security", scopes = {
                    @AuthorizationScope(scope = "apim:gov_template_manage",
                            description = "Manage Devportal Governance templates")
            })
    }, tags = { "Devportal Governance Templates" })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK. Template updated successfully.",
                    response = DevportalGovernanceTemplateDTO.class),
            @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response updateDevportalGovernanceTemplateById(
            @ApiParam(value = "UUID of the template.", required = true) @PathParam("templateId")
            String templateId,
            @ApiParam(value = "Devportal Governance template payload.", required = true)
            @Valid DevportalGovernanceTemplateDTO templateDTO) throws APIMGovernanceException {

        return delegate.updateDevportalGovernanceTemplateById(templateId, templateDTO, securityContext);
    }

    @DELETE
    @Path("/{templateId}")
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a Devportal Governance template.",
            notes = "Deletes the Devportal Governance template identified by the templateId.",
            response = Void.class, authorizations = {
            @Authorization(value = "OAuth2Security", scopes = {
                    @AuthorizationScope(scope = "apim:gov_template_manage",
                            description = "Manage Devportal Governance templates")
            })
    }, tags = { "Devportal Governance Templates" })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "OK. Template deleted successfully.", response = Void.class),
            @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
            @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
            @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response deleteDevportalGovernanceTemplate(
            @ApiParam(value = "UUID of the template.", required = true) @PathParam("templateId")
            String templateId) throws APIMGovernanceException {

        return delegate.deleteDevportalGovernanceTemplate(templateId, securityContext);
    }
}
