package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.BlockApiService;
import org.wso2.carbon.throttle.service.impl.BlockApiServiceImpl;

public class BlockApiServiceFactory {

   private final static BlockApiService service = new BlockApiServiceImpl();

   public static BlockApiService getBlockApi()
   {
      return service;
   }
}
