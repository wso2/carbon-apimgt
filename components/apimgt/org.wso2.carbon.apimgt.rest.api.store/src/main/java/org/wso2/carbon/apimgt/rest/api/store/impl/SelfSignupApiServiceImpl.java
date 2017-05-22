package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.SelfSignupApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.UserDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.MiscMappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import javax.ws.rs.core.Response;

public class SelfSignupApiServiceImpl extends SelfSignupApiService {

    private static final Logger log = LoggerFactory.getLogger(SelfSignupApiServiceImpl.class);

    @Override
    public Response selfSignupPost(UserDTO body, String contentType, Request request) throws NotFoundException {
        try {
            APIStore apiStore = RestApiUtil.getConsumer();
            apiStore.selfSignUp(MiscMappingUtil.fromUserDTOToUser(body));
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while user signup: " + body.getUsername();
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.USERNAME, body.getUsername());
            paramList.put(APIMgtConstants.ExceptionsConstants.EMAIL, body.getEmail());
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().build();
    }
}
