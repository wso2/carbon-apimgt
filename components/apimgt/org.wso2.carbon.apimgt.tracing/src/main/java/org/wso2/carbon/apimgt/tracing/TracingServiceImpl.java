package org.wso2.carbon.apimgt.tracing;

import brave.Tracing;
import brave.opentracing.BraveTracer;
import io.opentracing.Tracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.okhttp3.OkHttpSender;

import java.io.File;

public class TracingServiceImpl implements TracingService{

    private static final Log log = LogFactory.getLog(TracingServiceImpl.class) ;
    private APIManagerConfiguration configuration = new APIManagerConfiguration();
    TracerLoader tracerLoader = TracerLoader.getInstance();

    String filePath = getFilePath();
    configuration.load(filePath);

    String openTracerName = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_NAME);
    String enabled = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_ENABLED);



    @Override
    public Tracer getTracer(String tracerName) {

        OkHttpSender sender = OkHttpSender.create("http://localhost:9411/api/v1/spans");
        Tracer tracer = BraveTracer.create(Tracing.newBuilder()
                .localServiceName("Hello")
                .spanReporter(AsyncReporter.builder(sender).build())
                .build());

        return tracer;
    }

    protected String getFilePath() {
        return CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
    }
}
