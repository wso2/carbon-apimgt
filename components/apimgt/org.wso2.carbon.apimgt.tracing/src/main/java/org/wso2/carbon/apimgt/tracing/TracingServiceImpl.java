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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

/**
 * Osgi Registration for Tracing Service.
 * @deprecated
 * <p> Use {@link org.wso2.carbon.apimgt.tracing.telemetry.TelemetryServiceImpl} instead</p>
 */

@Deprecated
public class TracingServiceImpl implements TracingService {

    private static final Log log = LogFactory.getLog(TracingServiceImpl.class);
    private static APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfiguration();
    private static TracingServiceImpl instance = new TracingServiceImpl();
    private OpenTracer tracer;

    public static TracingServiceImpl getInstance() {
        return instance;
    }

    private TracingServiceImpl() {

        String openTracerName = configuration.getFirstProperty(TracingConstants.OPEN_TRACER_NAME) != null ?
                configuration.getFirstProperty(TracingConstants.OPEN_TRACER_NAME)
                : TracingConstants.DEFAULT_OPEN_TRACER_NAME;

        Boolean remoteTracerEnabled =
                Boolean.valueOf(configuration.getFirstProperty(TracingConstants.OPEN_TRACER_ENABLED) != null ?
                        configuration.getFirstProperty(TracingConstants.OPEN_TRACER_ENABLED)
                        : TracingConstants.DEFAULT_OPEN_TRACER_ENABLED);

        String tracerName = (openTracerName != null && remoteTracerEnabled) ? openTracerName : TracingConstants.LOG;
        if (log.isDebugEnabled()) {
            log.debug("Initializing tracer: " + tracerName + ", remote tracer enabled: " + remoteTracerEnabled);
        }
        this.tracer = ServiceReferenceHolder.getOpenTracerMap().get(tracerName);
        if (this.tracer == null) {
            log.warn("Tracer " + tracerName + " not found in tracer map, falling back to default");
        } else {
            log.info("Tracer initialized successfully: " + tracerName);
        }
    }

    @Override
    public TracingTracer buildTracer(String serviceName) {
        return new TracingTracer(tracer.getTracer(serviceName));
    }
}
