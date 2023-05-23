/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class represent an Virtual Host
 */
public class VHost {
    // host name from the http endpoint
    private String host;
    private String httpContext = "";
    private Integer httpPort = -1;
    private Integer httpsPort = -1;
    private Integer wsPort = DEFAULT_WS_PORT;
    private String wsHost;
    private Integer wssPort = DEFAULT_WSS_PORT;
    private String wssHost;
    private Integer websubHttpPort = DEFAULT_WEBSUB_HTTP_PORT;
    private Integer websubHttpsPort = DEFAULT_WEBSUB_HTTPS_PORT;

    public static final int DEFAULT_HTTP_PORT = 80;
    public static final int DEFAULT_HTTPS_PORT = 443;
    public static final int DEFAULT_WS_PORT = 9099;
    public static final int DEFAULT_WSS_PORT = 8099;
    public static final int DEFAULT_WEBSUB_HTTP_PORT = 9021;
    public static final int DEFAULT_WEBSUB_HTTPS_PORT = 8021;

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String WS_PROTOCOL = "ws";
    public static final String WSS_PROTOCOL = "wss";
    public static final String PROTOCOL_SEPARATOR = "://";

    private static final String WEBSUB_HTTP_PROTOCOL = "websub_http";
    private static final String WEBSUB_HTTPS_PROTOCOL = "websub_https";

    public VHost() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHttpContext() {
        return httpContext;
    }

    public void setHttpContext(String httpContext) {
        this.httpContext = httpContext;
    }

    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer httpPort) {
        this.httpPort = httpPort;
    }

    public Integer getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(Integer httpsPort) {
        this.httpsPort = httpsPort;
    }

    public Integer getWsPort() {
        return wsPort;
    }

    public void setWsPort(Integer wsPort) {
        this.wsPort = wsPort;
    }

    public String getWsHost() {
        return wsHost;
    }

    public void setWsHost(String wsHost) {
        this.wsHost = wsHost;
    }

    public Integer getWssPort() {
        return wssPort;
    }

    public void setWssPort(Integer wssPort) {
        this.wssPort = wssPort;
    }

    public String getWssHost() {
        return wssHost;
    }

    public void setWssHost(String wssHost) {
        this.wssHost = wssHost;
    }

    public Integer getWebsubHttpPort() {
        return websubHttpPort;
    }

    public void setWebsubHttpPort(Integer websubHttpPort) {
        this.websubHttpPort = websubHttpPort;
    }

    public Integer getWebsubHttpsPort() {
        return websubHttpsPort;
    }

    public void setWebsubHttpsPort(Integer websubHttpsPort) {
        this.websubHttpsPort = websubHttpsPort;
    }

    public String getHttpUrl() {
        return getUrl("http", host, httpPort == DEFAULT_HTTP_PORT ? "" : ":" + httpPort, httpContext);
    }

    public String getHttpsUrl() {
        return getUrl("https", host, httpsPort == DEFAULT_HTTPS_PORT ? "" : ":" + httpsPort, httpContext);
    }

    public String getWsUrl() {
        return getUrl("ws", wsHost, wsPort == DEFAULT_HTTP_PORT ? ""  : ":" + wsPort, "");
    }

    public String getWssUrl() {
        return getUrl("wss", wssHost, wssPort == DEFAULT_HTTPS_PORT ? "" : ":" + wssPort, "");
    }

    private String getUrl(String protocol, String hostName, String port, String context) {
        // {protocol}://{host}{port}{context}
        if (StringUtils.isNotEmpty(context) && !context.startsWith("/")) {
            context = "/" + context;
        }
        return String.format("%s://%s%s%s", protocol, hostName, port, context);
    }

    public static VHost fromEndpointUrls(String[] endpoints) throws APIManagementException {
        VHost vhost = new VHost();

        for (String endpoint : endpoints) {
            if (StringUtils.isEmpty(endpoint)) {
                continue;
            }

            String[] elem = endpoint.split(PROTOCOL_SEPARATOR);
            if (elem.length != 2) {
                throw new APIManagementException("Error reading gateway environment endpoint URL");
            }
            URL url; // URL is not parsing for ws and wss protocols
            try {
                switch (elem[0]) {
                    case HTTPS_PROTOCOL:
                        url = new URL(endpoint);
                        vhost.setHttpsPort(url.getPort() < 0 ? DEFAULT_HTTPS_PORT : url.getPort());
                        vhost.setHost(url.getHost());
                        vhost.setHttpContext(url.getPath());
                        break;
                    case HTTP_PROTOCOL:
                        url = new URL(endpoint);
                        vhost.setHttpPort(url.getPort() < 0 ? DEFAULT_HTTP_PORT : url.getPort());
                        String host = StringUtils.isNotEmpty(vhost.getHost()) ?
                                vhost.getHost() : url.getHost();
                        vhost.setHost(host);
                        String httpContext = StringUtils.isNotEmpty(vhost.getHttpContext()) ?
                                vhost.getHttpContext() : url.getPath();
                        vhost.setHttpContext(httpContext);
                        break;
                    case WSS_PROTOCOL:
                        // URL is not parsing for wss protocols, hence change to https
                        url = new URL(HTTPS_PROTOCOL + PROTOCOL_SEPARATOR + elem[1]);
                        vhost.setWssPort(url.getPort() < 0 ? DEFAULT_WSS_PORT : url.getPort());
                        vhost.setWssHost(url.getHost());
                        break;
                    case WS_PROTOCOL:
                        // URL is not parsing for ws protocols, hence change to http
                        url = new URL(HTTP_PROTOCOL + PROTOCOL_SEPARATOR + elem[1]);
                        vhost.setWsPort(url.getPort() < 0 ? DEFAULT_WS_PORT : url.getPort());
                        vhost.setWsHost(url.getHost());
                        break;
                    case WEBSUB_HTTP_PROTOCOL:
                        url = new URL(HTTP_PROTOCOL + PROTOCOL_SEPARATOR + elem[1]);
                        vhost.setWebsubHttpPort(url.getPort() < 0 ? DEFAULT_WEBSUB_HTTP_PORT : url.getPort());
                        break;
                    case WEBSUB_HTTPS_PROTOCOL:
                        url = new URL(HTTPS_PROTOCOL + PROTOCOL_SEPARATOR + elem[1]);
                        vhost.setWebsubHttpsPort(url.getPort() < 0 ? DEFAULT_WEBSUB_HTTPS_PORT : url.getPort());
                        break;
                }
            } catch (MalformedURLException e) {
                throw new APIManagementException("Error reading gateway environment endpoint URL", e);
            }
        }

        // host of Vhost is set by HTTP or HTTPS endpoints, if host is empty, the required fields are missing.
        if (StringUtils.isEmpty(vhost.getHost())) {
            throw new APIManagementException("Error while building VHost, missing required HTTP or HTTPS endpoint");
        }

        return vhost;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        VHost vHost = (VHost) obj;
        return StringUtils.equals(vHost.host, this.host)
                && StringUtils.equals(vHost.httpContext, this.httpContext)
                && vHost.httpPort.equals(this.httpPort)
                && vHost.httpsPort.equals(this.httpsPort)
                && vHost.wsPort.equals(this.wsPort)
                && vHost.wssPort.equals(this.wssPort);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((host == null) ? 0 : host.hashCode());
        result = prime * result
                + ((httpContext == null) ? 0 : httpContext.hashCode());
        result = prime * result + httpPort;
        result = prime * result + httpsPort;
        result = prime * result + wsPort;
        result = prime * result + wssPort;
        return result;
    }
}
