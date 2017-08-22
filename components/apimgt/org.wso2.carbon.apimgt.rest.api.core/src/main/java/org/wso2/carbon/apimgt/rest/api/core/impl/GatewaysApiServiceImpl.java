package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.core.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

public class GatewaysApiServiceImpl extends GatewaysApiService {

    private static final Logger log = LoggerFactory.getLogger(GatewaysApiServiceImpl.class);

    /**
     * Register gateway
     *
     * @param body        RegistrationDTO
     * @param contentType Content-Type header value
     * @return Registration summary details
     * @throws NotFoundException If failed to register gateway
     */
    @Override
    public Response gatewaysRegisterPost(RegistrationDTO body, String contentType, Request request)
            throws NotFoundException {
        return Response.ok().build();
    }





}
