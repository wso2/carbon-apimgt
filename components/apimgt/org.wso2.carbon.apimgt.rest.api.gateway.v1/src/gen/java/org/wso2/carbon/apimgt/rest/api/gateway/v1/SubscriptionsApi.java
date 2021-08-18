package org.wso2.carbon.apimgt.rest.api.gateway.v1;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.impl.SubscriptionsApiServiceImpl;
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
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class SubscriptionsApi  {

  @Context MessageContext securityContext;

SubscriptionsApiService delegate = new SubscriptionsApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the subscriptions meta information of an api by providing the api name, version and context.", notes = "This operation is used to get the subscription information of an API from storage. If the Tenant domain is not provided carbon.super will be picked as the Tenant domain. ", response = SubscriptionDTO.class, tags={ "Get Subscription Info" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. End-points successfully retrieved from the storage for the API. ", response = SubscriptionDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class) })
    public Response subscriptionsGet( @ApiParam(value = "UUID of the API ")  @QueryParam("apiUUID") String apiUUID,  @ApiParam(value = "UUID of the Application ")  @QueryParam("appUUID") String appUUID,  @ApiParam(value = "Tenant Domain of the Application ")  @QueryParam("tenantDomain") String tenantDomain) throws APIManagementException{
        return delegate.subscriptionsGet(apiUUID, appUUID, tenantDomain, securityContext);
    }
}
