package org.wso2.carbon.apimgt.rest.api.publisher;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.WsdlsApiServiceFactory;

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
    name = "org.wso2.carbon.apimgt.rest.api.publisher.WsdlsApi",
    service = Microservice.class,
    immediate = true
)
@Path("/api/am/publisher/v1.[\\d]+/wsdls")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/wsdls")
@io.swagger.annotations.Api(description = "the wsdls API")
public class WsdlsApi implements Microservice  {
   private final WsdlsApiService delegate = WsdlsApiServiceFactory.getWsdlsApi();

    @POST
    @Path("/validate")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Validate a WSDL and retrieve a summary", notes = "This operation can be used to validate a WSDL and retrieve a summary ", response = WSDLValidationResponseDTO.class, tags={ "WSDLs (Individual)", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. WSDL validation information is returned ", response = WSDLValidationResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = WSDLValidationResponseDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = WSDLValidationResponseDTO.class) })
    public Response wsdlsValidatePost(@ApiParam(value = "URL of the WSDL ") @QueryParam("wsdlUrl") String wsdlUrl
, @Context Request request)
    throws NotFoundException {
        return delegate.wsdlsValidatePost(wsdlUrl, request);
    }
}
