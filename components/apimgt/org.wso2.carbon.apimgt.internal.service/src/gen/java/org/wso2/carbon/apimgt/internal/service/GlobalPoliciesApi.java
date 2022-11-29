package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.GlobalPolicyListDTO;
import org.wso2.carbon.apimgt.internal.service.GlobalPoliciesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.GlobalPoliciesApiServiceImpl;
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
@Path("/global-policies")

@Api(description = "the global-policies API")

@Produces({ "application/json" })


public class GlobalPoliciesApi  {

  @Context MessageContext securityContext;

GlobalPoliciesApiService delegate = new GlobalPoliciesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all Global throttling policies", notes = "This will provide access to global throttling policies in database. ", response = GlobalPolicyListDTO.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of global policies in the database", response = GlobalPolicyListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response globalPoliciesGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  Global policy name ")  @QueryParam("policyName") String policyName) throws APIManagementException{
        return delegate.globalPoliciesGet(xWSO2Tenant, policyName, securityContext);
    }
}
