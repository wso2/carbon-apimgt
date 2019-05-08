package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ApiProductsApiService {
    public abstract Response apiProductsApiProductIdDelete(String apiProductId,String ifMatch);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId,String documentId,String accept,String ifNoneMatch);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdContentPost(String apiProductId,String documentId,InputStream fileInputStream,Attachment fileDetail,String inlineContent,String ifMatch);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdDelete(String apiProductId,String documentId,String ifMatch);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId,String documentId,String accept,String ifNoneMatch);
    public abstract Response apiProductsApiProductIdDocumentsDocumentIdPut(String apiProductId,String documentId,DocumentDTO body,String ifMatch);
    public abstract Response apiProductsApiProductIdDocumentsGet(String apiProductId,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response apiProductsApiProductIdDocumentsPost(String apiProductId,DocumentDTO body);
    public abstract Response apiProductsApiProductIdGet(String apiProductId,String accept,String ifNoneMatch);
    public abstract Response apiProductsApiProductIdPut(String apiProductId,APIProductDTO body,String ifMatch);
    public abstract Response apiProductsApiProductIdSwaggerGet(String apiProductId,String accept,String ifNoneMatch);
    public abstract Response apiProductsApiProductIdSwaggerPut(String apiProductId,String apiDefinition,String ifMatch);
    public abstract Response apiProductsApiProductIdThumbnailGet(String apiProductId,String accept,String ifNoneMatch);
    public abstract Response apiProductsApiProductIdThumbnailPost(String apiProductId,InputStream fileInputStream,Attachment fileDetail,String ifMatch);
    public abstract Response apiProductsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch,Boolean expand);
    public abstract Response apiProductsPost(APIProductDTO body);
}

