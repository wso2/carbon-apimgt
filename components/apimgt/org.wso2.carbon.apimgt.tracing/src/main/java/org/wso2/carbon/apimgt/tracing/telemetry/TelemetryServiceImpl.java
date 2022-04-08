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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

/**
 * Osgi Registration for Telemetry Service.
 */

public class TelemetryServiceImpl implements TelemetryService {

    private static final Log log = LogFactory.getLog(TelemetryServiceImpl.class);
    private static APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfiguration();
    private static TelemetryServiceImpl instance = new TelemetryServiceImpl();
    private APIMOpenTelemetry tracer;

    private TelemetryServiceImpl() {

        String openTelemetryTracerName =
                configuration.getFirstProperty(TelemetryConstants.OPEN_TELEMETRY_TRACER_NAME) != null ?
                        configuration.getFirstProperty(TelemetryConstants.OPEN_TELEMETRY_TRACER_NAME)
                        : TelemetryConstants.DEFAULT_OPEN_TELEMETRY_TRACER_NAME;

        Boolean remoteTelemetryTracerEnabled =
                Boolean.valueOf(configuration
                        .getFirstProperty(TelemetryConstants.OPEN_TELEMETRY_TRACER_ENABLED) != null ?
                        configuration.getFirstProperty(TelemetryConstants.OPEN_TELEMETRY_TRACER_ENABLED)
                        : TelemetryConstants.DEFAULT_OPEN_TELEMETRY_TRACER_ENABLED);

        String telemetryTracerName = (openTelemetryTracerName != null && remoteTelemetryTracerEnabled) ?
                openTelemetryTracerName : TelemetryConstants.LOG;
        this.tracer = ServiceReferenceHolder.getOpenTelemetryTracerMap().get(telemetryTracerName);
    }

    public static TelemetryServiceImpl getInstance() {

        return instance;
    }

    @Override
    public TelemetryTracer buildTelemetryTracer(String serviceName) {

        tracer.init(serviceName);
        return new TelemetryTracer(tracer.getTelemetryTracer());
    }

    @Override
    public OpenTelemetry getOpenTelemetry() {

        return tracer.getAPIMOpenTelemetry();
    }
}
