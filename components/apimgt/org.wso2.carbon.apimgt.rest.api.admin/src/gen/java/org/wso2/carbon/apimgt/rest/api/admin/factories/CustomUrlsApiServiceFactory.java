package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.CustomUrlsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.CustomUrlsApiServiceImpl;

public class CustomUrlsApiServiceFactory {

   private final static CustomUrlsApiService service = new CustomUrlsApiServiceImpl();

   public static CustomUrlsApiService getCustomUrlsApi()
   {
      return service;
   }
}
