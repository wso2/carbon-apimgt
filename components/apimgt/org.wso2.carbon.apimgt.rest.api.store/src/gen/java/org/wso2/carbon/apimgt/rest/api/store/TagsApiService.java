package org.wso2.carbon.apimgt.rest.api.store;

import javax.ws.rs.core.Response;

public abstract class TagsApiService {
    public abstract Response tagsGet(Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch);

    public abstract String tagsGetGetLastUpdatedTime(Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch);
}

