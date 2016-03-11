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

package org.wso2.carbon.apimgt.rest.api.util.interceptors.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.impl.tl.ThreadLocalHttpHeaders;
import org.apache.cxf.jaxrs.impl.tl.ThreadLocalMessageContext;
import org.apache.cxf.jaxrs.impl.tl.ThreadLocalProtocolHeaders;
import org.apache.cxf.jaxrs.impl.tl.ThreadLocalProviders;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.TreeMap;

public class BasicAuthenticationInterceptor extends AbstractPhaseInterceptor {

    private static final Log logger = LogFactory.getLog(BasicAuthenticationInterceptor.class);
    public BasicAuthenticationInterceptor() {
        //We will use PRE_INVOKE phase as we need to process message before hit actual service
        super(Phase.PRE_INVOKE);
    }
    public void handleMessage(Message inMessage) {
        //by-passes the interceptor if user calls an anonymous api
        if (inMessage.get(RestApiConstants.AUTHENTICATION_REQUIRED) != null &&
                !Boolean.parseBoolean(RestApiConstants.AUTHENTICATION_REQUIRED)) {
            return;
        }

        if(handleRequest(inMessage, null)){
            /*String requestedTenant = ((ArrayList) ((TreeMap) (inMessage.get(Message.PROTOCOL_HEADERS))).get("X-WSO2_Tenant")).get(0).toString();
            if(requestedTenant!=null){
                RestApiUtil.setThreadLocalRequestedTenant(requestedTenant);
            }
            else {
                RestApiUtil.unsetThreadLocalRequestedTenant();
            }*/
            if(logger.isDebugEnabled()) {
                logger.debug("User logged into Web app using Basic Authentication");
            }
        }
        else{
            ErrorDTO errorDetail = new ErrorDTO();
            errorDetail.setCode((long)401);
            errorDetail.setMoreInfo("");
            errorDetail.setMessage("");
            errorDetail.setDescription("Unauthenticated request");
            Response response = Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(errorDetail)
                    .build();
            inMessage.getExchange().put(Response.class, response);
        }
    }

    public boolean handleRequest(Message message, ClassResourceInfo resourceInfo) {

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Authenticating request: " + message.getId()));
        }

        // Extract auth header from the message.
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy == null) {
            // if auth header is missing, terminates with 401 Unauthorized response
            logger.error("Authentication failed: Basic authentication header is missing");
            return false;
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
            logger.error("Username cannot be null/empty.");
            return false;
        } else if (StringUtils.isEmpty(password) && certObject == null) {
            logger.error("Password cannot be null/empty.");
            return false;
        }

        return authenticate(certObject, username, password);
    }

    /**
     * authenticate with the user credentials.
     *
     * @param certObject Certificate object of the request
     * @param username   Username
     * @param password   Password
     * @return Response, if unauthorized. Null, if Authorized.
     */
    private boolean authenticate(Object certObject, String username, String password) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
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
                    return false;
                }
            }
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            // if authenticated
            if (certObject != null || userRealm.getUserStoreManager().authenticate(tenantAwareUsername, password)) {
                // set the correct tenant info for downstream code.
                RestApiUtil.setThreadLocalRequestedTenant(username);
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                carbonContext.setUsername(username);
                return true;
            } else {
                logger.error(String.format("Authentication failed. Please check your username/password"));
                return false;
            }
        } catch (CarbonException e) {
            logger.error("Authentication failed for user: " + username, e);
            return false;
        } catch (UserStoreException e) {
            logger.error("Authentication failed for user: " + username, e);
            return false;
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            logger.error("Authentication failed for user: " + username, e);
            return false;
        }
    }
}