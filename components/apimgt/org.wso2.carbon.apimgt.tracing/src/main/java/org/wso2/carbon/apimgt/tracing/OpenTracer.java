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

import io.opentracing.Span;
import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

public abstract class OpenTracer {

    public abstract Tracer getTracer(APIManagerConfiguration configuration, String serviceName);

    public abstract String getName();

    public static TracingSpan startSpan(String spanName, TracingSpan parentSpan, TracingTracer tracer) {

        if (parentSpan == null) {
            Span span = tracer.getTracingTracer().buildSpan(spanName).start();
            return new TracingSpan(span);

        } else {
            Span childSpan = tracer.getTracingTracer().buildSpan(spanName).asChildOf(parentSpan.getSpan()).start();
            return new TracingSpan(childSpan);
        }
    }

    public static void setTag(TracingSpan span, String key, String value) {

        span.getSpan().setTag(key, value);
    }

    public static void finishSpan(TracingSpan span) {

        span.getSpan().finish();
    }

}