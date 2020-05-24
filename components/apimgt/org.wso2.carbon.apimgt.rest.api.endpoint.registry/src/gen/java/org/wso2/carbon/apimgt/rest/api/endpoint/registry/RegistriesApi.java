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




@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJAXRSCXFCDIServerCodegen", date = "2020-05-22T14:14:59.940+05:30[Asia/Colombo]")
public class RegistriesApi  {

@Context MessageContext securityContext;

RegistriesApiService delegate = new RegistriesApiServiceImpl();

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Create a new Registry", description = "This operation can be used to create a new Registry specifying the details of the Registry in the payload. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "201",
            description = "Created. Successful response with the newly created Registry object as entity in the body. ",
            content = @Content(
            schema = @Schema(implementation = RegistryDTO.class))),
    
        @ApiResponse(responseCode = "405",
            description = "Bad Request. Invalid request or validation error. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "415",
            description = "Unsupported Media Type. The entity of the request was in a not supported format. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response addRegistry(    
    @Parameter(description = "" ) RegistryDTO body


) throws APIManagementException{
        return delegate.addRegistry(body, securityContext);
        }
    @POST
    @Path("/{registryId}/entry")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @Operation(summary = "Create a new Registry Entry", description = "This operation can be used to create a new Registry Entry specifying the details of the Entry in the payload. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registry Entries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "201",
            description = "Created. Successful response with the newly created Registry Entry as entity in the body. ",
            content = @Content(
            schema = @Schema(implementation = RegistryEntryDTO.class))),
    
        @ApiResponse(responseCode = "405",
            description = "Bad Request. Invalid request or validation error. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "415",
            description = "Unsupported Media Type. The entity of the request was in a not supported format. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response createRegistryEntry(

@Parameter(description = "uuid of the registry",required=true) @PathParam("registryId") String registryId


, @Multipart(value = "registryEntry", required = false)  RegistryEntryDTO registryEntry

,  @Multipart(value = "definitionFile", required = false) InputStream definitionFileInputStream, @Multipart(value = "definitionFile" , required = false) Attachment definitionFileDetail

) throws APIManagementException{
        return delegate.createRegistryEntry(registryId, registryEntry, definitionFileInputStream, definitionFileDetail, securityContext);
        }
    @DELETE
    @Path("/{registryId}")
    
    @Produces({ "application/json" })
    @Operation(summary = "Delete an Endpoint Registry", description = "This operation can be used to delete an existing Registry proving the Id of the Registry. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "Successfully deleted. "),
    
        @ApiResponse(responseCode = "400",
            description = "Invalid Request ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Registry does not exist. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response deleteRegistry(

@Parameter(description = "uuid of the Registry",required=true) @PathParam("registryId") String registryId


) throws APIManagementException{
        return delegate.deleteRegistry(registryId, securityContext);
        }
    @DELETE
    @Path("/{registryId}/entries/{entryId}")
    
    @Produces({ "application/json" })
    @Operation(summary = "Delete an Entry in a Registry", description = "This operation can be used to delete an existing Entry in Registry by specifying the registryId and entryId. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registry Entries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "Successfully deleted the registry entry. "),
    
        @ApiResponse(responseCode = "400",
            description = "Invalid Request ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Registry or Entry does not exist. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response deleteRegistryEntry(

@Parameter(description = "uuid of the registry",required=true) @PathParam("registryId") String registryId


, 

@Parameter(description = "uuid of the registry entry",required=true) @PathParam("entryId") String entryId


) throws APIManagementException{
        return delegate.deleteRegistryEntry(registryId, entryId, securityContext);
        }
    @GET
    @Path("/{registryId}/entries")
    
    @Produces({ "application/json" })
    @Operation(summary = "Get All entries in the registry", description = "",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registry Entries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "Array of entries in Registry is returned. ",
            content = @Content(
            schema = @Schema(implementation = RegistryEntryArrayDTO.class))),
    
        @ApiResponse(responseCode = "400",
            description = "Invalid Request ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Registry does not exist. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response getAllEntriesInRegistry(

@Parameter(description = "uuid of the Registry",required=true) @PathParam("registryId") String registryId


,             @Parameter(description = "**Search condition**.  Filter entries by serviceType ",     schema=@Schema(allowableValues={ "REST", "SOAP_1_1", "GQL", "WS" })
) 
        @QueryParam("serviceType") ServiceTypeEnum serviceType


,             @Parameter(description = "Filter registry entries by definitionType ",     schema=@Schema(allowableValues={ "OAS", "WSDL1", "WSDL2", "GQL_SDL" })
) 
        @QueryParam("definitionType") DefinitionTypeEnum definitionType


,             @Parameter(description = "Filter registry entries by the name of the Entry ") 
        @QueryParam("name") String name


,             @Parameter(description = "Filter registry entries by the service category of the Entry ",     schema=@Schema(allowableValues={ "UTILITY", "EDGE", "DOMAIN" })
) 
        @QueryParam("serviceCategory") ServiceCategoryEnum serviceCategory


,             @Parameter(description = "",     schema=@Schema(allowableValues={ "definitionType", "serviceType" })
) 
        @QueryParam("sortEntryBy") SortEntryByEnum sortEntryBy


,             @Parameter(description = "",     schema=@Schema(allowableValues={ "asc", "desc" })
) 
        @QueryParam("sortEntryOrder") SortEntryOrderEnum sortEntryOrder


,             @Parameter(description = "Maximum limit of items to return. ") 
            @DefaultValue("25")
        @QueryParam("limit") Integer limit


,             @Parameter(description = "Starting point within the complete list of items qualified. ") 
            @DefaultValue("0")
        @QueryParam("offset") Integer offset


) throws APIManagementException{
        return delegate.getAllEntriesInRegistry(registryId, serviceType, definitionType, name, serviceCategory, sortEntryBy, sortEntryOrder, limit, offset, securityContext);
        }
    public enum ServiceTypeEnum {
    REST,SOAP_1_1,GQL,WS;
    }    public enum DefinitionTypeEnum {
    OAS,WSDL1,WSDL2,GQL_SDL;
    }    public enum ServiceCategoryEnum {
    UTILITY,EDGE,DOMAIN;
    }    public enum SortEntryByEnum {
    definitionType,serviceType;
    }    public enum SortEntryOrderEnum {
    asc,desc;
    }    @GET
    @Path("/{registryId}/entries/{entryId}/definition-file")
    
    @Produces({ "application/octet-stream", "application/json" })
    @Operation(summary = "Retrieve the definition file of a specific Entry in a Registry", description = "Using this operation, you can retrieve the definition file of a specific entry in a Registry using the EntryId and RegistryId. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registry Entries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "Updated. Successful response with the definition file as entity in the body. ",
            content = @Content(
            schema = @Schema(implementation = File.class))),
    
        @ApiResponse(responseCode = "400",
            description = "Invalid Request ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Registry or Entry does not exist. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response getEndpointDefinition(

@Parameter(description = "uuid of the registry",required=true) @PathParam("registryId") String registryId


, 

@Parameter(description = "uuid of the registry entry",required=true) @PathParam("entryId") String entryId


) throws APIManagementException{
        return delegate.getEndpointDefinition(registryId, entryId, securityContext);
        }
    @GET
    
    
    @Produces({ "application/json" })
    @Operation(summary = "Retrieve all Registries ", description = "This operation provides you an array of available Registries. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "OK. Array of Registries is returned. ",
            content = @Content(
            schema = @Schema(implementation = RegistryArrayDTO.class)))
     })
    public Response getRegistries(            @Parameter(description = "**Search condition**. You can search for a registry by specifying the registry name as \"query\" attribute.  Eg. \"prodServer\" will match a registry entry if the name is exactly \"prodServer\". ") 
        @QueryParam("query") String query


,             @Parameter(description = "",     schema=@Schema(allowableValues={ "registryName" })
) 
        @QueryParam("sortRegistryBy") SortRegistryByEnum sortRegistryBy


,             @Parameter(description = "",     schema=@Schema(allowableValues={ "asc", "desc" })
) 
        @QueryParam("sortRegistryOrder") SortRegistryOrderEnum sortRegistryOrder


,             @Parameter(description = "Maximum limit of items to return. ") 
            @DefaultValue("25")
        @QueryParam("limit") Integer limit


,             @Parameter(description = "Starting point within the complete list of items qualified. ") 
            @DefaultValue("0")
        @QueryParam("offset") Integer offset


) throws APIManagementException{
        return delegate.getRegistries(query, sortRegistryBy, sortRegistryOrder, limit, offset, securityContext);
        }
    public enum SortRegistryByEnum {
    registryName;
    }    public enum SortRegistryOrderEnum {
    asc,desc;
    }    @GET
    @Path("/{registryId}")
    
    @Produces({ "application/json" })
    @Operation(summary = "Get details of a Registry", description = "Using this operation, you can retrieve complete details of a single Registry using the RegistryId. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "OK. Requested Registry is returned ",
            content = @Content(
            schema = @Schema(implementation = RegistryDTO.class))),
    
        @ApiResponse(responseCode = "400",
            description = "Invalid Request",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Registry does not exist. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response getRegistryByUUID(

@Parameter(description = "ID of the Registry",required=true) @PathParam("registryId") String registryId


) throws APIManagementException{
        return delegate.getRegistryByUUID(registryId, securityContext);
        }
    @GET
    @Path("/{registryId}/entries/{entryId}")
    
    @Produces({ "application/json" })
    @Operation(summary = "Retrieve a specific Entry in a Registry", description = "Using this operation, you can retrieve a specific entry in a single Registry using the EntryId and RegistryId. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registry Entries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "Requested Entry in Registry is returned. ",
            content = @Content(
            schema = @Schema(implementation = RegistryEntryArrayDTO.class))),
    
        @ApiResponse(responseCode = "400",
            description = "Invalid Request ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Registry or Entry does not exist. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response getRegistryEntryByUuid(

@Parameter(description = "uuid of the registry",required=true) @PathParam("registryId") String registryId


, 

@Parameter(description = "uuid of the registry entry",required=true) @PathParam("entryId") String entryId


) throws APIManagementException{
        return delegate.getRegistryEntryByUuid(registryId, entryId, securityContext);
        }
    @PUT
    @Path("/{registryId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "Update an existing Registry", description = "This operation can be used to update an existing Endpoint Registry ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "OK. Updated Registry is returned ",
            content = @Content(
            schema = @Schema(implementation = RegistryDTO.class))),
    
        @ApiResponse(responseCode = "400",
            description = "Invalid Request",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Registry does not exist. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response updateRegistry(

@Parameter(description = "ID of the Registry",required=true) @PathParam("registryId") String registryId


,     
    @Parameter(description = "" ) RegistryDTO body


) throws APIManagementException{
        return delegate.updateRegistry(registryId, body, securityContext);
        }
    @PUT
    @Path("/{registryId}/entries/{entryId}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @Operation(summary = "Update an existing Entry in a Registry", description = "This operation can be used to update an existing Entry in Registry with the details of the Entry in the payload. ",
        security = {  @SecurityRequirement(name = "default" , scopes = { "" })
                 }, tags={ "Registry Entries" })

    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200",
            description = "Updated. Successful response with the newly updated Registry Entry as entity in the body. ",
            content = @Content(
            schema = @Schema(implementation = RegistryEntryDTO.class))),
    
        @ApiResponse(responseCode = "400",
            description = "Invalid Request ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class))),
    
        @ApiResponse(responseCode = "404",
            description = "Not Found. Requested Registry or Entry does not exist. ",
            content = @Content(
            schema = @Schema(implementation = ErrorDTO.class)))
     })
    public Response updateRegistryEntry(

@Parameter(description = "uuid of the registry",required=true) @PathParam("registryId") String registryId


, 

@Parameter(description = "uuid of the registry entry",required=true) @PathParam("entryId") String entryId


, @Multipart(value = "registryEntry", required = false)  RegistryEntryDTO registryEntry

,  @Multipart(value = "definitionFile", required = false) InputStream definitionFileInputStream, @Multipart(value = "definitionFile" , required = false) Attachment definitionFileDetail

) throws APIManagementException{
        return delegate.updateRegistryEntry(registryId, entryId, registryEntry, definitionFileInputStream, definitionFileDetail, securityContext);
        }
}
