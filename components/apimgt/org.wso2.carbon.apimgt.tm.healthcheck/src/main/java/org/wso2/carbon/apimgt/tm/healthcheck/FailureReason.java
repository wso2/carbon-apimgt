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

/**
 * The closed, sanitized set of plain-language reasons a Control Plane connection may be reported
 * {@code DOWN}.
 *
 * <p>These five strings are the <b>only</b> values any {@code failureReason} field or
 * {@link org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError#getErrorDescription()} value
 * produced by this feature may ever take. A raw exception message or stack trace must never be used
 * as a failure reason - that detail belongs only in the internal {@code WARN} log, never in the
 * {@code GET /health} response (FR-013, FR-014).</p>
 */
public final class FailureReason {

    /**
     * The connect attempt could not reach the target at all (e.g. unresolved host, no route to host) -
     * any stage-1 failure that is neither a timeout nor an active refusal.
     */
    public static final String UNREACHABLE = "Unreachable";

    /**
     * The connect attempt exhausted {@code connect.timeout.ms} with no response.
     */
    public static final String TIMED_OUT = "Timed out";

    /**
     * The connect attempt, or a TLS handshake, was actively rejected by the OS or the target
     * (e.g. {@code java.net.ConnectException: Connection refused}).
     */
    public static final String CONNECTION_REFUSED = "Connection refused";

    /**
     * The port responded and accepted the connection, but the Control Plane rejected the
     * accept-confirmation exercise (marker event/message publish, or the management API call).
     */
    public static final String REJECTED_BY_CONTROL_PLANE = "Rejected by Control Plane";

    /**
     * The accept-confirmation login against the Control Plane (the 9711 publisher login) failed.
     */
    public static final String AUTHENTICATION_FAILED = "Authentication failed";

    private FailureReason() {

        // Prevent instantiation - this class is a closed set of constants only.
    }
}
