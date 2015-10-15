package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.EnvironmentsApiServiceImpl;

public class EnvironmentsApiServiceFactory {

   private final static EnvironmentsApiService service = new EnvironmentsApiServiceImpl();

   public static EnvironmentsApiService getEnvironmentsApi()
   {
      return service;
   }
}
