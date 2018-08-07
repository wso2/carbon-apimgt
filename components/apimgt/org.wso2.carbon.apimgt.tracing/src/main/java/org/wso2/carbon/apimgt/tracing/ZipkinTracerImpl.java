package org.wso2.carbon.apimgt.tracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.opentracing.Tracer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

import javax.cache.InvalidConfigurationException;
import java.io.File;
import java.io.PrintStream;

public class ZipkinTracerImpl implements OpenTracer {

    private static final PrintStream console = System.out;
    private static final String NAME = "zipkin";

    private String hostname;
    private int port;
    private String apiContext;
    private boolean compressionEnabled;
    private String apiVersion;

    private APIManagerConfiguration configuration = new APIManagerConfiguration();


    @Override
    public void init() throws InvalidConfigurationException {

        try {
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
            configuration.load(filePath);

            hostname = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_HOST);
            port = Integer.parseInt(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_PORT));
            apiContext = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_API_CONTEXT);
            compressionEnabled = Boolean.parseBoolean(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_COMPRESSION_ENABLED));
            apiVersion = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_API_VERSION);
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Tracer getTracer(String tracerName, String serviceName) {

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
