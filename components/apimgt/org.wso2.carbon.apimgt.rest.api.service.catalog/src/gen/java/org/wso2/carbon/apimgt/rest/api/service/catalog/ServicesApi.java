package org.wso2.carbon.apimgt.rest.api.service.catalog;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServicesApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.impl.ServicesApiServiceImpl;
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
@Path("/services")

@Api(description = "the services API")




public class ServicesApi  {

  @Context MessageContext securityContext;

ServicesApiService delegate = new ServicesApiServiceImpl();


    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new service to Service Catalog", notes = "Add a new service to the service catalog of the user's organization (or tenant) by specifying the details of the service along with its definition.  ", response = ServiceDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:service_write", description = "write access to services in service catalog")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created service as the response payload ", response = ServiceDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was not in a supported format.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response addService(@Multipart(value = "catalogEntry")  ServiceDTO catalogEntry,  @Multipart(value = "definitionFile") InputStream definitionFileInputStream, @Multipart(value = "definitionFile" ) Attachment definitionFileDetail) throws APIManagementException{
        return delegate.addService(catalogEntry, definitionFileInputStream, definitionFileDetail, securityContext);
    }

    @DELETE
    @Path("/{serviceId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a service", notes = "Delete a service by providing the service id ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:service_write", description = "write access to services in service catalog")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 204, message = "Successfully deleted the catalog entry. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response deleteService(@ApiParam(value = "uuid of the service",required=true) @PathParam("serviceId") String serviceId) throws APIManagementException{
        return delegate.deleteService(serviceId, securityContext);
    }

    @GET
    @Path("/export")
    
    @Produces({ "application/zip", "application/json" })
    @ApiOperation(value = "Export a service", notes = "Export a service as an archived zip file. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:service_view", description = "view access to services in service catalog")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response as the exported service as a zipped archive. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response exportService( @NotNull @ApiParam(value = "Name of the service to export ",required=true)  @QueryParam("name") String name,  @NotNull @ApiParam(value = "Version of the service to export ",required=true)  @QueryParam("version") String version) throws APIManagementException{
        return delegate.exportService(name, version, securityContext);
    }

    @GET
    @Path("/{serviceId}")
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of a service", notes = "Get details of a service using the id of the service. ", response = ServiceDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:service_view", description = "view access to services in service catalog")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Requested service in the service catalog is returned. ", response = ServiceDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getServiceById(@ApiParam(value = "uuid of the service",required=true) @PathParam("serviceId") String serviceId) throws APIManagementException{
        return delegate.getServiceById(serviceId, securityContext);
    }

    @GET
    @Path("/{serviceKey}/definition")
    
    @Produces({ "application/json", "application/yaml" })
    @ApiOperation(value = "Retrieve a service definition", notes = "Retrieve the definition of a service identified by the service id. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:service_view", description = "view access to services in service catalog")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with the definition file as entity in the body. ", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response getServiceDefinition(@ApiParam(value = "service key of the service",required=true) @PathParam("serviceKey") String serviceKey) throws APIManagementException{
        return delegate.getServiceDefinition(serviceKey, securityContext);
    }

    @POST
    @Path("/import")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import a service", notes = "Import  a service by providing an archived service ", response = ServiceInfoListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:service_write", description = "write access to services in service catalog")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Successful response with the imported service metadata. ", response = ServiceInfoListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response importService( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail,  @ApiParam(value = "Whether to overwrite if there is any existing service with the same name and version. ", defaultValue="false") @DefaultValue("false") @QueryParam("overwrite") Boolean overwrite, @Multipart(value = "verifier", required = false)  String verifier) throws APIManagementException{
        return delegate.importService(fileInputStream, fileDetail, overwrite, verifier, securityContext);
    }

    @GET
    
    
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/search services", notes = "Retrieve or search services in the service catalog of the user's organization or tenant. ", response = ServiceListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:service_view", description = "view access to services in service catalog")
        })
    }, tags={ "Services",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Paginated matched list of services returned. ", response = ServiceListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response searchServices( @ApiParam(value = "Filter services by the name of the service ")  @QueryParam("name") String name,  @ApiParam(value = "Filter services by version of the service ")  @QueryParam("version") String version,  @ApiParam(value = "Filter services by definitionType ", allowableValues="OAS, WSDL1, WSDL2, GRAPHQL_SDL, ASYNC_API")  @QueryParam("definitionType") String definitionType,  @ApiParam(value = "Filter services by the display name ")  @QueryParam("displayName") String displayName,  @ApiParam(value = "Comma seperated keys of the services to check ")  @QueryParam("key") String key,  @ApiParam(value = "If this set to true, a minimal set of fields will be provided for each service including the md5 ", defaultValue="false") @DefaultValue("false") @QueryParam("shrink") Boolean shrink,  @ApiParam(value = "", allowableValues="name, definitionType")  @QueryParam("sortBy") String sortBy,  @ApiParam(value = "", allowableValues="asc, desc")  @QueryParam("sortOrder") String sortOrder,  @ApiParam(value = "Maximum limit of items to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset) throws APIManagementException{
        return delegate.searchServices(name, version, definitionType, displayName, key, shrink, sortBy, sortOrder, limit, offset, securityContext);
    }

    @PUT
    @Path("/{serviceId}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a service", notes = "Update a service's details and definition ", response = ServiceDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "service_catalog:service_write", description = "write access to services in service catalog")
        })
    }, tags={ "Services" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Updated. Successful response with the newly updated service as entity in the body. ", response = ServiceDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response updateService(@ApiParam(value = "service key of the service",required=true) @PathParam("serviceKey") String serviceKey, @Multipart(value = "catalogEntry")  ServiceDTO catalogEntry,  @Multipart(value = "definitionFile") InputStream definitionFileInputStream, @Multipart(value = "definitionFile" ) Attachment definitionFileDetail) throws APIManagementException{
        return delegate.updateService(serviceKey, catalogEntry, definitionFileInputStream, definitionFileDetail, securityContext);
    }
}
