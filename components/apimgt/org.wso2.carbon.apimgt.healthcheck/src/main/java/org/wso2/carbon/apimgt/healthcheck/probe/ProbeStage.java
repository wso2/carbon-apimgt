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
 * The step of a Traffic Manager probe that failed. Each value has a safe message for the health check response and
 * an error code such as {@code TM_HC_PORT_CLOSED}.
 */
public enum ProbeStage {

    CONFIG_UNRESOLVED("Address could not be resolved from configuration"),
    PORT_CLOSED("Port is not reachable"),
    TLS_FAILED("Secure connection failed"),
    LOGIN_FAILED("Login failed"),
    PUBLISH_FAILED("Test message was not accepted"),
    TIMEOUT("No answer in time"),
    UNEXPECTED_ERROR("Unexpected error during the check"),

    STREAM_UNDEFINED("Health check stream is not deployed"),
    SESSION_EXPIRED("Login session was rejected"),
    /**
     * Set by {@code TMHealthChecker} on the publish side when the login step failed, so publish never ran.
     */
    NOT_ATTEMPTED("Not attempted - the login step failed first"),

    /**
     * Broker connect failed for an unknown reason; the Qpid client throws a cause-less {@code TransportException},
     * so a timeout cannot be told apart from a refused connection.
     */
    CONNECT_FAILED("Could not connect to the broker"),
    SUBSCRIBE_FAILED("Could not subscribe to the health check topic"),
    NOT_RECEIVED("Test message was not delivered back in time"),

    /**
     * Every probe thread is still busy with an earlier probe that has not finished, so this one could not start.
     */
    PROBE_POOL_EXHAUSTED("No free thread to run the check");

    private static final String ERROR_CODE_PREFIX = "TM_HC_";

    private final String message;

    ProbeStage(String message) {

        this.message = message;
    }

    public String getMessage() {

        return message;
    }

    public String getErrorCode() {

        return ERROR_CODE_PREFIX + name();
    }
}
