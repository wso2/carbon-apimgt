package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIExternalStoreListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevenueDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AuditReportDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeploymentStatusListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLQueryComplexityInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLSchemaTypeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MockResponsePayloadListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePathListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ApisApiServiceImpl;
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
@Path("/apis")

@Api(description = "the apis API")
@Consumes({ "application/json" })
@Produces({ "application/json" })


public class ApisApi  {

  @Context MessageContext securityContext;

ApisApiService delegate = new ApisApiServiceImpl();


    @GET
    @Path("/{apiId}/amznResourceNames")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve the ARNs of AWS Lambda functions", notes = "This operation can be use to retrieve ARNs of AWS Lambda function for a given AWS credentials. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "AWS Lambda (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested ARN List of the API is returned ", response = String.class),
        @ApiResponse(code = 404, message = "Not Found. Requested resource does not exist. ", response = ErrorDTO.class) })
    public Response apisApiIdAmznResourceNamesGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdAmznResourceNamesGet(apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/auditapi")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve the Security Audit Report of the Audit API", notes = "Retrieve the Security Audit Report of the Audit API ", response = AuditReportDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Audit",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Security Audit Report has been returned. ", response = AuditReportDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The Security Audit Report was not found. ", response = ErrorDTO.class) })
    public Response apisApiIdAuditapiGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.apisApiIdAuditapiGet(apiId, accept, securityContext);
    }

    @GET
    @Path("/{apiId}/client-certificates/{alias}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Download a certificate.", notes = "This operation can be used to download a certificate which matches the given alias. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_view", description = "View client certificates"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Alias not provided or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Certificate for the Alias not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response apisApiIdClientCertificatesAliasContentGet(@ApiParam(value = "The api identifier",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "",required=true) @PathParam("alias") String alias) throws APIManagementException{
        return delegate.apisApiIdClientCertificatesAliasContentGet(apiId, alias, securityContext);
    }

    @DELETE
    @Path("/{apiId}/client-certificates/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a certificate.", notes = "This operation can be used to delete an uploaded certificate. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_update", description = "Update and delete client certificates"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate deleted successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Alias not found or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. | Failed to delete the certificate. Certificate could not found for the given alias ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response apisApiIdClientCertificatesAliasDelete(@ApiParam(value = "The alias of the certificate that should be deleted. ",required=true) @PathParam("alias") String alias, @ApiParam(value = "The api identifier",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdClientCertificatesAliasDelete(alias, apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/client-certificates/{alias}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the certificate information.", notes = "This operation can be used to get the information about a certificate. ", response = CertificateInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_view", description = "View client certificates"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ", response = CertificateInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Alias not found or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Alias not found ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response apisApiIdClientCertificatesAliasGet(@ApiParam(value = "",required=true) @PathParam("alias") String alias, @ApiParam(value = "The api identifier",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdClientCertificatesAliasGet(alias, apiId, securityContext);
    }

    @PUT
    @Path("/{apiId}/client-certificates/{alias}")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a certificate.", notes = "This operation can be used to update an uploaded certificate. ", response = ClientCertMetadataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_update", description = "Update and delete client certificates"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate updated successfully. ", response = ClientCertMetadataDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Failure due to not providing alias. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Updating certificate failed. Alias not found or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response apisApiIdClientCertificatesAliasPut(@ApiParam(value = "Alias for the certificate",required=true) @PathParam("alias") String alias, @ApiParam(value = "The api identifier",required=true) @PathParam("apiId") String apiId,  @Multipart(value = "certificate", required = false) InputStream certificateInputStream, @Multipart(value = "certificate" , required = false) Attachment certificateDetail, @Multipart(value = "tier", required = false)  String tier) throws APIManagementException{
        return delegate.apisApiIdClientCertificatesAliasPut(alias, apiId, certificateInputStream, certificateDetail, tier, securityContext);
    }

    @GET
    @Path("/{apiId}/client-certificates")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/ Search uploaded Client Certificates.", notes = "This operation can be used to retrieve and search the uploaded client certificates. ", response = ClientCertificatesDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:client_certificates_view", description = "View client certificates"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the list of matching certificate information in the body. ", response = ClientCertificatesDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Failure due to not providing alias or server is not configured to support mutual SSL authentication. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response apisApiIdClientCertificatesGet(@ApiParam(value = "UUID of the API",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="80") @DefaultValue("80") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "Alias for the client certificate")  @QueryParam("alias") String alias) throws APIManagementException{
        return delegate.apisApiIdClientCertificatesGet(apiId, limit, offset, alias, securityContext);
    }

    @POST
    @Path("/{apiId}/client-certificates")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Upload a new certificate.", notes = "This operation can be used to upload a new certificate for an endpoint. ", response = ClientCertMetadataDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:client_certificates_add", description = "Add client certificates")
        })
    }, tags={ "Client Certificates",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. The Certificate added successfully. ", response = ClientCertMetadataDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Failures due to existing alias or expired certificate. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error Failed to add the Certificate due to an Internal Server Error ", response = ErrorDTO.class) })
    public Response apisApiIdClientCertificatesPost( @Multipart(value = "certificate") InputStream certificateInputStream, @Multipart(value = "certificate" ) Attachment certificateDetail, @Multipart(value = "alias")  String alias, @ApiParam(value = "apiId to which the certificate should be applied.",required=true) @PathParam("apiId") String apiId, @Multipart(value = "tier")  String tier) throws APIManagementException{
        return delegate.apisApiIdClientCertificatesPost(certificateInputStream, certificateDetail, alias, apiId, tier, securityContext);
    }

    @DELETE
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an API", notes = "This operation can be used to delete an existing API proving the Id of the API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_delete", description = "Delete API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. The request could not be completed due to a conflict with the current state of the target resource. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdDelete(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdDelete(apiId, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/octet-stream" })
    @ApiOperation(value = "Get the content of an API document", notes = "This operation can be used to retrive the content of an API's document.  The document can be of 3 types. In each cases responses are different.  1. **Inline type**:    The content of the document will be retrieved in `text/plain` content type     _Sample cURL_ : `curl -k -H \"Authorization:Bearer 579f0af4-37be-35c7-81a4-f1f1e9ee7c51\" -F inlineContent=@\"docs.txt\" -X POST \"https://localhost:9443/api/am/publisher/v1/apis/995a4972-3178-4b17-a374-756e0e19127c/documents/43c2bcce-60e7-405f-bc36-e39c0c5e189e/content` 2. **FILE type**:    The file will be downloaded with the related content type (eg. `application/pdf`) 3. **URL type**:     The client will recieve the URL of the document as the Location header with the response with - `303 See Other` ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. File or inline content returned. ", response = Void.class),
        @ApiResponse(code = 303, message = "See Other. Source can be retrived from the URL specified at the Location header. ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Document does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsDocumentIdContentGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsDocumentIdContentGet(apiId, documentId, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Upload the content of an API document", notes = "Thid operation can be used to upload a file or add inline content to an API document.  **IMPORTANT:** * Either **file** or **inlineContent** form data parameters should be specified at one time. * Document's source type should be **FILE** in order to upload a file to the document using **file** parameter. * Document's source type should be **INLINE** in order to add inline content to the document using **inlineContent** parameter. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:document_create", description = "Create API documents"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document updated ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsDocumentIdContentPost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "inlineContent", required = false)  String inlineContent, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsDocumentIdContentPost(apiId, documentId, fileInputStream, fileDetail, inlineContent, ifMatch, securityContext);
    }

    @DELETE
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete a document of an API", notes = "This operation can be used to delete a document associated with an API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:document_manage", description = "Update and delete API documents"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsDocumentIdDelete(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsDocumentIdDelete(apiId, documentId, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a document of an API", notes = "This operation can be used to retrieve a particular document's metadata associated with an API. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document returned. ", response = DocumentDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Document does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsDocumentIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsDocumentIdGet(apiId, documentId, ifNoneMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update a document of an API", notes = "This operation can be used to update metadata of an API's document. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:document_manage", description = "Update and delete API documents"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document updated ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsDocumentIdPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document Identifier ",required=true) @PathParam("documentId") String documentId, @ApiParam(value = "Document object that needs to be added " ,required=true) DocumentDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsDocumentIdPut(apiId, documentId, body, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get a list of documents of an API", notes = "This operation can be used to retrieve a list of documents belonging to an API by providing the id of the API. ", response = DocumentListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Document list is returned. ", response = DocumentListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdDocumentsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="80") @DefaultValue("80") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsGet(apiId, limit, offset, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a new document to an API", notes = "This operation can be used to add a new documentation to an API. This operation only adds the metadata of a document. To add the actual content we need to use **Upload the content of an API document ** API once we obtain a document Id by this operation. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:document_create", description = "Create API documents"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created Document object as entity in the body. Location header contains URL of newly added document. ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported media type. The entity of the request was in a not supported format. ", response = Void.class) })
    public Response apisApiIdDocumentsPost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Document object that needs to be added " ,required=true) DocumentDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdDocumentsPost(apiId, body, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of an API", notes = "Using this operation, you can retrieve complete details of a single API. You need to provide the Id of the API to retrive it. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_product_import_export", description = "Import and export API Products related operations"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested API is returned ", response = APIDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdGet(apiId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/graphql-policies/complexity")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the complexity related details of an API", notes = "This operation can be used to retrieve complexity related details belonging to an API by providing the API id. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "GraphQL Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested complexity details returned. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not contain any complexity details. ", response = Void.class) })
    public Response apisApiIdGraphqlPoliciesComplexityGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdGraphqlPoliciesComplexityGet(apiId, securityContext);
    }

    @PUT
    @Path("/{apiId}/graphql-policies/complexity")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update complexity related details of an API", notes = "This operation can be used to update complexity details belonging to an API by providing the id of the API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "GraphQL Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Created. Complexity details created successfully. ", response = Void.class),
        @ApiResponse(code = 404, message = "Bad Request. Complexity detail addition failed. ", response = Void.class) })
    public Response apisApiIdGraphqlPoliciesComplexityPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Role-depth mapping that needs to be added " ,required=true) GraphQLQueryComplexityInfoDTO body) throws APIManagementException{
        return delegate.apisApiIdGraphqlPoliciesComplexityPut(apiId, body, securityContext);
    }

    @GET
    @Path("/{apiId}/graphql-policies/complexity/types")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve types and fields of a GraphQL Schema", notes = "This operation can be used to retrieve all types and fields of the GraphQL Schema by providing the API id. ", response = GraphQLSchemaTypeListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "GraphQL Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Types and fields returned successfully. ", response = GraphQLSchemaTypeListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Retrieving types and fields failed. ", response = Void.class) })
    public Response apisApiIdGraphqlPoliciesComplexityTypesGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdGraphqlPoliciesComplexityTypesGet(apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/graphql-schema")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the Schema of a GraphQL API", notes = "This operation can be used to retrieve the Schema definition of a GraphQL API. ", response = GraphQLSchemaDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "GraphQL Schema (Individual)",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested GraphQL Schema DTO object belongs to the API ", response = GraphQLSchemaDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdGraphqlSchemaGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdGraphqlSchemaGet(apiId, accept, ifNoneMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/graphql-schema")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add a Schema to a GraphQL API", notes = "This operation can be used to add a GraphQL Schema definition to an existing GraphQL API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "GraphQL Schema",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated schema definition ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdGraphqlSchemaPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @Multipart(value = "schemaDefinition")  String schemaDefinition, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdGraphqlSchemaPut(apiId, schemaDefinition, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/lifecycle-history")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Lifecycle state change history of the API.", notes = "This operation can be used to retrieve Lifecycle state change history of the API. ", response = LifecycleHistoryDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "API Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle state change history returned successfully. ", response = LifecycleHistoryDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class) })
    public Response apisApiIdLifecycleHistoryGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdLifecycleHistoryGet(apiId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/lifecycle-state")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Lifecycle state data of the API.", notes = "This operation can be used to retrieve Lifecycle state data of the API. ", response = LifecycleStateDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "API Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle state data returned successfully. ", response = LifecycleStateDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdLifecycleStateGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdLifecycleStateGet(apiId, ifNoneMatch, securityContext);
    }

    @DELETE
    @Path("/{apiId}/lifecycle-state/pending-tasks")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete pending lifecycle state change tasks.", notes = "This operation can be used to remove pending lifecycle state change requests that are in pending state ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "API Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle state change pending task removed successfully. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdLifecycleStatePendingTasksDelete(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdLifecycleStatePendingTasksDelete(apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/mediation-policies")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get all mediation policies of an API ", notes = "This operation provides you a list of available mediation policies of an API. ", response = MediationListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Mediation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying APIs is returned. ", response = MediationListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdMediationPoliciesGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="80") @DefaultValue("80") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset,  @ApiParam(value = "-Not supported yet-")  @QueryParam("query") String query, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdMediationPoliciesGet(apiId, limit, offset, query, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/mediation-policies/{mediationPolicyId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Download an API specific mediation policy", notes = "This operation can be used to download a particular API specific mediation policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Mediation Policy",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Mediation policy returned. ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested file does not exist. ", response = ErrorDTO.class) })
    public Response apisApiIdMediationPoliciesMediationPolicyIdContentGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Mediation policy Id ",required=true) @PathParam("mediationPolicyId") String mediationPolicyId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdMediationPoliciesMediationPolicyIdContentGet(apiId, mediationPolicyId, ifNoneMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/mediation-policies/{mediationPolicyId}/content")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an API specific mediation policy", notes = "This operation can be used to update an existing mediation policy of an API. ", response = MediationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies")
        })
    }, tags={ "API Mediation Policy",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated API object ", response = MediationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdMediationPoliciesMediationPolicyIdContentPut(@Multipart(value = "type")  String type, @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Mediation policy Id ",required=true) @PathParam("mediationPolicyId") String mediationPolicyId,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "inlineContent", required = false)  String inlineContent, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdMediationPoliciesMediationPolicyIdContentPut(type, apiId, mediationPolicyId, fileInputStream, fileDetail, inlineContent, ifMatch, securityContext);
    }

    @DELETE
    @Path("/{apiId}/mediation-policies/{mediationPolicyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Delete an API specific mediation policy", notes = "This operation can be used to delete an existing API specific mediation policy providing the Id of the API and the Id of the mediation policy. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:mediation_policy_manage", description = "Update and delete mediation policies")
        })
    }, tags={ "API Mediation Policy",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Resource successfully deleted. ", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Resource to be deleted does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdMediationPoliciesMediationPolicyIdDelete(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Mediation policy Id ",required=true) @PathParam("mediationPolicyId") String mediationPolicyId, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdMediationPoliciesMediationPolicyIdDelete(apiId, mediationPolicyId, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/mediation-policies/{mediationPolicyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get an API specific mediation policy", notes = "This operation can be used to retrieve a particular API specific mediation policy. ", response = MediationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:mediation_policy_view", description = "View mediation policies"),
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Mediation Policy",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Mediation policy returned. ", response = MediationDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Document does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdMediationPoliciesMediationPolicyIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Mediation policy Id ",required=true) @PathParam("mediationPolicyId") String mediationPolicyId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdMediationPoliciesMediationPolicyIdGet(apiId, mediationPolicyId, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/{apiId}/mediation-policies")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Add an API specific mediation policy", notes = "This operation can be used to add an API specifc mediation policy. ", response = MediationDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API"),
            @AuthorizationScope(scope = "apim:mediation_policy_create", description = "Create mediation policies")
        })
    }, tags={ "API Mediation Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. mediation policy uploaded ", response = MediationDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdMediationPoliciesPost(@Multipart(value = "type")  String type, @ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @Multipart(value = "mediationPolicyFile", required = false) InputStream mediationPolicyFileInputStream, @Multipart(value = "mediationPolicyFile" , required = false) Attachment mediationPolicyFileDetail, @Multipart(value = "inlineContent", required = false)  String inlineContent, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdMediationPoliciesPost(type, apiId, mediationPolicyFileInputStream, mediationPolicyFileDetail, inlineContent, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/monetization")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get monetization status for each tier in a given API", notes = "This operation can be used to get monetization status for each tier in a given API ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "API Monetization",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Monetization status for each tier returned successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested resource does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdMonetizationGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdMonetizationGet(apiId, securityContext);
    }

    @POST
    @Path("/{apiId}/monetize")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Configure monetization for a given API", notes = "This operation can be used to configure monetization for a given API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "API Monetization",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "OK. Monetization status changed successfully. ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested resource does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdMonetizePost(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Monetization data object " ,required=true) APIMonetizationInfoDTO body) throws APIManagementException{
        return delegate.apisApiIdMonetizePost(apiId, body, securityContext);
    }

    @PUT
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update an API", notes = "This operation can be used to update an existing API. But the properties `name`, `version`, `context`, `provider`, `state` will not be changed by this operation. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated API object ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. The request could not be completed due to a conflict with the current state of the target resource. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "API object that needs to be added " ,required=true) APIDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdPut(apiId, body, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/resource-paths")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get Resource Paths of an API", notes = "This operation can be used to retrieve resource paths defined for a specific api. ", response = ResourcePathListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. ResourcePaths returned. ", response = ResourcePathListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Document does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdResourcePathsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="80") @DefaultValue("80") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdResourcePathsGet(apiId, limit, offset, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/resource-policies")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the resource policy (inflow/outflow) definitions", notes = "This operation can be used to retrieve conversion policy resource definitions of an API. ", response = ResourcePolicyListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Resource Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of resource policy definitions of the API is returned ", response = ResourcePolicyListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdResourcePoliciesGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @NotNull @ApiParam(value = "sequence type of the resource policy resource definition",required=true)  @QueryParam("sequenceType") String sequenceType,  @ApiParam(value = "Resource path of the resource policy definition")  @QueryParam("resourcePath") String resourcePath,  @ApiParam(value = "HTTP verb of the resource path of the resource policy definition")  @QueryParam("verb") String verb, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdResourcePoliciesGet(apiId, sequenceType, resourcePath, verb, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/resource-policies/{resourcePolicyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the resource policy (inflow/outflow) definition for a given resource identifier.", notes = "This operation can be used to retrieve conversion policy resource definitions of an API given the resource identifier. ", response = ResourcePolicyInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "API Resource Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested resource policy definition of the API is returned for the given resource identifier. ", response = ResourcePolicyInfoDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdResourcePoliciesResourcePolicyIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "registry resource Id ",required=true) @PathParam("resourcePolicyId") String resourcePolicyId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdResourcePoliciesResourcePolicyIdGet(apiId, resourcePolicyId, ifNoneMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/resource-policies/{resourcePolicyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update the resource policy(inflow/outflow) definition for the given resource identifier", notes = "This operation can be used to update the resource policy(inflow/outflow) definition for the given resource identifier of an existing API. resource policy definition to be updated is passed as a body parameter `content`. ", response = ResourcePolicyInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "API Resource Policies",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated the resource policy definition ", response = ResourcePolicyInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdResourcePoliciesResourcePolicyIdPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "registry resource Id ",required=true) @PathParam("resourcePolicyId") String resourcePolicyId, @ApiParam(value = "Content of the resource policy definition that needs to be updated" ,required=true) ResourcePolicyInfoDTO body, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdResourcePoliciesResourcePolicyIdPut(apiId, resourcePolicyId, body, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/revenue")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get total revenue details of a given monetized API with meterd billing.", notes = "This operation can be used to get details of total revenue details of a given monetized API with meterd billing. ", response = APIRevenueDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "API Monetization",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Details of a total revenue returned. ", response = APIRevenueDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Subscription does not exist. ", response = ErrorDTO.class) })
    public Response apisApiIdRevenueGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.apisApiIdRevenueGet(apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/subscription-policies")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get details of the subscription throttling policies of an API ", notes = "This operation can be used to retrieve details of the subscription throttling policy of an API by specifying the API Id.  `X-WSO2-Tenant` header can be used to retrive API subscription throttling policies that belongs to a different tenant domain. If not specified super tenant will be used. If Authorization header is present in the request, the user's tenant associated with the access token will be used. ", response = ThrottlingPolicyDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Throttling Policy returned ", response = ThrottlingPolicyDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Throttling Policy does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported. ", response = ErrorDTO.class) })
    public Response apisApiIdSubscriptionPoliciesGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdSubscriptionPoliciesGet(apiId, xWSO2Tenant, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/swagger")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get swagger definition", notes = "This operation can be used to retrieve the swagger definition of an API. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the API is returned ", response = String.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdSwaggerGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdSwaggerGet(apiId, ifNoneMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/swagger")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update swagger definition", notes = "This operation can be used to update the swagger definition of an existing API. Swagger definition to be updated is passed as a form data parameter `apiDefinition`. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated Swagger definition ", response = String.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisApiIdSwaggerPut(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @Multipart(value = "apiDefinition", required = false)  String apiDefinition, @Multipart(value = "url", required = false)  String url,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisApiIdSwaggerPut(apiId, apiDefinition, url, fileInputStream, fileDetail, ifMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/thumbnail")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get thumbnail image", notes = "This operation can be used to download a thumbnail image of an API. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Thumbnail image returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested Document does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisApiIdThumbnailGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.apisApiIdThumbnailGet(apiId, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/change-lifecycle")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Change API Status", notes = "This operation is used to change the lifecycle of an API. Eg: Publish an API which is in `CREATED` state. In order to change the lifecycle, we need to provide the lifecycle `action` as a query parameter.  For example, to Publish an API, `action` should be `Publish`. Note that the `Re-publish` action is available only after calling `Block`.  Some actions supports providing additional paramters which should be provided as `lifecycleChecklist` parameter. Please see parameters table for more information. ", response = WorkflowResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "API Lifecycle",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Lifecycle changed successfully. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 202, message = "Accepted. The request has been accepted. ", response = WorkflowResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 409, message = "Conflict. Pending workflow task exists. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response apisChangeLifecyclePost( @NotNull @ApiParam(value = "The action to demote or promote the state of the API.  Supported actions are [ **Publish, Deploy as a Prototype, Demote to Created, Demote to Prototyped, Block, Deprecate, Re-Publish, Retire **] ",required=true, allowableValues="Publish, Deploy as a Prototype, Demote to Created, Demote to Prototyped, Block, Deprecate, Re-Publish, Retire")  @QueryParam("action") String action,  @NotNull @ApiParam(value = "**API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**. ",required=true)  @QueryParam("apiId") String apiId,  @ApiParam(value = " Supported checklist items are as follows. 1. **Deprecate Old Versions**: Setting this to true will deprecate older versions of a particular API when it is promoted to Published state from Created state. 2. **Require Re-Subscription**: If you set this to true, users need to re subscribe to the API although they may have subscribed to an older version.  You can specify additional checklist items by using an **\"attribute:\"** modifier.  Eg: \"Deprecate Old Versions:true\" will deprecate older versions of a particular API when it is promoted to Published state from Created state. Multiple checklist items can be given in \"attribute1:true, attribute2:false\" format.  **Sample CURL :**  curl -k -H \"Authorization: Bearer ae4eae22-3f65-387b-a171-d37eaa366fa8\" -X POST \"https://localhost:9443/api/am/publisher/v1/apis/change-lifecycle?apiId=890a4f4d-09eb-4877-a323-57f6ce2ed79b&action=Publish&lifecycleChecklist=Deprecate Old Versions:true,Require Re-Subscription:true\" ")  @QueryParam("lifecycleChecklist") String lifecycleChecklist, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisChangeLifecyclePost(action, apiId, lifecycleChecklist, ifMatch, securityContext);
    }

    @POST
    @Path("/copy-api")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new API version", notes = "This operation can be used to create a new version of an existing API. The new version is specified as `newVersion` query parameter. New API will be in `CREATED` state. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created API as entity in the body. Location header contains URL of newly created API. ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. API to copy does not exist. ", response = ErrorDTO.class) })
    public Response apisCopyApiPost( @NotNull @ApiParam(value = "Version of the new API.",required=true)  @QueryParam("newVersion") String newVersion,  @NotNull @ApiParam(value = "**API ID** consisting of the **UUID** of the API. The combination of the provider of the API, name of the API and the version is also accepted as a valid API I. Should be formatted as **provider-name-version**. ",required=true)  @QueryParam("apiId") String apiId,  @ApiParam(value = "Specifies whether new API should be added as default version.", defaultValue="false") @DefaultValue("false") @QueryParam("defaultVersion") Boolean defaultVersion) throws APIManagementException{
        return delegate.apisCopyApiPost(newVersion, apiId, defaultVersion, securityContext);
    }

    @GET
    @Path("/export")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/zip" })
    @ApiOperation(value = "Export an API", notes = "This operation can be used to export the details of a particular API as a zip file. ", response = File.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Import Export",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Export Successful. ", response = File.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error. Error in exporting API. ", response = ErrorDTO.class) })
    public Response apisExportGet( @ApiParam(value = "UUID of the API")  @QueryParam("apiId") String apiId,  @ApiParam(value = "API Name ")  @QueryParam("name") String name,  @ApiParam(value = "Version of the API ")  @QueryParam("version") String version,  @ApiParam(value = "Provider name of the API ")  @QueryParam("providerName") String providerName,  @ApiParam(value = "Format of output documents. Can be YAML or JSON. ", allowableValues="JSON, YAML")  @QueryParam("format") String format,  @ApiParam(value = "Preserve API Status on export ")  @QueryParam("preserveStatus") Boolean preserveStatus) throws APIManagementException{
        return delegate.apisExportGet(apiId, name, version, providerName, format, preserveStatus, securityContext);
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve/Search APIs ", notes = "This operation provides you a list of available APIs qualifying under a given search condition.  Each retrieved API is represented with a minimal amount of attributes. If you want to get complete details of an API, you need to use **Get details of an API** operation. ", response = APIListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API"),
            @AuthorizationScope(scope = "apim:api_import_export", description = "Import and export APIs related operations")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. List of qualifying APIs is returned. ", response = APIListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response apisGet( @ApiParam(value = "Maximum size of resource array to return. ", defaultValue="80") @DefaultValue("80") @QueryParam("limit") Integer limit,  @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset, @ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be   retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant,  @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"<attribute>:\"** modifier.  Eg. \"provider:wso2\" will match an API if the provider of the API contains \"wso2\". \"provider:\"wso2\"\" will match an API if the provider of the API is exactly \"wso2\". \"status:PUBLISHED\" will match an API if the API is in PUBLISHED state. \"label:external\" will match an API if it contains a Microgateway label called \"external\".  Also you can use combined modifiers Eg. name:pizzashack version:v1 will match an API if the name of the API is pizzashack and version is v1.  Supported attribute modifiers are [**version, context, name, status, description, subcontext, doc, provider, label**]  If no advanced attribute modifier has been specified,  the API names containing the search term will be returned as a result.  Please note that you need to use encoded URL (URL encoding) if you are using a client which does not support URL encoding (such as curl) ")  @QueryParam("query") String query, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch,  @ApiParam(value = "Defines whether the returned response should contain full details of API ")  @QueryParam("expand") Boolean expand, @ApiParam(value = "Media types acceptable for the response. Default is application/json. " , defaultValue="application/json")@HeaderParam("Accept") String accept) throws APIManagementException{
        return delegate.apisGet(limit, offset, xWSO2Tenant, query, ifNoneMatch, expand, accept, securityContext);
    }

    @POST
    @Path("/import-graphql-schema")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import API Definition", notes = "This operation can be used to create api from api definition.APIMgtDAOTest  API definition is GraphQL Schema ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response apisImportGraphqlSchemaPost(@Multipart(value = "type", required = false)  String type,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "additionalProperties", required = false)  String additionalProperties, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.apisImportGraphqlSchemaPost(type, fileInputStream, fileDetail, additionalProperties, ifMatch, securityContext);
    }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Create a new API", notes = "This operation can be used to create a new API specifying the details of the API in the payload. The new API will be in `CREATED` state.  There is a special capability for a user who has `APIM Admin` permission such that he can create APIs on behalf of other users. For that he can to specify `\"provider\" : \"some_other_user\"` in the payload so that the API's creator will be shown as `some_other_user` in the UI. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response apisPost(@ApiParam(value = "API object that needs to be added " ,required=true) APIDTO body,  @ApiParam(value = "Open api version", allowableValues="V2, V3")  @QueryParam("openAPIVersion") String openAPIVersion) throws APIManagementException{
        return delegate.apisPost(body, openAPIVersion, securityContext);
    }

    @POST
    @Path("/validate-graphql-schema")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Validate GraphQL API definition and retrieve a summary", notes = "This operation can be used to validate a graphQL definition and retrieve a summary. ", response = GraphQLValidationResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API definition validation information is returned ", response = GraphQLValidationResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = ErrorDTO.class) })
    public Response apisValidateGraphqlSchemaPost( @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail) throws APIManagementException{
        return delegate.apisValidateGraphqlSchemaPost(fileInputStream, fileDetail, securityContext);
    }

    @GET
    @Path("/{apiId}/deployments")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieve deployment status details", notes = "This operation can be used to retrieve the status of deployments in cloud clusters.  With that you can get the status of the deployed APIs in cloud environments. ", response = DeploymentStatusListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "DeploymentStatus",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with the list of deployment environments information in the body. ", response = DeploymentStatusListDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal Server Error ", response = ErrorDTO.class) })
    public Response deploymentsGetStatus(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.deploymentsGetStatus(apiId, securityContext);
    }

    @POST
    @Path("/{apiId}/generate-mock-scripts")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Generate mock response payloads", notes = "This operation can be used to generate mock responses from examples of swagger definition of an API. ", response = String.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the API is returned with example responses ", response = String.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response generateMockScripts(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.generateMockScripts(apiId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/external-stores")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the list of external stores which an API is published to", notes = "This operation can be used to retrieve a list of external stores which an API is published to by providing the id of the API. ", response = APIExternalStoreListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "External Stores",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. External Store list is returned. ", response = APIExternalStoreListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while getting external stores of the API.", response = ErrorDTO.class) })
    public Response getAllPublishedExternalStoresByAPI(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getAllPublishedExternalStoresByAPI(apiId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/generated-mock-scripts")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Generate mock response payloads", notes = "This operation can be used to generate mock responses from examples of swagger definition of an API. ", response = MockResponsePayloadListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested swagger document of the API is returned with example responses ", response = MockResponsePayloadListDTO.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response getGeneratedMockScriptsOfAPI(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getGeneratedMockScriptsOfAPI(apiId, ifNoneMatch, securityContext);
    }

    @GET
    @Path("/{apiId}/wsdl-info")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get WSDL definition", notes = "This operation can be used to retrieve the WSDL meta information of an API. It states whether the API is a SOAP API. If the API is a SOAP API, it states whether it has a single WSDL or a WSDL archive. ", response = WSDLInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested WSDL meta information of the API is returned ", response = WSDLInfoDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist or the API is not a SOAP API. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response getWSDLInfoOfAPI(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId) throws APIManagementException{
        return delegate.getWSDLInfoOfAPI(apiId, securityContext);
    }

    @GET
    @Path("/{apiId}/wsdl")
    @Consumes({ "application/json" })
    @Produces({ "application/json", "application/wsdl", "application/zip" })
    @ApiOperation(value = "Get WSDL definition", notes = "This operation can be used to retrieve the WSDL definition of an API. It can be either a single WSDL file or a WSDL archive.  The type of the WSDL of the API is indicated at the \"wsdlInfo\" element of the API payload definition. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_view", description = "View API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Requested WSDL document of the API is returned ", response = Void.class),
        @ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource (Will be supported in future). ", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 406, message = "Not Acceptable. The requested media type is not supported ", response = ErrorDTO.class) })
    public Response getWSDLOfAPI(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.getWSDLOfAPI(apiId, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/import-openapi")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import an OpenAPI Definition", notes = "This operation can be used to create an API from an OpenAPI definition. Provide either `url` or `file` to specify the definition.  Specify additionalProperties with **at least** API's name, version, context and endpointConfig. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response importOpenAPIDefinition( @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "url", required = false)  String url, @Multipart(value = "additionalProperties", required = false)  String additionalProperties) throws APIManagementException{
        return delegate.importOpenAPIDefinition(fileInputStream, fileDetail, url, additionalProperties, securityContext);
    }

    @POST
    @Path("/import-wsdl")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Import a WSDL Definition", notes = "This operation can be used to create an API using a WSDL definition. Provide either `url` or `file` to specify the definition.  WSDL can be speficied as a single file or a ZIP archive with WSDLs and reference XSDs etc. Specify additionalProperties with **at least** API's name, version, context and endpointConfig. ", response = APIDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 201, message = "Created. Successful response with the newly created object as entity in the body. Location header contains URL of newly created entity. ", response = APIDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 415, message = "Unsupported Media Type. The entity of the request was in a not supported format. ", response = ErrorDTO.class) })
    public Response importWSDLDefinition( @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "url", required = false)  String url, @Multipart(value = "additionalProperties", required = false)  String additionalProperties, @Multipart(value = "implementationType", required = false)  String implementationType) throws APIManagementException{
        return delegate.importWSDLDefinition(fileInputStream, fileDetail, url, additionalProperties, implementationType, securityContext);
    }

    @POST
    @Path("/{apiId}/publish-to-external-stores")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Publish an API to external stores", notes = "This operation can be used to publish an API to a list of external stores. ", response = APIExternalStoreListDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API")
        })
    }, tags={ "External Stores",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API was successfully published to all the selected external stores. ", response = APIExternalStoreListDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Request API resource or external store Ids not found. ", response = ErrorDTO.class),
        @ApiResponse(code = 500, message = "Internal server error while publishing to external stores", response = ErrorDTO.class) })
    public Response publishAPIToExternalStores(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @NotNull @ApiParam(value = "External Store Ids of stores which the API needs to be published or updated.",required=true)  @QueryParam("externalStoreIds") String externalStoreIds, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.publishAPIToExternalStores(apiId, externalStoreIds, ifMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/thumbnail")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Upload a thumbnail image", notes = "This operation can be used to upload a thumbnail image of an API. The thumbnail to be uploaded should be given as a form data parameter `file`. ", response = FileInfoDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_publish", description = "Publish API"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Image updated ", response = FileInfoDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response updateAPIThumbnail(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @Multipart(value = "file") InputStream fileInputStream, @Multipart(value = "file" ) Attachment fileDetail, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.updateAPIThumbnail(apiId, fileInputStream, fileDetail, ifMatch, securityContext);
    }

    @PUT
    @Path("/{apiId}/wsdl")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Update WSDL definition", notes = "This operation can be used to update the WSDL definition of an existing API. WSDL to be updated can be passed as either \"url\" or \"file\". Only one of \"url\" or \"file\" can be used at the same time. \"file\" can be specified as a single WSDL file or as a zip file which has a WSDL and its dependencies (eg: XSDs) ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "APIs",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response with updated WSDL definition ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 403, message = "Forbidden. The request must be conditional but no condition has been specified. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. The resource to be updated does not exist. ", response = ErrorDTO.class),
        @ApiResponse(code = 412, message = "Precondition Failed. The request has not been performed because one of the preconditions is not met. ", response = ErrorDTO.class) })
    public Response updateWSDLOfAPI(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail, @Multipart(value = "url", required = false)  String url, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.updateWSDLOfAPI(apiId, fileInputStream, fileDetail, url, ifMatch, securityContext);
    }

    @POST
    @Path("/validate")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Check given API attibute name is already exist.", notes = "Using this operation, you can check a given API context is already used. You need to provide the context name you want to check. ", response = Void.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API definition validation information is returned ", response = Void.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = ErrorDTO.class) })
    public Response validateAPI( @NotNull @ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"<attribute>:\"** modifier.  Eg. \"name:wso2\" will match an API if the provider of the API is exactly \"wso2\".  Supported attribute modifiers are [** version, context, name **]  If no advanced attribute modifier has been specified, search will match the given query string against API Name. ",required=true)  @QueryParam("query") String query, @ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resource. " )@HeaderParam("If-None-Match") String ifNoneMatch) throws APIManagementException{
        return delegate.validateAPI(query, ifNoneMatch, securityContext);
    }

    @POST
    @Path("/{apiId}/documents/validate")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Check whether a document with the provided name exist.", notes = "This operation can be used to verify the document name exists or not. ", response = DocumentDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:document_create", description = "Create API documents"),
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "API Documents",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. Successful response if the api name exist. ", response = DocumentDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Requested document not found. ", response = ErrorDTO.class) })
    public Response validateDocument(@ApiParam(value = "**API ID** consisting of the **UUID** of the API. ",required=true) @PathParam("apiId") String apiId,  @NotNull @ApiParam(value = "The name of the document which needs to be checked for the existance. ",required=true)  @QueryParam("name") String name, @ApiParam(value = "Validator for conditional requests; based on ETag. " )@HeaderParam("If-Match") String ifMatch) throws APIManagementException{
        return delegate.validateDocument(apiId, name, ifMatch, securityContext);
    }

    @POST
    @Path("/validate-endpoint")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Check whether given endpoint url is valid", notes = "Using this operation, it is possible check whether the given API endpoint url is a valid url ", response = ApiEndpointValidationResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API definition validation information is returned ", response = ApiEndpointValidationResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = ErrorDTO.class) })
    public Response validateEndpoint( @NotNull @ApiParam(value = "API endpoint url",required=true)  @QueryParam("endpointUrl") String endpointUrl,  @ApiParam(value = "")  @QueryParam("apiId") String apiId) throws APIManagementException{
        return delegate.validateEndpoint(endpointUrl, apiId, securityContext);
    }

    @POST
    @Path("/validate-openapi")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Validate an OpenAPI Definition", notes = "This operation can be used to validate an OpenAPI definition and retrieve a summary. Provide either `url` or `file` to specify the definition. ", response = OpenAPIDefinitionValidationResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Validation",  })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API definition validation information is returned ", response = OpenAPIDefinitionValidationResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = ErrorDTO.class) })
    public Response validateOpenAPIDefinition(@Multipart(value = "url", required = false)  String url,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail,  @ApiParam(value = "Specify whether to return the full content of the OpenAPI definition in the response. This is only applicable when using url based validation ", defaultValue="false") @DefaultValue("false") @QueryParam("returnContent") Boolean returnContent) throws APIManagementException{
        return delegate.validateOpenAPIDefinition(url, fileInputStream, fileDetail, returnContent, securityContext);
    }

    @POST
    @Path("/validate-wsdl")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Validate a WSDL Definition", notes = "This operation can be used to validate a WSDL definition and retrieve a summary. Provide either `url` or `file` to specify the definition. ", response = WSDLValidationResponseDTO.class, authorizations = {
        @Authorization(value = "OAuth2Security", scopes = {
            @AuthorizationScope(scope = "apim:api_create", description = "Create API")
        })
    }, tags={ "Validation" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK. API definition validation information is returned ", response = WSDLValidationResponseDTO.class),
        @ApiResponse(code = 400, message = "Bad Request. Invalid request or validation error. ", response = ErrorDTO.class),
        @ApiResponse(code = 404, message = "Not Found. Workflow for the given reference in not found. ", response = ErrorDTO.class) })
    public Response validateWSDLDefinition(@Multipart(value = "url", required = false)  String url,  @Multipart(value = "file", required = false) InputStream fileInputStream, @Multipart(value = "file" , required = false) Attachment fileDetail) throws APIManagementException{
        return delegate.validateWSDLDefinition(url, fileInputStream, fileDetail, securityContext);
    }
}
