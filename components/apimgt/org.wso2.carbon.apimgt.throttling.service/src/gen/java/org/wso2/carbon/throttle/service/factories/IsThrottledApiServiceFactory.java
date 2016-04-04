package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.IsThrottledApiService;
import org.wso2.carbon.throttle.service.impl.IsThrottledApiServiceImpl;

public class IsThrottledApiServiceFactory {

   private final static IsThrottledApiService service = new IsThrottledApiServiceImpl();

   public static IsThrottledApiService getIsThrottledApi()
   {
      return service;
   }
}
