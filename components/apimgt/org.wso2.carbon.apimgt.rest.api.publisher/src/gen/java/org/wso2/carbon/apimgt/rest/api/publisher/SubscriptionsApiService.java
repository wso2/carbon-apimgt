package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionList;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Subscription;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-26T15:09:45.077+05:30")
public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsBlockSubscriptionPost(String subscriptionId ,String blockState ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response subscriptionsGet(String apiId ,Integer limit ,Integer offset ,String accept ,String ifNoneMatch ) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response subscriptionsUnblockSubscriptionPost(String subscriptionId ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
}
