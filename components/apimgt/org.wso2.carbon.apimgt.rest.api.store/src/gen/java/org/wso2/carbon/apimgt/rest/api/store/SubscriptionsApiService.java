package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsGet(String apiId,String applicationId,String groupId,Integer offset,Integer limit,String accept,String ifNoneMatch);
    public abstract Response subscriptionsPost(SubscriptionDTO body,String contentType);
    public abstract Response subscriptionsPost(List<SubscriptionDTO> body,String contentType);
    public abstract Response subscriptionsSubscriptionIdDelete(String subscriptionId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince);

    public abstract String subscriptionsGetGetLastUpdatedTime(String apiId,String applicationId,String groupId,Integer offset,Integer limit,String accept,String ifNoneMatch);
    public abstract String subscriptionsPostGetLastUpdatedTime(SubscriptionDTO body,String contentType);
    public abstract String subscriptionsSubscriptionIdDeleteGetLastUpdatedTime(String subscriptionId,String ifMatch,String ifUnmodifiedSince);
    public abstract String subscriptionsSubscriptionIdGetGetLastUpdatedTime(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince);
}

