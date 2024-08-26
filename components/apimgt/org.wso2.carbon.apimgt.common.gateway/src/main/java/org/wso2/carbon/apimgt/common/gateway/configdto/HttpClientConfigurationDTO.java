/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.apimgt.common.gateway.configdto;

import org.apache.http.ssl.SSLContexts;

import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Configuration related to Http Clients within gateway.
 */
public class HttpClientConfigurationDTO {

    private int connectionLimit;
    private int maximumConnectionsPerRoute;
    private int connectionTimeout;
    private boolean proxyEnabled;
    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private char[] proxyPassword = new char[]{};
    private String[] nonProxyHosts = new String[]{};
    private String proxyProtocol;
    private SSLContext sslContext;
    private HostnameVerifier hostnameVerifier;

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    private HttpClientConfigurationDTO() {
    }

    public int getConnectionLimit() {
        return connectionLimit;
    }

    public int getMaximumConnectionsPerRoute() {
        return maximumConnectionsPerRoute;
    }

    public boolean isProxyEnabled() {
        return proxyEnabled;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public char[] getProxyPassword() {
        return Arrays.copyOf(proxyPassword, proxyPassword.length);
    }

    public String[] getNonProxyHosts() {
        return Arrays.copyOf(nonProxyHosts, nonProxyHosts.length);
    }

    public String getProxyProtocol() {
        return proxyProtocol;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Builder class for @code{HTTPClientConfigurationDTO}
     */
    public static class Builder {

        private int connectionLimit;
        private int maximumConnectionsPerRoute;
        private int connectionTimeout;
        private boolean proxyEnabled;
        private String proxyHost;
        private int proxyPort;
        private String proxyUsername;
        private char[] proxyPassword = new char[]{};
        private String[] nonProxyHosts = new String[]{};
        private String proxyProtocol;
        private SSLContext sslContext;
        private HostnameVerifier hostnameVerifier;

        public Builder withConnectionParams(int connectionLimit, int maximumConnectionsPerRoute,
                                            int connectionTimeout) {
            this.connectionLimit = connectionLimit;
            this.maximumConnectionsPerRoute = maximumConnectionsPerRoute;
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder withProxy(String proxyHost, int proxyPort, String proxyUsername, String proxyPassword,
                                 String proxyProtocol, String[] nonProxyHosts) {
            this.proxyEnabled = true;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
            this.proxyUsername = proxyUsername;
            this.proxyPassword = proxyPassword != null ? proxyPassword.toCharArray() : new char[]{};
            this.proxyProtocol = proxyProtocol;
            this.nonProxyHosts = nonProxyHosts != null ?
                    Arrays.copyOf(nonProxyHosts, nonProxyHosts.length) : new String[]{};
            return this;
        }

        public Builder withSSLContext(SSLContext sslContext) {
            this.sslContext = sslContext;
            return this;
        }

        public Builder withSSLContext(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
            this.sslContext = sslContext;
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public HttpClientConfigurationDTO build() {
            HttpClientConfigurationDTO configuration = new HttpClientConfigurationDTO();
            configuration.connectionLimit = this.connectionLimit;
            configuration.maximumConnectionsPerRoute = this.maximumConnectionsPerRoute;
            configuration.connectionTimeout = this.connectionTimeout;
            configuration.proxyEnabled = this.proxyEnabled;
            configuration.proxyHost = this.proxyHost;
            configuration.proxyPort = this.proxyPort;
            configuration.proxyUsername = this.proxyUsername;
            configuration.proxyPassword = this.proxyPassword;
            configuration.proxyProtocol = this.proxyProtocol;
            configuration.nonProxyHosts = this.nonProxyHosts;
            configuration.hostnameVerifier = this.hostnameVerifier;
            if (this.sslContext != null) {
                configuration.sslContext = this.sslContext;
            } else {
                configuration.sslContext = SSLContexts.createDefault();
            }
            return configuration;
        }
    }
}
