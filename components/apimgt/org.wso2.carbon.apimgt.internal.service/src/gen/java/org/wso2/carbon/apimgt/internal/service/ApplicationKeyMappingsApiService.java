package org.wso2.carbon.apimgt.internal.service;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.core.Response;

public interface ApplicationKeyMappingsApiService {
      public Response applicationKeyMappingsGet(String xWSO2Tenant, String consumerKey, String keymanager, MessageContext messageContext) throws APIManagementException;
}
