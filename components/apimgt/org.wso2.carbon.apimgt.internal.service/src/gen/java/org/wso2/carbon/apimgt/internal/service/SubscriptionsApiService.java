package org.wso2.carbon.apimgt.internal.service;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.core.Response;

public interface SubscriptionsApiService {
      public Response subscriptionsGet(String xWSO2Tenant, Integer apiId, Integer appId, String apiUUID, String applicationUUID, MessageContext messageContext) throws APIManagementException;
}
