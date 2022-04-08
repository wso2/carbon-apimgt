/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.tracing.telemetry;

/**
 * OpenTelemetry Constants
 */
public class TelemetryConstants {

    static final String OPEN_TELEMETRY_TRACER_NAME = "OpenTracer.RemoteTracer.Name";
    static final String DEFAULT_OPEN_TELEMETRY_TRACER_NAME = "zipkin";
    static final String OPEN_TELEMETRY_TRACER_ENABLED = "OpenTracer.RemoteTracer.Enabled";
    static final String DEFAULT_OPEN_TELEMETRY_TRACER_ENABLED = "false";
    static final String REMOTE_TELEMETRY_TRACER_ENABLED = "OpenTracer.RemoteTracer.Enabled";
    static final String LOG_TELEMETRY_TRACER_ENABLED = "OpenTracer.LogTracer.Enabled";

    /**
     * Jaeger Constants
     */
    static final String JAEGER = "JAEGER";
    static final String JAEGER_CONFIG_PORT = "OpenTracer.RemoteTracer.Properties.Port";
    static final String JAEGER_CONFIG_HOST = "OpenTracer.RemoteTracer.Properties.HostName";
    static final int JAEGER_DEFAULT_PORT = 5775;
    static final String JAEGER_DEFAULT_HOST = "localhost";

    /**
     * Zipkin Constants
     */
    static final String ZIPKIN = "ZIPKIN";
    static final String ZIPKIN_CONFIG_PORT = "OpenTracer.RemoteTracer.Properties.Port";
    static final String ZIPKIN_CONFIG_HOST = "OpenTracer.RemoteTracer.Properties.HostName";
    static final String REQUEST_ID = "request-id";
    static final int ZIPKIN_DEFAULT_PORT = 9411;
    static final String ZIPKIN_DEFAULT_HOST = "localhost";
    static final String ZIPKIN_API_CONTEXT = "/api/v2/spans";

    /**
     * Log Constants
     */
    static final String LOG_ENABLED = "OpenTracer.LogTracer.Enabled";
    static final String LOG = "log";
}
