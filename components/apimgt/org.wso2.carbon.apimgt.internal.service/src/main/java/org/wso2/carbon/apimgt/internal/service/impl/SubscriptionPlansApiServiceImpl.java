package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;


import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class SubscriptionPlansApiServiceImpl implements SubscriptionPlansApiService {

    public Response subscriptionPlansGet(MessageContext messageContext) {
        return Response.ok(java.util.Collections.emptyList()).build();
    }
}
