package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.SelfSignupApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.factories.SelfSignupApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.UserDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/self-signup")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/self-signup", description = "the self-signup API")
public class SelfSignupApi  {

   private final SelfSignupApiService delegate = SelfSignupApiServiceFactory.getSelfSignupApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Register a new user", notes = "User self signup API\n", response = UserDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created.\nSuccessful response with the newly created object as entity in the body.\nLocation header contains URL of newly created entity.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted.\nThe request has been accepted.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n") })

    public Response selfSignupPost(@ApiParam(value = "User object to represent the new user\n" ,required=true ) UserDTO body)
    {
    return delegate.selfSignupPost(body);
    }
}

