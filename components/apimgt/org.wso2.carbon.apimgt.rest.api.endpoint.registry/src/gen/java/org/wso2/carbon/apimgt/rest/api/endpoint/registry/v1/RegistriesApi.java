package org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.RegistryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.RegistryEntryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.dto.RegistryEntryListDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.RegistriesApiService;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.v1.impl.RegistriesApiServiceImpl;
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
@Path("/registries")

@Api(description = "the registries API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class RegistriesApi  {

  @Context MessageContext securityContext;

RegistriesApiService delegate = new RegistriesApiServiceImpl();


    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new Registry", notes = "This operation can be used to create a new Registry specifying the details of the Registry in the payload. ", response = RegistryDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created Registry object as entity in the body. ", response = RegistryDTO.class),
        @ApiResponse(code = 405, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response addRegistry(@ApiParam(value = "Regsitry object that needs to be added " ,required=true) RegistryDTO body) throws APIManagementException{
        return delegate.addRegistry(body, securityContext);
    }

    @POST
    @Path("/{registryId}/entries")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add new entry to a Registry", notes = "Using this operation, you can add new entry to Registry. ", response = RegistryEntryDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created Registry Entry as entity in the body. ", response = RegistryEntryDTO.class),
        @ApiResponse(code = 405, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response addRegistryEntry(@ApiParam(value = "**UUID** of the Registry. ",required=true) @PathParam("registryId") String registryId, @ApiParam(value = "Registry Entry object that needs to be added " ,required=true) RegistryEntryDTO registryEntry) throws APIManagementException{
        return delegate.addRegistryEntry(registryId, registryEntry, securityContext);
    }

    @GET
    @Path("/{registryId}/entries")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all the entries in a Registry", notes = "Using this operation, you can retrieve the entries added in a single Registry by providing the Registry Id. ", response = RegistryEntryListDTO.class, tags={ "Registries",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested Entries in the Registry are returned ", response = RegistryEntryListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid UUID provided ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Registry does not exist. ", response = ErrorDTO.class) })
    public Response getAllEntriesInRegistry(@ApiParam(value = "**UUID** of the Registry. ",required=true) @PathParam("registryId") String registryId) throws APIManagementException{
        return delegate.getAllEntriesInRegistry(registryId, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search Registries ", notes = "This operation provides an array of available Registries ", response = RegistryArrayDTO.class, tags={  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Array of Registries is returned. ", response = RegistryArrayDTO.class) })
    public Response getAllRegistries() throws APIManagementException{
        return delegate.getAllRegistries(securityContext);
    }

    @GET
    @Path("/{registryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of a Registry", notes = "Using this operation, you can retrieve complete details of a single Registry by providing the uuid. ", response = RegistryDTO.class, tags={ "Registries" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested Registry is returned ", response = RegistryDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid UUID provided ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Registry does not exist. ", response = ErrorDTO.class) })
    public Response getRegistryByUUID(@ApiParam(value = "**UUID** of the Registry. ",required=true) @PathParam("registryId") String registryId) throws APIManagementException{
        return delegate.getRegistryByUUID(registryId, securityContext);
    }
}
