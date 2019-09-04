package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApiProductsApiService {
      public Response apiProductsApiProductIdCommentsCommentIdDelete(Integer commentId, String apiProductId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdCommentsCommentIdGet(Integer commentId, String apiProductId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdCommentsCommentIdPut(Integer commentId, String apiProductId, CommentDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdCommentsGet(String apiProductId, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdCommentsPost(String apiProductId, CommentDTO body, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId, String documentId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId, String documentId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdDocumentsGet(String apiProductId, Integer limit, Integer offset, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdGet(String apiProductId, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdRatingsGet(String apiProductId, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdRatingsRatingIdGet(String apiProductId, String ratingId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdSubscriptionPoliciesGet(String apiProductId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdSwaggerGet(String apiProductId, String labelName, String environmentName, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdThumbnailGet(String apiProductId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsApiProductIdUserRatingPut(String apiProductId, RatingDTO body, MessageContext messageContext) throws APIManagementException;
      public Response apiProductsGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
}
