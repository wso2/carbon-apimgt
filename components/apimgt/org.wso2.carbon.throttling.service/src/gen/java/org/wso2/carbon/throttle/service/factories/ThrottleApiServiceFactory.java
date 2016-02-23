package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.ThrottleApiService;
import org.wso2.carbon.throttle.service.impl.ThrottleApiServiceImpl;

public class ThrottleApiServiceFactory {

   private final static ThrottleApiService service = new ThrottleApiServiceImpl();

   public static ThrottleApiService getThrottleApi()
   {
      return service;
   }
}
