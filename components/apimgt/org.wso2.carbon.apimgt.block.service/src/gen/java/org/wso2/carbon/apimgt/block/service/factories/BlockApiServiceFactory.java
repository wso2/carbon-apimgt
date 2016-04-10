package org.wso2.carbon.apimgt.block.service.factories;

import org.wso2.carbon.apimgt.block.service.BlockApiService;
import org.wso2.carbon.apimgt.block.service.impl.BlockApiServiceImpl;

public class BlockApiServiceFactory {

   private final static BlockApiService service = new BlockApiServiceImpl();

   public static BlockApiService getBlockApi()
   {
      return service;
   }
}
