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

package org.wso2.carbon.apimgt.impl.proxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

public class ExtendedProxyRoutePlanner extends DefaultProxyRoutePlanner {
    private static final Log log = LogFactory.getLog(ExtendedProxyRoutePlanner.class);
    APIManagerConfiguration configuration;
    String nonProxyHosts;
    String proxyHost;
    String proxyPort;

    public ExtendedProxyRoutePlanner(HttpHost host, APIManagerConfiguration configuration) {
        super(host);
        this.configuration = configuration;
        this.nonProxyHosts = configuration.getFirstProperty(APIConstants.NON_PROXY_HOSTS);
        this.proxyHost = configuration.getFirstProperty(APIConstants.PROXY_HOST);
        this.proxyPort = configuration.getFirstProperty(APIConstants.PROXY_PORT);
    }

    private HttpHost getProxy(String scheme) {
        log.debug("Get proxy for scheme: " + scheme);
        String proto = scheme;

        String protoProxyHost = proxyHost;
        if (protoProxyHost == null) {
            return null;
        }
        String proxyPortStr = proxyPort;
        if (proxyPortStr == null) {
            return null;
        }
        int protoProxyPort = -1;
        if (proxyPortStr != null) {
            try {
                protoProxyPort = Integer.valueOf(proxyPortStr);
            } catch (NumberFormatException nfe) {
                log.warn("invalid proxy port: " + proxyPortStr + ". proxy will be ignored");
                return null;
            }
        }
        if (protoProxyPort < 1) {
            return null;
        }
        log.debug("set " + proto + " proxy '" + protoProxyHost + ":" + protoProxyPort + "'");
        return new HttpHost(protoProxyHost, protoProxyPort, scheme);
    }

    private String[] getNonProxyHosts(String uriScheme) {
        String nonproxyHost = nonProxyHosts;
        if (nonproxyHost == null) {
            return null;
        }
        return nonproxyHost.split("\\|");
    }

    private boolean doesTargetMatchNonProxy(HttpHost target) {
        String uriHost = target.getHostName();
        String uriScheme = target.getSchemeName();
        String[] nonProxyHosts = getNonProxyHosts(uriScheme);
        int nphLength = nonProxyHosts != null ? nonProxyHosts.length : 0;
        if (nonProxyHosts == null || nphLength < 1) {
            log.debug("sheme:'" + uriScheme + "', host:'" + uriHost + "' : DEFAULT (0 non proxy host)");
            return false;
        }
        for (String nonProxyHost : nonProxyHosts) {
            if (uriHost.matches(nonProxyHost)) {
                log.debug("sheme:'" + uriScheme + "', host:'" + uriHost + "' matches nonProxyHost '" + nonProxyHost + "' : NO PROXY");
                return true;
            }
        }
        log.debug("sheme:'" + uriScheme + "', host:'" + uriHost + "' : DEFAULT  (no match of " + nphLength + " non proxy host)");
        return false;
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, final HttpRequest request, final HttpContext context)
            throws HttpException {
        if (doesTargetMatchNonProxy(target)) {
            return null;
        }
        return getProxy(target.getSchemeName());
    }
}
