package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.PoliciesApiServiceImpl;

public class PoliciesApiServiceFactory {

   private final static PoliciesApiService service = new PoliciesApiServiceImpl();

   public static PoliciesApiService getPoliciesApi()
   {
      return service;
   }
}
