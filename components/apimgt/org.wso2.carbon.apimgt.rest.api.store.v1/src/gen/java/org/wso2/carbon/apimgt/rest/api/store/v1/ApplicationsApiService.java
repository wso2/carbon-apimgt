package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyReGenerateResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ApplicationsApiService {
    public abstract Response applicationsApplicationIdDelete(String applicationId,String ifMatch);
    public abstract Response applicationsApplicationIdGenerateKeysPost(String applicationId,ApplicationKeyGenerateRequestDTO body);
    public abstract Response applicationsApplicationIdGet(String applicationId,String ifNoneMatch);
    public abstract Response applicationsApplicationIdKeysGet(String applicationId);
    public abstract Response applicationsApplicationIdKeysKeyTypeGenerateTokenPost(String applicationId,String keyType,ApplicationTokenGenerateRequestDTO body,String ifMatch);
    public abstract Response applicationsApplicationIdKeysKeyTypeGet(String applicationId,String keyType,String groupId);
    public abstract Response applicationsApplicationIdKeysKeyTypePut(String applicationId,String keyType,ApplicationKeyDTO body);
    public abstract Response applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(String applicationId,String keyType);
    public abstract Response applicationsApplicationIdMapKeysPost(String applicationId,ApplicationKeyMappingRequestDTO body);
    public abstract Response applicationsApplicationIdPut(String applicationId,ApplicationDTO body,String ifMatch);
    public abstract Response applicationsApplicationIdScopesGet(String applicationId,Boolean filterByUserRoles,String ifNoneMatch);
    public abstract Response applicationsGet(String groupId,String query,String sortBy,String sortOrder,Integer limit,Integer offset,String ifNoneMatch);
    public abstract Response applicationsPost(ApplicationDTO body);
}

