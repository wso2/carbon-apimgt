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
 * The outcome of probing one {@link ProbeEndpoint} once. A failed result names the {@link ProbeStage} that broke and
 * keeps the raw error detail for logging only (it is never put on the health check response).
 *
 * <p>For a throttle receiver endpoint only, {@link #isAuthSucceeded()} says whether the 9711 login step itself
 * succeeded - this is extra, read-only detail: it is never counted when working out {@code UP}/{@code DEGRADED}/
 * {@code DOWN}, which is still based only on {@link #isSuccess()} (the whole 9711-login-then-9611-publish errand,
 * same as before). It only changes what the response shows underneath an already-computed result.</p>
 */
public final class ProbeResult {

    private final CheckType checkType;
    private final ProbeEndpoint endpoint;
    private final boolean success;
    private final boolean authSucceeded;
    private final ProbeStage failedStage;
    private final String detail;
    private final long latencyMs;

    private ProbeResult(CheckType checkType, ProbeEndpoint endpoint, boolean success, boolean authSucceeded,
                        ProbeStage failedStage, String detail, long latencyMs) {

        this.checkType = checkType;
        this.endpoint = endpoint;
        this.success = success;
        this.authSucceeded = authSucceeded;
        this.failedStage = failedStage;
        this.detail = detail;
        this.latencyMs = latencyMs;
    }

    public static ProbeResult success(ProbeEndpoint endpoint, long latencyMs) {

        // The whole errand passed, so the 9711 login step inside it must have passed too.
        return new ProbeResult(endpoint.getCheckType(), endpoint, true, true, null, null, latencyMs);
    }

    /**
     * @see #failure(ProbeEndpoint, ProbeStage, String, long, boolean) - this overload is for a message broker
     *      result, or any other failure where "did the 9711 login succeed" does not apply
     */
    public static ProbeResult failure(ProbeEndpoint endpoint, ProbeStage failedStage, String detail, long latencyMs) {

        return failure(endpoint, failedStage, detail, latencyMs, true);
    }

    /**
     * @param authSucceeded for a throttle receiver endpoint: {@code true} if 9711's login step itself succeeded
     *                      (the failure happened afterwards, trying to publish on 9611); {@code false} if the
     *                      login step is what failed. Ignored for every other kind of endpoint.
     */
    public static ProbeResult failure(ProbeEndpoint endpoint, ProbeStage failedStage, String detail, long latencyMs,
                                      boolean authSucceeded) {

        return new ProbeResult(endpoint.getCheckType(), endpoint, false, authSucceeded, failedStage, detail,
                latencyMs);
    }

    /**
     * For when no address at all could be resolved for a check, so there is nothing to probe.
     */
    public static ProbeResult unresolved(CheckType checkType, String detail) {

        return new ProbeResult(checkType, null, false, false, ProbeStage.CONFIG_UNRESOLVED, detail, 0);
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
     * @return for a throttle receiver endpoint, whether the 9711 login step succeeded - see the class Javadoc.
     *         Meaningless for any other kind of endpoint.
     */
    public boolean isAuthSucceeded() {

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

    /**
     * @return the address this result is about, or {@code "unresolved"} if there was no endpoint
     */
    public String getTarget() {

        return endpoint == null ? "unresolved" : endpoint.getUrl();
    }
}
