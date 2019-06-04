package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.HoneyPotApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.HoneyPotApiServiceImpl;

public class HoneyPotApiServiceFactory {

   private final static HoneyPotApiService service = new HoneyPotApiServiceImpl();

   public static HoneyPotApiService getHoneyPotApi()
   {
      return service;
   }
}
