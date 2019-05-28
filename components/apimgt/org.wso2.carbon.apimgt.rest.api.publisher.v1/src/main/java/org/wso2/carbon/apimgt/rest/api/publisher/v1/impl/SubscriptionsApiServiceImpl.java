package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExtendedSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class SubscriptionsApiServiceImpl implements SubscriptionsApiService {
    public Response subscriptionsBlockSubscriptionPost(String subscriptionId, String blockState, String ifMatch,
            MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    public Response subscriptionsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch,
            MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    public Response subscriptionsSubscriptionIdGet(String subscriptionId, String ifNoneMatch,
            MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    public Response subscriptionsUnblockSubscriptionPost(String subscriptionId, String ifMatch,
            MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }
}
