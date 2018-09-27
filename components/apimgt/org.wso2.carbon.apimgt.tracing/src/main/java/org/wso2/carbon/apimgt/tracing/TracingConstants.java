/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.tracing;

public class TracingConstants {

    /**
     * OpenTracer Constants
     * */
    static final String OPEN_TRACER_NAME = "OpenTracer.Name";
    static final String DEFAULT_OPEN_TRACER_NAME = "zipkin";
    static final String OPEN_TRACER_ENABLED = "OpenTracer.Enabled";
    static final String DEFAULT_OPEN_TRACER_ENABLED = "true";
    static final String LATENCY = "Latency";
    static final String OPERATION_NAME = "Operation";
    static final String TAGS = "Tags";
    public static final String TRACER = "tracer";
    public static final String CONFIG_TRACER_LOG_ENABLED = "OpenTracer.TracerLogEnabled";
    public static final String DEFAULT_TRACER_LOG_ENABLED = "false";

    /**
     * Jaeger Tracer Constants
     * */
    public static final String JAEGER = "JAEGER";
    public static final String JAEGER_CONFIG_PORT = "OpenTracer.Port";
    public static final String JAEGER_CONFIG_HOST = "OpenTracer.Hostname";
    public static final String CONFIG_SAMPLER_PARAM = "OpenTracer.SamplerParam";
    public static final String CONFIG_SAMPLER_TYPE = "OpenTracer.SamplerType";
    public static final String CONFIG_REPORTER_FLUSH_INTERVAL = "OpenTracer.ReporterFlushInterval";
    public static final String CONFIG_REPORTER_BUFFER_SIZE = "OpenTracer.ReporterBufferSize";

    public static final int JAEGER_DEFAULT_PORT = 5775;
    public static final String JAEGER_DEFAULT_HOST = "localhost";
    public static final int DEFAULT_SAMPLER_PARAM = 1;
    public static final String DEFAULT_SAMPLER_TYPE = "const";
    public static final int DEFAULT_REPORTER_FLUSH_INTERVAL = 1000;
    public static final int DEFAULT_REPORTER_BUFFER_SIZE = 1000;

    /**
     * Zipkin Constants
     * */
    public static final String ZIPKIN = "ZIPKIN";
    public static final String ZIPKIN_CONFIG_PORT = "OpenTracer.Port";
    public static final String ZIPKIN_CONFIG_HOST = "OpenTracer.Hostname";
    public static final String CONFIG_API_CONTEXT = "OpenTracer.APIContext";
    public static final String REQUEST_ID = "request-id";

    public static final int ZIPKIN_DEFAULT_PORT = 9411;
    public static final String ZIPKIN_DEFAULT_HOST = "localhost";
    public static final String DEFAULT_API_CONTEXT = "/api/v2/spans";
}
