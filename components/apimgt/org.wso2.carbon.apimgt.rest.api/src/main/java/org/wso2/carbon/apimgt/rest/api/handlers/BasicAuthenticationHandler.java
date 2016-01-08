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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class BasicAuthenticationHandler implements RequestHandler {

    private static final Log logger = LogFactory.getLog(BasicAuthenticationHandler.class);

    /**
     * authenticate requests received at the REST API endpoint, using HTTP basic-auth headers as the authentication
     * mechanism. This method returns a null value which indicates that the request to be processed. 
     */
    @Override
    public Response handleRequest(Message message, ClassResourceInfo resourceInfo) {

        if(logger.isDebugEnabled()) {
            logger.debug(String.format("Authenticating request: " + message.getId()));
        }

        // Extract auth header from the message.
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy == null) {
            // if auth header is missing, terminates with 401 Unauthorized response
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                    "Authentication failed: Basic authentication header is missing").build();
        }
        
        /*
        // If Mutual SSL is enabled
        HttpServletRequest request = (HttpServletRequest) message.get(RestAPIConstants.HTTP_REQUEST_HEADER);
        Object certObject = request.getAttribute(RestAPIConstants.CERTIFICATE_HEADER);
        */
        Object certObject = null;

        // Extract user credentials from the auth header.
        String username = StringUtils.trim(policy.getUserName());
        String password = StringUtils.trim(policy.getPassword());

        if (StringUtils.isEmpty(username)) {
            ErrorDTO errorDTO = RestApiUtil.getAuthenticationErrorDTO("Username cannot be null/empty.");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(errorDTO)
                    .build();
        } else if (StringUtils.isEmpty(password) && certObject == null) {
            ErrorDTO errorDTO = RestApiUtil.getAuthenticationErrorDTO("Password cannot be null/empty.");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(errorDTO)
                    .build();
        }

        return authenticate(certObject, username, password);
    }

    /**
     * authenticate with the user credentials.
     *
     * @param certObject   Certificate object of the request
     * @param username     Username
     * @param password     Password
     * @return             Response, if unauthorized. Null, if Authorized.
     */
    private Response authenticate(Object certObject, String username, String password){
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService)carbonContext.getOSGiService(RealmService.class, null);
        RegistryService registryService = (RegistryService) carbonContext.getOSGiService(RegistryService.class, null);
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            UserRealm userRealm = null;
            if (certObject == null) {
                userRealm = AnonymousSessionUtil.getRealmByTenantDomain(registryService, realmService, tenantDomain);
                if (userRealm == null) {
                    logger.error("Invalid domain or unactivated tenant login");
                    return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                            "Tenant not found").build();
                }
            }
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            // if authenticated
            if (certObject != null || userRealm.getUserStoreManager().authenticate(tenantAwareUsername, password)) {
                // set the correct tenant info for downstream code.
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                carbonContext.setUsername(username);
                return null;
            } else {
                logger.error(String.format("Authentication failed. Please check your username/password"));
                return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                        "Authentication failed. Please check your username/password").build();
            }
        } catch (Exception e) {
            logger.error("Authentication failed: ", e);
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                    "Authentication failed: ").build();
        }
    }
}