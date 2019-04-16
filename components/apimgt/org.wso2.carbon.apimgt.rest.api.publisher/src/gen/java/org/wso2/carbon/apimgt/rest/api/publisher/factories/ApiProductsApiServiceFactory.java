package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ApiProductsApiServiceImpl;

public class ApiProductsApiServiceFactory {

   private final static ApiProductsApiService service = new ApiProductsApiServiceImpl();

   public static ApiProductsApiService getApiProductsApi()
   {
      return service;
   }
}
