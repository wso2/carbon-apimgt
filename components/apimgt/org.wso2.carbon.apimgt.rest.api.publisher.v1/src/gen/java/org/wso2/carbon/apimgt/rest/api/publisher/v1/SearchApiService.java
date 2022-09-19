package org.wso2.carbon.apimgt.rest.api.publisher.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;


import javax.ws.rs.core.Response;


public interface SearchApiService {
      public Response search(Integer limit, Integer offset, String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
}
