package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.TagsApiServiceImpl;

public class TagsApiServiceFactory {
    private static final TagsApiService service = new TagsApiServiceImpl();

    public static TagsApiService getTagsApi() {
        return service;
    }
}
