package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.TagsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.TagsApiServiceImpl;

public class TagsApiServiceFactory {
    private final static TagsApiService service = new TagsApiServiceImpl();

    public static TagsApiService getTagsApi() {
        return service;
    }
}
