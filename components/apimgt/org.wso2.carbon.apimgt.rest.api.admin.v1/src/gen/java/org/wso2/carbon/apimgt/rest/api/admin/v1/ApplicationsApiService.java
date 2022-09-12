package org.wso2.carbon.apimgt.rest.api.admin.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;



import javax.ws.rs.core.Response;


public interface ApplicationsApiService {
      public Response applicationsApplicationIdChangeOwnerPost(String owner, String applicationId, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdDelete(String applicationId, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdGet(String applicationId, MessageContext messageContext) throws APIManagementException;
      public Response applicationsGet(String user, Integer limit, Integer offset, String accept, String name, String tenantDomain, String sortBy, String sortOrder, MessageContext messageContext) throws APIManagementException;
}
