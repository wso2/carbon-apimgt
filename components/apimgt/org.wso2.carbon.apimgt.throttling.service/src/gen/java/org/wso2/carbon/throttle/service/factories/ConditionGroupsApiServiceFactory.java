package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.ConditionGroupsApiService;
import org.wso2.carbon.throttle.service.impl.ConditionGroupsApiServiceImpl;

public class ConditionGroupsApiServiceFactory {

   private final static ConditionGroupsApiService service = new ConditionGroupsApiServiceImpl();

   public static ConditionGroupsApiService getConditionGroupsApi()
   {
      return service;
   }
}
