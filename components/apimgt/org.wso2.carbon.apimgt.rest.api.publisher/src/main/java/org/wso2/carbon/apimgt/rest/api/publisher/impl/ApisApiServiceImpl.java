package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Document;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentList;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.FileInfo;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIList;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-26T14:09:00.126+05:30")
public class ApisApiServiceImpl extends ApisApiService {
    @Override
    public Response apisApiIdDelete(String apiId, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String accept, String ifNoneMatch, String ifModifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdContentPost(String apiId, String documentId, String contentType, InputStream fileInputStream, FormDataContentDisposition fileDetail, String inlineContent, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId, String documentId, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String accept, String ifNoneMatch, String ifModifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, Document body, String contentType, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String accept, String ifNoneMatch ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsPost(String apiId, Document body, String contentType ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdPut(String apiId, API body, String contentType, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String contentType, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdThumbnailGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdThumbnailPost(String apiId, InputStream fileInputStream, FormDataContentDisposition fileDetail, String contentType, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisChangeLifecyclePost(String action, String apiId, String lifecycleChecklist, String ifMatch, String ifUnmodifiedSince ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisCopyApiPost(String newVersion, String apiId ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisPost(API body, String contentType ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
