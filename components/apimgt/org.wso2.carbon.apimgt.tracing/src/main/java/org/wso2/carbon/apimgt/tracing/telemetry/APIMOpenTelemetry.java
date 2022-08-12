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

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;

/**
 * This interface used to implement OpenTelemetry Bridge Implementations for APIM.
 */

public interface APIMOpenTelemetry {

    /**
     * Initialize the exporter and configure an openTelemetry instance from it.
     *
     * @param serviceName API:Gateway
     */
    void init(String serviceName);

    /**
     *
     * Return the initialized the openTelemetry instance.
     * @return openTelemetry instance.
     */
    OpenTelemetry getAPIMOpenTelemetry();

    /**
     * Return the OpenTelemetry tracer from the initialized openTelemetry instance.
     * @return OpenTelemetry tracer.
     */
    Tracer getTelemetryTracer();

    /**
     * Return the exporter name.
     * @return exporter name.
     */
    String getName();

    /**
     * Shutdown the SDK cleanly at JVM exit.
     */
    void close();
}
