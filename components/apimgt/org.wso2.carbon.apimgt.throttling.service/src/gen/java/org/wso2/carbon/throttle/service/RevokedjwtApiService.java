package org.wso2.carbon.throttle.service;

import javax.ws.rs.core.Response;

public abstract class RevokedjwtApiService {
    public abstract Response revokedjwtGet(String query);
}

