package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ApiPolicyListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.ApiPoliciesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ApiPoliciesApiServiceImpl;
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
@Path("/api-policies")

@Api(description = "the api-policies API")

@Produces({ "application/json" })


public class ApiPoliciesApi  {

  @Context MessageContext securityContext;

ApiPoliciesApiService delegate = new ApiPoliciesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all API throttling policies", notes = "This will provide access to api level throttling policies in database. ", response = ApiPolicyListDTO.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of api policies in the database", response = ApiPolicyListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response apiPoliciesGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  Api policy name ")  @QueryParam("policyName") String policyName,  @ApiParam(value = "**Search condition**.   Request policies of all the tenants or not ")  @QueryParam("allTenants") Boolean allTenants) throws APIManagementException{
        return delegate.apiPoliciesGet(xWSO2Tenant, policyName, allTenants, securityContext);
    }
}
