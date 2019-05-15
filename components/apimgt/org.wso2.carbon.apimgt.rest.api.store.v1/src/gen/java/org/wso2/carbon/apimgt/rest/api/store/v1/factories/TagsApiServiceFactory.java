package org.wso2.carbon.apimgt.rest.api.store.v1.factories;

import org.wso2.carbon.apimgt.rest.api.store.v1.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.impl.TagsApiServiceImpl;

public class TagsApiServiceFactory {

   private final static TagsApiService service = new TagsApiServiceImpl();

   public static TagsApiService getTagsApi()
   {
      return service;
   }
}
