package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ClientCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ClientCertificatesApiServiceImpl;

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
@Path("/client-certificates")

@Api(description = "the client-certificates API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ClientCertificatesApi  {

  @Context MessageContext securityContext;

ClientCertificatesApiService delegate = new ClientCertificatesApiServiceImpl();


    @GET
    @Path("/{alias}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Download a certificate.", notes = "This operation can be used to download a certificate which matches the given alias. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_view", description = "View client certificates")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Alias not provided or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Certificate for the Alias not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response clientCertificatesAliasContentGet(@ApiParam(value = "",required=true) @PathParam("alias") String alias) {
        return delegate.clientCertificatesAliasContentGet(alias, securityContext);
    }

    @DELETE
    @Path("/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a certificate.", notes = "This operation can be used to delete an uploaded certificate. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_update", description = "Update and delete client certificates")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate deleted successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Alias not found or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. | Failed to delete the certificate. Certificate could not found for the given alias ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response clientCertificatesAliasDelete(@ApiParam(value = "The alias of the certificate that should be deleted. ",required=true) @PathParam("alias") String alias) {
        return delegate.clientCertificatesAliasDelete(alias, securityContext);
    }

    @GET
    @Path("/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the certificate information.", notes = "This operation can be used to get the information about a certificate. ", response = CertificateInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_view", description = "View client certificates")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = CertificateInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Alias not found or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Alias not found ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response clientCertificatesAliasGet(@ApiParam(value = "",required=true) @PathParam("alias") String alias) {
        return delegate.clientCertificatesAliasGet(alias, securityContext);
    }

    @PUT
    @Path("/{alias}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a certificate.", notes = "This operation can be used to update an uploaded certificate. ", response = ClientCertMetadataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_view", description = "View client certificates")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate updated successfully. ", response = ClientCertMetadataDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Failure due to not providing alias. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Updating certificate failed. Alias not found or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response clientCertificatesAliasPut(@ApiParam(value = "Alias for the certificate",required=true) @PathParam("alias") String alias,  @Multipart(value = "certificate", required = false) InputStream certificateInputStream, @Multipart(value = "certificate" , required = false) Attachment certificateDetail, @Multipart(value = "tier", required = false)  String tier) {
        return delegate.clientCertificatesAliasPut(alias, certificateInputStream, certificateDetail, tier, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/ Search uploaded Client Certificates.", notes = "This operation can be used to retrieve and search the uploaded client certificates. ", response = ClientCertificatesDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_view", description = "View client certificates")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the list of matching certificate information in the body. ", response = ClientCertificatesDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Failure due to not providing alias or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response clientCertificatesGet( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Alias for the client certificate")  @QueryParam("alias") String alias,  @ApiParam(value = "UUID of the API")  @QueryParam("apiId") String apiId) {
        return delegate.clientCertificatesGet(limit, offset, alias, apiId, securityContext);
    }

    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Upload a new certificate.", notes = "This operation can be used to upload a new certificate for an endpoint. ", response = ClientCertMetadataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_add", description = "Add client certificates")
        })
    }, tags={ "Client Certificates" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate added successfully. ", response = ClientCertMetadataDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Failures due to existing alias or expired certificate. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error Failed to add the Certificate due to an Internal Server Error ", response = ErrorDTO.class) })
    public Response clientCertificatesPost( @Multipart(value = "certificate") InputStream certificateInputStream, @Multipart(value = "certificate" ) Attachment certificateDetail, @Multipart(value = "alias")  String alias, @Multipart(value = "apiId")  String apiId, @Multipart(value = "tier")  String tier) {
        return delegate.clientCertificatesPost(certificateInputStream, certificateDetail, alias, apiId, tier, securityContext);
    }
}
