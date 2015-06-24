/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.nhttp.config;

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.http.HttpHost;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.synapse.transport.http.conn.ProxyConfig;

public class ProxyConfigBuilder {

    private HttpHost proxy;
    private UsernamePasswordCredentials proxycreds;
    private String[] proxyBypass;

    public ProxyConfigBuilder parse(TransportOutDescription transportOut) {
        String proxyHost = null;
        int proxyPort = -1;
        Parameter proxyHostParam = transportOut.getParameter("http.proxyHost");
        if (proxyHostParam != null) {
            proxyHost = (String) proxyHostParam.getValue();
            Parameter proxyPortParam = transportOut.getParameter("http.proxyPort");
            if (proxyPortParam != null) {
                proxyPort = Integer.parseInt((String) proxyPortParam.getValue());
            }
        }
        if (proxyHost == null) {
            proxyHost = System.getProperty("http.proxyHost");
            if (proxyHost != null) {
                String s = System.getProperty("http.proxyPort");
                if (s != null) {
                    proxyPort = Integer.parseInt(s);
                }
            }
        }
        if (proxyHost != null) {
            proxy = new HttpHost(proxyHost, proxyPort >= 0 ? proxyPort : 80);

            String s = null;
            Parameter bypassListParam = transportOut.getParameter("http.nonProxyHosts");
            if (bypassListParam != null) {
                s = (String) bypassListParam.getValue();
            }
            if (s == null) {
                s = System.getProperty("http.nonProxyHosts");
            }
            if (s != null) {
                proxyBypass = s.split("\\|");
            }

            Parameter proxyUsernameParam = transportOut.getParameter("http.proxy.username");
            Parameter proxyPasswordParam = transportOut.getParameter("http.proxy.password");
            if (proxyUsernameParam != null) {
                proxycreds = new UsernamePasswordCredentials((String) proxyUsernameParam.getValue(),
                        proxyPasswordParam != null ? (String) proxyPasswordParam.getValue() : "");
            }
        }
        return this;
    }
    
    public ProxyConfig build() {
        return new ProxyConfig(proxy, proxycreds, proxyBypass);
    }
    
}
