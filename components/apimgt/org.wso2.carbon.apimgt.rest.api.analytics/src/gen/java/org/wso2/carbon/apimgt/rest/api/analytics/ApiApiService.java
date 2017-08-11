package org.wso2.carbon.apimgt.rest.api.analytics;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class ApiApiService {
    public abstract Response apiApiInfoGet(String from
 ,String to
 ,String createdBy
 ,String apiFilter
 , Request request) throws NotFoundException;
    public abstract Response apiApisCreatedOverTimeGet(String from
 ,String to
 ,String createdBy
 , Request request) throws NotFoundException;
    public abstract Response apiSubscriberCountByApisGet(String createdBy
 , Request request) throws NotFoundException;
}
