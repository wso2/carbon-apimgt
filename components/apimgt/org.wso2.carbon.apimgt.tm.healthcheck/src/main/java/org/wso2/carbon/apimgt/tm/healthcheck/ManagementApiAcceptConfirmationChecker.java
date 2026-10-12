/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.tm.healthcheck;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Stage 2 accept confirmation for {@link ControlPlaneConnectionId#MANAGEMENT_API} - Research &sect;3.
 *
 * <p>Unlike the event/broker connections, this one is inherently request/response, so its accept-confirmation
 * step is simply one lightweight authenticated HTTPS call to an existing, already-cheap endpoint already used
 * by the dataplane today - the same {@code /internal/data/v1/block} endpoint
 * {@code BlockingConditionRetriever} already polls for blocking-condition data. Success/failure is correlated
 * only with this one call's own immediate response - never a separate side-channel or a queued reply
 * (FR-002b).</p>
 */
public class ManagementApiAcceptConfirmationChecker {

    private static final Log log = LogFactory.getLog(ManagementApiAcceptConfirmationChecker.class);

    private static final String LIGHTWEIGHT_ENDPOINT_PATH = APIConstants.INTERNAL_WEB_APP_EP + "/block";

    /**
     * Runs stage 2 for the management connection.
     *
     * @param serviceUrl       the live Event Hub service URL to call against
     * @param username         the Event Hub username to authenticate with
     * @param password         the Event Hub password to authenticate with
     * @param connectTimeoutMs the bounded timeout, in milliseconds, for both connecting and reading the response
     * @param stage1Result     this connection's stage-1 (port-responsiveness) result
     * @return {@code stage1Result} unchanged if stage 1 did not succeed; otherwise a fresh {@code UP}/{@code
     *         DOWN} result from this one HTTPS call
     */
    public ConnectionCheckResult check(String serviceUrl, String username, String password, int connectTimeoutMs,
                                        ConnectionCheckResult stage1Result) {

        if (!stage1Result.isUp()) {
            return stage1Result;
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(serviceUrl + LIGHTWEIGHT_ENDPOINT_PATH);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeoutMs);
            connection.setReadTimeout(connectTimeoutMs);
            connection.setRequestProperty("Authorization", "Basic " + encodeBasicAuth(username, password));

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                return ConnectionCheckResult.up(ControlPlaneConnectionId.MANAGEMENT_API);
            }
            log.warn("Control Plane management connection returned a non-success response: " + responseCode);
            return ConnectionCheckResult.down(ControlPlaneConnectionId.MANAGEMENT_API,
                    FailureReason.REJECTED_BY_CONTROL_PLANE, "HTTP " + responseCode);
        } catch (IOException e) {
            log.warn("Control Plane management connection rejected the health-check call: " + e.getMessage());
            return ConnectionCheckResult.down(ControlPlaneConnectionId.MANAGEMENT_API,
                    FailureReason.REJECTED_BY_CONTROL_PLANE, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String encodeBasicAuth(String username, String password) {

        byte[] credentials = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(credentials);
    }
}
