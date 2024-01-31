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
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.apimgt.tracing.telemetry.TelemetryConstants.OTEL_RESOURCE_ATTRIBUTES_ENVIRONMENT_VARIABLE_NAME;
import static org.wso2.carbon.apimgt.tracing.telemetry.TelemetryConstants.OTEL_RESOURCE_ATTRIBUTE_CONFIG_KEYS_PREFIX;

/**
 * Span utility class.
 */

public class TelemetryUtil {

    private static final Log log = LogFactory.getLog(TelemetryUtil.class);

    /**
     * Start the telemetry tracing span.
     *
     * @param spanName   Operation name of the span.
     * @param parentSpan Root span of the new span to be created.
     * @param tracer     Initialized {@link io.opentelemetry.api.trace.Tracer}.
     * @return TelemetrySpan object.
     */
    public static TelemetrySpan startSpan(String spanName, TelemetrySpan parentSpan, TelemetryTracer tracer) {

        Span childSpan;
        if (parentSpan == null) {
            Span span = tracer.getTelemetryTracingTracer().spanBuilder(spanName).startSpan();
            return new TelemetrySpan(span);
        } else {
            Object sp = parentSpan.getSpan();
            if (sp != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Parent span exist");
                }
                if (sp instanceof Span) {
                    childSpan = tracer.getTelemetryTracingTracer().spanBuilder(spanName).setParent(Context.current()
                            .with((Span) sp)).startSpan();
                } else {
                    childSpan =
                            tracer.getTelemetryTracingTracer().spanBuilder(spanName)
                                    .setParent((Context) sp).startSpan();
                }
            } else {
                childSpan = tracer.getTelemetryTracingTracer().spanBuilder(spanName).startSpan();
            }
        }
        return new TelemetrySpan(childSpan);
    }

    /**
     * Start telemetry tracing span after extracting a context.
     *
     * @param spanName   Operation name of the span.
     * @param parentSpan Root context of the new span to be created.
     * @param tracer     Initialized {@link io.opentelemetry.api.trace.Tracer}.
     * @return TelemetrySpan object.
     */
    public static TelemetrySpan startSpan(String spanName, Context parentSpan, TelemetryTracer tracer) {

        Span childSpan;
        childSpan = tracer.getTelemetryTracingTracer().spanBuilder(spanName).setParent(parentSpan).startSpan();

        return new TelemetrySpan(childSpan);
    }

    /**
     * Set tag to the span.
     *
     * @param span  Span which the tag is set.
     * @param key   keys.
     * @param value value for the keys.
     */
    public static void setTag(TelemetrySpan span, String key, String value) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).setAttribute(key, value);
        }
    }

    /**
     * Update operation to the span.
     *
     * @param span Current span which the update operation is done.
     * @param name Updated name.
     */
    public static void updateOperation(TelemetrySpan span, String name) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).updateName(name);
        }
    }

    /**
     * Finish the span.
     *
     * @param span Span to be ended.
     */
    public static void finishSpan(TelemetrySpan span) {

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            ((Span) sp).end();
        }
    }

    /**
     * Inject tracer specific information to tracerSpecificCarrier.
     *
     * @param span                  Span which the span information will be injected to the tracerSpecificCarrier.
     * @param tracerSpecificCarrier Hashmap to inject the tracer and span context.
     */
    public static void inject(TelemetrySpan span, Map<String, String> tracerSpecificCarrier) {

        OpenTelemetry openTelemetry = TelemetryServiceImpl.getInstance().getOpenTelemetry();
        TextMapSetter<Map<String, String>> setter = (tracerSpecificCarrier1, key, value) -> {

            if (tracerSpecificCarrier1 != null) {
                if (log.isDebugEnabled()) {
                    log.debug("value: " + value + " was set for key: " + key + " in tracer specific carrier");
                }

                tracerSpecificCarrier1.put(key, value);
            }

        };

        Object sp = span.getSpan();
        if (sp instanceof Span) {
            try (Scope ignored = ((Span) sp).makeCurrent()) {
                openTelemetry.getPropagators().getTextMapPropagator().inject(Context.current(), tracerSpecificCarrier
                        , setter);
            }
        }
    }

    /**
     * Extract tracer specific information from tracerSpecificCarrier and return the extracted context.
     *
     * @param tracerSpecificCarrier Hashmap to extract the tracer and span context.
     * @return extracted context.
     */
    public static Context extract(Map<String, String> tracerSpecificCarrier) {

        OpenTelemetry openTelemetry = TelemetryServiceImpl.getInstance().getOpenTelemetry();
        TextMapGetter<Map<String, String>> getter =
                new TextMapGetter<Map<String, String>>() {
                    public String get(Map<String, String> tracerSpecificCarrier, String key) {

                        if (tracerSpecificCarrier != null && tracerSpecificCarrier.containsKey(key)) {
                            if (log.isDebugEnabled()) {
                                log.debug("value: " + tracerSpecificCarrier.get(key) + " found for key: " + key +
                                        " in tracer specific carrier");
                            }
                            return tracerSpecificCarrier.get(key);
                        }
                        return null;
                    }

                    public Iterable<String> keys(Map<String, String> tracerSpecificCarrier) {

                        return tracerSpecificCarrier.keySet();
                    }
                };

        if (log.isDebugEnabled()) {
            log.debug("Extraction starts");
        }

        return openTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), tracerSpecificCarrier, getter);
    }

    /**
     * Check whether telemetry tracing is enabled.
     **/
    public static boolean telemetryEnabled() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            if (log.isDebugEnabled()) {
                log.debug("API Manager Configuration is set");
            }
            boolean remoteTelemetryTracerEnabled =
                    Boolean.parseBoolean(apiManagerConfiguration
                            .getFirstProperty(TelemetryConstants.REMOTE_TELEMETRY_TRACER_ENABLED));
            boolean logTelemetryTracerEnabled =
                    Boolean.parseBoolean(apiManagerConfiguration
                            .getFirstProperty(TelemetryConstants.LOG_TELEMETRY_TRACER_ENABLED));
            if (log.isDebugEnabled()) {
                log.debug("Remote Telemetry Tracer Enabled: " + remoteTelemetryTracerEnabled);
                log.debug("Log Telemetry Tracer Enabled: " + logTelemetryTracerEnabled);
            }
            return remoteTelemetryTracerEnabled || logTelemetryTracerEnabled;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("API Manager Configuration is null");
            }
        }
        return false;
    }

    /**
     * Gets the tracer provider resource with the provided default service name.
     *
     * @param defaultServiceName    Default service name.
     * @return                      Tracer provider resource.
     */
    public static Resource getTracerProviderResource(String defaultServiceName) {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        Map<String, String> otelResourceAttributes = new HashMap<>();

        // Get resource attributes from configuration
        Set<String> otelResourceAttributeConfigKeys = configuration.getConfigKeySet().stream()
                .filter(entry -> entry.startsWith(OTEL_RESOURCE_ATTRIBUTE_CONFIG_KEYS_PREFIX))
                .collect(Collectors.toSet());
        for (String configKey : otelResourceAttributeConfigKeys) {
            String otelResourceAttributeKey = configKey.substring(OTEL_RESOURCE_ATTRIBUTE_CONFIG_KEYS_PREFIX.length());
            otelResourceAttributes.put(otelResourceAttributeKey, configuration.getFirstProperty(configKey));
        }

        /* Get resource attributes from environment variables. If a resource attribute's value has been already
        provided via configuration, the value provided via environment variable will overwrite that. */
        String environmentVariableValue = System.getenv(OTEL_RESOURCE_ATTRIBUTES_ENVIRONMENT_VARIABLE_NAME);
        if (environmentVariableValue != null) {
            String[] resourceAttributes = StringUtils.split(environmentVariableValue, ",");
            for (String keyValuePair : resourceAttributes) {
                String[] keyValue = StringUtils.split(keyValuePair, "=");
                otelResourceAttributes.put(keyValue[0], keyValue[1]);
            }
        }

        AttributesBuilder attributesBuilder = Attributes.builder();
        for (Map.Entry<String, String> otelResourceAttribute : otelResourceAttributes.entrySet()) {
            attributesBuilder.put(otelResourceAttribute.getKey(), otelResourceAttribute.getValue());
        }
        Attributes attributes = attributesBuilder.build();

        Resource tracerProviderResource = Resource.getDefault();
        Resource serviceNameResource = Resource.create(
                Attributes.of(ResourceAttributes.SERVICE_NAME, defaultServiceName));
        tracerProviderResource = tracerProviderResource.merge(serviceNameResource);
        tracerProviderResource = tracerProviderResource.merge(Resource.create(attributes));

        return tracerProviderResource;
    }

    private TelemetryUtil() {
    }
}
