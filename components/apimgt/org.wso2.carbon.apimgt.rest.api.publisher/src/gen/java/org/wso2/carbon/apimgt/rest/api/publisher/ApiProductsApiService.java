package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductDetailedDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ApiProductsApiService {
    public abstract Response apiProductsApiProductIdDelete(String apiProductId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdContentPost(String apiProductId,String documentId,String contentType,InputStream fileInputStream,Attachment fileDetail,String inlineContent,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdDelete(String apiProductId,String documentId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdPut(String apiProductId,String documentId,DocumentDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiProductsApiProductIdDocumentsGet(String apiProductId,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response apiProductsApiProductIdDocumentsPost(String apiProductId,DocumentDTO body,String contentType);
    public abstract Response apiProductsApiProductIdGet(String apiProductId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apiProductsApiProductIdPut(String apiProductId,APIProductDetailedDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiProductsApiProductIdSwaggerGet(String apiProductId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apiProductsApiProductIdSwaggerPut(String apiProductId,String apiDefinition,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiProductsApiProductIdThumbnailGet(String apiProductId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apiProductsApiProductIdThumbnailPost(String apiProductId,InputStream fileInputStream,Attachment fileDetail,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiProductsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch,Boolean expand,String tenantDomain);
    public abstract Response apiProductsPost(APIProductDetailedDTO body,String contentType);
}

