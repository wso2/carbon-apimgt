package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.OldAndNewPasswordsDTO;
import javax.ws.rs.core.Response;

public interface MeApiService {
      public Response changeUserPassword(OldAndNewPasswordsDTO body, MessageContext messageContext) throws APIManagementException;
}
