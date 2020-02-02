/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.event.output.adapter.http.extended.oauth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class AccessTokenGeneratorImpl implements AccessTokenGenerator{

    private static final Log log = LogFactory.getLog(AccessTokenGeneratorImpl.class);

    private static volatile AccessTokenGeneratorImpl accessTokenGenerator = null;
    Long generatedTime = Long.valueOf(0);
    Long validityPeriod = Long.valueOf(3600000);
    String accessToken = null;
    public static final String STRICT = "Strict";
    public static final String ALLOW_ALL = "AllowAll";
    public static final String DEFAULT_AND_LOCALHOST = "DefaultAndLocalhost";
    public static final String HOST_NAME_VERIFIER = "httpclient.hostnameVerifier";

    public AccessTokenGeneratorImpl() {
    }

    public static AccessTokenGeneratorImpl getInstance() {

        if (accessTokenGenerator == null) {
            synchronized (AccessTokenGeneratorImpl.class) {
                if (accessTokenGenerator == null) {
                    accessTokenGenerator = new AccessTokenGeneratorImpl();
                }
            }
        }
        return accessTokenGenerator;
    }

    @Override
    public String getAccessToken(String oauthUrl, String consumerKey, String consumerSecret) {

        Long currentTime = System.currentTimeMillis();
        Long gen = this.generatedTime;
        Long val = this.validityPeriod;
        String access = this.accessToken;
        if (currentTime > gen + val || access == null) {
            return generateNewAccessToken(oauthUrl, consumerKey, consumerSecret);
        } else {
            return this.accessToken;
        }
    }

    public String generateNewAccessToken(String oauthUrl, String consumerKey, String consumerSecret) {
        if(oauthUrl != null && consumerKey != null && consumerSecret != null) {
            try {
                URL oauthURL = new URL(oauthUrl);
                int serverPort = oauthURL.getPort();
                String serverProtocol = oauthURL.getProtocol();

                HttpPost request = new HttpPost(oauthUrl);
                HttpClient httpClient = getHttpClient(serverPort, serverProtocol);

                byte[] credentials = org.apache.commons.codec.binary.Base64
                        .encodeBase64((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));

                request.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
                request.setHeader("Content-Type", "application/x-www-form-urlencoded");

                List<BasicNameValuePair> urlParameters = new ArrayList<>();
                urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
                request.setEntity(new UrlEncodedFormEntity(urlParameters));
                HttpResponse httpResponse = httpClient.execute(request);

                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String payload = EntityUtils.toString(httpResponse.getEntity());
                    JSONObject response = new JSONObject(payload);
                    this.accessToken = (String) response.get("access_token");
                    this.validityPeriod = (long) ((int) response.get("expires_in") * 1000);
                    this.generatedTime = System.currentTimeMillis();
                    return (String) response.get("access_token");
                } else {
                }
            } catch (IOException e) {

            }
        }
        return null;
    }

    /**
     * Return a http client instance
     *
     * @param port      - server port
     * @param protocol- service endpoint protocol http/https
     * @return
     */
    public static HttpClient getHttpClient(int port, String protocol) {

        SchemeRegistry registry = new SchemeRegistry();
        SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
        String hostnameVerifierOption = System.getProperty(HOST_NAME_VERIFIER);
        String sslValue = null;

        X509HostnameVerifier hostnameVerifier;
        if (ALLOW_ALL.equalsIgnoreCase(hostnameVerifierOption)) {
            hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        } else if (STRICT.equalsIgnoreCase(hostnameVerifierOption)) {
            hostnameVerifier = SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
        } else {
            hostnameVerifier = SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;
        }
        socketFactory.setHostnameVerifier(hostnameVerifier);

        if ("https".equals(protocol)) {
            try {
                if ("require".equals(sslValue)) {
                    socketFactory = createSocketFactory();
                    socketFactory.setHostnameVerifier(hostnameVerifier);
                }
                if (port >= 0) {
                    registry.register(new Scheme("https", port, socketFactory));
                } else {
                    registry.register(new Scheme("https", 443, socketFactory));
                }
            } catch (Exception e) {
                log.error(e);
            }
        } else if ("http".equals(protocol)) {
            if (port >= 0) {
                registry.register(new Scheme("http", port, PlainSocketFactory.getSocketFactory()));
            } else {
                registry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
            }
        }
        HttpParams params = new BasicHttpParams();
        ThreadSafeClientConnManager tcm = new ThreadSafeClientConnManager(registry);
        return new DefaultHttpClient(tcm, params);

    }

    private static SSLSocketFactory createSocketFactory() throws Exception {

        KeyStore keyStore;
        String keyStorePath = null;
        String keyStorePassword;
        try {
            keyStorePath = CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStore.Location");
            keyStorePassword = CarbonUtils.getServerConfiguration()
                    .getFirstProperty("Security.KeyStore.Password");
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
            SSLSocketFactory sslSocketFactory = new SSLSocketFactory(keyStore, keyStorePassword);

            return sslSocketFactory;

        } catch (KeyStoreException e) {
            new Exception("Failed to read from Key Store", e);
        } catch (CertificateException e) {
            new Exception("Failed to read Certificate", e);
        } catch (NoSuchAlgorithmException e) {
            new Exception("Failed to load Key Store from " + keyStorePath, e);
        } catch (IOException e) {
            new Exception("Key Store not found in " + keyStorePath, e);
        } catch (UnrecoverableKeyException e) {
            new Exception("Failed to load key from" + keyStorePath, e);
        } catch (KeyManagementException e) {
            new Exception("Failed to load key from" + keyStorePath, e);
        }
        return null;
    }
}
