package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationAttributeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SettingsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SettingsApiServiceImpl;
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
@Path("/settings")

@Api(description = "the settings API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class SettingsApi  {

  @Context MessageContext securityContext;

SettingsApiService delegate = new SettingsApiServiceImpl();


    @GET
    @Path("/application-attributes")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all application attributes from configuration ", notes = "This operation can be used to retrieve the application attributes from configuration. It will not return hidden attributes. ", response = ApplicationAttributeListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:subscribe", description = "Subscribe API")
        })
    }, tags={ "Settings",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Application attributes returned. ", response = ApplicationAttributeListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested application does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response settingsApplicationAttributesGet(@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.settingsApplicationAttributesGet(ifNoneMatch, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retreive store settings", notes = "Retreive store settings ", response = SettingsDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:store_settings", description = "Retrieve store settings")
        })
    }, tags={ "Settings" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Settings returned ", response = SettingsDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Settings does not exist. ", response = ErrorDTO.class) })
    public Response settingsGet(@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.settingsGet(xWSO2Tenant, securityContext);
    }
}
