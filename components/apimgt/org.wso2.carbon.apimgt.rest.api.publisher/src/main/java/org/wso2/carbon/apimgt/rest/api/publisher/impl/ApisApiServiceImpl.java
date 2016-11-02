package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.publisher.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import java.io.InputStream;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:47:43.416+05:30")
public class ApisApiServiceImpl extends ApisApiService {
    @Override
    public Response apisApiIdDelete(String apiId
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId
, String documentId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdContentPost(String apiId
, String documentId
, String contentType
, InputStream fileInputStream, FileInfo fileDetail
, String inlineContent
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId
, String documentId
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId
, String documentId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId
, String documentId
, DocumentDTO body
, String contentType
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsGet(String apiId
, Integer limit
, Integer offset
, String accept
, String ifNoneMatch
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsPost(String apiId
, DocumentDTO body
, String contentType
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdPut(String apiId
, APIDTO body
, String contentType
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdSwaggerGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdSwaggerPut(String apiId
, String apiDefinition
, String contentType
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdThumbnailGet(String apiId
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdThumbnailPost(String apiId
, InputStream fileInputStream, FileInfo fileDetail
, String contentType
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisChangeLifecyclePost(String action
, String apiId
, String lifecycleChecklist
, String ifMatch
, String ifUnmodifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisCopyApiPost(String newVersion
, String apiId
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisGet(Integer limit
, Integer offset
, String query
, String accept
, String ifNoneMatch
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisPost(APIDTO body
, String contentType
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
