package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WorkflowResponseDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class ApisApiService {
    public abstract Response apisApiIdDelete(String apiId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId
 ,String documentId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdContentPost(String apiId
 ,String documentId
 ,InputStream fileInputStream, FileInfo fileDetail
 ,String inlineContent
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdDelete(String apiId
 ,String documentId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId
 ,String documentId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdPut(String apiId
 ,String documentId
 ,DocumentDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsGet(String apiId
 ,Integer limit
 ,Integer offset
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsPost(String apiId
 ,DocumentDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdGatewayConfigGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdGatewayConfigPut(String apiId
 ,String gatewayConfig
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdLifecycleGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdLifecycleHistoryGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdLifecycleLifecyclePendingTaskDelete(String apiId
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdPut(String apiId
 ,APIDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdSwaggerGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdSwaggerPut(String apiId
 ,String endpointId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdThumbnailGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdThumbnailPost(String apiId
 ,InputStream fileInputStream, FileInfo fileDetail
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdWsdlGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdWsdlPut(String apiId
 ,InputStream fileInputStream, FileInfo fileDetail
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisChangeLifecyclePost(String action
 ,String apiId
 ,String lifecycleChecklist
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisCopyApiPost(String newVersion
 ,String apiId
 , Request request) throws NotFoundException;
    public abstract Response apisGet(Integer limit
 ,Integer offset
 ,String query
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response apisHead(String query
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response apisImportDefinitionPost(String type
 ,InputStream fileInputStream, FileInfo fileDetail
 ,String url
 ,String additionalProperties
 ,String implementationType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisPost(APIDTO body
 , Request request) throws NotFoundException;
    public abstract Response apisValidateDefinitionPost(String type
 ,InputStream fileInputStream, FileInfo fileDetail
 ,String url
 , Request request) throws NotFoundException;
}
