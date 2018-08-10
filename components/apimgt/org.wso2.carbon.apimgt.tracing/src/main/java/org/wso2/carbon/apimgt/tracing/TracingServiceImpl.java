package org.wso2.carbon.apimgt.tracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.opentracing.Tracer;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class TracingServiceImpl implements TracingService{
    @Override
    public Tracer getTracer(String tracerName) {

        OkHttpSender sender = OkHttpSender.create("http://localhost:9411/api/v1/spans");
        Tracer tracer = BraveTracer.create(Tracing.newBuilder()
                .localServiceName("Hello")
                .spanReporter(AsyncReporter.builder(sender).build())
                .build());

        return tracer;
    }

//    private static final Logger LOGGER = Logger.getLogger(TracingServiceImpl.class.getName());
//
//    @Override
//    public void produce(String name) {
//
//        LOGGER.info("Successfully produced: "+ name);
//
//    }
}
