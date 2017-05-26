package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.UserDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public abstract class SelfSignupApiService {
    public abstract Response selfSignupPost(UserDTO body
 ,String contentType
 , Request request) throws NotFoundException;
}
