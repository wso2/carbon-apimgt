/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.transport.passthru.config;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.synapse.transport.http.conn.ProxyAuthenticator;
import org.apache.synapse.transport.passthru.connections.TargetConnections;
import org.apache.synapse.transport.passthru.jmx.PassThroughTransportMetricsCollector;

/**
 * This class stores configuration specific to HTTP Connectors (Senders)
 */
public class TargetConfiguration extends BaseConfiguration {

    private HttpProcessor httpProcessor = null;

    private ProxyAuthenticator proxyAuthenticator = null;
    
    private int maxConnections = Integer.MAX_VALUE;

    /** Weather User-Agent header coming from client should be preserved */
    private boolean preserveUserAgentHeader = false;
    /** Weather Server header coming from server should be preserved */
    private boolean preserveServerHeader = true;

    private TargetConnections connections = null;

    public TargetConfiguration(ConfigurationContext configurationContext,
                               ParameterInclude parameters,
                               WorkerPool pool,
                               PassThroughTransportMetricsCollector metrics,
                               ProxyAuthenticator proxyAuthenticator) {
        super(configurationContext, parameters, pool, metrics);

        httpProcessor = new ImmutableHttpProcessor(
                new HttpRequestInterceptor[] {
                        new RequestContent(),
                        new RequestTargetHost(),
                        new RequestConnControl(),
                        new RequestUserAgent(),
                        new RequestExpectContinue()
         });
        this.proxyAuthenticator = proxyAuthenticator;
    }

    public void build() throws AxisFault {
        super.build();

        maxConnections = conf.getIntProperty(PassThroughConfigPNames.MAX_CONNECTION_PER_HOST_PORT,
                Integer.MAX_VALUE);
        preserveUserAgentHeader = conf.isPreserveUserAgentHeader();
        preserveServerHeader = conf.isPreserveServerHeader();
    }

    public HttpParams getHttpParams() {
        return httpParams;
    }

    public IOReactorConfig getIOReactorConfig() {
        return ioReactorConfig;
    }

    public HttpProcessor getHttpProcessor() {
        return httpProcessor;
    }
    
    public ProxyAuthenticator getProxyAuthenticator() {
        return proxyAuthenticator;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public boolean isPreserveUserAgentHeader() {
        return preserveUserAgentHeader;
    }

    public boolean isPreserveServerHeader() {
        return preserveServerHeader;
    }

    public TargetConnections getConnections() {
        return connections;
    }

    public void setConnections(TargetConnections connections) {
        this.connections = connections;
    }
}
