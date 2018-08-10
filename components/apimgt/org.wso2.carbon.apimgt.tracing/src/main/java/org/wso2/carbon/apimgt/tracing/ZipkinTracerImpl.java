package org.wso2.carbon.apimgt.tracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class ZipkinTracerImpl implements OpenTracer {

    private static final String NAME = "zipkin";

    @Override
    public Tracer getTracer(String tracerName, APIManagerConfiguration configuration) {

        String hostname = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_HOST);
        int port = Integer.parseInt(configuration.getFirstProperty(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_PORT)));
        String apiContext = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_API_CONTEXT);
        boolean compressionEnabled = Boolean.parseBoolean(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_COMPRESSION_ENABLED));
        String apiVersion = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_API_VERSION);

        OkHttpSender sender = OkHttpSender.create("http://" + hostname + ":" + port + apiContext);
        Tracer tracer = BraveTracer.create(Tracing.newBuilder()
                .localServiceName("Hello")
                .spanReporter(AsyncReporter.builder(sender).build())
                .build());

        return tracer;

    }

    @Override
    public String getName() {
        return NAME;
    }
}
