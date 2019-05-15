package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.SdkGenApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SdkGenApiServiceImpl;

public class SdkGenApiServiceFactory {

   private final static SdkGenApiService service = new SdkGenApiServiceImpl();

   public static SdkGenApiService getSdkGenApi()
   {
      return service;
   }
}
