package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-26T15:09:45.077+05:30")
public abstract class ApisApiService {
    public abstract Response apisApiIdDelete(String apiId ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId ,String documentId ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdContentPost(String apiId ,String documentId ,String contentType ,InputStream fileInputStream, FormDataContentDisposition fileDetail ,String inlineContent ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdDelete(String apiId ,String documentId ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId ,String documentId ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdPut(String apiId ,String documentId ,Document body ,String contentType ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsGet(String apiId ,Integer limit ,Integer offset ,String accept ,String ifNoneMatch ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsPost(String apiId ,Document body ,String contentType ) throws NotFoundException;
    public abstract Response apisApiIdGet(String apiId ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdPut(String apiId ,API body ,String contentType ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdSwaggerGet(String apiId ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdSwaggerPut(String apiId ,String apiDefinition ,String contentType ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdThumbnailGet(String apiId ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdThumbnailPost(String apiId ,InputStream fileInputStream, FormDataContentDisposition fileDetail ,String contentType ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response apisChangeLifecyclePost(String action ,String apiId ,String lifecycleChecklist ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response apisCopyApiPost(String newVersion ,String apiId ) throws NotFoundException;
    public abstract Response apisGet(Integer limit ,Integer offset ,String query ,String accept ,String ifNoneMatch ) throws NotFoundException;
    public abstract Response apisPost(API body ,String contentType ) throws NotFoundException;
}
