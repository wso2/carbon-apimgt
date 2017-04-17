package org.wso2.carbon.apimgt.rest.api.store;

import javax.ws.rs.core.Response;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-07T10:04:16.863+05:30")
public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsGet(String apiId
 ,String applicationId
 ,Integer offset
 ,Integer limit
 ,String accept
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response subscriptionsPost(SubscriptionDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdDelete(String subscriptionId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
}
