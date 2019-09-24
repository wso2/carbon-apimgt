package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.BotDataApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.BotDataApiServiceImpl;

public class BotDataApiServiceFactory {

   private final static BotDataApiService service = new BotDataApiServiceImpl();

   public static BotDataApiService getBotDataApi()
   {
      return service;
   }
}
