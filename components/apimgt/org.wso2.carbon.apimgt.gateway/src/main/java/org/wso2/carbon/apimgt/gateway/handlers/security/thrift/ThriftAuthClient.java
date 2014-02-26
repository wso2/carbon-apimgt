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
package org.wso2.carbon.apimgt.gateway.handlers.security.thrift;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.base.ServerConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ThriftAuthClient {

    private String sessionId = null;
    private THttpClient client = null;

    public String getSessionId(String userName, String password) throws AuthenticationException {
        try {
            //if (sessionId == null) {
                TProtocol protocol = new TCompactProtocol(client);
                AuthenticatorService.Client authClient = new AuthenticatorService.Client(
                        protocol);
                client.open();
                sessionId = authClient.authenticate(userName, password);
                client.close();
            //}
        } catch (TTransportException e) {
            throw new AuthenticationException("Error in authenticating with thrift client..");
        } catch (TException e) {
            throw new AuthenticationException("Error in authenticating with thrift client..");
        } catch (AuthenticationException e) {
            throw new AuthenticationException("Error in authenticating with thrift client..");
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return sessionId;
    }

    public ThriftAuthClient(String serverIP, String remoteServerPort, String webContextRoot)
            throws AuthenticationException {
        try {
            TrustManager easyTrustManager = new X509TrustManager() {
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] x509Certificates,
                        String s)
                        throws java.security.cert.CertificateException {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] x509Certificates,
                        String s)
                        throws java.security.cert.CertificateException {
                }

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            //skip host name verification
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{easyTrustManager}, null);
            SSLSocketFactory sf = new SSLSocketFactory(sslContext);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme httpsScheme = new Scheme("https", sf, Integer.parseInt(remoteServerPort));

            DefaultHttpClient httpClient = new DefaultHttpClient();
            httpClient.getConnectionManager().getSchemeRegistry().register(httpsScheme);

            //If the webContextRoot is null or /
            if(webContextRoot == null || "/".equals(webContextRoot)){
                //Assign it an empty value since it is part of the thriftServiceURL.
                webContextRoot = "";
            }
            String thriftServiceURL = "https://" + serverIP + ":" + remoteServerPort +
                                      webContextRoot + "/" + "thriftAuthenticator";
            client = new THttpClient(thriftServiceURL, httpClient);

        } catch (TTransportException e) {
            throw new AuthenticationException("Error in creating thrift authentication client..");
        } catch (Exception e) {
            throw new AuthenticationException("Error in creating thrift authentication client..");
        }
    }
}
