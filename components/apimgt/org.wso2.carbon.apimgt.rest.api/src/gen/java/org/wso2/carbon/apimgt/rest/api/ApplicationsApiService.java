package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ApplicationDTO;

import java.util.List;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class ApplicationsApiService {
    public abstract Response applicationsGet(String subscriber,String groupId,String limit,String offset,String accept,String ifNoneMatch);
    public abstract Response applicationsPost(ApplicationDTO body,String contentType);
    public abstract Response applicationsApplicationIdGet(String applicationId,String subscriber,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response applicationsApplicationIdPut(String applicationId,ApplicationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response applicationsApplicationIdDelete(String applicationId,String subscriber,String ifMatch,String ifUnmodifiedSince);
    public abstract Response applicationsApplicationIdGenerateKeysPost(String applicationId,ApplicationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
}

