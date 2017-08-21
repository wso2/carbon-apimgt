package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowResponseDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsGet(String apiId
 ,String applicationId
 ,String apiType
 ,Integer offset
 ,Integer limit
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response subscriptionsPost(SubscriptionDTO body
 , Request request) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdDelete(String subscriptionId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
}
