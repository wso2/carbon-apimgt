package org.wso2.carbon.apimgt.internal.service;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.impl.RuntimeMetadataApiServiceImpl;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("/runtime-metadata")

@Api(description = "the runtime-metadata API")

@Produces({"application/json"})

public class RuntimeMetadataApi {

    @Context
    MessageContext securityContext;

    RuntimeMetadataApiService delegate = new RuntimeMetadataApiServiceImpl();

    @GET

    @Produces({"application/json"})
    @ApiOperation(value = "Metadata information for API runtimes", notes = "This will provide access to the deployment metadata in json format ", response = Void.class, tags = {
            "Retrieving Runtime artifacts"})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Json file of runtime metadata", response = Void.class),
            @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class)})
    public Response runtimeMetadataGet(
            @ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. ", required = true) @HeaderParam("xWSO2Tenant") String xWSO2Tenant,
            @ApiParam(value = "**Search condition**.   Api ID ") @QueryParam("apiId") String apiId,
            @ApiParam(value = "**Search condition**.  label associated with the APIs ") @QueryParam("gatewayLabel") String gatewayLabel,
            @ApiParam(value = "**Search condition**.  name of API ") @QueryParam("name") String name,
            @ApiParam(value = "**Search condition**.  version of API ") @QueryParam("version") String version)
            throws APIManagementException {

        return delegate.runtimeMetadataGet(xWSO2Tenant, apiId, gatewayLabel, name, version, securityContext);
    }
}
