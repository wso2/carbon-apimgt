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
import org.apache.synapse.SynapseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.authenticators.WebAppAuthenticator;
import org.wso2.carbon.apimgt.rest.api.util.impl.WebAppAuthenticatorImpl;


import javax.ws.rs.core.Response;
import java.util.regex.Pattern;

/**
 * This class will validate incoming requests with OAUth authenticator headers
 * You can place this handler name in your web application if you need OAuth
 * based authentication.
 */
public class OAuthAuthenticationHandler implements RequestHandler {

    private static final Log logger = LogFactory.getLog(OAuthAuthenticationHandler.class);
    private static final String OAUTH_AUTHENTICATOR = "OAuth";
    private static final String REGEX_BEARER_PATTERN = "Bearer\\s";
    private static final Pattern PATTERN = Pattern.compile(REGEX_BEARER_PATTERN);
    private volatile WebAppAuthenticator authenticator;

    /**
     * This method will initialize Web APP authenticator to validate incoming requests
     * Here we will get implementation class and create object of it.
     */
    public void initializeAuthenticator() {
        try {
            //TODO Retrieve this class name from configuration and let it configurable.
          //  authenticator = (WebAppAuthenticator) APIUtil.getClassForName(
              //      RestApiConstants.REST_API_WEB_APP_AUTHENTICATOR_IMPL_CLASS_NAME).newInstance();
            authenticator = new WebAppAuthenticatorImpl();
        } catch (Exception e) {
            throw new SynapseException("Error while initializing authenticator of " + "type: ");
        }

    }

    /**
     * authenticate requests received at the REST API endpoint, using HTTP OAuth headers as the authentication
     * mechanism. This method returns a null value which indicates that the request to be processed.
     */
    @Override
    public Response handleRequest(Message message, ClassResourceInfo resourceInfo) {

        if (authenticator == null) {
            initializeAuthenticator();
        } else {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Authenticating request: " + message.getId()));
                }
                if (authenticator.authenticate(message)) {
                    return null;
                } else {
                    //TODO generate authentication failure response if we get auth failure
                    //handle oauth failure
                }
            } catch (APIManagementException e) {
                logger.error("Error while authenticating incoming request to API Manager REST API");
            }
        }
        return null;
    }


}