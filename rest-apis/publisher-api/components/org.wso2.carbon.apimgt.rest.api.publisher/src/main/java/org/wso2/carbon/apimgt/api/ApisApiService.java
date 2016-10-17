package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.model.APIList;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.API;
import org.wso2.carbon.apimgt.model.DocumentList;
import org.wso2.carbon.apimgt.model.Document;
import java.io.File;
import org.wso2.carbon.apimgt.model.FileInfo;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public abstract class ApisApiService {
      public abstract Response apisGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch);
      public abstract Response apisPost(API body,String contentType);
      public abstract Response apisChangeLifecyclePost(String action,String apiId,String lifecycleChecklist,String ifMatch,String ifUnmodifiedSince);
      public abstract Response apisCopyApiPost(String newVersion,String apiId);
      public abstract Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
      public abstract Response apisApiIdPut(String apiId,API body,String contentType,String ifMatch,String ifUnmodifiedSince);
      public abstract Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince);
      public abstract Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String accept,String ifNoneMatch);
      public abstract Response apisApiIdDocumentsPost(String apiId,Document body,String contentType);
      public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince);
      public abstract Response apisApiIdDocumentsDocumentIdPut(String apiId,String documentId,Document body,String contentType,String ifMatch,String ifUnmodifiedSince);
      public abstract Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch,String ifUnmodifiedSince);
      public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince);
      public abstract Response apisApiIdDocumentsDocumentIdContentPost(String apiId,String documentId,String contentType,FormDataContentDisposition fileDetail,String inlineContent,String ifMatch,String ifUnmodifiedSince);
      public abstract Response apisApiIdSwaggerGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
      public abstract Response apisApiIdSwaggerPut(String apiId,String apiDefinition,String contentType,String ifMatch,String ifUnmodifiedSince);
      public abstract Response apisApiIdThumbnailGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
      public abstract Response apisApiIdThumbnailPost(String apiId,FormDataContentDisposition fileDetail,String contentType,String ifMatch,String ifUnmodifiedSince);
}
