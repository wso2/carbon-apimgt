package org.wso2.carbon.apimgt.rest.api.util.handlers;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OAuthAuthenticationHandler implements RequestHandler {

    private static final Log logger = LogFactory.getLog(OAuthAuthenticationHandler.class);
    private static final String OAUTH_AUTHENTICATOR = "OAuth";
    private static final String REGEX_BEARER_PATTERN = "Bearer\\s";
    private static final Pattern PATTERN = Pattern.compile(REGEX_BEARER_PATTERN);
    private static final String BEARER_TOKEN_TYPE = "bearer";
    private static final String RESOURCE_KEY = "resource";

    /**
     * authenticate requests received at the REST API endpoint, using HTTP OAuth headers as the authentication
     * mechanism. This method returns a null value which indicates that the request to be processed.
     */
    @Override
    public Response handleRequest(Message message, ClassResourceInfo resourceInfo) {

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Authenticating request: " + message.getId()));
        }
        String authHeader = ((ArrayList) ((TreeMap) (message.get(Message.PROTOCOL_HEADERS))).get("Authorization")).get(0).toString();
        Matcher matcher = PATTERN.matcher(authHeader);
        if (matcher.find()) {
            authHeader = authHeader.substring(matcher.end());
            if (authHeader != null) {
                return this.authenticate(authHeader);
            }
        }
        return null;
    }


    private Response authenticate(String authHeader) {
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);
        AccessTokenInfo tokenInfo = null;
        try {
            tokenInfo = KeyManagerHolder.getKeyManagerInstance().getTokenMetaData(authHeader);
        } catch (APIManagementException e) {
            logger.error("Error while retrieving token information for token: " + authHeader + e.getMessage());
        }
        try {
            // if authenticated
            if (tokenInfo != null && tokenInfo.isTokenValid()) {
                // If token is valid then we have to do other validations and set user and tenant to
                //carbon context. Scope validation should come here.
                String tenantDomain = MultitenantUtils.getTenantDomain(tokenInfo.getEndUserName());
                int tenantId;
                tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                carbonContext.setUsername(tokenInfo.getEndUserName());
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