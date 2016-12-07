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


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.ErrorHandler;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.messaging.Headers;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.msf4j.security.oauth2.IntrospectionResponse;
import org.wso2.msf4j.util.SystemVariableUtil;

import javax.ws.rs.HttpMethod;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;


/*
*  This is the interceptor that is responsible for OAuth2 authentication and authorization of REST API calls.
*  @implements Interceptor
* */
@Component(
        name = "org.wso2.carbon.apimgt.rest.api.common.interceptors.RestAPIOAUTH2SecurityInterceptor",
        service = Interceptor.class,
        immediate = true
)
public class RestAPIOAUTH2SecurityInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger(RestAPIOAUTH2SecurityInterceptor.class);

    private static String authServerURL;

    static {
        authServerURL = SystemVariableUtil.getValue(RestApiConstants.AUTH_SERVER_URL_KEY,
                RestApiConstants.AUTH_SERVER_URL);
        if (authServerURL == null) {
            throw new RuntimeException(RestApiConstants.AUTH_SERVER_URL_KEY + " is not specified.");
        }
    }

    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) {
        org.wso2.carbon.apimgt.core.exception.ErrorHandler errorHandler = null;
        boolean isScopeValid = false;
        try {
            Headers headers = request.getHeaders();
            if (headers != null && headers.contains(RestApiConstants.AUTHORIZATION_HTTP_HEADER)) {
                String authHeader = headers.get(RestApiConstants.AUTHORIZATION_HTTP_HEADER);
                Map<String, String> tokenInfo = validateToken(authHeader);
                String restAPIResource = getRestAPIResource(request);

                //scope validation
                isScopeValid = validateScopes(request, serviceMethodInfo, tokenInfo.get(RestApiConstants.SCOPE),
                        restAPIResource);

            } else {
                throw new APIMgtSecurityException("Missing Authorization header in the request.`",
                        ExceptionCodes.INVALID_AUTHORIZATION_HEADER);
            }


        } catch (APIMgtSecurityException e) {
            errorHandler = e.getErrorHandler();
            log.error(e.getMessage() + " Requested Path: " + request.getUri());
        }
        if (!isScopeValid) {
            handleSecurityError(errorHandler, responder);

        }
        return isScopeValid;
    }

    //    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) {

    }

    /**
     * Extract the accessToken from the give Authorization header value and validates the accessToken
     * with an external key manager.
     *
     * @param authHeader Authorization Bearer header which contains the access token
     * @return responseData if the token is a valid token
     */
    private Map<String, String> validateToken(String authHeader) throws APIMgtSecurityException {
        // 1. Check whether this token is bearer token, if not return false
        String accessToken = extractAccessToken(authHeader);

        // 2. Send a request to key server's introspect endpoint to validate this token
        String responseStr = getValidatedTokenResponse(accessToken);
        Map<String, String> responseData = getResponseDataMap(responseStr);

        // 3. Process the response and return true if the token is valid.
        if (responseData == null || !Boolean.parseBoolean(responseData.get(IntrospectionResponse.ACTIVE))) {
            throw new APIMgtSecurityException("Invalid Access token.", ExceptionCodes.ACCESS_TOKEN_INACTIVE);
        }

        return responseData;
    }

    /**
     * @param authHeader Authorization Bearer header which contains the access token
     * @return access token
     * @throws APIMgtSecurityException if the Authorization header is invalid
     */
    private String extractAccessToken(String authHeader) throws APIMgtSecurityException {
        authHeader = authHeader.trim();
        if (authHeader.toLowerCase(Locale.US).startsWith(RestApiConstants.BEARER_PREFIX)) {
            // Split the auth header to get the access token.
            // Value should be in this format ("Bearer" 1*SP b64token)
            String[] authHeaderParts = authHeader.split(" ");
            if (authHeaderParts.length == 2) {
                return authHeaderParts[1];
            }
        }

        throw new APIMgtSecurityException("Invalid Authorization header: " +
                authHeader, ExceptionCodes.INVALID_AUTHORIZATION_HEADER);
    }

    /**
     * Validated the given accessToken with an external key server.
     *
     * @param accessToken AccessToken to be validated.
     * @return the response from the key manager server.
     */
    private String getValidatedTokenResponse(String accessToken) throws APIMgtSecurityException {
        URL url;
        try {
            url = new URL(authServerURL);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod(HttpMethod.POST);
            urlConn.getOutputStream()
                    .write(("token=" + accessToken + "&token_type_hint=" +
                            RestApiConstants.BEARER_PREFIX).getBytes(Charsets.UTF_8));
            return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
        } catch (java.io.IOException e) {
            log.error("Error invoking Authorization Server", e);
            throw new APIMgtSecurityException("Error invoking Authorization Server", ExceptionCodes.AUTH_GENERAL_ERROR);
        }
    }

    /**
     * @param responseStr validated token response string returned from the key server.
     * @return a Map of key, value pairs available the response String.
     */
    private Map<String, String> getResponseDataMap(String responseStr) {
        Gson gson = new Gson();
        Type typeOfMapOfStrings = new ExtendedTypeToken<Map<String, String>>() {
        }.getType();
        return gson.fromJson(responseStr, typeOfMapOfStrings);
    }

    /**
     * This class extends the {@link com.google.gson.reflect.TypeToken}.
     * Created due to the findbug issue when passing anonymous inner class.
     *
     * @param <T> Generic type
     */
    private static class ExtendedTypeToken<T> extends TypeToken {
    }

    /**
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

    /*
    * This method validates the given scope against scopes defined in the api resource
    * @param Request
    * @param ServiceMethodInfo
    * @param scopesToValidate scopes extracted from the access token
    * @return true if scope validation successful
    * */
    @SuppressFBWarnings({"DLS_DEAD_LOCAL_STORE"})
    private boolean validateScopes(Request request, ServiceMethodInfo serviceMethodInfo, String scopesToValidate,
                                   String restAPIResource) throws APIMgtSecurityException {
        final boolean authorized[] = {false};

        String path = (String) request.getProperty("REQUEST_URL");
        String verb = (String) request.getProperty("HTTP_METHOD");
        String resource = path.substring(path.length() - 1);


        if (!StringUtils.isEmpty(scopesToValidate)) {
            final List<String> scopes = Arrays.asList(scopesToValidate.split(" "));
            if (restAPIResource != null) {
                APIDefinition apiDefinition = new APIDefinitionFromSwagger20();
                try {
                    Map<String, Scope> apiDefinitionScopes = apiDefinition.getScopes(restAPIResource);
                    if (apiDefinitionScopes.isEmpty()) {
                        if (log.isDebugEnabled()) {
                            log.debug("Scope not defined in swagger for matching resource " + resource + " and verb "
                                    + verb + " . Hence consider as anonymous permission and let request to continue.");
                        }
                        // scope validation gets through if no scopes found in the api definition
                        authorized[0] = true;
                    }
                    apiDefinitionScopes.keySet()      //Only do the scope validation.hence key set is sufficient.
                            .forEach(scopeKey -> {
                                Optional<String> key = scopes.stream().filter(scp -> {
                                    return scp.equalsIgnoreCase(scopeKey);
                                }).findAny();
                                if (key.isPresent()) {
                                    authorized[0] = true;
                                }
                            });
                } catch (APIManagementException e) {
                    String message = "Error while validating scopes";
                    log.error(message);
                    throw new APIMgtSecurityException(message, ExceptionCodes.AUTH_GENERAL_ERROR);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Rest API resource could not be found for resource '" + resource + "'");
                }
            }
        } else { // scope validation gets through if access token does not contain scopes to validate
            authorized[0] = true;
        }

        if (!authorized[0]) {
            String message = "Scope validation fails for the scope " + scopesToValidate;
            log.error(message);
            throw new APIMgtSecurityException(message, ExceptionCodes.ACCESS_TOKEN_INACTIVE);

        }
        return authorized[0];
    }

    private String getRestAPIResource(Request request) throws APIMgtSecurityException {
        String path = (String) request.getProperty("REQUEST_URL");
        String restAPIResource = null;
        //this is publisher API so pick that API
        try {
            if (path.contains(RestApiConstants.REST_API_PUBLISHER_CONTEXT)) {
                restAPIResource = RestApiUtil.getPublisherRestAPIResource();
            } else if (path.contains(RestApiConstants.REST_API_STORE_CONTEXT)) {
                restAPIResource = RestApiUtil.getStoreRestAPIResource();
            } else if (path.contains(RestApiConstants.REST_API_ADMIN_CONTEXT)) {
                restAPIResource = RestApiUtil.getAdminRestAPIResource();
            } else {
                APIUtils.logAndThrowException("No matching Rest Api definition found for path:", log);
            }
        } catch (APIManagementException e) {
            throw new APIMgtSecurityException(e.getMessage(), ExceptionCodes.AUTH_GENERAL_ERROR);
        }

        return restAPIResource;

    }

    public static void setAuthServerUrl(String authServerURL) {
        RestAPIOAUTH2SecurityInterceptor.authServerURL = authServerURL;
    }


}
