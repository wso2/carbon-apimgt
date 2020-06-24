package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.store.v1.MeApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.OldAndNewPasswordsDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.mgt.common.UserAdminException;

import javax.ws.rs.core.Response;

public class MeApiServiceImpl implements MeApiService {

    private static final Log log = LogFactory.getLog(MeApiServiceImpl.class);

    public Response changeUserPassword(OldAndNewPasswordsDTO body, MessageContext messageContext) {

        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = RestApiUtil.getConsumer(username);
            apiConsumer.changeUserPassword(body.getOldPassword(), body.getNewPassword());
            return Response.ok().build();
        } catch (APIManagementException | UserAdminException e) {
            String errorMessage = "Error occurred while changing the user password"; // log the user name or not ? security concern of revealing username from a stolen token
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
