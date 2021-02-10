package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.WebhooksSubscriptionsListDTO;
import org.wso2.carbon.apimgt.internal.service.WebhooksSubscriptionsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.WebhooksSubscriptionsApiServiceImpl;
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
@Path("/webhooks-subscriptions")

@Api(description = "the webhooks-subscriptions API")

@Produces({ "application/json" })


public class WebhooksSubscriptionsApi  {

  @Context MessageContext securityContext;

WebhooksSubscriptionsApiService delegate = new WebhooksSubscriptionsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get webhooks subscriptions", notes = "This will provide list of webhooks subscriptions from the database. ", response = WebhooksSubscriptionsListDTO.class, tags={ "Retrieving webhooks subscriptions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of webhooks subscriptions", response = WebhooksSubscriptionsListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response webhooksSubscriptionsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be             retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.webhooksSubscriptionsGet(xWSO2Tenant, securityContext);
    }
}
