package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.SelfSignupApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.SelfSignupApiServiceImpl;

public class SelfSignupApiServiceFactory {

   private final static SelfSignupApiService service = new SelfSignupApiServiceImpl();

   public static SelfSignupApiService getSelfSignupApi()
   {
      return service;
   }
}
