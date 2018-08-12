package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.tracing.jaeger.JaegerTracerImpl;
import org.wso2.carbon.apimgt.tracing.zipkin.ZipkinTracerImpl;
import org.wso2.carbon.utils.CarbonUtils;


import java.io.File;

public class TracingServiceImpl implements TracingService {

    private static final Log log = LogFactory.getLog(TracingServiceImpl.class);
    private APIManagerConfiguration configuration = new APIManagerConfiguration();

    @Override
    public Tracer getTracer(String serviceName) {
        try {
            log.error("$$$$$$$Praveen hello$$$$$$$$");
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
            configuration.load(filePath);

        } catch (APIManagementException e) {
            e.printStackTrace();
        }

        String t = configuration.getFirstProperty("APIManager.DataSourceName");
        String openTracerName = configuration.getFirstProperty("OpenTracer.Name");
        String enabled = configuration.getFirstProperty("OpenTracer.Enabled");

        if (openTracerName.equalsIgnoreCase("JAEGER") && enabled.equalsIgnoreCase("TRUE")) {

            Tracer tracer = new JaegerTracerImpl().getTracer(openTracerName, configuration, serviceName);
            return tracer;

        } else if (openTracerName.equalsIgnoreCase("ZIPKIN") && enabled.equalsIgnoreCase("TRUE")) {

            Tracer tracer = new ZipkinTracerImpl().getTracer(openTracerName, configuration, serviceName);
            return tracer;

        } else {
            log.error("Invalid Configuration");
        }

        return null;
    }

}
