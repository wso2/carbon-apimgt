package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SubscriptionListDTO;
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
    @ApiOperation(value = "Get all subscriptions", notes = "This will provide access to subscriptions in database. ", response = SubscriptionListDTO.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of subscriptions in the database", response = SubscriptionListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response subscriptionsGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.   Api ID  of the subscription ")  @QueryParam("apiId") Integer apiId,  @ApiParam(value = "**Search condition**.   Application ID  of the subscription ")  @QueryParam("appId") Integer appId,  @ApiParam(value = "**Search condition**.   Api UUID  of the subscription ")  @QueryParam("apiUUID") String apiUUID,  @ApiParam(value = "**Search condition**.   Application UUID  of the subscription ")  @QueryParam("applicationUUID") String applicationUUID) throws APIManagementException{
        return delegate.subscriptionsGet(xWSO2Tenant, apiId, appId, apiUUID, applicationUUID, securityContext);
    }
}
