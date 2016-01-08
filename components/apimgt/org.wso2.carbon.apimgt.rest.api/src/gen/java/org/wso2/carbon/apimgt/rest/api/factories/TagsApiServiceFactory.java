package org.wso2.carbon.apimgt.rest.api.factories;

import org.wso2.carbon.apimgt.rest.api.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.impl.TagsApiServiceImpl;

public class TagsApiServiceFactory {

   private final static TagsApiService service = new TagsApiServiceImpl();

   public static TagsApiService getTagsApi()
   {
      return service;
   }
}
