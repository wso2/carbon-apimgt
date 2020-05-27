package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.internal.service.SubscriptionsApiService;
import org.wso2.carbon.apimgt.internal.service.impl.SubscriptionsApiServiceImpl;
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
@Path("/subscriptions")

@Api(description = "the subscriptions API")

@Produces({ "application/json" })


public class SubscriptionsApi  {

  @Context MessageContext securityContext;

SubscriptionsApiService delegate = new SubscriptionsApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all subscriptions", notes = "This will provide access to subscriptions in database. ", response = SubscriptionDTO.class, responseContainer = "List", tags={ "Subscription Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of subscriptions in the database", response = SubscriptionDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response subscriptionsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.subscriptionsGet(xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{subscriptionId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a subscription", notes = "This will provide access to subscription in database. ", response = Object.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "A subscription in the database", response = Object.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response subscriptionsSubscriptionIdGet(@ApiParam(value = "Subscription Id ",required=true) @PathParam("subscriptionId") Integer subscriptionId) throws APIManagementException{
        return delegate.subscriptionsSubscriptionIdGet(subscriptionId, securityContext);
    }
}
