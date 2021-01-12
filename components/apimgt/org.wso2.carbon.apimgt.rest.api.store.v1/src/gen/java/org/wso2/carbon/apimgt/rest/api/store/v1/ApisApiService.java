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
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.GraphQLSchemaTypeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApisApiService {
      public Response addCommentToAPI(String apiId, CommentDTO commentDTO, String organizationId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String organizationId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String organizationId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsGet(String apiId, String organizationId, Integer limit, Integer offset, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGet(String apiId, String organizationId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGraphqlPoliciesComplexityGet(String apiId, String organizationId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGraphqlPoliciesComplexityTypesGet(String apiId, String organizationId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGraphqlSchemaGet(String apiId, String organizationId, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdRatingsGet(String apiId, String organizationId, Integer limit, Integer offset, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSdksLanguageGet(String apiId, String language, String organizationId, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSubscriptionPoliciesGet(String apiId, String organizationId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSwaggerGet(String apiId, String organizationId, String labelName, String environmentName, String clusterName, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdThumbnailGet(String apiId, String organizationId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdUserRatingDelete(String apiId, String organizationId, String xWSO2Tenant, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdUserRatingGet(String apiId, String organizationId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdUserRatingPut(String apiId, RatingDTO ratingDTO, String organizationId, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response apisGet(String organizationId, Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteComment(String commentId, String apiId, String organizationId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAllCommentsOfAPI(String apiId, String organizationId, String xWSO2Tenant, Integer limit, Integer offset, Boolean includeCommenterInfo, MessageContext messageContext) throws APIManagementException;
      public Response getCommentOfAPI(String commentId, String apiId, String organizationId, String xWSO2Tenant, String ifNoneMatch, Boolean includeCommenterInfo, MessageContext messageContext) throws APIManagementException;
      public Response getWSDLOfAPI(String apiId, String organizationId, String labelName, String environmentName, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
}
