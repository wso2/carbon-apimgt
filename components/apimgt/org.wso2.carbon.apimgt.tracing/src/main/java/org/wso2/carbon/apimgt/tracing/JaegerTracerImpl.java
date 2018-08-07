package org.wso2.carbon.apimgt.tracing;

import io.jaegertracing.Configuration;
import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import javax.cache.InvalidConfigurationException;
import java.io.File;

public class JaegerTracerImpl implements OpenTracer{

    private static final String NAME = "jaeger";

    private String hostname;
    private int port;
    private String samplerType;
    private float samplerParam;
    private int reporterFlushInterval;
    private int reporterBufferSize;
    private  String openTracerName;


    private APIManagerConfiguration configuration = new APIManagerConfiguration();

    @Override
    public void init() throws InvalidConfigurationException {

        try {
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
            configuration.load(filePath);

            port = Integer.parseInt(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_PORT));
            hostname = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_HOST);
            samplerType = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_SAMPLER_TYPE);
            samplerParam = Float.parseFloat(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_SAMPLER_PARAM));
            reporterFlushInterval = Integer.parseInt(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_REPORTER_FLUSH_INTERVAL));
            reporterBufferSize = Integer.parseInt(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_REPORTER_BUFFER_SIZE));
            openTracerName = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_NAME);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tracer getTracer(String tracerName,String serviceName) {


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
        tracer = new Configuration(serviceName).withSampler(samplerConfig).withReporter(reporterConfig).getTracer();

        return tracer;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

