package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExtendedSubscriptionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface SubscriptionsApiService {
      public Response subscriptionsBlockSubscriptionPost(String subscriptionId, String blockState, String ifMatch, MessageContext messageContext);
      public Response subscriptionsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext);
      public Response subscriptionsSubscriptionIdGet(String subscriptionId, String ifNoneMatch, MessageContext messageContext);
      public Response subscriptionsUnblockSubscriptionPost(String subscriptionId, String ifMatch, MessageContext messageContext);
}
