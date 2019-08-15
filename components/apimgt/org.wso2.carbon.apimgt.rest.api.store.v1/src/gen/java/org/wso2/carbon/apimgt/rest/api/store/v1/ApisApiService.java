package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;
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


public interface ApisApiService {
      public Response apisApiIdCommentsCommentIdDelete(String commentId, String apiId, String ifMatch, MessageContext messageContext);
      public Response apisApiIdCommentsCommentIdGet(String commentId, String apiId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdCommentsCommentIdPut(String commentId, String apiId, CommentDTO body, String ifMatch, MessageContext messageContext);
      public Response apisApiIdCommentsGet(String apiId, Integer limit, Integer offset, MessageContext messageContext);
      public Response apisApiIdCommentsPost(String apiId, CommentDTO body, MessageContext messageContext);
      public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdGraphqlSchemaGet(String apiId, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext);
      public Response apisApiIdRatingsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant, MessageContext messageContext);
      public Response apisApiIdSdksLanguageGet(String apiId, String language, MessageContext messageContext);
      public Response apisApiIdSubscriptionPoliciesGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdSwaggerGet(String apiId, String labelName, String environmentName, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext);
      public Response apisApiIdThumbnailGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdUserRatingDelete(String apiId, String xWSO2Tenant, String ifMatch, MessageContext messageContext);
      public Response apisApiIdUserRatingGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdUserRatingPut(String apiId, RatingDTO body, String xWSO2Tenant, MessageContext messageContext);
      public Response apisApiIdWsdlGet(String apiId, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext);
      public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, MessageContext messageContext);
}
