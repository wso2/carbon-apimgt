package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.MediationPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.MediationPoliciesApiServiceImpl;

public class MediationPoliciesApiServiceFactory {

   private final static MediationPoliciesApiService service = new MediationPoliciesApiServiceImpl();

   public static MediationPoliciesApiService getMediationPoliciesApi()
   {
      return service;
   }
}
