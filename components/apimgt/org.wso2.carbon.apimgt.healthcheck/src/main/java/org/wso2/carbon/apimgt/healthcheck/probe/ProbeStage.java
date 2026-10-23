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
 * The step of a Traffic Manager probe that failed, for both the throttle receiver and the message broker. Each value
 * carries a safe, plain-language message that may be shown in the health check response, and its own error code
 * (e.g. {@code TM_HC_PORT_CLOSED}). The response also says which {@link CheckType} and address the failure is for.
 */
public enum ProbeStage {

    // Both checks
    CONFIG_UNRESOLVED("Address could not be resolved from configuration"),
    PORT_CLOSED("Port is not reachable"),
    TLS_FAILED("Secure connection failed"),
    LOGIN_FAILED("Login failed"),
    PUBLISH_FAILED("Test message was not accepted"),
    TIMEOUT("No answer in time"),
    UNEXPECTED_ERROR("Unexpected error during the check"),

    // Throttle receiver only
    STREAM_UNDEFINED("Health check stream is not deployed"),
    SESSION_EXPIRED("Login session was rejected"),
    /**
     * Not thrown by a probe step itself - set by {@code TMHealthChecker} for the publish (9611) side of a result
     * when the 9711 login step is what actually failed, so publish was never attempted at all. The real reason is
     * reported separately, under that same endpoint's {@code .auth.error.code}.
     */
    NOT_ATTEMPTED("Not attempted - the 9711 login step failed first"),

    // Message broker only
    /**
     * The broker connect attempt itself failed in a way this checker could not narrow down further - unlike the
     * throttle receiver's own raw socket connect (which lets {@link ProbeClient} inspect the real
     * {@link java.net.ConnectException}/timeout directly), the Qpid client used for the broker only ever throws a
     * bare, cause-less {@code org.wso2.andes.transport.TransportException} ("Could not open connection...") once
     * its own connect attempt gives up, whether that is because the connection was silently dropped (a timeout)
     * or actively refused - there is nothing left in the exception to tell the two apart. Deliberately worded to
     * not claim a specific reason that may well be wrong.
     */
    CONNECT_FAILED("Could not connect to the broker"),
    SUBSCRIBE_FAILED("Could not subscribe to the health check topic"),
    NOT_RECEIVED("Test message was not delivered back in time");

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
