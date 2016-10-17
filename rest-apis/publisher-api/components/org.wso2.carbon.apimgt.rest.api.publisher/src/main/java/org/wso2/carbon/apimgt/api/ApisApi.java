package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.factories.ApisApiServiceFactory;
import org.wso2.carbon.apimgt.model.API;
import org.wso2.carbon.apimgt.model.Document;
import org.wso2.msf4j.MicroservicesRunner;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/apis")
@Consumes({ "application/json" })
@Produces({ "application/json" })

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class ApisApi  {

   private final ApisApiService delegate = ApisApiServiceFactory.getApisApi();

   public static void main(String[] args) {
       new MicroservicesRunner().deploy(new ApisApi()).start();
    }

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisGet(@QueryParam("limit") Integer limit




,@QueryParam("offset") Integer offset




,@QueryParam("query") String query




,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


) {
        return delegate.apisGet(limit,offset,query,accept,ifNoneMatch);
    }
    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisPost(


@BodyParam("body") API body

,

@HeaderParam("Content-Type") String contentType


) {
        return delegate.apisPost(body,contentType);
    }
    @POST
    @Path("/change-lifecycle")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisChangeLifecyclePost(@QueryParam("action") String action




,@QueryParam("apiId") String apiId




,@QueryParam("lifecycleChecklist") String lifecycleChecklist




,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.apisChangeLifecyclePost(action,apiId,lifecycleChecklist,ifMatch,ifUnmodifiedSince);
    }
    @POST
    @Path("/copy-api")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisCopyApiPost(@QueryParam("newVersion") String newVersion




,@QueryParam("apiId") String apiId




) {
        return delegate.apisCopyApiPost(newVersion,apiId);
    }
    @GET
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdGet(
@PathParam("apiId") String apiId



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.apisApiIdGet(apiId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdPut(
@PathParam("apiId") String apiId



,


@BodyParam("body") API body

,

@HeaderParam("Content-Type") String contentType


,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.apisApiIdPut(apiId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{apiId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdDelete(
@PathParam("apiId") String apiId



,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.apisApiIdDelete(apiId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdDocumentsGet(
@PathParam("apiId") String apiId



,@QueryParam("limit") Integer limit




,@QueryParam("offset") Integer offset




,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


) {
        return delegate.apisApiIdDocumentsGet(apiId,limit,offset,accept,ifNoneMatch);
    }
    @POST
    @Path("/{apiId}/documents")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdDocumentsPost(
@PathParam("apiId") String apiId



,


@BodyParam("body") Document body

,

@HeaderParam("Content-Type") String contentType


) {
        return delegate.apisApiIdDocumentsPost(apiId,body,contentType);
    }
    @GET
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdDocumentsDocumentIdGet(
@PathParam("apiId") String apiId



,
@PathParam("documentId") String documentId



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.apisApiIdDocumentsDocumentIdGet(apiId,documentId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdDocumentsDocumentIdPut(
@PathParam("apiId") String apiId



,
@PathParam("documentId") String documentId



,


@BodyParam("body") Document body

,

@HeaderParam("Content-Type") String contentType


,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.apisApiIdDocumentsDocumentIdPut(apiId,documentId,body,contentType,ifMatch,ifUnmodifiedSince);
    }
    @DELETE
    @Path("/{apiId}/documents/{documentId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdDocumentsDocumentIdDelete(
@PathParam("apiId") String apiId



,
@PathParam("documentId") String documentId



,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.apisApiIdDocumentsDocumentIdDelete(apiId,documentId,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdDocumentsDocumentIdContentGet(
@PathParam("apiId") String apiId



,
@PathParam("documentId") String documentId



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.apisApiIdDocumentsDocumentIdContentGet(apiId,documentId,accept,ifNoneMatch,ifModifiedSince);
    }
    @POST
    @Path("/{apiId}/documents/{documentId}/content")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
   
    public Response apisApiIdDocumentsDocumentIdContentPost(
@PathParam("apiId") String apiId



,
@PathParam("documentId") String documentId



,

@HeaderParam("Content-Type") String contentType


,



  @FormDataParam("file") InputStream inputStream,@FormDataParam("file") FormDataContentDisposition fileDetail
,



@FormParam("inlineContent")  String inlineContent
,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.apisApiIdDocumentsDocumentIdContentPost(apiId,documentId,contentType,fileDetail,inlineContent,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/swagger")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdSwaggerGet(
@PathParam("apiId") String apiId



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.apisApiIdSwaggerGet(apiId,accept,ifNoneMatch,ifModifiedSince);
    }
    @PUT
    @Path("/{apiId}/swagger")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
   
    public Response apisApiIdSwaggerPut(
@PathParam("apiId") String apiId



,



@FormParam("apiDefinition")  String apiDefinition
,

@HeaderParam("Content-Type") String contentType


,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.apisApiIdSwaggerPut(apiId,apiDefinition,contentType,ifMatch,ifUnmodifiedSince);
    }
    @GET
    @Path("/{apiId}/thumbnail")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
   
    public Response apisApiIdThumbnailGet(
@PathParam("apiId") String apiId



,

@HeaderParam("Accept") String accept


,

@HeaderParam("If-None-Match") String ifNoneMatch


,

@HeaderParam("If-Modified-Since") String ifModifiedSince


) {
        return delegate.apisApiIdThumbnailGet(apiId,accept,ifNoneMatch,ifModifiedSince);
    }
    @POST
    @Path("/{apiId}/thumbnail")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
   
    public Response apisApiIdThumbnailPost(
@PathParam("apiId") String apiId



,



  @FormDataParam("file") InputStream inputStream,@FormDataParam("file") FormDataContentDisposition fileDetail
,

@HeaderParam("Content-Type") String contentType


,

@HeaderParam("If-Match") String ifMatch


,

@HeaderParam("If-Unmodified-Since") String ifUnmodifiedSince


) {
        return delegate.apisApiIdThumbnailPost(apiId,fileDetail,contentType,ifMatch,ifUnmodifiedSince);
    }
}

