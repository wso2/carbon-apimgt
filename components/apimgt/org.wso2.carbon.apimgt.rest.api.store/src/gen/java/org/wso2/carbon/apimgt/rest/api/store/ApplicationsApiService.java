package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class ApplicationsApiService {
    public abstract Response applicationsApplicationIdDelete(String applicationId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdGenerateKeysPost(String applicationId
 ,ApplicationKeyGenerateRequestDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdGenerateTokenPost(String applicationId
 ,ApplicationTokenGenerateRequestDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdGet(String applicationId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdKeysGet(String applicationId
 ,String accept
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdKeysKeyTypeGet(String applicationId
 ,String keyType
 ,String accept
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdKeysKeyTypePut(String applicationId
 ,String keyType
 ,ApplicationKeysDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdMapKeysPost(String applicationId
 ,ApplicationKeyMappingRequestDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdPut(String applicationId
 ,ApplicationDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsGet(String query
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response applicationsPost(ApplicationDTO body
 ,String contentType
 , Request request) throws NotFoundException;
}
