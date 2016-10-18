package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ProductsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ProductsApiServiceImpl;

public class ProductsApiServiceFactory {

   private final static ProductsApiService service = new ProductsApiServiceImpl();

   public static ProductsApiService getProductsApi()
   {
      return service;
   }
}
