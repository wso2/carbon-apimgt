package org.wso2.carbon.apimgt.rest.api.publisher;


import javax.ws.rs.core.Response;

public abstract class EnvironmentsApiService {
    public abstract Response environmentsGet(String apiId);
}

