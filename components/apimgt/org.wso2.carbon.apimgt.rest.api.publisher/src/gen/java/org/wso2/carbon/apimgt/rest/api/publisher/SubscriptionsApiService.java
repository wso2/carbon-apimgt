package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.SubscriptionList;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Subscription;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:47:36.442+05:30")
public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsGet(String apiId ,String applicationId ,String groupId ,Integer offset ,Integer limit ,String accept ,String ifNoneMatch ) throws NotFoundException;
    public abstract Response subscriptionsPost(Subscription body ,String contentType ) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdDelete(String subscriptionId ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response subscriptionsSubscriptionIdGet(String subscriptionId ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
}
