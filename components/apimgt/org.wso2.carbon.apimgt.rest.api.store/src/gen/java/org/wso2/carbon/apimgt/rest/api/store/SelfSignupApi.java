package org.wso2.carbon.apimgt.rest.api.store;


import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.rest.api.store.dto.UserDTO;
import org.wso2.carbon.apimgt.rest.api.store.factories.SelfSignupApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.store.SelfSignupApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/store/v1.[\\d]+/self-signup")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the self-signup API")
public class SelfSignupApi implements Microservice  {
   private final SelfSignupApiService delegate = SelfSignupApiServiceFactory.getSelfSignupApi();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "User self signup API ", response = UserDTO.class, tags={ "Signup", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = UserDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = UserDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = UserDTO.class) })
    public Response selfSignupPost(@ApiParam(value = "User object to represent the new user " ,required=true) UserDTO body
,@ApiParam(value = "Media type of the entity in the body. Default is JSON. " ,required=true, defaultValue="JSON")@HeaderParam("Content-Type") String contentType
, @Context Request request)
    throws NotFoundException {
        return delegate.selfSignupPost(body,contentType, request);
    }
}
