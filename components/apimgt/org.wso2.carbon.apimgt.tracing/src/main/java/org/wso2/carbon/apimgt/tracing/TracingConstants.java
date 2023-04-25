/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.tracing;
/**
 * @deprecated
 * <p> Use {@link org.wso2.carbon.apimgt.tracing.telemetry.TelemetryConstants} instead</p>
 */

@Deprecated
class TracingConstants {
    /**
     * OpenTracer Constants
     * */
    static final String OPEN_TRACER_NAME = "OpenTracer.RemoteTracer.Name";
    static final String DEFAULT_OPEN_TRACER_NAME = "zipkin";
    static final String OPEN_TRACER_ENABLED = "OpenTracer.RemoteTracer.Enabled";
    static final String DEFAULT_OPEN_TRACER_ENABLED = "false";
    static final String LATENCY = "Latency";
    static final String OPERATION_NAME = "Operation";
    static final String TAGS = "Tags";
    static final String TRACER = "tracer";
    static final String CONFIG_TRACER_LOG_ENABLED = "OpenTracer.LogTracer.Enabled";
    static final String DEFAULT_TRACER_LOG_ENABLED = "false";
    static final String REMOTE_TRACER_ENABLED = "OpenTracer.RemoteTracer.Enabled";
    static final String LOG_TRACER_ENABLED = "OpenTracer.LogTracer.Enabled";

    /**
     * Jaeger Tracer Constants
     * */
    static final String JAEGER = "JAEGER";
    static final String JAEGER_CONFIG_PORT = "OpenTracer.RemoteTracer.Properties.Port";
    static final String JAEGER_CONFIG_HOST = "OpenTracer.RemoteTracer.Properties.HostName";
    static final String CONFIG_SAMPLER_PARAM = "OpenTracer.RemoteTracer.Properties.SamplerParam";
    static final String CONFIG_SAMPLER_TYPE = "OpenTracer.RemoteTracer.Properties.SamplerType";
    static final String CONFIG_REPORTER_FLUSH_INTERVAL = "OpenTracer.RemoteTracer.Properties.ReporterFlushInterval";
    static final String CONFIG_REPORTER_BUFFER_SIZE = "OpenTracer.RemoteTracer.Properties.ReporterBufferSize";

    static final int JAEGER_DEFAULT_PORT = 5775;
    static final String JAEGER_DEFAULT_HOST = "localhost";
    static final int DEFAULT_SAMPLER_PARAM = 1;
    static final String DEFAULT_SAMPLER_TYPE = "const";
    static final int DEFAULT_REPORTER_FLUSH_INTERVAL = 1000;
    static final int DEFAULT_REPORTER_BUFFER_SIZE = 1000;

    /**
     * Zipkin Constants
     * */
    static final String ZIPKIN = "ZIPKIN";
    static final String ZIPKIN_CONFIG_PORT = "OpenTracer.RemoteTracer.Properties.Port";
    static final String ZIPKIN_CONFIG_HOST = "OpenTracer.RemoteTracer.Properties.HostName";
    static final String ZIPKIN_CONFIG_PROXY_HOST = "OpenTracer.RemoteTracer.Properties.ProxyHost";
    static final String ZIPKIN_CONFIG_PROXY_PORT = "OpenTracer.RemoteTracer.Properties.ProxyPort";
    static final String ZIPKIN_CONFIG_ENDPOINT_URL
            = "OpenTracer.RemoteTracer.Properties.EndpointUrl";

    static final String REQUEST_ID = "request-id";

    static final int ZIPKIN_DEFAULT_PORT = 9411;
    static final String ZIPKIN_DEFAULT_HOST = "localhost";
    static final String ZIPKIN_API_CONTEXT = "/api/v2/spans";

    /**
     * Log Constants
     * */
    static final String LOG_ENABLED = "OpenTracer.LogTracer.Enabled";
    static final String LOG = "log";

    private TracingConstants() {
    }
}
