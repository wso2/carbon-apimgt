package org.wso2.carbon.apimgt.tracing.jaeger;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.OpenTracer;

public class JaegerTracerImpl extends OpenTracer {

    private static final String NAME = "jaeger";

    @Override
    public Tracer getTracer(String tracerName, APIManagerConfiguration configuration, String serviceName) {


        String hostname = configuration.getFirstProperty(Constants.CONFIG_HOST) != null ?
            configuration.getFirstProperty(Constants.CONFIG_HOST) : Constants.DEFAULT_HOST;

        int port = configuration.getFirstProperty(Constants.CONFIG_PORT) != null ?
            Integer.parseInt(configuration.getFirstProperty(Constants.CONFIG_PORT)) : Constants.DEFAULT_PORT;

        String samplerType = configuration.getFirstProperty(Constants.CONFIG_SAMPLER_TYPE) != null ?
            configuration.getFirstProperty(Constants.CONFIG_SAMPLER_TYPE) : Constants.DEFAULT_SAMPLER_TYPE;

        float samplerParam = configuration.getFirstProperty(Constants.CONFIG_SAMPLER_PARAM) != null ?
            Float.parseFloat(configuration.getFirstProperty(Constants.CONFIG_SAMPLER_PARAM)) : Constants.DEFAULT_SAMPLER_PARAM;

        int reporterFlushInterval = configuration.getFirstProperty(Constants.CONFIG_REPORTER_FLUSH_INTERVAL) != null ?
            Integer.parseInt(configuration.getFirstProperty(Constants.CONFIG_REPORTER_FLUSH_INTERVAL)) : Constants.DEFAULT_REPORTER_FLUSH_INTERVAL;

        int reporterBufferSize = configuration.getFirstProperty(Constants.CONFIG_REPORTER_BUFFER_SIZE) != null ?
            Integer.parseInt(configuration.getFirstProperty(Constants.CONFIG_REPORTER_BUFFER_SIZE)) : Constants.DEFAULT_REPORTER_BUFFER_SIZE;

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

        Tracer tracer = new Configuration(serviceName).withSampler(samplerConfig).withReporter(reporterConfig).getTracer();
        return tracer;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

