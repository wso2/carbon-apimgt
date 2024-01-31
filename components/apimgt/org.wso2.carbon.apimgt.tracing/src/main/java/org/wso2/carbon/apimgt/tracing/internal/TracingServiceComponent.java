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
package org.wso2.carbon.apimgt.tracing.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.tracing.JaegerTracer;
import org.wso2.carbon.apimgt.tracing.LogTracer;
import org.wso2.carbon.apimgt.tracing.OpenTracer;
import org.wso2.carbon.apimgt.tracing.TracingService;
import org.wso2.carbon.apimgt.tracing.TracingServiceImpl;
import org.wso2.carbon.apimgt.tracing.ZipkinTracer;
import org.wso2.carbon.apimgt.tracing.telemetry.APIMOpenTelemetry;
import org.wso2.carbon.apimgt.tracing.telemetry.JaegerTelemetry;
import org.wso2.carbon.apimgt.tracing.telemetry.LogTelemetry;
import org.wso2.carbon.apimgt.tracing.telemetry.OTLPTelemetry;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryService;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryServiceImpl;
import org.wso2.carbon.apimgt.tracing.telemetry.ZipkinTelemetry;

/**
 * Osgi Service Component to Opentracing and Opentelemetry.
 */
@Component(
        name = "org.wso2.carbon.apimgt.tracing.internal.TracingServiceComponent",
        immediate = true)
public class TracingServiceComponent {

    private static final Log log = LogFactory.getLog(TracingServiceComponent.class);

    private ServiceRegistration registration = null;

    @Activate
    protected void activate(ComponentContext componentContext) {

        try {
            log.debug("Tracing Component activated");
            BundleContext bundleContext = componentContext.getBundleContext();
            registration = bundleContext.registerService(OpenTracer.class, new JaegerTracer(), null);
            registration = bundleContext.registerService(OpenTracer.class, new ZipkinTracer(), null);
            registration = bundleContext.registerService(OpenTracer.class, new LogTracer(), null);
            registration = bundleContext.registerService(TracingService.class, TracingServiceImpl.getInstance(), null);
            registration = bundleContext.registerService(APIMOpenTelemetry.class, new JaegerTelemetry(), null);
            registration = bundleContext.registerService(APIMOpenTelemetry.class, new ZipkinTelemetry(), null);
            registration = bundleContext.registerService(APIMOpenTelemetry.class, new LogTelemetry(), null);
            registration = bundleContext.registerService(APIMOpenTelemetry.class, new OTLPTelemetry(), null);
            registration = bundleContext.registerService(TelemetryService.class, TelemetryServiceImpl.getInstance(),
                    null);

        } catch (Exception e) {
            log.error("Error occured in tracing component activation", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {

        log.debug("Tracing Component deactivated");
        registration.unregister();
    }

    @Reference(
            name = "api.manager.config.service",
            service = org.wso2.carbon.apimgt.impl.APIManagerConfigurationService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetAPIManagerConfigurationService")
    protected void setAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(amcService);
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService amcService) {

        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(null);
    }

    @Reference(
            name = "opentracing.tracer.service",
            service = OpenTracer.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTracerService")
    protected void setTracerService(OpenTracer tracer) {

        ServiceReferenceHolder.getOpenTracerMap().put(tracer.getName(), tracer);
    }

    protected void unsetTracerService(OpenTracer tracer) {

        ServiceReferenceHolder.getOpenTracerMap().remove(tracer.getName());
    }

    @Reference(
            name = "opentelemetry.tracer.service",
            service = APIMOpenTelemetry.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetTelemetryService")
    protected void setTelemetryService(APIMOpenTelemetry tracer) {

        ServiceReferenceHolder.getOpenTelemetryTracerMap().put(tracer.getName(), tracer);
    }

    protected void unsetTelemetryService(APIMOpenTelemetry tracer) {

        tracer.close();
        ServiceReferenceHolder.getOpenTelemetryTracerMap().remove(tracer.getName());
    }
}

