/*
 *Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *WSO2 Inc. licenses this file to you under the Apache License,
 *Version 2.0 (the "License"); you may not use this file except
 *in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing,
 *software distributed under the License is distributed on an
 *"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *KIND, either express or implied.  See the License for the
 *specific language governing permissions and limitations
 *under the License.
 */

package org.wso2.carbon.apimgt.keymgt.events;

import org.apache.axis2.util.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.token.TokenRevocationNotifier;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Implemented class for TokenRevocationNotifier interface
 * SendMessageOnRealtime() method is implemented
 * SendMessageToPersistentStorage() method is implemented
 */
public class TokenRevocationNotifierImpl implements TokenRevocationNotifier {

    private static final Log log = LogFactory.getLog(APIMOAuthEventInterceptor.class);
    private final String DEFAULT_TTL = "3600";

    /**
     * Method to publish the revoked token on to the realtime message broker
     *
     * @param revokedToken requested revoked token
     * @param properties realtime notifier properties read from the config
     */
    @Override
    public void sendMessageOnRealtime(String revokedToken, Properties properties) {
        //Variables related to Realtime Notifier
        String realtimeNotifierTTL = properties.getProperty("ttl", DEFAULT_TTL);
        long expiryTimeForJWT = Long.parseLong(properties.getProperty("expiryTime"));
        Object[] objects = new Object[] { revokedToken, realtimeNotifierTTL, expiryTimeForJWT};
        Event tokenRevocationMessage = new Event(APIConstants.TOKEN_REVOCATION_STREAM_ID, System.currentTimeMillis(),
                null, null, objects);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        boolean isTenantFlowStarted = false;
        try {
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().
                        setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            }
            ServiceReferenceHolder.getInstance().getOutputEventAdapterService()
                    .publish(APIConstants.TOKEN_REVOCATION_EVENT_PUBLISHER, Collections.EMPTY_MAP,
                            tokenRevocationMessage);
            log.debug("Successfully sent the revoked token notification on realtime");
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    /**
     * Method to send the revoked token to the persistent storage
     *
     * @param revokedToken token to be revoked
     * @param properties persistent notifier properties read from the config
     */
    @Override
    public void sendMessageToPersistentStorage(String revokedToken, Properties properties) {
        //Variables related to Persistent Notifier
        String DEFAULT_PERSISTENT_NOTIFIER_HOSTNAME = "https://localhost:2379/v2/keys/jti/";
        String persistentNotifierHostname = properties
                .getProperty("hostname", DEFAULT_PERSISTENT_NOTIFIER_HOSTNAME);
        String persistentNotifierTTL = properties.getProperty("ttl", DEFAULT_TTL);
        String DEFAULT_PERSISTENT_NOTIFIER_USERNAME = "root";
        String persistentNotifierUsername = properties
                .getProperty("username", DEFAULT_PERSISTENT_NOTIFIER_USERNAME);
        String DEFAULT_PERSISTENT_NOTIFIER_PASSWORD = "root";
        String persistentNotifierPassword = properties
                .getProperty("password", DEFAULT_PERSISTENT_NOTIFIER_PASSWORD);
        String etcdEndpoint = persistentNotifierHostname + revokedToken;
        URL etcdEndpointURL = new URL(etcdEndpoint);
        String etcdEndpointProtocol = etcdEndpointURL.getProtocol();
        int etcdEndpointPort = etcdEndpointURL.getPort();
        HttpClient etcdEPClient = APIUtil.getHttpClient(etcdEndpointPort, etcdEndpointProtocol);
        HttpPut httpETCDPut = new HttpPut(etcdEndpoint);
        byte[] encodedAuth = Base64.encodeBase64((persistentNotifierUsername + ":" + persistentNotifierPassword).
                getBytes(StandardCharsets.UTF_8));
        String authHeader = "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
        httpETCDPut.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        List<NameValuePair> etcdParams = new ArrayList<>(2);
        etcdParams.add(new BasicNameValuePair("value", "true"));
        etcdParams.add(new BasicNameValuePair("ttl", persistentNotifierTTL));

        //Send the revoked token to the persistent storage Server
        httpETCDPut.setEntity(new UrlEncodedFormEntity(etcdParams, StandardCharsets.UTF_8));
        HttpResponse etcdResponse;
        try {
            etcdResponse = etcdEPClient.execute(httpETCDPut);
            if (etcdResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
                    || etcdResponse.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
                if (log.isDebugEnabled()) {
                    log.debug("Successfully submitted the request for revoked token. HTTP status :" + etcdResponse
                            .getStatusLine().getStatusCode());
                }
            } else {
                log.error("Sending revoked token to persistent storage failed. HTTP error code : " + etcdResponse
                        .getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            log.error("Error while sending revoked token to the persistent storage :", e);
        }
    }
}
