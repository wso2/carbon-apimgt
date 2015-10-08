package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.impl.EnvironmentsApiServiceImpl;

public class EnvironmentsApiServiceFactory {

   private final static EnvironmentsApiService service = new EnvironmentsApiServiceImpl();

   public static EnvironmentsApiService getEnvironmentsApi()
   {
      return service;
   }
}
