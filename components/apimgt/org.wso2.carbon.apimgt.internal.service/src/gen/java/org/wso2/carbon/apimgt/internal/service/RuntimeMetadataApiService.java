package org.wso2.carbon.apimgt.internal.service;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.ws.rs.core.Response;

public interface RuntimeMetadataApiService {

      public Response runtimeMetadataGet(String xWSO2Tenant, String apiId, String gatewayLabel, String name,
                                         String version, MessageContext messageContext) throws APIManagementException;
}
