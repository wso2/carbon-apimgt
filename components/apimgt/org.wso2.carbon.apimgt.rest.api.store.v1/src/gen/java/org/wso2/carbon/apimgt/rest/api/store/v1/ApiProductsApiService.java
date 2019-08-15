package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

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
      public Response apiProductsApiProductIdCommentsCommentIdDelete(String commentId, String apiProductId, String ifMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdCommentsCommentIdGet(String commentId, String apiProductId, String ifNoneMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdCommentsCommentIdPut(String commentId, String apiProductId, CommentDTO body, String ifMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdCommentsGet(String apiProductId, Integer limit, Integer offset, MessageContext messageContext);
      public Response apiProductsApiProductIdCommentsPost(String apiProductId, CommentDTO body, MessageContext messageContext);
      public Response apiProductsApiProductIdDocumentsDocumentIdContentGet(String apiProductId, String documentId, String ifNoneMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdDocumentsDocumentIdGet(String apiProductId, String documentId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdDocumentsGet(String apiProductId, Integer limit, Integer offset, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdGet(String apiProductId, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext);
      public Response apiProductsApiProductIdRatingsGet(String apiProductId, Integer limit, Integer offset, MessageContext messageContext);
      public Response apiProductsApiProductIdRatingsRatingIdGet(String apiProductId, String ratingId, String ifNoneMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdSubscriptionPoliciesGet(String apiProductId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdSwaggerGet(String apiProductId, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext);
      public Response apiProductsApiProductIdThumbnailGet(String apiProductId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apiProductsApiProductIdUserRatingPut(String apiProductId, RatingDTO body, MessageContext messageContext);
      public Response apiProductsGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, MessageContext messageContext);
}
