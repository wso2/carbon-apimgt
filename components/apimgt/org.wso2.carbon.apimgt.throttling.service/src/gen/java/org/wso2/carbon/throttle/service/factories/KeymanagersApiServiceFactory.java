package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.KeymanagersApiService;
import org.wso2.carbon.throttle.service.impl.KeymanagersApiServiceImpl;

public class KeymanagersApiServiceFactory {

   private final static KeymanagersApiService service = new KeymanagersApiServiceImpl();

   public static KeymanagersApiService getKeymanagersApi()
   {
      return service;
   }
}
