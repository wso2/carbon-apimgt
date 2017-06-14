package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class EndpointsApiService {
    public abstract Response endpointsEndpointIdGatewayConfigGet(String endpointId
            , String accept
            , Request request) throws NotFoundException;

    public abstract Response endpointsGet(Integer limit
            , String accept
            , Request request) throws NotFoundException;
}
