package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.SubscriptionPlansApiService;
import org.wso2.carbon.apimgt.internal.service.impl.SubscriptionPlansApiServiceImpl;
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
@Path("/subscription-plans")

@Api(description = "the subscription-plans API")




public class SubscriptionPlansApi  {

  @Context MessageContext securityContext;

SubscriptionPlansApiService delegate = new SubscriptionPlansApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "List subscription plans (stub)", notes = "Temporary stub endpoint for platform gateway bulk sync. Returns an empty list in this APIM version. ", response = Object.class, responseContainer = "List", tags={ "Subscriptions" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Object.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response subscriptionPlansGet() throws APIManagementException{
        return delegate.subscriptionPlansGet(securityContext);
    }
}
