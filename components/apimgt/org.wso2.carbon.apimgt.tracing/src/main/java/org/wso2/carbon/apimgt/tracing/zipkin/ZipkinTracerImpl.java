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
    public Tracer getTracer(String tracerName, APIManagerConfiguration configuration, String serviceName) {

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
