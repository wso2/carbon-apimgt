package org.wso2.carbon.apimgt.rest.api.publisher;


import javax.ws.rs.core.Response;

public abstract class BlockSubscriptionApiService {
    public abstract Response blockSubscriptionPost(String subscriptionId,String ifMatch,String ifUnmodifiedSince);
}

