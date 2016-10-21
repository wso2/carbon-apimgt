package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;


import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SubscriptionDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl extends SubscriptionsApiService {
    @Override
    public Response subscriptionsGet(String apiId,String applicationId,String groupId,Integer offset,Integer limit,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response subscriptionsPost(SubscriptionDTO body,String contentType){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response subscriptionsSubscriptionIdDelete(String subscriptionId,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response subscriptionsSubscriptionIdGet(String subscriptionId,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
