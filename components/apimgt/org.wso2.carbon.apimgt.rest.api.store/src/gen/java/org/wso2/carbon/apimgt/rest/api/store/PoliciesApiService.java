package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class PoliciesApiService {
    public abstract Response policiesTierLevelGet(String tierLevel
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response policiesTierLevelTierNameGet(String tierName
 ,String tierLevel
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
}
