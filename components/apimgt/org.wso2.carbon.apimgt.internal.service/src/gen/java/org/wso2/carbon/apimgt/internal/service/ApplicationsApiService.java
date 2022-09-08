package org.wso2.carbon.apimgt.internal.service;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.core.Response;

public interface ApplicationsApiService {
      public Response applicationsGet(String xWSO2Tenant, Integer appId, MessageContext messageContext) throws APIManagementException;
}
