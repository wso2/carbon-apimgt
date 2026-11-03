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

package org.wso2.carbon.apimgt.healthcheck.probe;

/**
 * The outcome of probing one {@link ProbeEndpoint} once. A failure names the {@link ProbeStage} that broke; its raw
 * detail is for logging only and never goes on the health check response.
 */
public final class ProbeResult {

    private final CheckType checkType;
    private final ProbeEndpoint endpoint;
    private final boolean success;
    private final Boolean authSucceeded;
    private final ProbeStage failedStage;
    private final String detail;
    private final long latencyMs;
    private final boolean credentialsRejected;

    private ProbeResult(CheckType checkType, ProbeEndpoint endpoint, boolean success, Boolean authSucceeded,
                        ProbeStage failedStage, String detail, long latencyMs) {

        this(checkType, endpoint, success, authSucceeded, failedStage, detail, latencyMs, false);
    }

    private ProbeResult(CheckType checkType, ProbeEndpoint endpoint, boolean success, Boolean authSucceeded,
                        ProbeStage failedStage, String detail, long latencyMs, boolean credentialsRejected) {

        this.checkType = checkType;
        this.endpoint = endpoint;
        this.success = success;
        this.authSucceeded = authSucceeded;
        this.failedStage = failedStage;
        this.detail = detail;
        this.latencyMs = latencyMs;
        this.credentialsRejected = credentialsRejected;
    }

    /**
     * The receiver rejected the configured credentials, which, unlike a network or TLS failure, counts as a failed
     * login attempt for that user.
     */
    public static ProbeResult credentialsRejected(ProbeEndpoint endpoint, String detail, long latencyMs) {

        return new ProbeResult(endpoint.getCheckType(), endpoint, false, false, ProbeStage.LOGIN_FAILED, detail,
                latencyMs, true);
    }

    public static ProbeResult success(ProbeEndpoint endpoint, long latencyMs) {

        return new ProbeResult(endpoint.getCheckType(), endpoint, true, true, null, null, latencyMs);
    }

    /**
     * A failure where it is not known whether the login step succeeded, such as a message broker failure.
     */
    public static ProbeResult failure(ProbeEndpoint endpoint, ProbeStage failedStage, String detail, long latencyMs) {

        return failure(endpoint, failedStage, detail, latencyMs, null);
    }

    /**
     * @param authSucceeded throttle receiver only: whether login succeeded before the failure, or {@code null} if
     *                      unknown
     */
    public static ProbeResult failure(ProbeEndpoint endpoint, ProbeStage failedStage, String detail, long latencyMs,
                                      Boolean authSucceeded) {

        return new ProbeResult(endpoint.getCheckType(), endpoint, false, authSucceeded, failedStage, detail,
                latencyMs);
    }

    /**
     * For a check where no address could be resolved, so there is nothing to probe.
     */
    public static ProbeResult unresolved(CheckType checkType, String detail) {

        return new ProbeResult(checkType, null, false, null, ProbeStage.CONFIG_UNRESOLVED, detail, 0);
    }

    public CheckType getCheckType() {

        return checkType;
    }

    /**
     * @return the probed endpoint, or {@code null} when no address could be resolved
     */
    public ProbeEndpoint getEndpoint() {

        return endpoint;
    }

    public boolean isSuccess() {

        return success;
    }

    /**
     * Throttle receiver only: whether the login step succeeded, or {@code null} if unknown. It is shown in the
     * response but never affects the overall status, which uses {@link #isSuccess()} only.
     */
    public Boolean isAuthSucceeded() {

        return authSucceeded;
    }

    public ProbeStage getFailedStage() {

        return failedStage;
    }

    public String getDetail() {

        return detail;
    }

    public long getLatencyMs() {

        return latencyMs;
    }

    public boolean isCredentialsRejected() {

        return credentialsRejected;
    }

    /**
     * @return the address this result is about, or {@code "unresolved"} if there was no endpoint
     */
    public String getTarget() {

        return endpoint == null ? "unresolved" : endpoint.getUrl();
    }
}
