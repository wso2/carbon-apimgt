package org.wso2.carbon.apimgt.rest.api.service.catalog;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServiceEntriesApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.impl.ServiceEntriesApiServiceImpl;
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
@Path("/service-entries")

@Api(description = "the service-entries API")




public class ServiceEntriesApi  {

  @Context MessageContext securityContext;

ServiceEntriesApiService delegate = new ServiceEntriesApiServiceImpl();


    @HEAD
    @Path("/export")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Check service existence", notes = "Check service existence by name and version. Upon successful response, this will also return the current state of the service as ETag header. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_view", description = "view service catalog entry")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with the available service's current state as the ETag header. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Service does not exist. ", response = ErrorDTO.class) })
    public Response checkServiceExistence( @NotNull @ApiParam(value = "Name of the service to export ",required=true)  @QueryParam("name") String name,  @NotNull @ApiParam(value = "Version of the service to export ",required=true)  @QueryParam("version") String version) throws APIManagementException{
        return delegate.checkServiceExistence(name, version, securityContext);
    }

    @GET
    @Path("/status")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Check services existence", notes = "Check multiple services existence by service keys. Upon successful response, this will also return the current states of the services as MD5 hash values. ", response = ServiceInfoListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_view", description = "view service catalog entry")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with the available services' current states as the MD5 hashes. ", response = ServiceInfoListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Service does not exist. ", response = ErrorDTO.class) })
    public Response checkServicesExistence( @NotNull @ApiParam(value = "Comma seperated keys of the services to check ",required=true)  @QueryParam("key") String key,  @ApiParam(value = "If this set to true, a minimal set of fields will be provided for each service including the md5 ")  @QueryParam("shrink") Boolean shrink) throws APIManagementException{
        return delegate.checkServicesExistence(key, shrink, securityContext);
    }

    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new service", notes = "Create a new service and add it to the service catalog of the user's organization (or tenant)  by specifying the details of the service along with its definition.  ", response = ServiceDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_write", description = "write service catalog entry")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created service entry as the response payload ", response = ServiceDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response createService(@Multipart(value = "catalogEntry")  ServiceDTO catalogEntry,  @Multipart(value = "definitionFile") InputStream definitionFileInputStream, @Multipart(value = "definitionFile" ) Attachment definitionFileDetail) throws APIManagementException{
        return delegate.createService(catalogEntry, definitionFileInputStream, definitionFileDetail, securityContext);
    }

    @DELETE
    @Path("/{serviceId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a service", notes = "Delete a service by providing the service id ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_write", description = "write service catalog entry")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Successfully deleted the catalog entry. ", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid Request ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Service does not exist. ", response = ErrorDTO.class) })
    public Response deleteService(@ApiParam(value = "uuid of the catalog entry",required=true) @PathParam("serviceId") String serviceId) throws APIManagementException{
        return delegate.deleteService(serviceId, securityContext);
    }

    @GET
    @Path("/export")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Export a service", notes = "Export a service as an archived zip file. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_view", description = "view service catalog entry")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response as the exported service as a zipped archive. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Service does not exist. ", response = ErrorDTO.class) })
    public Response exportService( @NotNull @ApiParam(value = "Name of the service to export ",required=true)  @QueryParam("name") String name,  @NotNull @ApiParam(value = "Version of the service to export ",required=true)  @QueryParam("version") String version) throws APIManagementException{
        return delegate.exportService(name, version, securityContext);
    }

    @GET
    @Path("/{serviceId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of a service", notes = "Get details of a service using the id of the service. ", response = ServiceDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_view", description = "view service catalog entry")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Requested service in the service catalog is returned. ", response = ServiceDTO.class),
        @ApiResponse(code = 400, message = "Invalid Request ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested service does not exist in the service catalog. ", response = ErrorDTO.class) })
    public Response getServiceById(@ApiParam(value = "uuid of the catalog entry",required=true) @PathParam("serviceId") String serviceId) throws APIManagementException{
        return delegate.getServiceById(serviceId, securityContext);
    }

    @GET
    @Path("/{serviceId}/definition")
    
    @Produces({ "application/json", "application/yaml" })
    @ApiOperation(value = "Retrieve a service definition", notes = "Retrieve the definition of a service identified by the service id. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_view", description = "view service catalog entry")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with the definition file as entity in the body. ", response = String.class),
        @ApiResponse(code = 400, message = "Invalid Request ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Service does not exist. ", response = ErrorDTO.class) })
    public Response getServiceDefinition(@ApiParam(value = "uuid of the catalog entry",required=true) @PathParam("serviceId") String serviceId) throws APIManagementException{
        return delegate.getServiceDefinition(serviceId, securityContext);
    }

    @POST
    @Path("/import")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import a service", notes = "Import  a service by providing an archived service ", response = ServiceInfoListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_create", description = "")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with the imported service metadata. ", response = ServiceInfoListDTO.class),
        @ApiResponse(code = 400, message = "Invalid Request ", response = ErrorDTO.class) })
    public Response importService(@ApiParam(value = "uuid of the catalog entry",required=true) @PathParam("serviceId") String serviceId,  @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail, @Multipart(value = "verifier")  String verifier,  @ApiParam(value = "ETag of the service resource to update" )@HeaderParam("If-Match") String ifMatch,  @ApiParam(value = "Whether to overwrite if there is any existing service with the same name and version. ")  @QueryParam("overwrite") Boolean overwrite) throws APIManagementException{
        return delegate.importService(serviceId, fileInputStream, fileDetail, verifier, ifMatch, overwrite, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/search services", notes = "Retrieve or search services in the service catalog of the user's organization or tenant. ", response = ServiceListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_view", description = "view service catalog entry")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Paginated matched list of services returned. ", response = ServiceListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Due to an invalid search parameter ", response = ErrorDTO.class) })
    public Response searchServices( @ApiParam(value = "Filter services by the name of the service ")  @QueryParam("name") String name,  @ApiParam(value = "Filter services by version of the service ")  @QueryParam("version") String version,  @ApiParam(value = "Filter services by definitionType ", allowableValues="OAS, WSDL1, WSDL2, GRAPHQL_SDL, ASYNC_API")  @QueryParam("definitionType") String definitionType,  @ApiParam(value = "Filter services by the display name ")  @QueryParam("displayName") String displayName,  @ApiParam(value = "Comma seperated keys of the services to check ")  @QueryParam("key") String key,  @ApiParam(value = "If this set to true, a minimal set of fields will be provided for each service including the md5 ")  @QueryParam("shrink") Boolean shrink,  @ApiParam(value = "", allowableValues="name, definitionType")  @QueryParam("sortBy") String sortBy,  @ApiParam(value = "", allowableValues="asc, desc")  @QueryParam("sortOrder") String sortOrder,  @ApiParam(value = "Maximum limit of items to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.searchServices(name, version, definitionType, displayName, key, shrink, sortBy, sortOrder, limit, offset, securityContext);
    }

    @PUT
    @Path("/{serviceId}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a service", notes = "Update a service's details and definition ", response = ServiceDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:entry_write", description = "write service catalog entry")
        })
    }, tags={ "Services" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Updated. Successful response with the newly updated service as entity in the body. ", response = ServiceDTO.class),
        @ApiResponse(code = 400, message = "Invalid Request ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Service does not exist. ", response = ErrorDTO.class) })
    public Response updateService(@ApiParam(value = "uuid of the catalog entry",required=true) @PathParam("serviceId") String serviceId, @Multipart(value = "catalogEntry")  ServiceDTO catalogEntry,  @Multipart(value = "definitionFile") InputStream definitionFileInputStream, @Multipart(value = "definitionFile" ) Attachment definitionFileDetail) throws APIManagementException{
        return delegate.updateService(serviceId, catalogEntry, definitionFileInputStream, definitionFileDetail, securityContext);
    }
}
