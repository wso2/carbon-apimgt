package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionListDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsGet(String apiId,String applicationId,String groupId,Integer offset,Integer limit,String ifNoneMatch);
    public abstract Response subscriptionsMultiplePost(List<SubscriptionDTO> body);
    public abstract Response subscriptionsPost(SubscriptionDTO body);
    public abstract Response subscriptionsSubscriptionIdDelete(String subscriptionId,String ifMatch);
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId,String ifNoneMatch);
}

