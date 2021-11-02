package org.wso2.carbon.apimgt.rest.api.gateway;

import org.wso2.carbon.apimgt.rest.api.gateway.ServerStartupHealthcheckApiService;
import org.wso2.carbon.apimgt.rest.api.gateway.impl.ServerStartupHealthcheckApiServiceImpl;
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
@Path("/server-startup-healthcheck")

@Api(description = "the server-startup-healthcheck API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ServerStartupHealthcheckApi  {

  @Context MessageContext securityContext;

ServerStartupHealthcheckApiService delegate = new ServerStartupHealthcheckApiServiceImpl();


    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Health check to check if all the API are deployed during the server startup ", notes = "Health check to check if all the API are deployed during the server startup ", response = Void.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = Void.class),
        @ApiResponse(code = 500, message = "Internal Server Error. ", response = Void.class) })
    public Response serverStartupHealthcheckGet() throws APIManagementException{
        return delegate.serverStartupHealthcheckGet(securityContext);
    }
}
