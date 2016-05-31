/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.throttling;

/**
 * Container class to store configuration information for Global CEP node
 */
public class GlobalThrottleEngineConfig {
    private String hostname;
    private String binaryTCPPort;
    private String binarySSLPort;
    private String HTTPPort;
    private String HTTPSPort;
    private String username;
    private String password;


    /**
     * Constructor enforcing to provide every parameter.
     *
     * @param hostname      Hostname of the global CEP node
     * @param binaryTCPPort Binary TCP transport port of the global CEP node (Default : 9611)
     * @param binarySSLPort Binary SSL transport port of the global CEP node (Default : 9711)
     * @param HTTPSPort     HTTPS port of the global CEP node (Default : 9443)
     * @param username      Username of the user to authenticate to CEP before start sending events (Default : admin)
     * @param password      Password of the user to authenticate to CEP before start sending events (Default : admin)
     */
    public GlobalThrottleEngineConfig(String hostname, String binaryTCPPort, String binarySSLPort, String HTTPPort, String HTTPSPort, String username,
                                      String password) {
        this.hostname = hostname;
        this.binaryTCPPort = binaryTCPPort;
        this.binarySSLPort = binarySSLPort;
        this.HTTPPort = HTTPPort;
        this.HTTPSPort = HTTPSPort;
        this.username = username;
        this.password = password;

    }


    public String getHostname() {
        return hostname;
    }

    public String getBinaryTCPPort() {
        return binaryTCPPort;
    }

    public String getBinarySSLPort() {
        return binarySSLPort;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }


    public String getHTTPSPort() {
        return HTTPSPort;
    }

    public String getHTTPPort() {
        return HTTPPort;
    }
}
