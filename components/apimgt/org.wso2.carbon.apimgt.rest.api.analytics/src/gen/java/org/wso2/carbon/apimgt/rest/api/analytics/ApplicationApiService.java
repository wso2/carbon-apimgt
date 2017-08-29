package org.wso2.carbon.apimgt.rest.api.analytics;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class ApplicationApiService {
    public abstract Response applicationCountOverTimeGet(String startTime
 ,String endTime
 , Request request) throws NotFoundException;
}
