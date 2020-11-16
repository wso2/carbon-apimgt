package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.EndpointCertificatesApiServiceImpl;
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
@Path("/endpoint-certificates")

@Api(description = "the endpoint-certificates API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class EndpointCertificatesApi  {

  @Context MessageContext securityContext;

EndpointCertificatesApiService delegate = new EndpointCertificatesApiServiceImpl();


    @GET
    @Path("/{alias}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Download a Certificate", notes = "This operation can be used to download a certificate which matches the given alias. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:ep_certificates_view", description = "View backend endpoint certificates"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Endpoint Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response endpointCertificatesAliasContentGet(@ApiParam(value = "",required=true) @PathParam("alias") String alias) throws APIManagementException{
        return delegate.endpointCertificatesAliasContentGet(alias, securityContext);
    }

    @DELETE
    @Path("/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a certificate.", notes = "This operation can be used to delete an uploaded certificate. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:ep_certificates_update", description = "Update and delete backend endpoint certificates")
        })
    }, tags={ "Endpoint Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate deleted successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response endpointCertificatesAliasDelete(@ApiParam(value = "The alias of the certificate that should be deleted. ",required=true) @PathParam("alias") String alias) throws APIManagementException{
        return delegate.endpointCertificatesAliasDelete(alias, securityContext);
    }

    @GET
    @Path("/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the Certificate Information", notes = "This operation can be used to get the information about a certificate. ", response = CertificateInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:ep_certificates_view", description = "View backend endpoint certificates"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Endpoint Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = CertificateInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response endpointCertificatesAliasGet(@ApiParam(value = "",required=true) @PathParam("alias") String alias) throws APIManagementException{
        return delegate.endpointCertificatesAliasGet(alias, securityContext);
    }

    @PUT
    @Path("/{alias}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a certificate.", notes = "This operation can be used to update an uploaded certificate. ", response = CertMetadataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:ep_certificates_update", description = "Update and delete backend endpoint certificates")
        })
    }, tags={ "Endpoint Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate updated successfully. ", response = CertMetadataDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response endpointCertificatesAliasPut( @Multipart(value = "certificate") InputStream certificateInputStream, @Multipart(value = "certificate" ) Attachment certificateDetail,  @Size(min=1,max=30)@ApiParam(value = "Alias for the certificate",required=true) @PathParam("alias") String alias) throws APIManagementException{
        return delegate.endpointCertificatesAliasPut(certificateInputStream, certificateDetail, alias, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search Uploaded Certificates", notes = "This operation can be used to retrieve and search the uploaded certificates. ", response = CertificatesDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:ep_certificates_view", description = "View backend endpoint certificates"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Endpoint Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the list of matching certificate information in the body. ", response = CertificatesDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The specified resource does not exist.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response endpointCertificatesGet( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @Size(max=30) @ApiParam(value = "Alias for the certificate")  @QueryParam("alias") String alias,  @ApiParam(value = "Endpoint of which the certificate is uploaded")  @QueryParam("endpoint") String endpoint) throws APIManagementException{
        return delegate.endpointCertificatesGet(limit, offset, alias, endpoint, securityContext);
    }

    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Upload a new Certificate.", notes = "This operation can be used to upload a new certificate for an endpoint. ", response = CertMetadataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:ep_certificates_add", description = "Add backend endpoint certificates"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Endpoint Certificates" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate added successfully. ", response = CertMetadataDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error.", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error.", response = ErrorDTO.class) })
    public Response endpointCertificatesPost( @Multipart(value = "certificate") InputStream certificateInputStream, @Multipart(value = "certificate" ) Attachment certificateDetail, @Multipart(value = "alias")  @Size(min=1,max=30) 
  String alias, @Multipart(value = "endpoint")  
  String endpoint) throws APIManagementException{
        return delegate.endpointCertificatesPost(certificateInputStream, certificateDetail, alias, endpoint, securityContext);
    }
}
