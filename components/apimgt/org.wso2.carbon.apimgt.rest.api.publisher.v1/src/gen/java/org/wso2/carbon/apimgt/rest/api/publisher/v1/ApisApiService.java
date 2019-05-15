package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDefinitionValidationResponseDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ApisApiService {
    public abstract Response apisApiIdDelete(String apiId,String ifMatch);
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId,String documentId,String ifNoneMatch);
    public abstract Response apisApiIdDocumentsDocumentIdContentPost(String apiId,String documentId,InputStream fileInputStream,Attachment fileDetail,String inlineContent,String ifMatch);
    public abstract Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch);
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String ifNoneMatch);
    public abstract Response apisApiIdDocumentsDocumentIdPut(String apiId,String documentId,DocumentDTO body,String ifMatch);
    public abstract Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String ifNoneMatch);
    public abstract Response apisApiIdDocumentsPost(String apiId,DocumentDTO body,String ifMatch);
    public abstract Response apisApiIdGet(String apiId,String xWSO2Tenant,String ifNoneMatch);
    public abstract Response apisApiIdLifecycleHistoryGet(String apiId,String ifNoneMatch);
    public abstract Response apisApiIdLifecycleStateGet(String apiId,String ifNoneMatch);
    public abstract Response apisApiIdLifecycleStatePendingTasksDelete(String apiId);
    public abstract Response apisApiIdMediationPoliciesGet(String apiId,Integer limit,Integer offset,String query,String ifNoneMatch);
    public abstract Response apisApiIdMediationPoliciesMediationPolicyIdDelete(String apiId,String mediationPolicyId,String ifMatch);
    public abstract Response apisApiIdMediationPoliciesMediationPolicyIdGet(String apiId,String mediationPolicyId,String ifNoneMatch);
    public abstract Response apisApiIdMediationPoliciesMediationPolicyIdPut(String apiId,String mediationPolicyId,MediationDTO body,String ifMatch);
    public abstract Response apisApiIdMediationPoliciesPost(MediationDTO body,String apiId,String ifMatch);
    public abstract Response apisApiIdPut(String apiId,APIDTO body,String ifMatch);
    public abstract Response apisApiIdResourcePoliciesGet(String apiId,String sequenceType,String resourcePath,String verb,String ifNoneMatch);
    public abstract Response apisApiIdResourcePoliciesResourcePolicyIdGet(String apiId,String resourcePolicyId,String ifNoneMatch);
    public abstract Response apisApiIdResourcePoliciesResourcePolicyIdPut(String apiId,String resourcePolicyId,ResourcePolicyInfoDTO body,String ifMatch);
    public abstract Response apisApiIdScopesGet(String apiId,String ifNoneMatch);
    public abstract Response apisApiIdScopesNameDelete(String apiId,String name,String ifMatch);
    public abstract Response apisApiIdScopesNameGet(String apiId,String name,String ifNoneMatch);
    public abstract Response apisApiIdScopesNamePut(String apiId,String name,ScopeDTO body,String ifMatch);
    public abstract Response apisApiIdScopesPost(String apiId,ScopeDTO body,String ifMatch);
    public abstract Response apisApiIdSwaggerGet(String apiId,String ifNoneMatch);
    public abstract Response apisApiIdSwaggerPut(String apiId,String apiDefinition,String ifMatch);
    public abstract Response apisApiIdThreatProtectionPoliciesDelete(String apiId,String policyId);
    public abstract Response apisApiIdThreatProtectionPoliciesGet(String apiId);
    public abstract Response apisApiIdThreatProtectionPoliciesPost(String apiId,String policyId);
    public abstract Response apisApiIdThumbnailGet(String apiId,String ifNoneMatch);
    public abstract Response apisApiIdThumbnailPost(String apiId,InputStream fileInputStream,Attachment fileDetail,String ifMatch);
    public abstract Response apisApiIdWsdlGet(String apiId,String ifNoneMatch);
    public abstract Response apisApiIdWsdlPut(String apiId,InputStream fileInputStream,Attachment fileDetail,String ifMatch);
    public abstract Response apisChangeLifecyclePost(String action,String apiId,String lifecycleChecklist,String ifMatch);
    public abstract Response apisCopyApiPost(String newVersion,String apiId);
    public abstract Response apisGet(Integer limit,Integer offset,String xWSO2Tenant,String query,String ifNoneMatch,Boolean expand,String accept,String tenantDomain);
    public abstract Response apisHead(String query,String ifNoneMatch);
    public abstract Response apisImportDefinitionPost(String type,InputStream fileInputStream,Attachment fileDetail,String url,String additionalProperties,String implementationType,String ifMatch);
    public abstract Response apisPost(APIDTO body);
    public abstract Response apisValidateDefinitionPost(String type,String url,InputStream fileInputStream,Attachment fileDetail);
}

