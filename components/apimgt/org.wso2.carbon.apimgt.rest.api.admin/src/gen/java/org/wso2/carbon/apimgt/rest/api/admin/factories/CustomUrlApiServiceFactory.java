package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.CustomUrlApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.CustomUrlApiServiceImpl;

public class CustomUrlApiServiceFactory {

   private final static CustomUrlApiService service = new CustomUrlApiServiceImpl();

   public static CustomUrlApiService getCustomUrlApi()
   {
      return service;
   }
}
