package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ApisApiServiceImpl;

public class ApisApiServiceFactory {

   private final static ApisApiService service = new ApisApiServiceImpl();

   public static ApisApiService getApisApi()
   {
      return service;
   }
}
