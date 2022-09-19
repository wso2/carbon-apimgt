package org.wso2.carbon.apimgt.rest.api.store.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;


import javax.ws.rs.core.Response;


public interface SettingsApiService {
      public Response settingsApplicationAttributesGet(String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response settingsGet(String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
}
