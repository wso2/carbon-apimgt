package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.EndpointCertificatesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.factories.EndpointCertificatesApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertMetadataDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificatesDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/endpoint-certificates")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/endpoint-certificates", description = "the endpoint-certificates API")
public class EndpointCertificatesApi  {

   private final EndpointCertificatesApiService delegate = EndpointCertificatesApiServiceFactory.getEndpointCertificatesApi();

    @GET
    @Path("/{alias}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Download a certificate.", notes = "This operation can be used to download a certificate which matches the given alias.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n*\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Certificate for the Alias not found.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response endpointCertificatesAliasContentGet(@ApiParam(value = "",required=true ) @PathParam("alias")  String alias)
    {
    return delegate.endpointCertificatesAliasContentGet(alias);
    }
    @DELETE
    @Path("/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Delete a certificate.", notes = "This operation can be used to delete an uploaded certificate.\n", response = void.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThe Certificate deleted successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\n\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. |\nFailed to delete the certificate. Certificate could not found for\nthe given alias\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response endpointCertificatesAliasDelete(@ApiParam(value = "The alias of the certificate that should be deleted.\n",required=true ) @PathParam("alias")  String alias)
    {
    return delegate.endpointCertificatesAliasDelete(alias);
    }
    @GET
    @Path("/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Get the certificate information.", notes = "This operation can be used to get the information about a certificate.\n", response = CertificateInfoDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nAlias not found\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response endpointCertificatesAliasGet(@ApiParam(value = "",required=true ) @PathParam("alias")  String alias)
    {
    return delegate.endpointCertificatesAliasGet(alias);
    }
    @PUT
    @Path("/{alias}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update a certificate.", notes = "This operation can be used to update an uploaded certificate.\n", response = CertMetadataDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThe Certificate updated successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\nUpdating certificate failed. Alias not found\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response endpointCertificatesAliasPut(@ApiParam(value = "The certificate that needs to be uploaded.") @Multipart(value = "certificate") InputStream certificateInputStream,
    @ApiParam(value = "The certificate that needs to be uploaded. : details") @Multipart(value = "certificate" ) Attachment certificateDetail,
    @ApiParam(value = "Alias for the certificate",required=true ) @PathParam("alias")  String alias)
    {
    return delegate.endpointCertificatesAliasPut(certificateInputStream,certificateDetail,alias);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieve/Search uploaded certificates.", notes = "This operation can be used to retrieve and search the uploaded certificates.\n", response = CertificatesDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Successful response with the list of matching certificate information in the body.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n") })

    public Response endpointCertificatesGet(@ApiParam(value = "Maximum size of resource array to return.\n", defaultValue="25") @QueryParam("limit")  Integer limit,
    @ApiParam(value = "Starting point within the complete list of items qualified.\n", defaultValue="0") @QueryParam("offset")  Integer offset,
    @ApiParam(value = "Alias for the certificate") @QueryParam("alias")  String alias,
    @ApiParam(value = "Endpoint of which the certificate is uploaded") @QueryParam("endpoint")  String endpoint)
    {
    return delegate.endpointCertificatesGet(limit,offset,alias,endpoint);
    }
    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Upload a new Certificate.", notes = "This operation can be used to upload a new certificate for an endpoint.\n", response = CertMetadataDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.\nThe Certificate added successfully.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request.\nInvalid request or validation error.\n* Failures due to existing alias or expired certificate.\n"),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server Error\n* Failed to add the Certificate due to an Internal Server Error\n") })

    public Response endpointCertificatesPost(@ApiParam(value = "The certificate that needs to be uploaded.") @Multipart(value = "certificate") InputStream certificateInputStream,
    @ApiParam(value = "The certificate that needs to be uploaded. : details") @Multipart(value = "certificate" ) Attachment certificateDetail,
    @ApiParam(value = "Alias for the certificate", required=true )@Multipart(value = "alias")  String alias,
    @ApiParam(value = "Endpoint to which the certificate should be applied.", required=true )@Multipart(value = "endpoint")  String endpoint)
    {
    return delegate.endpointCertificatesPost(certificateInputStream,certificateDetail,alias,endpoint);
    }
}

