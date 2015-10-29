package org.wso2.carbon.apimgt.rest.api.handlers;
/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.wso2.carbon.apimgt.rest.api.utils.EntitlementServiceClient;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.apache.cxf.message.Message;
//import org.wso2.carbon.identity.entitlement.proxy.PEPProxy;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class XACMLAuthenticationHandler implements RequestHandler {

    private static final Log logger = LogFactory.getLog(XACMLAuthenticationHandler.class);

    /**
     * isUserPermitted requests received at the ml endpoint, using HTTP basic-auth headers as the authentication
     * mechanism. This method returns a null value which indicates that the request to be processed.
     */
    @Override
    public Response handleRequest(Message message, ClassResourceInfo resourceInfo) {

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Authenticating request: " + message.getId()));
        }
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy == null) {
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                    "Authentication failed: Basic authentication header is missing").build();
        }
        Object certObject = null;
        String username = StringUtils.trim(policy.getUserName());
        if (StringUtils.isEmpty(username)) {
            ErrorDTO errorDTO = RestApiUtil.getAuthenticationErrorDTO("Username cannot be null/empty.");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(errorDTO)
                    .build();
        }
        return isUserPermitted(username, (String) message.get(Message.REQUEST_URI),
                (String) message.get(Message.HTTP_REQUEST_METHOD), null);
    }


    private Response isUserPermitted(String userName, String resource, String httpMethod, String[] arr) {
        try {
            EntitlementServiceClient client = new EntitlementServiceClient();
            client.validateAction(userName, resource, httpMethod, arr);
        } catch (Exception e) {
            logger.error("Error while validating XACML request" + e);
        }
        return null;
    }
}