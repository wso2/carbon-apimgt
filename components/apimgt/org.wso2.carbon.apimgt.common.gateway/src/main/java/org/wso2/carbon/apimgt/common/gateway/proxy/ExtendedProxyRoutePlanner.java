/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.common.gateway.proxy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.wso2.carbon.apimgt.common.gateway.configdto.HttpClientConfigurationDTO;

/**
 * Extended ProxyRoutePlanner class to handle non proxy hosts implementation
 */
public class ExtendedProxyRoutePlanner extends DefaultProxyRoutePlanner {
    private static final Log log = LogFactory.getLog(ExtendedProxyRoutePlanner.class);
    private final HttpClientConfigurationDTO configuration;
    String proxyHost;
    int proxyPort;
    String protocol;

    public ExtendedProxyRoutePlanner(HttpHost host, HttpClientConfigurationDTO configuration) {
        super(host);
        this.configuration = configuration;
        this.proxyHost = configuration.getProxyHost();
        this.proxyPort = configuration.getProxyPort();
        this.protocol = configuration.getProxyProtocol();
    }

    private HttpHost getProxy(String scheme) {
        log.debug("Get proxy for scheme: " + scheme);

        String protoProxyHost = proxyHost;
        if (protoProxyHost == null) {
            return null;
        }

        int protoProxyPort = proxyPort;
        if (protoProxyPort < 1) {
            return null;
        }
        log.debug("set " + scheme + " proxy '" + protoProxyHost + ":" + protoProxyPort + "'");
        return new HttpHost(protoProxyHost, protoProxyPort, scheme);
    }

    private boolean doesTargetMatchNonProxy(HttpHost target) {
        String uriHost = target.getHostName();
        String uriScheme = target.getSchemeName();
        String[] nonProxyHosts = configuration.getNonProxyHosts();
        String[] targetProxyHosts = configuration.getTargetProxyHosts();

        if (nonProxyHosts != null) {
            for (String nonProxyHost : nonProxyHosts) {
                if ("*".equals(nonProxyHost)) {
                    return true;
                }
                if (uriHost.matches(nonProxyHost)) {
                    log.debug("sheme:'" + uriScheme + "', host:'" + uriHost + "' matches nonProxyHost '" + nonProxyHost
                            + "' : NO PROXY");
                    return true;
                }
            }
        }

        if (targetProxyHosts != null) {
            for (String targetProxyHost : targetProxyHosts) {
                if ("*".equals(targetProxyHost)) {
                    return false;
                }
                if (uriHost.matches(targetProxyHost)) {
                    return false;
                }
            }
        }

        log.debug("sheme:'" + uriScheme + "', host:'" + uriHost + "' : DEFAULT  (no match of non proxy hosts)");
        return false;
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, final HttpRequest request, final HttpContext context) {

        if (doesTargetMatchNonProxy(target)) {
            return null;
        }
        if (StringUtils.isNotEmpty(protocol)) {
            return getProxy(protocol);
        } else {
            return getProxy(target.getSchemeName());
        }
    }
}
