package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

<<<<<<< HEAD
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
=======
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

>>>>>>> upstream/master
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.WorkflowResponseDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class SubscriptionsApiService {
<<<<<<< HEAD
    public abstract Response subscriptionsGet(String apiId,String applicationId,String groupId,Integer offset,Integer limit,String accept,String ifNoneMatch);
    public abstract Response subscriptionsPost(SubscriptionDTO body,String contentType);
    public abstract Response subscriptionsPost(List<SubscriptionDTO> body,String contentType);
    public abstract Response subscriptionsSubscriptionIdDelete(String subscriptionId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince);

    public abstract String subscriptionsGetGetLastUpdatedTime(String apiId,String applicationId,String groupId,Integer offset,Integer limit,String accept,String ifNoneMatch);
    public abstract String subscriptionsPostGetLastUpdatedTime(SubscriptionDTO body,String contentType);
    public abstract String subscriptionsSubscriptionIdDeleteGetLastUpdatedTime(String subscriptionId,String ifMatch,String ifUnmodifiedSince);
    public abstract String subscriptionsSubscriptionIdGetGetLastUpdatedTime(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince);
=======
    public abstract Response subscriptionsGet(String apiId
 ,String applicationId
 ,String apiType
 ,Integer offset
 ,Integer limit
 ,String ifNoneMatch
  ,Request request) throws NotFoundException;
    public abstract Response subscriptionsPost(SubscriptionDTO body
  ,Request request) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdDelete(String subscriptionId
 ,String ifMatch
 ,String ifUnmodifiedSince
  ,Request request) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId
 ,String ifNoneMatch
 ,String ifModifiedSince
  ,Request request) throws NotFoundException;
>>>>>>> upstream/master
}
