package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.APIDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.internal.service.SubscribeInternalApiService;
import org.wso2.carbon.apimgt.internal.service.impl.SubscribeInternalApiServiceImpl;
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
@Path("/subscribe-internal")

@Api(description = "the subscribe-internal API")

@Produces({ "application/json" })


public class SubscribeInternalApi  {

  @Context MessageContext securityContext;

SubscribeInternalApiService delegate = new SubscribeInternalApiServiceImpl();


    @POST
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Subscribe to a subscription validation disabled API", notes = "This will allow creating subscriptions from applications to APIs which have subscription validation disabled. ", response = SubscriptionDTO.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Subscription created successfully", response = SubscriptionDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response subscribeToAPI(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "Application ID of the subscription ")  @QueryParam("appId") Integer appId,  @ApiParam(value = "Application UUID ")  @QueryParam("appUuid") String appUuid, @ApiParam(value = "The API object" ) APIDTO api) throws APIManagementException{
        return delegate.subscribeToAPI(xWSO2Tenant, appId, appUuid, api, securityContext);
    }
}
