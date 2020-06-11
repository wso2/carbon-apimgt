package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.ClientCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.factories.ClientCertificatesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertMetadataDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertificatesDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/clientCertificates")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/clientCertificates", description = "the clientCertificates API")
public class ClientCertificatesApi  {

   private final ClientCertificatesApiService delegate = ClientCertificatesApiServiceFactory.getClientCertificatesApi();

    @GET
    @Path("/{alias}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Download a certificate.", notes = "This operation can be used to download a certificate which matches the given alias.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nAlias not provided or server is not configured to support mutual SSL authentication.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Certificate for the Alias not found.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response clientCertificatesAliasContentGet(@ApiParam(value = "",required=true ) @PathParam("alias")  String alias)
    {
    return delegate.clientCertificatesAliasContentGet(alias);
    }
    @DELETE
    @Path("/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a certificate.", notes = "This operation can be used to delete an uploaded certificate.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThe Certificate deleted successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nAlias not found or server is not configured to support mutual SSL authentication.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. |\nFailed to delete the certificate. Certificate could not found for\nthe given alias\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response clientCertificatesAliasDelete(@ApiParam(value = "The alias of the certificate that should be deleted.\n",required=true ) @PathParam("alias")  String alias)
    {
    return delegate.clientCertificatesAliasDelete(alias);
    }
    @GET
    @Path("/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get the certificate information.", notes = "This operation can be used to get the information about a certificate.\n", response = CertificateInfoDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nAlias not found or server is not configured to support mutual SSL authentication.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nAlias not found\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response clientCertificatesAliasGet(@ApiParam(value = "",required=true ) @PathParam("alias")  String alias)
    {
    return delegate.clientCertificatesAliasGet(alias);
    }
    @PUT
    @Path("/{alias}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a certificate.", notes = "This operation can be used to update an uploaded certificate.\n", response = ClientCertMetadataDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThe Certificate updated successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nFailure due to not providing alias.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nUpdating certificate failed. Alias not found or server is not configured to support mutual SSL\nauthentication.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response clientCertificatesAliasPut(@ApiParam(value = "Alias for the certificate",required=true ) @PathParam("alias")  String alias,
    @ApiParam(value = "The certificate that needs to be uploaded.") @Multipart(value = "certificate", required = false) InputStream certificateInputStream,
    @ApiParam(value = "The certificate that needs to be uploaded. : details") @Multipart(value = "certificate" , required = false) Attachment certificateDetail,
    @ApiParam(value = "The tier of the certificate" )@Multipart(value = "tier", required = false)  String tier)
    {
    return delegate.clientCertificatesAliasPut(alias,certificateInputStream,certificateDetail,tier);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/ Search uploaded Client Certificates.", notes = "This operation can be used to retrieve and search the uploaded client certificates.\n", response = ClientCertificatesDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with the list of matching certificate information in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nFailure due to not providing alias or server is not configured to support mutual SSL authentication.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response clientCertificatesGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Alias for the client certificate") @QueryParam("alias")  String alias,
    @ApiParam(value = "UUID of the API") @QueryParam("apiId")  String apiId)
    {
    return delegate.clientCertificatesGet(limit,offset,alias,apiId);
    }
    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Upload a new certificate.", notes = "This operation can be used to upload a new certificate for an endpoint.\n", response = ClientCertMetadataDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThe Certificate added successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nFailures due to existing alias or expired certificate.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\nFailed to add the Certificate due to an Internal Server Error\n") })

    public Response clientCertificatesPost(@ApiParam(value = "The certificate that needs to be uploaded.") @Multipart(value = "certificate") InputStream certificateInputStream,
    @ApiParam(value = "The certificate that needs to be uploaded. : details") @Multipart(value = "certificate" ) Attachment certificateDetail,
    @ApiParam(value = "Alias for the certificate", required=true )@Multipart(value = "alias")  String alias,
    @ApiParam(value = "apiId to which the certificate should be applied.", required=true )@Multipart(value = "apiId")  String apiId,
    @ApiParam(value = "apiId to which the certificate should be applied.", required=true )@Multipart(value = "tier")  String tier)
    {
    return delegate.clientCertificatesPost(certificateInputStream,certificateDetail,alias,apiId,tier);
    }
}

