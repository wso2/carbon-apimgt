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

/**
 * This Interface used to register Telemetry Tracers.
 */

public interface TelemetryService {

    /**
     * Create and return the initialized tracer for an exporter.
     * @param serviceName API:Gateway.
     * @return OpenTelemetry tracer.
     */
    TelemetryTracer buildTelemetryTracer(String serviceName);

    /**
     * Return the openTelemetry instance for the initialized tracer.
     * @return OpenTelemetry instance.
     */
    OpenTelemetry getOpenTelemetry();
}
