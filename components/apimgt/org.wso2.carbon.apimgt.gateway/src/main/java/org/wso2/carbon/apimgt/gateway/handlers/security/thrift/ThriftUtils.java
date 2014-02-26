/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.gateway.handlers.security.thrift;

import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.base.ServerConfiguration;

import java.lang.String;

public class ThriftUtils {

    private static String userName;
    private static String password;
    private static String remoteServerURL;
    private static String remoteServerIP;
    private static String thriftServerHost;
    private static String remoteServerPort;
    private static String sessionId;
    private static int thriftPort;
    private static int thriftClientConnectionTimeOut;
    private static ThriftAuthClient thriftAuthClient = null;
    private static ThriftUtils thriftUtils;
    private static String trustStorePath;
    private static String trustStorePassword;

    private ThriftUtils() throws APISecurityException {
        try {

            ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
            //read configuration
            trustStorePath = serverConfiguration.getFirstProperty("Security.KeyStore.Location");
            trustStorePassword = serverConfiguration.getFirstProperty("Security.KeyStore.Password");
            String webContextRoot = serverConfiguration.getFirstProperty("WebContextRoot");

            APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();

            remoteServerURL = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
            remoteServerPort = remoteServerURL.split(":")[2].split("/")[0];
            //we expect in the form of : https://ip:port.. as in api-mgt.xml
            thriftServerHost=config.getFirstProperty(APIConstants.API_KEY_MANAGER_THRIFT_SERVER_HOST);
            if(thriftServerHost==null){
                thriftServerHost = remoteServerURL.split(":")[1].split("//")[1];
            }
            remoteServerIP = remoteServerURL.split(":")[1].split("//")[1];
            String thriftPortString = config.getFirstProperty(APIConstants.API_KEY_MANGER_THRIFT_CLIENT_PORT);
            String clientTimeOutString = config.getFirstProperty(APIConstants.API_KEY_MANGER_CONNECTION_TIMEOUT);
            if (thriftPortString == null || clientTimeOutString == null) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                               "Thrift client can not be created. Required details are not provided..");
            }

            thriftPort = Integer.parseInt(thriftPortString);
            thriftClientConnectionTimeOut = Integer.parseInt(clientTimeOutString);
            userName = config.getFirstProperty(APIConstants.API_KEY_MANAGER_USERNAME);
            password = config.getFirstProperty(APIConstants.API_KEY_MANAGER_PASSWORD);
            if (remoteServerIP == null || userName == null || password == null) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                                               "Required connection details for the thrift key management server not provided");
            }

            //create new authentication client and authenticate
            thriftAuthClient = new ThriftAuthClient(remoteServerIP, remoteServerPort, webContextRoot);
            sessionId = thriftAuthClient.getSessionId(userName, password);

        } catch (AuthenticationException e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR, e.getMessage());
        }
    }

    public static ThriftUtils getInstance() throws APISecurityException {
        if (thriftAuthClient == null) {
            synchronized (ThriftUtils.class) {
                if (thriftAuthClient == null) {
                    thriftUtils = new ThriftUtils();
                    return thriftUtils;
                } else {
                    return thriftUtils;
                }
            }
        }
        return thriftUtils;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public static String getRemoteServerURL() {
        return remoteServerURL;
    }

    public String getRemoteServerIP() {
        return remoteServerIP;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getThriftPort() {
        return thriftPort;
    }

    public int getThriftClientConnectionTimeOut() {
        return thriftClientConnectionTimeOut;
    }

    public ThriftAuthClient getThriftAuthClient() {
        return thriftAuthClient;
    }

    public String reLogin() throws AuthenticationException {
        sessionId = thriftAuthClient.getSessionId(userName, password);
        return sessionId;
    }

    public String getTrustStorePath() {
        return trustStorePath;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public static String getThriftServerHost() {
        return thriftServerHost;
    }

    public static void setThriftServerHost(String thriftServerHost) {
        ThriftUtils.thriftServerHost = thriftServerHost;
    }

}
