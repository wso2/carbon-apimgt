package org.wso2.carbon.apimgt.tracing;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

public class JaegerTracerImpl implements OpenTracer{

    private static final String NAME = "jaeger";

    @Override
    public Tracer getTracer(String tracerName, APIManagerConfiguration configuration) {


        String hostname = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_HOST);
        int port = Integer.parseInt(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_PORT));
        String samplerType = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_SAMPLER_PARAM);
        float samplerParam = Float.parseFloat(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_SAMPLER_PARAM));
        int reporterFlushInterval = Integer.parseInt(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_REPORTER_FLUSH_INTERVAL));
        int reporterBufferSize = Integer.parseInt(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_REPORTER_BUFFER_SIZE));

        Tracer tracer = null;
        Configuration.SamplerConfiguration samplerConfig = new Configuration.SamplerConfiguration()
                .withType(samplerType)
                .withParam(samplerParam);
        Configuration.SenderConfiguration senderConfig = new Configuration.SenderConfiguration()
                .withAgentHost(hostname)
                .withAgentPort(port);
        Configuration.ReporterConfiguration reporterConfig = new Configuration.ReporterConfiguration()
                .withLogSpans(true)
                .withFlushInterval(reporterFlushInterval)
                .withMaxQueueSize(reporterBufferSize)
                .withSender(senderConfig);

        tracer = new Configuration("kk").withSampler(samplerConfig).withReporter(reporterConfig).getTracer();

        return tracer;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

