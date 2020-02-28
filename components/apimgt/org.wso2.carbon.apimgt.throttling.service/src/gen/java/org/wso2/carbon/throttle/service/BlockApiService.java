package org.wso2.carbon.throttle.service;

import javax.ws.rs.core.Response;

public abstract class BlockApiService {
    public abstract Response blockGet();
}

