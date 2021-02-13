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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represent an Virtual Host
 */
public class VHost {
    private String host;
    private String httpContext;
    private Integer httpPort;
    private Integer httpsPort;
    private Integer wsPort;
    private Integer wssPort;

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
