package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.RevokedjwtApiService;
import org.wso2.carbon.throttle.service.impl.RevokedjwtApiServiceImpl;

public class RevokedjwtApiServiceFactory {

   private final static RevokedjwtApiService service = new RevokedjwtApiServiceImpl();

   public static RevokedjwtApiService getRevokedjwtApi()
   {
      return service;
   }
}
