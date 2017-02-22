package org.wso2.carbon.apimgt.rest.api.publisher;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-22T11:40:36.320+05:30")
public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsBlockSubscriptionPost(String subscriptionId
 ,String blockState
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response subscriptionsGet(String apiId
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response subscriptionsUnblockSubscriptionPost(String subscriptionId
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
}
