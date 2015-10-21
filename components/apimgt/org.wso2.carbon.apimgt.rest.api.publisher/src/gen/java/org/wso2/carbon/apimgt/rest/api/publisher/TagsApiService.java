package org.wso2.carbon.apimgt.rest.api.publisher;


import javax.ws.rs.core.Response;

public abstract class TagsApiService {
    public abstract Response tagsGet(String accept,String ifNoneMatch,String query);
}

