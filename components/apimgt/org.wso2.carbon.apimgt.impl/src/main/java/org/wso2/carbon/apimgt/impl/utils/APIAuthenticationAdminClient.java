/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.handlers.security.stub.APIAuthenticationServiceStub;
import org.wso2.carbon.apimgt.handlers.security.stub.types.APIKeyMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * A service client implementation for the APIAuthenticationService (an admin service offered
 * by the API gateway).
 */
//TODO need to refactor code after design review
public class APIAuthenticationAdminClient {

    private static final Log log = LogFactory.getLog(APIAuthenticationAdminClient.class);
    public static final int TIME_OUT_IN_MILLI_SECONDS = 15 * 60 * 1000;

    private APIAuthenticationServiceStub stub;

    public APIAuthenticationAdminClient(Environment environment) throws AxisFault {
        stub = new APIAuthenticationServiceStub(ServiceReferenceHolder.getContextService().getClientConfigContext(),
                                            getServiceEndpointToClearCache(environment, "APIAuthenticationService"));
        setup(stub, environment);
    }

    public void invalidateKeys(List<APIKeyMapping> mappings) throws AxisFault {
        try {
            stub.invalidateKeys(mappings.toArray(new APIKeyMapping[mappings.size()]));
        } catch (Exception e) {
            throw new AxisFault("Error while invalidating API keys", e);
        }
    }
    public void invalidateOAuthKeys(String consumerKey,String authUser) throws AxisFault {
        try {
            stub.invalidateOAuthKeys(consumerKey,authUser);
        } catch (Exception e) {
            throw new AxisFault("Error while invalidating API keys", e);
        }
    }

    public void invalidateResourceCache(String apiContext, String apiVersion,
                                        String resourceURLContext, String httpVerb) throws AxisFault {
        try {
            stub.invalidateResourceCache(apiContext,apiVersion,resourceURLContext,httpVerb);
        } catch (Exception e) {
            throw new AxisFault("Error while invalidating API keys", e);
        }
    }

    /**
     * Removes the active tokens that are cached on the API Gateway
     * @param activeTokens - The active access tokens to be removed from the gateway cache.
     * @throws AxisFault - If a communication error occurs.
     */
    public void invalidateCachedTokens(Set<String> activeTokens) throws AxisFault {
        try {
            stub.invalidateCachedTokens(activeTokens.toArray(new String[activeTokens.size()]));
        } catch (RemoteException e) {
            log.error("RemoteException occurred when calling service method 'invalidateCachedTokens' of " +
                    "APIAuthenticationService", e);
            throw new AxisFault("RemoteException occurred when calling service method 'invalidateCachedTokens' of " +
                    " APIAuthenticationService" , e);
        }
    }

    /**
     * Removes a given username that is cached on the API Gateway
     *
     * @param username - The username to be removed from the gateway cache.
     * @throws AxisFault - If a communication error occurs.
     */
    public void invalidateCachedUsername(String username) throws AxisFault {
        try {
            stub.invalidateCachedUsername(username);
        } catch (RemoteException e) {
            log.error("RemoteException occurred when calling service method 'invalidateCachedUsername' of " +
                    "APIAuthenticationService", e);
            throw new AxisFault("RemoteException occurred when calling service method 'invalidateCachedUsername' of " +
                    " APIAuthenticationService" , e);
        }
    }

    /**
     * Removes given usernames that is cached on the API Gateway
     *
     * @param username_list - The list of usernames to be removed from the gateway cache.
     * @throws AxisFault - If a communication error occurs.
     */
    public void invalidateCachedUsernames(String[] username_list) throws AxisFault {
        try {
            stub.invalidateCachedUsernames(username_list);
        } catch (RemoteException e) {
            log.error("RemoteException occurred when calling service method 'invalidateCachedUsernames' of " +
                    "APIAuthenticationService", e);
            throw new AxisFault("RemoteException occurred when calling service method 'invalidateCachedUsernames' of " +
                    " APIAuthenticationService" , e);
        }
    }

    /**
     * Log into the API gateway or keyMgt as an admin, and initialize the specified client stub using
     * the established authentication session. This method will also set some timeout
     * values and enable session management on the stub so that it can be successfully used
     * for any subsequent admin service invocations.
     *
     * @param stub A client stub to be setup
     * @throws AxisFault if an error occurs when logging into the API gateway
     */
    protected final void setup(Stub stub, Environment environment) throws AxisFault {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        String cookie = null;
        boolean loggedIn = false;

        String keyMgtKeyCacheEnabledString =
                config.getFirstProperty(APIConstants.KEY_MANAGER_TOKEN_CACHE);

        //If keyMgt server key cache enabled we login to KM
        if (keyMgtKeyCacheEnabledString != null) {
            boolean keyMgtKeyCacheEnabled = Boolean.parseBoolean(keyMgtKeyCacheEnabledString);
            if (keyMgtKeyCacheEnabled) {
                loggedIn = true;
                cookie = loginKeyMgt();
            }
        }

        if (!loggedIn) {
            //login to Gateway
            loggedIn = true;
            cookie = loginGateway(environment);
        }
        /*String gatewayKeyCacheEnabledString = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
        //If gateway key cache enabled we need to login to gateway
        if (gatewayKeyCacheEnabledString != null) {
            Boolean gatewayKeyCacheEnabled = Boolean.parseBoolean(gatewayKeyCacheEnabledString);
            if (gatewayKeyCacheEnabled) {
                cookie = loginGateway(environment);
            }
        }*/

        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setTimeOutInMilliSeconds(TIME_OUT_IN_MILLI_SECONDS);
        options.setProperty(HTTPConstants.SO_TIMEOUT, TIME_OUT_IN_MILLI_SECONDS);
        options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIME_OUT_IN_MILLI_SECONDS);
        options.setManageSession(true);
        options.setProperty(HTTPConstants.COOKIE_STRING, cookie);
    }

    /**
     * Login to the API gateway as an admin
     *
     * @return A session cookie string
     * @throws AxisFault if an error occurs while logging in
     */
    private String loginGateway(Environment environment) throws AxisFault {
        //APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                //getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String user = environment.getUserName();
        String password = environment.getPassword();
        String url = environment.getServerURL();

        if (url == null || user == null || password == null) {
            throw new AxisFault("Required API gateway admin configuration unspecified");
        }

        String host;
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("API gateway URL is malformed", e);
        }

        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(
                    ServiceReferenceHolder.getContextService().getClientConfigContext(), url + "AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(user, password, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating against the API gateway admin", e);
        }
    }

    /**
     * Login to the API keyMgt as an admin
     *
     * @return A session cookie string
     * @throws AxisFault if an error occurs while logging in
     */
    private String loginKeyMgt() throws AxisFault {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String user = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_USERNAME);
        String password = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_PASSWORD);
        String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);

        if (url == null || user == null || password == null) {
            throw new AxisFault("Required API keyMgt admin configuration unspecified");
        }

        String host;
        try {
            host = new URL(url).getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("API KeyMgt URL is malformed", e);
        }

        AuthenticationAdminStub authAdminStub = new AuthenticationAdminStub(null, url + "AuthenticationAdmin");
        ServiceClient client = authAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            authAdminStub.login(user, password, host);
            ServiceContext serviceContext = authAdminStub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        } catch (RemoteException e) {
            throw new AxisFault("Error while contacting the authentication admin services", e);
        } catch (LoginAuthenticationExceptionException e) {
            throw new AxisFault("Error while authenticating against the API keyMgt admin", e);
        }
    }

    /**
     * Computes endpoint to clear Key validation information cache
     *
     * @param environment Environment of the Gateway on which we need to invalidate the cache
     * @param serviceName Name of the admin service
     * @return A String representation of the service endpoint
     */
    private String getServiceEndpointToClearCache(Environment environment, String serviceName) {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String gatewayKeyCacheEnabledString = config.getFirstProperty(APIConstants.GATEWAY_TOKEN_CACHE_ENABLED);
        //If gateway key cache enabled we return gateway URL
        if (gatewayKeyCacheEnabledString != null) {
            boolean gatewayKeyCacheEnabled = Boolean.parseBoolean(gatewayKeyCacheEnabledString);
            if (gatewayKeyCacheEnabled) {
                return environment.getServerURL() + serviceName;
            }
        }
        String keyMgtKeyCacheEnabledString = config.getFirstProperty(APIConstants.KEY_MANAGER_TOKEN_CACHE);
        //If keyMgt server key cache enabled we return gateway URL
        if (keyMgtKeyCacheEnabledString != null) {
            boolean keyMgtKeyCacheEnabled = Boolean.parseBoolean(keyMgtKeyCacheEnabledString);
            if (keyMgtKeyCacheEnabled) {
                String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
                return url + serviceName;
            }
        }
        //By default return url of Gateway
        //String url = config.getFirstProperty(APIConstants.API_KEY_VALIDATOR_URL);
        String url = environment.getServerURL();
        return url + serviceName;
    }
}
