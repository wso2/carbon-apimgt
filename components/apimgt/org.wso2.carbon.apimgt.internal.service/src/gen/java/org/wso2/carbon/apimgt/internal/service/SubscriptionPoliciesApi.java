package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SubscriptionPolicyListDTO;
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
    @ApiOperation(value = "Get all subscription throttling policies", notes = "This will provide access to subscription level throttling policies in database. ", response = SubscriptionPolicyListDTO.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of subscription policies in the database", response = SubscriptionPolicyListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response subscriptionPoliciesGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  Subscription policy name ")  @QueryParam("policyName") String policyName,  @ApiParam(value = "**Search condition**.   Request policies of all the tenants or not ")  @QueryParam("allTenants") Boolean allTenants) throws APIManagementException{
        return delegate.subscriptionPoliciesGet(xWSO2Tenant, policyName, allTenants, securityContext);
    }
}
