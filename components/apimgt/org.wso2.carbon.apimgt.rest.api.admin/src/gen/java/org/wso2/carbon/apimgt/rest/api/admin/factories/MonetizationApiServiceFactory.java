package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.MonetizationApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.MonetizationApiServiceImpl;

public class MonetizationApiServiceFactory {

   private final static MonetizationApiService service = new MonetizationApiServiceImpl();

   public static MonetizationApiService getMonetizationApi()
   {
      return service;
   }
}
