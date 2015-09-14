package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.impl.ApplicationsApiServiceImpl;

public class ApplicationsApiServiceFactory {

   private final static ApplicationsApiService service = new ApplicationsApiServiceImpl();

   public static ApplicationsApiService getApplicationsApi()
   {
      return service;
   }
}
