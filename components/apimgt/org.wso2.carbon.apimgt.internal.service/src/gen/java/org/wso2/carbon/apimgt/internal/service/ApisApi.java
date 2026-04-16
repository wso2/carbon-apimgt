package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.APIListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.DeployedAPIRevisionDTO;
import org.wso2.carbon.apimgt.internal.service.dto.DeploymentAcknowledgmentResponseDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.internal.service.dto.UnDeployedAPIRevisionDTO;
import org.wso2.carbon.apimgt.internal.service.ApisApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ApisApiServiceImpl;
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
@Path("/apis")

@Api(description = "the apis API")




public class ApisApi  {

  @Context MessageContext securityContext;

ApisApiService delegate = new ApisApiServiceImpl();


    @POST
    @Path("/{apiId}/gateway-deployments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Notify API deployment (API Platform gateway format)", notes = "Invoked by the API Platform gateway when an API is successfully deployed. Uses the same path and JSON body as the platform control plane so the gateway can call this endpoint when connected to on-prem (BaseURL/apis/{apiId}/gateway-deployments). Authenticated via api-key header (platform gateway registration token). ", response = DeploymentAcknowledgmentResponseDTO.class, tags={ "Gateway Monitoring",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Deployment notification received.", response = DeploymentAcknowledgmentResponseDTO.class),
        @ApiResponse(code = 401, message = "Invalid or missing api-key.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error.", response = ErrorDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response apisApiIdGatewayDeploymentsPost(@ApiParam(value = "API UUID (must match the deployed API).",required=true) @PathParam("apiId") String apiId,  @NotNull  @ApiParam(value = "Platform gateway registration token." ,required=true)@HeaderParam("api-key") String apiKey, @ApiParam(value = "" ,required=true) Map<String, Object> requestBody,  @ApiParam(value = "Deployment/revision identifier.")  @QueryParam("deploymentId") String deploymentId) throws APIManagementException{
        return delegate.apisApiIdGatewayDeploymentsPost(apiId, apiKey, requestBody, deploymentId, securityContext);
    }

    @GET
    @Path("/{apiId}")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Get API by ID (path)", notes = "Get a single API by UUID. When Accept is application/zip, returns a zip archive containing api.yaml in API Platform Gateway format (for platform gateway fetch after api.deployed). ", response = File.class, tags={ "Subscription Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "API definition (zip with api.yaml when Accept application/zip, else JSON).", response = File.class),
        @ApiResponse(code = 404, message = "API not found", response = Void.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response apisApiIdGet(@ApiParam(value = "API UUID.",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "application/zip for API Platform gateway format; application/json for APIList." , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.apisApiIdGet(apiId, accept, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all apis", notes = "This will provide access to apis in database. ", response = APIListDTO.class, tags={ "Subscription Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of APIs in the database", response = APIListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response apisGet( @NotNull  @ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   Api ID ")  @QueryParam("apiId") String apiId,  @ApiParam(value = "**Search condition**.  context of the api ")  @QueryParam("context") String context,  @ApiParam(value = "**Search condition**.  versio  of the api ")  @QueryParam("version") String version,  @ApiParam(value = "**Search condition**.  label associated with the APIs ")  @QueryParam("gatewayLabel") String gatewayLabel,  @ApiParam(value = "Defines whether the returned response should contain full details of API ", defaultValue="true") @DefaultValue("true") @QueryParam("expand") Boolean expand,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.apisGet(xWSO2Tenant, apiId, context, version, gatewayLabel, expand, accept, securityContext);
    }

    @PATCH
    @Path("/deployed-revisions")
    
    
    @ApiOperation(value = "Deploy Revision", notes = "Deploy a revision ", response = Void.class, tags={ "API Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. ", response = Void.class),
        @ApiResponse(code = 200, message = "", response = Void.class) })
    public Response deployedAPIRevision(@ApiParam(value = "Notification event payload" ) List<DeployedAPIRevisionDTO> deployedAPIRevisionDTOList) throws APIManagementException{
        return delegate.deployedAPIRevision(deployedAPIRevisionDTOList, securityContext);
    }

    @POST
    @Path("/import")
    @Consumes({ "multipart/form-data" })
    @Produces({ "text/plain", "application/json" })
    @ApiOperation(value = "Import an API", notes = "This operation can be used to import an API from the API Platform gateway controller. ", response = String.class, tags={ "Import",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. API Imported Successfully. ", response = String.class),
        @ApiResponse(code = 403, message = "", response = Void.class),
        @ApiResponse(code = 404, message = "", response = Void.class),
        @ApiResponse(code = 409, message = "", response = Void.class),
        @ApiResponse(code = 500, message = "", response = Void.class) })
    public Response importAPI( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "Preserve Original Provider of the API. This is the user choice to keep or replace the API provider ")  @QueryParam("preserveProvider") Boolean preserveProvider,  @ApiParam(value = "Once the revision max limit reached, undeploy and delete the earliest revision and create a new revision ")  @QueryParam("rotateRevision") Boolean rotateRevision,  @ApiParam(value = "Whether to update the API or not. This is used when updating already existing APIs ")  @QueryParam("overwrite") Boolean overwrite,  @ApiParam(value = "Preserve Portal Configurations. This is used to preserve the portal configurations of the API ")  @QueryParam("preservePortalConfigurations") Boolean preservePortalConfigurations,  @ApiParam(value = "Dry Run. This is used to validate the API without importing it ", defaultValue="false") @DefaultValue("false") @QueryParam("dryRun") Boolean dryRun,  @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.importAPI(fileInputStream, fileDetail, preserveProvider, rotateRevision, overwrite, preservePortalConfigurations, dryRun, accept, securityContext);
    }

    @POST
    @Path("/undeployed-revision")
    
    
    @ApiOperation(value = "Remove undeployed revision", notes = "Remove undeployed Revision entry from the database", response = Void.class, tags={ "UnDeployed API Revision" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Fetch un-deployed revision", response = Void.class),
        @ApiResponse(code = 200, message = "", response = Void.class) })
    public Response unDeployedAPIRevision(@ApiParam(value = "Notification event payload" ) UnDeployedAPIRevisionDTO unDeployedAPIRevisionDTO) throws APIManagementException{
        return delegate.unDeployedAPIRevision(unDeployedAPIRevisionDTO, securityContext);
    }
}
