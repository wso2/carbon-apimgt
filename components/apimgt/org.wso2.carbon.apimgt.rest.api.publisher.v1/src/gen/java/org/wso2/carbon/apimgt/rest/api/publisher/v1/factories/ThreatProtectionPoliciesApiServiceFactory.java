package org.wso2.carbon.apimgt.rest.api.publisher.v1.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.ThreatProtectionPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.impl.ThreatProtectionPoliciesApiServiceImpl;

public class ThreatProtectionPoliciesApiServiceFactory {

   private final static ThreatProtectionPoliciesApiService service = new ThreatProtectionPoliciesApiServiceImpl();

   public static ThreatProtectionPoliciesApiService getThreatProtectionPoliciesApi()
   {
      return service;
   }
}
