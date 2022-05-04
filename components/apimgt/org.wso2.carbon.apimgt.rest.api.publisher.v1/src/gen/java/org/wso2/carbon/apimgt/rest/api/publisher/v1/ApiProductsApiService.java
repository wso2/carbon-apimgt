package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductOutdatedStatusDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApiProductsApiService {
      public Response addAPIProductDocument(String apiProductId, DocumentDTO documentDTO, MessageContext messageContext) throws APIManagementException;
      public Response addAPIProductDocumentContent(String apiProductId, String documentId, String ifMatch, InputStream fileInputStream, Attachment fileDetail, String inlineContent, MessageContext messageContext) throws APIManagementException;
      public Response changeAPIProductLifecycle(String action, String apiProductId, String lifecycleChecklist, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response createAPIProduct(APIProductDTO apIProductDTO, MessageContext messageContext) throws APIManagementException;
      public Response createAPIProductRevision(String apiProductId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext) throws APIManagementException;
      public Response deleteAPIProduct(String apiProductId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteAPIProductDocument(String apiProductId, String documentId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteAPIProductLifecycleStatePendingTasks(String apiProductId, MessageContext messageContext) throws APIManagementException;
      public Response deleteAPIProductRevision(String apiProductId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response deployAPIProductRevision(String apiProductId, String revisionId, List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response exportAPIProduct(String name, String version, String providerName, String revisionNumber, String format, Boolean preserveStatus, Boolean latestRevision, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProduct(String apiProductId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductDocument(String apiProductId, String documentId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductDocumentContent(String apiProductId, String documentId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductDocuments(String apiProductId, Integer limit, Integer offset, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductLifecycleHistory(String apiProductId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductLifecycleState(String apiProductId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductRevision(String apiProductId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductRevisionDeployments(String apiProductId, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductRevisions(String apiProductId, String query, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductSwagger(String apiProductId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAPIProductThumbnail(String apiProductId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getAllAPIProducts(Integer limit, Integer offset, String query, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getIsAPIProductOutdated(String apiProductId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response importAPIProduct(InputStream fileInputStream, Attachment fileDetail, Boolean preserveProvider, Boolean rotateRevision, Boolean importAPIs, Boolean overwriteAPIProduct, Boolean overwriteAPIs, MessageContext messageContext) throws APIManagementException;
      public Response restoreAPIProductRevision(String apiProductId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response undeployAPIProductRevision(String apiProductId, String revisionId, String revisionNumber, Boolean allEnvironments, List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateAPIProduct(String apiProductId, APIProductDTO apIProductDTO, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response updateAPIProductDeployment(String apiProductId, String deploymentId, APIRevisionDeploymentDTO apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateAPIProductDocument(String apiProductId, String documentId, DocumentDTO documentDTO, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response updateAPIProductThumbnail(String apiProductId, InputStream fileInputStream, Attachment fileDetail, String ifMatch, MessageContext messageContext) throws APIManagementException;
}
