package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.impl.ApisApiServiceImpl;

public class ApisApiServiceFactory {

   private final static ApisApiService service = new ApisApiServiceImpl();

   public static ApisApiService getApisApi()
   {
      return service;
   }
}
