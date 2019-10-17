package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApiProductsApiService {
      public Response apiProductsApiProductIdDelete(String apiProductId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId, String documentId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsDocumentIdContentPost(String apiProductId, String documentId, InputStream fileInputStream, Attachment fileDetail, String inlineContent, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsDocumentIdDelete(String apiProductId, String documentId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId, String documentId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsDocumentIdPut(String apiProductId, String documentId, DocumentDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsGet(String apiProductId, Integer limit, Integer offset, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsPost(String apiProductId, DocumentDTO body, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdGet(String apiProductId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdMonetizationGet(String apiProductId, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdMonetizePost(String apiProductId, APIMonetizationInfoDTO body, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdPut(String apiProductId, APIProductDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdSwaggerGet(String apiProductId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdThumbnailGet(String apiProductId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdThumbnailPut(String apiProductId, InputStream fileInputStream, Attachment fileDetail, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsPost(APIProductDTO body, MessageContext messageContext) throws APIManagementException;
}
