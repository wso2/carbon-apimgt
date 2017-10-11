package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ExtendedSubscriptionDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsBlockSubscriptionPost(String subscriptionId,String blockState,String ifMatch,String ifUnmodifiedSince);
    public abstract Response subscriptionsGet(String apiId,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response subscriptionsUnblockSubscriptionPost(String subscriptionId,String ifMatch,String ifUnmodifiedSince);
}

