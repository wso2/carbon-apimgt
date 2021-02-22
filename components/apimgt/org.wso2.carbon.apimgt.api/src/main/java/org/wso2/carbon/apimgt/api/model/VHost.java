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
import java.util.ArrayList;
import java.util.List;

/**
 * This class represent an Virtual Host
 */
public class VHost {
    private String host;
    private String httpContext = "";
    private Integer httpPort = DEFAULT_HTTP_PORT;
    private Integer httpsPort = DEFAULT_HTTPS_PORT;
    private Integer wsPort = DEFAULT_WS_PORT;
    private Integer wssPort = DEFAULT_WSS_PORT;

    public static final int DEFAULT_HTTP_PORT = 80;
    public static final int DEFAULT_HTTPS_PORT = 443;
    public static final int DEFAULT_WS_PORT = 9099;
    public static final int DEFAULT_WSS_PORT = 8099;

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String WS_PROTOCOL = "ws";
    public static final String WSS_PROTOCOL = "wss";
    public static final String PROTOCOL_SEPARATOR = "://";

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

    public Integer getWssPort() {
        return wssPort;
    }

    public void setWssPort(Integer wssPort) {
        this.wssPort = wssPort;
    }

    public String getHttpUrl() {
        return getUrl("http", httpPort == DEFAULT_HTTP_PORT ? "" : ":" + httpPort, httpContext);
    }

    public String getHttpsUrl() {
        return getUrl("https", httpsPort == DEFAULT_HTTPS_PORT ? "" : ":" + httpsPort, httpContext);
    }

    public String getWsUrl() {
        return getUrl("ws", wsPort == DEFAULT_WS_PORT ? "" : ":" + wsPort, "");
    }

    public String getWssUrl() {
        return getUrl("wss", wssPort == DEFAULT_WSS_PORT ? "" : ":" + wssPort, "");
    }

    private String getUrl(String protocol, String port, String context) {
        // {protocol}://{host}{port}{context}
        return String.format("%s://%s%s%s", protocol, host, port, context);
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
                        break;
                    case WS_PROTOCOL:
                        // URL is not parsing for ws protocols, hence change to http
                        url = new URL(HTTP_PROTOCOL + PROTOCOL_SEPARATOR + elem[1]);
                        vhost.setWsPort(url.getPort() < 0 ? DEFAULT_WS_PORT : url.getPort());
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
