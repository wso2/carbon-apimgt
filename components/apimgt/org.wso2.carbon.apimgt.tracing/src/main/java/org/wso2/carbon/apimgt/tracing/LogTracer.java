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

import io.opentracing.Tracer;
import io.opentracing.contrib.reporter.Reporter;
import io.opentracing.contrib.reporter.TracerR;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.internal.ServiceReferenceHolder;

/**
 * This class used to log tracing activities.
 */
public class LogTracer implements OpenTracer {

    private static final String NAME = "log";
    private static APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
            .getAPIManagerConfiguration();

    @Override
    public Tracer getTracer(String serviceName) {

        boolean logEnabled = Boolean.valueOf(configuration.getFirstProperty(TracingConstants.LOG_ENABLED));
        if (logEnabled) {
            Tracer tracer = NoopTracerFactory.create();
            Reporter reporter = new TracingReporter(LogFactory.getLog(TracingConstants.TRACER));
            Tracer tracerR = new TracerR(tracer, reporter, new ThreadLocalScopeManager());
            GlobalTracer.register(tracerR);
            return tracerR;
        }
        return null;
    }

    @Override
    public String getName() {

        return NAME;
    }
}
