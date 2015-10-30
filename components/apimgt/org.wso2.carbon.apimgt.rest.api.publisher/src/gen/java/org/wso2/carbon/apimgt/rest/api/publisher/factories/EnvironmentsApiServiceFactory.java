package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.EnvironmentsApiServiceImpl;

public class EnvironmentsApiServiceFactory {

   private final static EnvironmentsApiService service = new EnvironmentsApiServiceImpl();

   public static EnvironmentsApiService getEnvironmentsApi()
   {
      return service;
   }
}
