package org.wso2.carbon.apimgt.rest.api.publisher.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;


import javax.ws.rs.core.Response;


public interface SettingsApiService {
      public Response getSettings(MessageContext messageContext) throws APIManagementException;
}
