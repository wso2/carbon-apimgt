package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SubscriptionPolicyDTO;
import org.wso2.carbon.apimgt.internal.service.SubscriptionPoliciesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.SubscriptionPoliciesApiServiceImpl;
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
@Path("/subscription-policies")

@Api(description = "the subscription-policies API")

@Produces({ "application/json" })


public class SubscriptionPoliciesApi  {

  @Context MessageContext securityContext;

SubscriptionPoliciesApiService delegate = new SubscriptionPoliciesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all subscription throttling policies", notes = "This will provide access to subscription level throttling policies in database. ", response = SubscriptionPolicyDTO.class, responseContainer = "List", tags={ "Subscription Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of subscription policies in the database", response = SubscriptionPolicyDTO.class, responseContainer = "List"),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response subscriptionPoliciesGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant) throws APIManagementException{
        return delegate.subscriptionPoliciesGet(xWSO2Tenant, securityContext);
    }

    @GET
    @Path("/{policyId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all subscription throttling policies", notes = "This will provide access to subscription level throttling policy in database. ", response = Object.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "The subscription policy in the database", response = Object.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response subscriptionPoliciesPolicyIdGet(@ApiParam(value = "Policy Id represented as a UUID ",required=true) @PathParam("policyId") Integer policyId) throws APIManagementException{
        return delegate.subscriptionPoliciesPolicyIdGet(policyId, securityContext);
    }
}
