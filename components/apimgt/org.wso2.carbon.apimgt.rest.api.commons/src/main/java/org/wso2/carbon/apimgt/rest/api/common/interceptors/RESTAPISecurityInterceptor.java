/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.common.interceptors;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.api.RESTAPIAuthenticator;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import java.util.HashMap;
import java.util.Locale;
import javax.ws.rs.core.MediaType;

/**
 * Security Interceptor that does basic authentication for REST ApI requests.
 *
 */
@Component(
        name = "org.wso2.carbon.apimgt.rest.api.common.interceptors.RESTAPISecurityInterceptor",
        service = Interceptor.class,
        immediate = true
)
public class RESTAPISecurityInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(RESTAPISecurityInterceptor.class);
    //todo authenticatorName should be read from a configuration
    private static String authenticatorName = "org.wso2.carbon.apimgt.rest.api.common.impl.BasicAuthAuthenticator";
    private RESTAPIAuthenticator authenticatorImplClass = null;

    /**
     * preCall is run before a handler method call is made. If any of the preCalls throw exception or return false then
     * no other subsequent preCalls will be called and the request processing will be terminated,
     * also no postCall interceptors will be called.
     *
     * @param request           HttpRequest being processed.
     * @param response          HttpResponder to send response.
     * @param serviceMethodInfo Info on handler method that will be called.
     * @return true if the request processing can continue, otherwise the hook should send response and return false to
     * stop further processing.
     * @throws APIMgtSecurityException if error occurs while executing the preCall
     */
    @Override
    public boolean preCall(Request request, Response response, ServiceMethodInfo serviceMethodInfo) throws
            APIMgtSecurityException {
        ErrorHandler errorHandler = null;
        boolean isAuthenticated = false;
        /* TODO: Following string contains check is done to avoid checking security headers in non API requests.
         * Consider this as a tempory fix until MSF4J support context based interceptor registration */
        String requestURI = request.getUri().toLowerCase(Locale.ENGLISH);
        String publisherYml = null;
        if (requestURI.contains("/publisher")) {
            if (requestURI.contains("swagger.json")) {
                try {
                    publisherYml = RestApiUtil.getPublisherRestAPIResource();
                } catch (APIManagementException e) {
                    log.error("Couldn't find swagger.json for publisher", e);
                }
                response.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode()).setEntity(RestApiUtil
                        .convertYmlToJson(publisherYml)).setMediaType(MediaType.APPLICATION_JSON).send();
                return false;
            }
            return true;
        } else if (requestURI.contains("/store")) {
            if (requestURI.contains("swagger.json")) {
                try {
                    publisherYml = RestApiUtil.getStoreRestAPIResource();
                } catch (APIManagementException e) {
                    log.error("Couldn't find swagger.json for publisher", e);
                }
                response.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode()).setEntity(RestApiUtil
                        .convertYmlToJson(publisherYml)).setMediaType(MediaType.APPLICATION_JSON).send();
                return false;
            }
            return true;
        } else if (requestURI.contains("/editor")) {
            return true;
        }
        try {
            if (authenticatorImplClass == null) {
                Class<?> implClass = null;
                try {
                    implClass = Class.forName(authenticatorName);
                } catch (ClassNotFoundException e) {
                    throw new APIMgtSecurityException("Error while loading class " + authenticatorName, e);
                }
                authenticatorImplClass = (RESTAPIAuthenticator) implClass.newInstance();

            }
            isAuthenticated = authenticatorImplClass.authenticate(request, response, serviceMethodInfo);
        } catch (APIMgtSecurityException e) {
            errorHandler = e.getErrorHandler();
            log.error(e.getMessage() + " Requested Path: " + request.getUri());
        } catch (InstantiationException e) {
            log.error(e.getMessage() + " Error while instantiating authenticator: " + authenticatorName);
            isAuthenticated = false;
            errorHandler = ExceptionCodes.AUTH_GENERAL_ERROR;
        } catch (IllegalAccessException e) {
            log.error(e.getMessage() + " Error while accessing resource : " + authenticatorName);
            isAuthenticated = false;
            errorHandler = ExceptionCodes.AUTH_GENERAL_ERROR;

        }

        if (!isAuthenticated) {
            handleSecurityError(errorHandler, response);
        }
        return isAuthenticated;
    }

    /**
     * postCall is run after a handler method call is made. If any of the postCalls throw and exception then the
     * remaining postCalls will still be called. If the handler method was not called then postCall interceptors will
     * not be called.
     *
     * @param request           HttpRequest being processed.
     * @param status            Http status returned to the client.
     * @param serviceMethodInfo Info on handler method that was called.
     * @throws Exception if error occurs while executing the postCall
     */
    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) throws Exception {

    }

    /**
     * Handles error condition
     * @param errorHandler Security error code
     * @param responder    HttpResponder instance which is used send error messages back to the client
     */
    private void handleSecurityError(ErrorHandler errorHandler, Response responder) {
        HashMap<String, String> paramList = new HashMap<String, String>();
        ErrorDTO errorDTO = RestApiUtil.getErrorDTO(errorHandler, paramList);
        responder.setStatus(errorHandler.getHttpStatusCode());
        responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, RestApiConstants.AUTH_TYPE_OAUTH2);
        responder.setEntity(errorDTO);
        responder.send();
    }
}
