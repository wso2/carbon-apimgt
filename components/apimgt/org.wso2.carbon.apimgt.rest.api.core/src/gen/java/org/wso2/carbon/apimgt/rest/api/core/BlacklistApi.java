package org.wso2.carbon.apimgt.rest.api.core;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.core.dto.BlockingConditionListDTO;
import org.wso2.carbon.apimgt.rest.api.core.factories.BlacklistApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.core.BlacklistApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/core/v1.[\\d]+/blacklist")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/blacklist")
@io.swagger.annotations.Api(description = "the blacklist API")
public class BlacklistApi implements Microservice  {
   private final BlacklistApiService delegate = BlacklistApiServiceFactory.getBlacklistApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get all blocking condtions", notes = "Get all blocking condtions ", response = BlockingConditionListDTO.class, tags={ "Blacklist", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Blocking conditions returned ", response = BlockingConditionListDTO.class) })
    public Response blacklistGet(@ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept
, @Context Request request)
    throws NotFoundException {
        return delegate.blacklistGet(accept, request);
    }
}
