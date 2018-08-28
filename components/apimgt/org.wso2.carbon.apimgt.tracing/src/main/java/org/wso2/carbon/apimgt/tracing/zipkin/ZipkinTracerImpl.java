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

package org.wso2.carbon.apimgt.tracing.zipkin;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.OpenTracer;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class ZipkinTracerImpl extends OpenTracer {

    private static final String NAME = "zipkin";

    @Override
    public Tracer getTracer(APIManagerConfiguration configuration, String serviceName) {

        String hostname = configuration.getFirstProperty(Constants.CONFIG_HOST) != null ?
                configuration.getFirstProperty(Constants.CONFIG_HOST) : Constants.DEFAULT_HOST;

        int port = configuration.getFirstProperty(Constants.CONFIG_PORT) != null ?
                Integer.parseInt(configuration.getFirstProperty(Constants.CONFIG_PORT)) : Constants.DEFAULT_PORT;

        String apiContext = configuration.getFirstProperty(Constants.CONFIG_API_CONTEXT) != null ?
                configuration.getFirstProperty(Constants.CONFIG_API_CONTEXT) : Constants.DEFAULT_API_CONTEXT;

        OkHttpSender sender = OkHttpSender.create("http://" + hostname + ":" + port + apiContext);
        Tracer tracer = BraveTracer.create(Tracing.newBuilder()
                .localServiceName(serviceName)
                .spanReporter(AsyncReporter.builder(sender).build())
                .build());

        return tracer;
    }

    @Override
    public String getName() {

        return NAME;
    }
}
