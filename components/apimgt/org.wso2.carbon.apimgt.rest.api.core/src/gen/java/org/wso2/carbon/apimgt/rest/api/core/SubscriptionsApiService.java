package org.wso2.carbon.apimgt.rest.api.core;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-06T17:54:33.855+05:30")
public abstract class SubscriptionsApiService {
    public abstract Response subscriptionsGet(String apiContext
 ,String apiVersion
 ) throws NotFoundException;
}
