package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.KeyTemplatesApiService;
import org.wso2.carbon.throttle.service.impl.KeyTemplatesApiServiceImpl;

public class KeyTemplatesApiServiceFactory {

   private final static KeyTemplatesApiService service = new KeyTemplatesApiServiceImpl();

   public static KeyTemplatesApiService getKeyTemplatesApi()
   {
      return service;
   }
}
