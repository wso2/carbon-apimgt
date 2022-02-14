package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.APIListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.DeployedAPIRevisionDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import java.util.List;
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

@Produces({ "application/json" })


public class ApisApi  {

  @Context MessageContext securityContext;

ApisApiService delegate = new ApisApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all apis", notes = "This will provide access to apis in database. ", response = APIListDTO.class, tags={ "Subscription Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of APIs in the database", response = APIListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response apisGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   Api ID ")  @QueryParam("apiId") String apiId,  @ApiParam(value = "**Search condition**.  context of the api ")  @QueryParam("context") String context,  @ApiParam(value = "**Search condition**.  versio  of the api ")  @QueryParam("version") String version,  @ApiParam(value = "**Search condition**.  label associated with the APIs ")  @QueryParam("gatewayLabel") String gatewayLabel,  @ApiParam(value = "Defines whether the returned response should contain full details of API ", defaultValue="true") @DefaultValue("true") @QueryParam("expand") Boolean expand, @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.apisGet(xWSO2Tenant, apiId, context, version, gatewayLabel, expand, accept, securityContext);
    }

    @PATCH
    @Path("/deployed-revisions")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Deploy Revision", notes = "Deploy a revision ", response = Void.class, tags={ "API Revisions",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. ", response = Void.class),
        @ApiResponse(code = 200, message = "", response = Void.class) })
    public Response deployedAPIRevision(@ApiParam(value = "Notification event payload" ) List<DeployedAPIRevisionDTO> deployedAPIRevisionDTOList) throws APIManagementException{
        return delegate.deployedAPIRevision(deployedAPIRevisionDTOList, securityContext);
    }

    @POST
    @Path("/undeployed-revision")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Remove undeployed revision", notes = "Remove undeployed Revision entry from the database", response = Void.class, tags={ "UnDeployed API Revision" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Fetch un-deployed revision", response = Void.class),
        @ApiResponse(code = 200, message = "", response = Void.class) })
    public Response unDeployedAPIRevision(@ApiParam(value = "Notification event payload" ) UnDeployedAPIRevisionDTO unDeployedAPIRevisionDTO) throws APIManagementException{
        return delegate.unDeployedAPIRevision(unDeployedAPIRevisionDTO, securityContext);
    }
}
