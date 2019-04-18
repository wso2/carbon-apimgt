package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.ApiProductsApiServiceImpl;

public class ApiProductsApiServiceFactory {

   private final static ApiProductsApiService service = new ApiProductsApiServiceImpl();

   public static ApiProductsApiService getApiProductsApi()
   {
      return service;
   }
}
