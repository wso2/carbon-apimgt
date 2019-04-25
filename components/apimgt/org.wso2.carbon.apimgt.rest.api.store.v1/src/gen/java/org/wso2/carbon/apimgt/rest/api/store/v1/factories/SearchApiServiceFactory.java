package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.SearchApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SearchApiServiceImpl;

public class SearchApiServiceFactory {

   private final static SearchApiService service = new SearchApiServiceImpl();

   public static SearchApiService getSearchApi()
   {
      return service;
   }
}
