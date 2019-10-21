package org.wso2.carbon.throttle.service;

import org.wso2.carbon.throttle.service.dto.RevokeAPIKeyDTO;

import javax.ws.rs.core.Response;

public interface ApikeyApiService {
      public Response revokeAPIKey(RevokeAPIKeyDTO body);
}
