package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ApiProductsApiServiceImpl;

public class ApiProductsApiServiceFactory {

   private final static ApiProductsApiService service = new ApiProductsApiServiceImpl();

   public static ApiProductsApiService getApiProductsApi()
   {
      return service;
   }
}
