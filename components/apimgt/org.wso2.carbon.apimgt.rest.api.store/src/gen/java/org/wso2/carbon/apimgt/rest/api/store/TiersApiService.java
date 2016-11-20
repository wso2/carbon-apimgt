package org.wso2.carbon.apimgt.rest.api.store;

import javax.ws.rs.core.Response;

public abstract class TiersApiService {
    public abstract Response tiersTierLevelGet(String tierLevel,Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch);
    public abstract Response tiersTierLevelTierNameGet(String tierName,String tierLevel,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince);

    public abstract String tiersTierLevelGetGetLastUpdatedTime(String tierLevel,Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch);
    public abstract String tiersTierLevelTierNameGetGetLastUpdatedTime(String tierName,String tierLevel,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince);
}

