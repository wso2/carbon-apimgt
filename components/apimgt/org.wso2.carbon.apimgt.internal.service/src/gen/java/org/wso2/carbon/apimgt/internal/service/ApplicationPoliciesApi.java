package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.dto.ApplicationPolicyListDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.ApplicationPoliciesApiService;
import org.wso2.carbon.apimgt.internal.service.impl.ApplicationPoliciesApiServiceImpl;
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
@Path("/application-policies")

@Api(description = "the application-policies API")

@Produces({ "application/json" })


public class ApplicationPoliciesApi  {

  @Context MessageContext securityContext;

ApplicationPoliciesApiService delegate = new ApplicationPoliciesApiServiceImpl();


    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all application throttling policies", notes = "This will provide access to application level throttling policies in database. ", response = ApplicationPolicyListDTO.class, tags={ "Subscription Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "An array of application policies in the database", response = ApplicationPolicyListDTO.class),
        @ApiResponse(code = 200, message = "Unexpected error", response = ErrorDTO.class) })
    public Response applicationPoliciesGet(@ApiParam(value = "This is used to specify the tenant domain, where the resource need to be   retrieved from. " ,required=true)@HeaderParam("xWSO2Tenant") String xWSO2Tenant,      
        @ApiParam(value = "**Search condition**.  Application policy name ")  @QueryParam("policyName") String policyName
) throws APIManagementException{
        return delegate.applicationPoliciesGet(xWSO2Tenant, policyName, securityContext);
    }
}
