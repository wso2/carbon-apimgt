package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExtendedSubscriptionDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsBlockSubscriptionPost(String subscriptionId,String blockState,String ifMatch);
    public abstract Response subscriptionsGet(String apiId,Integer limit,Integer offset,String ifNoneMatch);
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId,String ifNoneMatch);
    public abstract Response subscriptionsUnblockSubscriptionPost(String subscriptionId,String ifMatch);
}

