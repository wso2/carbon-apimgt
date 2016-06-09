package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ThrottlingApiServiceImpl;

public class ThrottlingApiServiceFactory {

   private final static ThrottlingApiService service = new ThrottlingApiServiceImpl();

   public static ThrottlingApiService getThrottlingApi()
   {
      return service;
   }
}
