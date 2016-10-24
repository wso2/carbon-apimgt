package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.factories.ApisApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.Error;
import org.wso2.carbon.apimgt.rest.api.store.dto.Document;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentList;
import org.wso2.carbon.apimgt.rest.api.store.dto.API;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIList;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import org.wso2.msf4j.Microservice;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
//import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Component(
    name = "org.wso2.carbon.apimgt.rest.api.store.ApisApi",
    service = Microservice.class,
    immediate = true
)
@Path("/apis")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the apis API")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T13:00:35.955+05:30")
public class ApisApi implements Microservice  {
   private final ApisApiService delegate = ApisApiServiceFactory.getApisApi();

    @GET
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Downloads a FILE type document/get the inline content or source url of a certain document. ", response = void.class, tags={ "Retrieve Document", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  File or inline content returned. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 303, message = "See Other. Source can be retrived from the URL specified at the Location header. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified. Empty body because the client has already the latest version of the requested resource. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Requested Document does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported ", response = void.class) })
    public Response apisApiIdDocumentsDocumentIdContentGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.  The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**. ",required=true) @PathParam("apiId") String apiId
,@ApiParam(value = "**Document Identifier** ",required=true) @PathParam("documentId") String documentId
,@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be    retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the  formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
)
    throws NotFoundException {
        return delegate.apisApiIdDocumentsDocumentIdContentGet(apiId,documentId,xWSO2Tenant,accept,ifNoneMatch,ifModifiedSince);
    }
    @GET
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a particular document associated with an API. ", response = Document.class, tags={ "Retrieve Document", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Document returned. ", response = Document.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = Document.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Requested Document does not exist. ", response = Document.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported ", response = Document.class) })
    public Response apisApiIdDocumentsDocumentIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.  The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**. ",required=true) @PathParam("apiId") String apiId
,@ApiParam(value = "**Document Identifier** ",required=true) @PathParam("documentId") String documentId
,@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be    retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the  formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
)
    throws NotFoundException {
        return delegate.apisApiIdDocumentsDocumentIdGet(apiId,documentId,xWSO2Tenant,accept,ifNoneMatch,ifModifiedSince);
    }
    @GET
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get a list of documents belonging to an API. ", response = DocumentList.class, tags={ "Retrieve Documents", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Document list is returned. ", response = DocumentList.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = DocumentList.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Requested API does not exist. ", response = DocumentList.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported ", response = DocumentList.class) })
    public Response apisApiIdDocumentsGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.  The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**. ",required=true) @PathParam("apiId") String apiId
,@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Starting point within the complete list of items qualified.   ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
,@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be    retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
)
    throws NotFoundException {
        return delegate.apisApiIdDocumentsGet(apiId,limit,offset,xWSO2Tenant,accept,ifNoneMatch);
    }
    @GET
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get details of an API ", response = API.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Requested API is returned ", response = API.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = API.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Requested API does not exist. ", response = API.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported ", response = API.class) })
    public Response apisApiIdGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.  The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**. ",required=true) @PathParam("apiId") String apiId
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the  formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
,@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be    retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant
)
    throws NotFoundException {
        return delegate.apisApiIdGet(apiId,accept,ifNoneMatch,ifModifiedSince,xWSO2Tenant);
    }
    @GET
    @Path("/{apiId}/swagger")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get the swagger of an API ", response = void.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  Requested swagger document of the API is returned ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.  Requested API does not exist. ", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported ", response = void.class) })
    public Response apisApiIdSwaggerGet(@ApiParam(value = "**API ID** consisting of the **UUID** of the API.  The combination of the provider of the API, name of the API and the version is also accepted as a valid API ID. Should be formatted as **provider-name-version**. ",required=true) @PathParam("apiId") String apiId
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
,@ApiParam(value = "Validator for conditional requests; based on Last Modified header of the  formerly retrieved variant of the resource. " )@HeaderParam("If-Modified-Since") String ifModifiedSince
,@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be    retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant
)
    throws NotFoundException {
        return delegate.apisApiIdSwaggerGet(apiId,accept,ifNoneMatch,ifModifiedSince,xWSO2Tenant);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retrieving APIs ", notes = "Get a list of available APIs qualifying under a given search condition. ", response = APIList.class, tags={ "Retrieve", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.  List of qualifying APIs is returned. ", response = APIList.class),
        
        @io.swagger.annotations.ApiResponse(code = 304, message = "Not Modified.  Empty body because the client has already the latest version of the requested resource. ", response = APIList.class),
        
        @io.swagger.annotations.ApiResponse(code = 406, message = "Not Acceptable.  The requested media type is not supported ", response = APIList.class) })
    public Response apisGet(@ApiParam(value = "Maximum size of resource array to return. ", defaultValue="25") @DefaultValue("25") @QueryParam("limit") Integer limit
,@ApiParam(value = "Starting point within the complete list of items qualified.   ", defaultValue="0") @DefaultValue("0") @QueryParam("offset") Integer offset
,@ApiParam(value = "For cross-tenant invocations, this is used to specify the tenant domain, where the resource need to be    retirieved from. " )@HeaderParam("X-WSO2-Tenant") String xWSO2Tenant
,@ApiParam(value = "**Search condition**.  You can search in attributes by using an **\"attribute:\"** modifier.  Eg. \"provider:wso2\" will match an API if the provider of the API is exactly \"wso2\".  Additionally you can use wildcards.  Eg. \"provider:wso2*\" will match an API if the provider of the API starts with \"wso2\".  Supported attribute modifiers are [**version, context, status, description, subcontext, doc, provider, tag **]  If no advanced attribute modifier has been specified, search will match the given query string against API Name. ") @QueryParam("query") String query
,@ApiParam(value = "Media types acceptable for the response. Default is JSON. " , defaultValue="JSON")@HeaderParam("Accept") String accept
,@ApiParam(value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the resourec. " )@HeaderParam("If-None-Match") String ifNoneMatch
)
    throws NotFoundException {
        return delegate.apisGet(limit,offset,xWSO2Tenant,query,accept,ifNoneMatch);
    }
}
