package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.EndpointsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.EndpointsApiServiceImpl;

public class EndpointsApiServiceFactory {

   private final static EndpointsApiService service = new EndpointsApiServiceImpl();

   public static EndpointsApiService getEndpointsApi()
   {
      return service;
   }
}
