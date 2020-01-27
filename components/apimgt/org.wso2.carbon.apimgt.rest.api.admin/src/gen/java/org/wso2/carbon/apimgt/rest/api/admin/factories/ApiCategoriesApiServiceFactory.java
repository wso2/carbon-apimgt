package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.ApiCategoriesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ApiCategoriesApiServiceImpl;

public class ApiCategoriesApiServiceFactory {

   private final static ApiCategoriesApiService service = new ApiCategoriesApiServiceImpl();

   public static ApiCategoriesApiService getApiCategoriesApi()
   {
      return service;
   }
}
