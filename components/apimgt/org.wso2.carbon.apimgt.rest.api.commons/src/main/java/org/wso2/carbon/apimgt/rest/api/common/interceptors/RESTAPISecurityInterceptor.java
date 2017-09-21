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

import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.Json;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.common.APIConstants;
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

import static org.wso2.carbon.messaging.Constants.PROTOCOL;


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
    private static String authenticatorName = "org.wso2.carbon.apimgt.rest.api.common.impl.OAuth2Authenticator";
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

        /*
        * if request.method == option {
        *   response.addHeader("allow-cross-origin", "*")
        *   response.addHeader("allow-cross-origin-header")
        *   response.addHeader("allow-cross-origin-method")
        *   response
        *   return false;
        * */

        response.setHeader("Access-Control-Allow-Origin",
                "https://localhost:9292"); // TODo here the cross origin is alowed  .
        response.setHeader("Access-Control-Allow-Credentials", "true");
        if (request.getHttpMethod().equals(APIConstants.HTTP_OPTIONS)) {

            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
            response.setHeader("Access-Control-Allow-Headers", "Accept, Accept-Encoding, Accept-Language," +
                    " authorization, Cache-Control, Connection, Cookie, Host, Pragma," +
                    " Referer, User-Agent, Content-Type");
            response.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode()).send();
            return false;
        }
        if (!requestURI.contains("/api/am/")) {
            return true;
        }
        if (requestURI.contains("api/am/webserver/")) {
            return true;
        } else if (requestURI.contains("/login/token")) {
            return true;
        }

        String yamlContent = null;
        String protocol = (String) request.getProperty(PROTOCOL);
        Swagger swagger = null;
        if (requestURI.contains("/publisher")) {
            if (requestURI.contains("swagger.yaml")) {
                try {
                    yamlContent = RestApiUtil.getPublisherRestAPIResource();
                    response.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode()).setEntity(yamlContent)
                            .setMediaType("text/x-yaml").send();
                } catch (APIManagementException e) {
                    String msg = "Couldn't find swagger.yaml for publisher";
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
                    log.error(msg, e);
                    response.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                            .setEntity(errorDTO).send();
                }
                return false;
            }
        } else if (requestURI.contains("/store")) {
            if (requestURI.contains("swagger.json")) {
                try {
                    yamlContent = RestApiUtil.getStoreRestAPIResource();
                    swagger = new SwaggerParser().parse(yamlContent);
                    swagger.setBasePath(RestApiUtil.getContext(RestApiConstants.APPType.STORE));
                    swagger.setHost(RestApiUtil.getHost(protocol.toLowerCase(Locale.ENGLISH)));
                    response.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode()).setEntity(Json.pretty
                            (swagger)).setMediaType(MediaType.APPLICATION_JSON).send();

                } catch (APIManagementException e) {
                    String msg = "Couldn't find swagger.json for store";
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
                    log.error(msg, e);
                    response.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                            .setEntity(errorDTO).send();
                }
                return false;
            } else if (requestURI.contains("swagger.yaml")) {
                try {
                    yamlContent = RestApiUtil.getStoreRestAPIResource();
                    response.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode()).setEntity(yamlContent)
                            .setMediaType("text/x-yaml").send();
                } catch (APIManagementException e) {
                    String msg = "Couldn't find swagger.yaml for store";
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
                    log.error(msg, e);
                    response.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                            .setEntity(errorDTO).send();
                }
                return false;
            }
        } else if (requestURI.contains("/editor") || requestURI.contains("keyserver") || requestURI.contains("core")) {
            return true;
        } else if (requestURI.contains("/admin")) {
            if (requestURI.contains("swagger.json")) {
                try {
                    yamlContent = RestApiUtil.getAdminRestAPIResource();
                    swagger = new SwaggerParser().parse(yamlContent);
                    swagger.setBasePath(RestApiUtil.getContext(RestApiConstants.APPType.ADMIN));
                    swagger.setHost(RestApiUtil.getHost(protocol.toLowerCase(Locale.ENGLISH)));
                    response.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode())
                            .setEntity(Json.pretty(swagger)).setMediaType(MediaType.APPLICATION_JSON).send();
                } catch (APIManagementException e) {
                    String msg = "Couldn't find swagger.yaml for admin";
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
                    log.error(msg, e);
                    response.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                            .setEntity(errorDTO).send();
                }
                return false;
            } else if (requestURI.contains("swagger.yaml")) {
                try {
                    yamlContent = RestApiUtil.getAdminRestAPIResource();
                    response.setStatus(javax.ws.rs.core.Response.Status.OK.getStatusCode()).setEntity(yamlContent)
                            .setMediaType("text/x-yaml").send();
                } catch (APIManagementException e) {
                    String msg = "Couldn't find swagger.yaml for admin";
                    ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler());
                    log.error(msg, e);
                    response.setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                            .setEntity(errorDTO).send();
                }
                return false;
            }
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
        responder.setMediaType(MediaType.APPLICATION_JSON);
        responder.send();
    }
}
