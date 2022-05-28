package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WebhookSubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.WebhooksApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.WebhooksApiServiceImpl;
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
@Path("/webhooks")

@Api(description = "the webhooks API")




public class WebhooksApi  {

  @Context MessageContext securityContext;

WebhooksApiService delegate = new WebhooksApiServiceImpl();


    @GET
    @Path("/subscriptions")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get available web hook subscriptions for a given application. ", notes = "This operation will provide a list of web hook topic subscriptions for a given application. If the api id is provided results will be filtered by the api Id. ", response = WebhookSubscriptionListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            
        })
    }, tags={ "Webhooks" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Topic list returned. ", response = WebhookSubscriptionListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response webhooksSubscriptionsGet( @ApiParam(value = "**Application Identifier** consisting of the UUID of the Application. ")  @QueryParam("applicationId") String applicationId,  @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ")  @QueryParam("apiId") String apiId,  @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retrieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.webhooksSubscriptionsGet(applicationId, apiId, xWSO2Tenant, securityContext);
    }
}
