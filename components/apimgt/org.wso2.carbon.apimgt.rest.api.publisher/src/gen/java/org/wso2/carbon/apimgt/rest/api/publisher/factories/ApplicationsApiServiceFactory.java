package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ApplicationsApiServiceImpl;

public class ApplicationsApiServiceFactory {

   private final static ApplicationsApiService service = new ApplicationsApiServiceImpl();

   public static ApplicationsApiService getApplicationsApi()
   {
      return service;
   }
}
