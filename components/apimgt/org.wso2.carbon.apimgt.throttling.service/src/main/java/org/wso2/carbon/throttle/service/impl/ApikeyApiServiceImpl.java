package org.wso2.carbon.throttle.service.impl;

import org.wso2.carbon.throttle.service.ApikeyApiService;
import org.wso2.carbon.throttle.service.dto.RevokeAPIKeyDTO;

import javax.ws.rs.core.Response;

public class ApikeyApiServiceImpl implements ApikeyApiService {
      @Override
      public Response revokeAPIKey(RevokeAPIKeyDTO body) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
