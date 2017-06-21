package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class PoliciesApiService {
    public abstract Response policiesGet(String accept
 , Request request) throws NotFoundException;
}
