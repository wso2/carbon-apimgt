package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.ThrottleAsStringApiService;
import org.wso2.carbon.throttle.service.impl.ThrottleAsStringApiServiceImpl;

public class ThrottleAsStringApiServiceFactory {

   private final static ThrottleAsStringApiService service = new ThrottleAsStringApiServiceImpl();

   public static ThrottleAsStringApiService getThrottleAsStringApi()
   {
      return service;
   }
}
