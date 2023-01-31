package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.GraphQLQueryComplexityInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.GraphQLSchemaTypeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PostRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApisApiService {
      public Response addCommentToAPI(String apiId, PostRequestBodyDTO postRequestBodyDTO, String replyTo, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdAsyncApiSpecificationGet(String apiId, String environmentName, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGraphqlPoliciesComplexityGet(String apiId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGraphqlPoliciesComplexityTypesGet(String apiId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGraphqlSchemaGet(String apiId, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdRatingsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSdksLanguageGet(String apiId, String language, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSubscriptionPoliciesGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSwaggerGet(String apiId, String environmentName, String ifNoneMatch, String xWSO2Tenant, String xWSO2TenantQ, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdThumbnailGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdTopicsGet(String apiId, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdUserRatingDelete(String apiId, String xWSO2Tenant, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdUserRatingGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdUserRatingPut(String apiId, RatingDTO ratingDTO, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteComment(String commentId, String apiId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response editCommentOfAPI(String commentId, String apiId, PatchRequestBodyDTO patchRequestBodyDTO, MessageContext messageContext) throws APIManagementException;
      public Response getAllCommentsOfAPI(String apiId, String xWSO2Tenant, Integer limit, Integer offset, Boolean includeCommenterInfo, MessageContext messageContext) throws APIManagementException;
      public Response getCommentOfAPI(String commentId, String apiId, String xWSO2Tenant, String ifNoneMatch, Boolean includeCommenterInfo, Integer replyLimit, Integer replyOffset, MessageContext messageContext) throws APIManagementException;
      public Response getRepliesOfComment(String commentId, String apiId, String xWSO2Tenant, Integer limit, Integer offset, String ifNoneMatch, Boolean includeCommenterInfo, MessageContext messageContext) throws APIManagementException;
      public Response getWSDLOfAPI(String apiId, String environmentName, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
}
