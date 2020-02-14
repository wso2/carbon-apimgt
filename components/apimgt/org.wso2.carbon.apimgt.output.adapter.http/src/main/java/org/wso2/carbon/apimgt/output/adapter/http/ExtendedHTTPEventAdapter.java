/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.output.adapter.http;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.wso2.carbon.apimgt.output.adapter.http.internal.util.ExtendedHTTPEventAdapterConstants;
import org.wso2.carbon.apimgt.impl.recommendationmgt.AccessTokenGenerator;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The Extended Http event adapter is used to publish events to using both Basic OAuth and OAuth2
 */
public class ExtendedHTTPEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(ExtendedHTTPEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private static ExecutorService executorService;
    private String clientMethod;
    private int tenantId;

    private String contentType;
    private static HttpConnectionManager connectionManager;
    private static HttpClient httpClient = null;
    private HostConfiguration hostConfiguration = null;
    private AccessTokenGenerator accessTokenGenerator;

    public ExtendedHTTPEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                                    Map<String, String> globalProperties) {

        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        this.clientMethod = eventAdapterConfiguration.getStaticProperties()
                .get(ExtendedHTTPEventAdapterConstants.ADAPTER_HTTP_CLIENT_METHOD);
    }

    @Override
    public void init() throws OutputEventAdapterException {

        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        //ExecutorService will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(
                        globalProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = ExtendedHTTPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer.parseInt(
                        globalProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = ExtendedHTTPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(
                        globalProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = ExtendedHTTPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
            }

            if (globalProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer.parseInt(
                        globalProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = ExtendedHTTPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }
            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(jobQueSize));

            //configurations for the httpConnectionManager which will be shared by every http adapter
            int defaultMaxConnectionsPerHost;
            int maxTotalConnections;

            if (globalProperties.get(ExtendedHTTPEventAdapterConstants.DEFAULT_MAX_CONNECTIONS_PER_HOST) != null) {
                defaultMaxConnectionsPerHost = Integer
                        .parseInt(globalProperties
                                .get(ExtendedHTTPEventAdapterConstants.DEFAULT_MAX_CONNECTIONS_PER_HOST));
            } else {
                defaultMaxConnectionsPerHost =
                        ExtendedHTTPEventAdapterConstants.DEFAULT_DEFAULT_MAX_CONNECTIONS_PER_HOST;
            }

            if (globalProperties.get(ExtendedHTTPEventAdapterConstants.MAX_TOTAL_CONNECTIONS) != null) {
                maxTotalConnections = Integer
                        .parseInt(globalProperties.get(ExtendedHTTPEventAdapterConstants.MAX_TOTAL_CONNECTIONS));
            } else {
                maxTotalConnections = ExtendedHTTPEventAdapterConstants.DEFAULT_MAX_TOTAL_CONNECTIONS;
            }

            connectionManager = new MultiThreadedHttpConnectionManager();
            connectionManager.getParams().setDefaultMaxConnectionsPerHost(defaultMaxConnectionsPerHost);
            connectionManager.getParams().setMaxTotalConnections(maxTotalConnections);

            Map<String, String> staticProperties = eventAdapterConfiguration.getStaticProperties();
            if (staticProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_OAUTH_URL) != null) {
                accessTokenGenerator = new AccessTokenGenerator(
                        staticProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_OAUTH_URL),
                        staticProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_OAUTH_CONSUMER_KEY),
                        staticProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_OAUTH_CONSUMER_SECRET));
            }
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {

        throw new TestConnectionNotSupportedException("Test connection is not available");
    }

    @Override
    public void connect() {

        this.checkHTTPClientInit(eventAdapterConfiguration.getStaticProperties());
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        //Load dynamic properties
        String url = dynamicProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_MESSAGE_URL);
        String username = dynamicProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_USERNAME);
        String password = dynamicProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_PASSWORD);

        Map<String, String> headers = this
                .extractHeaders(dynamicProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_HEADERS));
        String payload = message.toString();

        try {
            if (accessTokenGenerator != null) {
                String accessToken = accessTokenGenerator.getAccessToken();
                executorService.execute(new HTTPSender(url, payload, accessToken, headers, httpClient));
            } else if (username != null && password != null) {
                executorService.execute(new HTTPSender(url, payload, username, password, headers, httpClient));
            } else {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message,
                        "Incorrect publishing configurations", log, tenantId);
            }
        } catch (RejectedExecutionException e) {
            EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Job queue is full", e,
                    log, tenantId);
        }
    }

    @Override
    public void disconnect() {
        //not required
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public boolean isPolled() {

        return false;
    }

    private void checkHTTPClientInit(Map<String, String> staticProperties) {

        if (this.httpClient != null) {
            return;
        }

        synchronized (ExtendedHTTPEventAdapter.class) {
            if (this.httpClient != null) {
                return;
            }

            httpClient = new HttpClient(connectionManager);
            String proxyHost = staticProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_PROXY_HOST);
            String proxyPort = staticProperties.get(ExtendedHTTPEventAdapterConstants.ADAPTER_PROXY_PORT);
            if (proxyHost != null && proxyHost.trim().length() > 0) {
                try {
                    HttpHost host = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
                    this.httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
                } catch (NumberFormatException e) {
                    log.error("Invalid proxy port: " + proxyPort + ", "
                            + "ignoring proxy settings for HTTP output event adaptor.", e);
                }
            }

            String messageFormat = eventAdapterConfiguration.getMessageFormat();
            if (ExtendedHTTPEventAdapterConstants.JSON_STRING.equalsIgnoreCase(messageFormat)) {
                contentType = ExtendedHTTPEventAdapterConstants.APPLICATION_JSON_CONTENT_TYPE;
            } else if (ExtendedHTTPEventAdapterConstants.TEXT_STRING.equalsIgnoreCase(messageFormat)) {
                contentType = ExtendedHTTPEventAdapterConstants.TEXT_PLAIN_CONTENT_TYPE;
            } else {
                contentType = ExtendedHTTPEventAdapterConstants.TEXT_XML_CONTENT_TYPE;
            }

        }

    }

    private Map<String, String> extractHeaders(String headers) {

        if (headers == null || headers.isEmpty()) {
            return null;
        }

        String[] entries = headers.split(ExtendedHTTPEventAdapterConstants.HEADER_SEPARATOR);
        String[] keyValue;
        Map<String, String> result = new HashMap<String, String>();
        for (String header : entries) {
            keyValue = header.split(ExtendedHTTPEventAdapterConstants.ENTRY_SEPARATOR, 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0].trim(), keyValue[1].trim());
            } else {
                log.warn("Header property '" + header + "' is not defined in the correct format.");
            }
        }
        return result;
    }

    /**
     * This class represents a job to send an HTTP request to a target URL.
     */
    class HTTPSender implements Runnable {

        private String url;

        private String payload;

        private String accessToken;

        private String userName;

        private String password;

        private Map<String, String> headers;

        private HttpClient httpClient;

        public HTTPSender(String url, String payload, String accessToken, Map<String, String> headers,
                          HttpClient httpClient) {

            this.url = url;
            this.payload = payload;
            this.accessToken = accessToken;
            this.headers = headers;
            this.httpClient = httpClient;
        }

        /**
         * If user name and password is given, basic auth is used. If not OAuth2 is used.
         */
        public HTTPSender(String url, String payload, String userName, String password, Map<String, String> headers,
                          HttpClient httpClient) {

            this.url = url;
            this.payload = payload;
            this.userName = userName;
            this.password = password;
            this.headers = headers;
            this.httpClient = httpClient;
        }

        public String getUrl() {

            return url;
        }

        public String getPayload() {

            return payload;
        }

        public String getAccessToken() {

            return accessToken;
        }

        public Map<String, String> getHeaders() {

            return headers;
        }

        public HttpClient getHttpClient() {

            return httpClient;
        }

        public String getUserName() {

            return userName;
        }

        public String getPassword() {

            return password;
        }

        public void run() {

            EntityEnclosingMethod method = null;
            try {
                if (clientMethod.equalsIgnoreCase(ExtendedHTTPEventAdapterConstants.CONSTANT_HTTP_PUT)) {
                    method = new PutMethod(this.getUrl());
                } else {
                    method = new PostMethod(this.getUrl());
                }
                if (hostConfiguration == null) {
                    URL hostUrl = new URL(this.getUrl());
                    hostConfiguration = new HostConfiguration();
                    hostConfiguration.setHost(hostUrl.getHost(), hostUrl.getPort(), hostUrl.getProtocol());
                }
                method.setRequestEntity(new StringRequestEntity(this.getPayload(), contentType,
                        ExtendedHTTPEventAdapterConstants.UTF_EIGHT_CONTENT_TYPE));
                if (this.getAccessToken() != null && !this.getAccessToken().isEmpty()) {
                    method.setRequestHeader(ExtendedHTTPEventAdapterConstants.AUTHORIZATION_HEADER_DEFAULT,
                            ExtendedHTTPEventAdapterConstants.AUTHORIZATION_BEARER + this.getAccessToken());
                } else if (this.getUserName() != null && this.getPassword() != null) {
                    method.setRequestHeader(ExtendedHTTPEventAdapterConstants.AUTHORIZATION_HEADER_DEFAULT,
                            ExtendedHTTPEventAdapterConstants.AUTHORIZATION_BASIC + Base64
                            .encode((this.getUserName() + ExtendedHTTPEventAdapterConstants.ENTRY_SEPARATOR + this
                                    .getPassword()).getBytes()));
                }
                if (this.getHeaders() != null) {
                    for (Map.Entry<String, String> header : this.getHeaders().entrySet()) {
                        method.setRequestHeader(header.getKey(), header.getValue());
                    }
                }
                this.getHttpClient().executeMethod(hostConfiguration, method);
            } catch (IOException e) {
                EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), this.getPayload(),
                        "Cannot connect to " + this.getUrl(), e, log, tenantId);
            } finally {
                if (method != null) {
                    method.releaseConnection();
                }
            }
        }
    }

}
