package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.ThrottlingPoliciesApiServiceImpl;

public class ThrottlingPoliciesApiServiceFactory {

   private final static ThrottlingPoliciesApiService service = new ThrottlingPoliciesApiServiceImpl();

   public static ThrottlingPoliciesApiService getThrottlingPoliciesApi()
   {
      return service;
   }
}
