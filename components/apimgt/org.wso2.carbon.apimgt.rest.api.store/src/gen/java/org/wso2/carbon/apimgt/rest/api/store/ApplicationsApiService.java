package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowResponseDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class ApplicationsApiService {
    public abstract Response applicationsApplicationIdDelete(String applicationId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdGenerateKeysPost(String applicationId
 ,ApplicationKeyGenerateRequestDTO body
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdGenerateTokenPost(String applicationId
 ,ApplicationTokenGenerateRequestDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdGet(String applicationId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdKeysGet(String applicationId
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdKeysKeyTypeGet(String applicationId
 ,String keyType
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdKeysKeyTypePut(String applicationId
 ,String keyType
 ,ApplicationKeysDTO body
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdMapKeysPost(String applicationId
 ,ApplicationKeyMappingRequestDTO body
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdPut(String applicationId
 ,ApplicationDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsGet(String query
 ,Integer limit
 ,Integer offset
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response applicationsPost(ApplicationDTO body
 , Request request) throws NotFoundException;
}
