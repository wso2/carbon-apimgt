package org.wso2.carbon.apimgt.governance.rest.api;

import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.DevportalGovernanceTemplateListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.TemplateDefaultViolationListDTO;
import org.wso2.carbon.apimgt.governance.rest.api.TemplatesApiService;
import org.wso2.carbon.apimgt.governance.rest.api.impl.TemplatesApiServiceImpl;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;

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
@Path("/templates")

@Api(description = "the templates API")




public class TemplatesApi  {

  @Context MessageContext securityContext;

TemplatesApiService delegate = new TemplatesApiServiceImpl();


    @POST

    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new Devportal Governance template.", notes = "Creates a Devportal Governance template in the user's organization.", response = DevportalGovernanceTemplateDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_template_manage", description = "Manage Devportal Governance templates")
        })
    }, tags={ "Devportal Governance Templates",  })
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "OK. Devportal Governance template created successfully.", response = DevportalGovernanceTemplateDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response createDevportalGovernanceTemplate(@ApiParam(value = "JSON object containing the details of the new Devportal Governance template." ,required=true) DevportalGovernanceTemplateDTO devportalGovernanceTemplateDTO) throws APIMGovernanceException{
        return delegate.createDevportalGovernanceTemplate(devportalGovernanceTemplateDTO, securityContext);
    }

    @DELETE
    @Path("/{templateId}")

    @Produces({ "application/json" })
    @ApiOperation(value = "Deletes a specific Devportal Governance template.", notes = "Deletes an existing Devportal Governance template identified by the templateId.", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_template_manage", description = "Manage Devportal Governance templates")
        })
    }, tags={ "Devportal Governance Templates",  })
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "OK. Devportal Governance template deleted successfully.", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response deleteDevportalGovernanceTemplate(@ApiParam(value = "**UUID** of the Devportal Governance template. ",required=true) @PathParam("templateId") String templateId) throws APIMGovernanceException{
        return delegate.deleteDevportalGovernanceTemplate(templateId, securityContext);
    }

    @GET
    @Path("/default")

    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves the default Devportal Governance template.", notes = "Retrieves the fallback Devportal Governance template configured for the requested organization.", response = DevportalGovernanceTemplateDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_template_read", description = "Read Devportal Governance templates")
        })
    }, tags={ "Devportal Governance Templates",  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK. Default Devportal Governance template retrieved successfully.", response = DevportalGovernanceTemplateDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getDefaultDevportalGovernanceTemplate() throws APIMGovernanceException{
        return delegate.getDefaultDevportalGovernanceTemplate(securityContext);
    }

    @GET
    @Path("/{templateId}")

    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves details of a specific Devportal Governance template.", notes = "Retrieves details of the Devportal Governance template identified by the templateId.", response = DevportalGovernanceTemplateDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_template_read", description = "Read Devportal Governance templates")
        })
    }, tags={ "Devportal Governance Templates",  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK. Devportal Governance template details retrieved successfully.", response = DevportalGovernanceTemplateDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getDevportalGovernanceTemplateById(@ApiParam(value = "**UUID** of the Devportal Governance template. ",required=true) @PathParam("templateId") String templateId) throws APIMGovernanceException{
        return delegate.getDevportalGovernanceTemplateById(templateId, securityContext);
    }

    @GET


    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves a list of Devportal Governance templates.", notes = "Returns all Devportal Governance templates associated with the requested organization.", response = DevportalGovernanceTemplateListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_template_read", description = "Read Devportal Governance templates")
        })
    }, tags={ "Devportal Governance Templates",  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK. Successful response with a list of Devportal Governance templates.", response = DevportalGovernanceTemplateListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response getDevportalGovernanceTemplates( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIMGovernanceException{
        return delegate.getDevportalGovernanceTemplates(limit, offset, securityContext);
    }

    @PUT
    @Path("/{templateId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Updates a specific Devportal Governance template.", notes = "Updates the details and ruleset bindings of the Devportal Governance template identified by the templateId.", response = DevportalGovernanceTemplateDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_template_manage", description = "Manage Devportal Governance templates")
        })
    }, tags={ "Devportal Governance Templates",  })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK. Devportal Governance template updated successfully.", response = DevportalGovernanceTemplateDTO.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response updateDevportalGovernanceTemplateById(@ApiParam(value = "**UUID** of the Devportal Governance template. ",required=true) @PathParam("templateId") String templateId, @ApiParam(value = "JSON object containing the updated Devportal Governance template details." ,required=true) DevportalGovernanceTemplateDTO devportalGovernanceTemplateDTO) throws APIMGovernanceException{
        return delegate.updateDevportalGovernanceTemplateById(templateId, devportalGovernanceTemplateDTO, securityContext);
    }

    @POST
    @Path("/{templateId}/validate-defaults")

    @Produces({ "application/json" })
    @ApiOperation(value = "Validates a template's hidden field defaults against its bound rulesets.", notes = "Dry-run check that returns any rule violations that would block publishing the template. Only rules targeting hidden fields with admin-set defaults are evaluated; violations about visible fields (filled by developers) are excluded. This endpoint does not modify the template. ", response = TemplateDefaultViolationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:gov_template_read", description = "Read Devportal Governance templates")
        })
    }, tags={ "Devportal Governance Templates" })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK. Validation completed. An empty violations list means the template is safe to publish. ", response = TemplateDefaultViolationListDTO.class),
        @ApiResponse(code = 404, message = "Not Found", response = ErrorDTO.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error", response = ErrorDTO.class) })
    public Response validateTemplateDefaults(@ApiParam(value = "**UUID** of the Devportal Governance template. ",required=true) @PathParam("templateId") String templateId) throws APIMGovernanceException{
        return delegate.validateTemplateDefaults(templateId, securityContext);
    }
}
