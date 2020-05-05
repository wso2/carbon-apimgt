package org.wso2.carbon.apimgt.rest.api.endpoint.registry;

import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryArrayDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.dto.RegistryEntryDTO;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.RegistriesApiService;
import org.wso2.carbon.apimgt.rest.api.endpoint.registry.impl.RegistriesApiServiceImpl;
import org.wso2.carbon.apimgt.api.APIManagementException;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.inject.Inject;

    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.responses.ApiResponses;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.media.ArraySchema;
    import io.swagger.v3.oas.annotations.media.Content;
    import io.swagger.v3.oas.annotations.media.Schema;
    import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.io.InputStream;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import java.util.Map;
import java.util.List;
    import javax.validation.constraints.*;
@Path("/registries")




@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJAXRSCXFCDIServerCodegen", date = "2020-05-05T16:15:55.172+05:30[Asia/Colombo]")
public class RegistriesApi  {

    @Context MessageContext securityContext;

    RegistriesApiService delegate = new RegistriesApiServiceImpl();

        @POST
        
        @Consumes({ "application/json" })
        @Produces({ "application/json" })
            @Operation(summary = "Create a new Registry", description = "This operation can be used to create a new Registry specifying the details of the Registry in the payload. ", tags={ "Registries" })
            @ApiResponses(value = { 
                @ApiResponse(responseCode = "201", description = "Created. Successful response with the newly created Registry object as entity in the body. ", content = @Content(schema = @Schema(implementation = RegistryDTO.class))),
                @ApiResponse(responseCode = "405", description = "Bad Request. Invalid request or validation error. ", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                @ApiResponse(responseCode = "415", description = "Unsupported Media Type. The entity of the request was in a not supported format. ", content = @Content(schema = @Schema(implementation = ErrorDTO.class))) })
        public Response addRegistry(    
    @Parameter(description = "" ) RegistryDTO body


) throws APIManagementException{
        return delegate.addRegistry(body, securityContext);
        }
        @GET
        @Path("/{registryId}/entries")
        
        @Produces({ "application/json" })
            @Operation(summary = "Get All entries in the registry", description = "", tags={ "Registries" })
            @ApiResponses(value = { 
                @ApiResponse(responseCode = "200", description = "Array of entries in Registry is returned. ", content = @Content(schema = @Schema(implementation = RegistryEntryArrayDTO.class))),
                @ApiResponse(responseCode = "400", description = "Invalid Request ", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                @ApiResponse(responseCode = "404", description = "Not Found. Requested Registry does not exist. ", content = @Content(schema = @Schema(implementation = ErrorDTO.class))) })
        public Response getAllEntriesInRegistry(

@Parameter(description = "uuid of the Registry",required=true) @PathParam("registryId") String registryId


) throws APIManagementException{
        return delegate.getAllEntriesInRegistry(registryId, securityContext);
        }
        @GET
        
        
        @Produces({ "application/json" })
            @Operation(summary = "Retrieve/Search APIs ", description = "This operation provides you an array of available Registries. ", tags={ "Registries" })
            @ApiResponses(value = { 
                @ApiResponse(responseCode = "200", description = "OK. Array of Registries is returned. ", content = @Content(schema = @Schema(implementation = RegistryArrayDTO.class))) })
        public Response getRegistries() throws APIManagementException{
        return delegate.getRegistries(securityContext);
        }
        @GET
        @Path("/{registryId}")
        
        @Produces({ "application/json" })
            @Operation(summary = "Get details of a Registry", description = "Using this operation, you can retrieve complete details of a single Registry using the RegistryId. ", tags={ "Registries" })
            @ApiResponses(value = { 
                @ApiResponse(responseCode = "200", description = "OK. Requested Registry is returned ", content = @Content(schema = @Schema(implementation = RegistryDTO.class))),
                @ApiResponse(responseCode = "400", description = "Invalid Request", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                @ApiResponse(responseCode = "404", description = "Not Found. Requested Registry does not exist. ", content = @Content(schema = @Schema(implementation = ErrorDTO.class))) })
        public Response getRegistryByUUID(

@Parameter(description = "ID of the Registry",required=true) @PathParam("registryId") String registryId


) throws APIManagementException{
        return delegate.getRegistryByUUID(registryId, securityContext);
        }
        @POST
        @Path("/{registryId}/entry")
        @Consumes({ "multipart/form-data" })
        @Produces({ "application/json" })
            @Operation(summary = "", description = "", tags={  })
            @ApiResponses(value = { 
                @ApiResponse(responseCode = "201", description = "Created. Successful response with the newly created Registry Entry as entity in the body. ", content = @Content(schema = @Schema(implementation = RegistryEntryDTO.class))),
                @ApiResponse(responseCode = "405", description = "Bad Request. Invalid request or validation error. ", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
                @ApiResponse(responseCode = "415", description = "Unsupported Media Type. The entity of the request was in a not supported format. ", content = @Content(schema = @Schema(implementation = ErrorDTO.class))) })
        public Response registriesRegistryIdEntryPost(

@Parameter(description = "uuid of the registry",required=true) @PathParam("registryId") String registryId


, @Multipart(value = "registryEntry", required = false)  RegistryEntryDTO registryEntry

,  @Multipart(value = "definitionFile", required = false) InputStream definitionFileInputStream, @Multipart(value = "definitionFile" , required = false) Attachment definitionFileDetail

) throws APIManagementException{
        return delegate.registriesRegistryIdEntryPost(registryId, registryEntry, definitionFileInputStream, definitionFileDetail, securityContext);
        }
    }
