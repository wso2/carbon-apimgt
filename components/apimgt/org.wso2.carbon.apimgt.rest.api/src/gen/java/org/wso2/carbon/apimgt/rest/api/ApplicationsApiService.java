package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.model.*;

import org.wso2.carbon.apimgt.rest.api.model.Error;
import org.wso2.carbon.apimgt.rest.api.model.Application;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class ApplicationsApiService {
    public abstract Response applicationsGet(String limit,String offset,String accept,String ifNoneMatch)
    throws NotFoundException;
    public abstract Response applicationsPost(Application body,String contentType)
    throws NotFoundException;
    public abstract Response applicationsApplicationIdGet(String applicationId,String accept,String ifNoneMatch,String ifModifiedSince)
    throws NotFoundException;
    public abstract Response applicationsApplicationIdPut(String applicationId,Application body,String contentType,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response applicationsApplicationIdDelete(String applicationId,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response applicationsApplicationIdGenerateKeysPost(String applicationId,Application body,String contentType,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
}
