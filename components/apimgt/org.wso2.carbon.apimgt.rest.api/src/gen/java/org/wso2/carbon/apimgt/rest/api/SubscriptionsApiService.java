package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.SubscriptionDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsGet(String apiId,String applicationId,String accept,String ifNoneMatch)
    throws NotFoundException;
    public abstract Response subscriptionsPost(SubscriptionDTO body,String contentType)
    throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdGet(String accept,String ifNoneMatch,String ifModifiedSince)
    throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdPut(SubscriptionDTO body,String contentType,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdDelete(String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
}

