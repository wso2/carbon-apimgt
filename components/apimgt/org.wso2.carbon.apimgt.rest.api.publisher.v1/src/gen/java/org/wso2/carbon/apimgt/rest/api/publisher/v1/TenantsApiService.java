package org.wso2.carbon.apimgt.rest.api.publisher.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;


import javax.ws.rs.core.Response;


public interface TenantsApiService {
      public Response getTenantExistence(String tenantDomain, MessageContext messageContext) throws APIManagementException;
      public Response getTenantsByState(String state, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
}
