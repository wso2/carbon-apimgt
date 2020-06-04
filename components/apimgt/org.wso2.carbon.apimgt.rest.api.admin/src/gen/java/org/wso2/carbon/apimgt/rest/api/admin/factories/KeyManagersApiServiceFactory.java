package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.KeyManagersApiServiceImpl;

public class KeyManagersApiServiceFactory {

   private final static KeyManagersApiService service = new KeyManagersApiServiceImpl();

   public static KeyManagersApiService getKeyManagersApi()
   {
      return service;
   }
}
