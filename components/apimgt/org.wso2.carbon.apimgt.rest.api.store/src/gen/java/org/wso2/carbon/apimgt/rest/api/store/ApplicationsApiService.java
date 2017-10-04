package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ApplicationsApiService {
    public abstract Response applicationsApplicationIdDelete(String applicationId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response applicationsApplicationIdGet(String applicationId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response applicationsApplicationIdKeysKeyTypeGet(String applicationId,String keyType,String groupId,String accept);
    public abstract Response applicationsApplicationIdKeysKeyTypePut(String applicationId,String keyType,ApplicationKeyDTO body);
    public abstract Response applicationsApplicationIdPut(String applicationId,ApplicationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response applicationsGenerateKeysPost(String applicationId,ApplicationKeyGenerateRequestDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response applicationsGet(String groupId,String query,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response applicationsPost(ApplicationDTO body,String contentType);

    public abstract String applicationsApplicationIdDeleteGetLastUpdatedTime(String applicationId,String ifMatch,String ifUnmodifiedSince);
    public abstract String applicationsApplicationIdGetGetLastUpdatedTime(String applicationId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract String applicationsApplicationIdKeysKeyTypeGetGetLastUpdatedTime(String applicationId,String keyType,String groupId,String accept);
    public abstract String applicationsApplicationIdKeysKeyTypePutGetLastUpdatedTime(String applicationId,String keyType,ApplicationKeyDTO body);
    public abstract String applicationsApplicationIdPutGetLastUpdatedTime(String applicationId,ApplicationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract String applicationsGenerateKeysPostGetLastUpdatedTime(String applicationId,ApplicationKeyGenerateRequestDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract String applicationsGetGetLastUpdatedTime(String groupId,String query,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract String applicationsPostGetLastUpdatedTime(ApplicationDTO body,String contentType);
}

