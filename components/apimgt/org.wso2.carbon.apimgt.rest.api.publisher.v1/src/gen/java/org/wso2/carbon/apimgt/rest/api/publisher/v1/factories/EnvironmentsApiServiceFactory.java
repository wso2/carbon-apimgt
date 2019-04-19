package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.EnvironmentsApiServiceImpl;

public class EnvironmentsApiServiceFactory {

   private final static EnvironmentsApiService service = new EnvironmentsApiServiceImpl();

   public static EnvironmentsApiService getEnvironmentsApi()
   {
      return service;
   }
}
