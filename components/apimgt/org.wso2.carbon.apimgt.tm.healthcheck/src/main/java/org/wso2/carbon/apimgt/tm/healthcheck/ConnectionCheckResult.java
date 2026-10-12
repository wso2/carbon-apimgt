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
 * The in-memory result of checking one {@link ControlPlaneConnectionId}, as produced by
 * {@link PortResponsivenessChecker} (stage 1) and then possibly overridden by a stage-2 accept-confirmation
 * checker. Mirrors data-model.md's "Control Plane Connection" entity's {@code status}/{@code failureReason}
 * fields - this is the strongly-typed, per-invocation form of those two fields, before they are flattened onto
 * the {@code Properties} bag {@code checkHealth()} returns.
 *
 * <p>An instance is immutable and carries no resolved host/port (FR-014) - only which connection it is for,
 * whether it is up, and (when down) one of the closed {@link FailureReason} category strings. It may also
 * carry an optional, diagnostic-only raw detail string (e.g. an exception message) for
 * {@link TMControlPlaneConnectivityHealthChecker}'s internal {@code WARN} log (Research &sect;5) - this detail
 * is never placed on the {@code Properties}/{@code HealthCheckError} returned from {@code checkHealth()}
 * (FR-013, FR-014).</p>
 */
public final class ConnectionCheckResult {

    private final ControlPlaneConnectionId connectionId;
    private final boolean up;
    private final String failureReason;
    private final String rawDetail;

    private ConnectionCheckResult(ControlPlaneConnectionId connectionId, boolean up, String failureReason,
                                   String rawDetail) {

        this.connectionId = connectionId;
        this.up = up;
        this.failureReason = failureReason;
        this.rawDetail = rawDetail;
    }

    /**
     * Creates a result reporting the given connection as {@code UP}.
     *
     * @param connectionId which connection this result is for
     * @return an {@code UP} result with no failure reason
     */
    public static ConnectionCheckResult up(ControlPlaneConnectionId connectionId) {

        return new ConnectionCheckResult(connectionId, true, null, null);
    }

    /**
     * Creates a result reporting the given connection as {@code DOWN}, with no raw diagnostic detail.
     *
     * @param connectionId   which connection this result is for
     * @param failureReason  one of the closed {@link FailureReason} category strings - never a raw exception
     *                       message (FR-013)
     * @return a {@code DOWN} result carrying the given failure reason
     */
    public static ConnectionCheckResult down(ControlPlaneConnectionId connectionId, String failureReason) {

        return new ConnectionCheckResult(connectionId, false, failureReason, null);
    }

    /**
     * Creates a result reporting the given connection as {@code DOWN}, additionally carrying a raw diagnostic
     * detail (e.g. an exception message) for the internal {@code WARN} log only.
     *
     * @param connectionId   which connection this result is for
     * @param failureReason  one of the closed {@link FailureReason} category strings - never a raw exception
     *                       message (FR-013)
     * @param rawDetail      the raw underlying error detail, permitted only in the internal {@code WARN} log
     *                       (Research &sect;5) - never a value this class, or any caller, may place on the
     *                       {@code checkHealth()} return value
     * @return a {@code DOWN} result carrying the given failure reason and raw detail
     */
    public static ConnectionCheckResult down(ControlPlaneConnectionId connectionId, String failureReason,
                                              String rawDetail) {

        return new ConnectionCheckResult(connectionId, false, failureReason, rawDetail);
    }

    public ControlPlaneConnectionId getConnectionId() {

        return connectionId;
    }

    public boolean isUp() {

        return up;
    }

    /**
     * Returns the failure reason, or {@code null} when {@link #isUp()} is {@code true}.
     *
     * @return the failure reason, or {@code null} if this connection is up
     */
    public String getFailureReason() {

        return failureReason;
    }

    /**
     * Returns the raw underlying error detail (e.g. an exception message), for the internal {@code WARN} log
     * only (Research &sect;5) - {@code null} when this connection is up or no further detail was captured.
     * This value must never be placed on the {@code Properties}/{@code HealthCheckError} that
     * {@code checkHealth()} returns (FR-013, FR-014).
     *
     * @return the raw diagnostic detail, or {@code null}
     */
    public String getRawDetail() {

        return rawDetail;
    }
}
