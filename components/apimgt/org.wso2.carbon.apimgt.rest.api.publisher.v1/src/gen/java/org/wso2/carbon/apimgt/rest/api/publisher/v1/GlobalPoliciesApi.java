package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GlobalPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.GlobalPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.GlobalPoliciesApiServiceImpl;
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




public class GlobalPoliciesApi  {

  @Context MessageContext securityContext;

GlobalPoliciesApiService delegate = new GlobalPoliciesApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Engage global policies to the gateways", notes = "This operation can be used to add global policies to the gateway/s. ", response = GlobalPoliciesDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:common_operation_policy_manage", description = "Add, Update and Delete common operation policies")
        })
    }, tags={ "Global Policies" })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Shared operation policy uploaded ", response = GlobalPoliciesDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response addGlobalPolicy(@ApiParam(value = "Policy details object that needs to be added." ,required=true) GlobalPoliciesDTO globalPoliciesDTO) throws APIManagementException{
        return delegate.addGlobalPolicy(globalPoliciesDTO, securityContext);
    }
}
