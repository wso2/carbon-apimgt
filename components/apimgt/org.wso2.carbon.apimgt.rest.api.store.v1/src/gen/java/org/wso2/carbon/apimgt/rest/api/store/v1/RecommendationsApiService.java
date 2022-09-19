package org.wso2.carbon.apimgt.rest.api.store.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;


import javax.ws.rs.core.Response;


public interface RecommendationsApiService {
      public Response recommendationsGet(MessageContext messageContext) throws APIManagementException;
}
