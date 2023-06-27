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
package org.wso2.carbon.apimgt.common.gateway.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.wso2.carbon.apimgt.common.gateway.configdto.HttpClientConfigurationDTO;
import org.wso2.carbon.apimgt.common.gateway.http.BrowserHostnameVerifier;
import org.wso2.carbon.apimgt.common.gateway.proxy.ExtendedProxyRoutePlanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * Utility Functions for Common gateway component.
 */
public class CommonAPIUtil {
    public static final String STRICT = "Strict";
    public static final String ALLOW_ALL = "AllowAll";
    public static final String DEFAULT_AND_LOCALHOST = "DefaultAndLocalhost";
    public static final String HOST_NAME_VERIFIER = "httpclient.hostnameVerifier";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";

    private static final HostnameVerifier strictHostNameVerifier = new DefaultHostnameVerifier();
    private static final HostnameVerifier browserHostNameVerifier = new BrowserHostnameVerifier();

    private static PoolingHttpClientConnectionManager getPoolingHttpClientConnectionManager(
            HttpClientConfigurationDTO clientConfiguration) {

        SSLConnectionSocketFactory socketFactory = createSocketFactory(clientConfiguration.getSslContext(),
                clientConfiguration.getHostnameVerifier());
        Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register(HTTP_PROTOCOL, PlainConnectionSocketFactory.getSocketFactory())
                            .register(HTTPS_PROTOCOL, socketFactory)
                            .build();
        return new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    }

    private static SSLConnectionSocketFactory createSocketFactory(SSLContext sslContext,
                                                                  HostnameVerifier hostnameVerifier) {
        return new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
    }

    /**
     * Return a http client instance
     *
     * @param protocol - service endpoint protocol http/https
     * @return {@link HttpClient} with all proxy, TLS, ConnectionPooling related configurations
     */
    public static HttpClient getHttpClient(String protocol, HttpClientConfigurationDTO clientConfiguration) {

        int maxTotal = clientConfiguration.getConnectionLimit();
        int defaultMaxPerRoute = clientConfiguration.getMaximumConnectionsPerRoute();

        boolean proxyEnabled = clientConfiguration.isProxyEnabled();
        String proxyHost = clientConfiguration.getProxyHost();
        int proxyPort = clientConfiguration.getProxyPort();
        String proxyUsername = clientConfiguration.getProxyUsername();
        char[] proxyPassword = clientConfiguration.getProxyPassword();
        String[] nonProxyHosts = clientConfiguration.getNonProxyHosts();
        String proxyProtocol = clientConfiguration.getProxyProtocol();

        if (proxyProtocol != null) {
            protocol = proxyProtocol;
        }

        PoolingHttpClientConnectionManager pool = getPoolingHttpClientConnectionManager(clientConfiguration);

        pool.setMaxTotal(maxTotal);
        pool.setDefaultMaxPerRoute(defaultMaxPerRoute);

        RequestConfig params = RequestConfig.custom().build();
        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(pool)
                .setDefaultRequestConfig(params);

        if (proxyEnabled) {
            HttpHost host = new HttpHost(proxyHost, proxyPort, protocol);
            DefaultProxyRoutePlanner routePlanner;
            if (nonProxyHosts.length > 0) {
                routePlanner = new ExtendedProxyRoutePlanner(host, clientConfiguration);
            } else {
                routePlanner = new DefaultProxyRoutePlanner(host);
            }
            clientBuilder.setRoutePlanner(routePlanner);
            if (!StringUtils.isBlank(proxyUsername) && proxyPassword.length > 0) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
                        new UsernamePasswordCredentials(proxyUsername, String.valueOf(proxyPassword)));
                clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
        }
        return clientBuilder.build();
    }
}
