package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.SubscriptionDTO;

import java.util.List;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsGet(String apiId,String applicationId,String groupId,String accept,String ifNoneMatch);
    public abstract Response subscriptionsPost(SubscriptionDTO body,String contentType);
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response subscriptionsSubscriptionIdPut(String subscriptionId,SubscriptionDTO body,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response subscriptionsSubscriptionIdDelete(String subscriptionId,String ifMatch,String ifUnmodifiedSince);
}

