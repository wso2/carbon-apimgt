package org.wso2.carbon.apimgt.rest.api.publisher.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;


import javax.ws.rs.core.Response;


public interface SubscriptionsApiService {
      public Response blockSubscription(String subscriptionId, String blockState, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response getSubscriberInfoBySubscriptionId(String subscriptionId, MessageContext messageContext) throws APIManagementException;
      public Response getSubscriptionUsage(String subscriptionId, MessageContext messageContext) throws APIManagementException;
      public Response getSubscriptions(String apiId, Integer limit, Integer offset, String ifNoneMatch, String query, MessageContext messageContext) throws APIManagementException;
      public Response unBlockSubscription(String subscriptionId, String ifMatch, MessageContext messageContext) throws APIManagementException;
}
