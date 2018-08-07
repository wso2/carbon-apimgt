package org.wso2.carbon.apimgt.tracing;

import com.hazelcast.config.InvalidConfigurationException;
import io.opentracing.Tracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class TracerLoader {

    private TracerGenerator tracer;
    private Map<String, Tracer> tracerLoader;
    private static TracerLoader instance = new TracerLoader();
    private APIManagerConfiguration configuration = new APIManagerConfiguration();
    private static final Log log = LogFactory.getLog(TracerLoader.class);
    public static TracerLoader getInstance() {
        return instance;
    }

    private TracerLoader() {
    }

    public void loadTracers() {

        try {
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
            configuration.load(filePath);

            this.tracerLoader = new HashMap<String, Tracer>();
            if(Boolean.valueOf(configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_ENABLED))) {

                ServiceLoader<OpenTracer> openTracers = ServiceLoader.load(OpenTracer.class);
                Map<String, OpenTracer> tracerMap = new HashMap<String, OpenTracer>();
                openTracers.forEach(t -> tracerMap.put(t.getName().toLowerCase(), t));

                String tracerName = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_NAME) ;

                OpenTracer openTracer = tracerMap.get(tracerName.toLowerCase());
                if (openTracer != null) {
                    try {
                        openTracer.init();
                        tracer = new TracerGenerator(openTracer.getName(), openTracer);
                    } catch (InvalidConfigurationException e) {
                        log.error("API Manager: error in observability tracing configurations: " + e.getMessage());
                    }
                } else {
                    log.error("API Manager: observability enabled but no tracing extension found for name " + tracerName);
                }
            }
        } catch (APIManagementException e) {
            e.printStackTrace();
        }
    }

    public Tracer getTracer(String serviceName) {
        if (tracerLoader.containsKey(serviceName)) {
            return tracerLoader.get(serviceName);
        } else {
            Tracer openTracer = null;
            if (tracer != null) {
                try {
                    openTracer = tracer.generate(serviceName);
                } catch (Throwable e) {
                    log.error("ballerina: error getting tracer for " + tracer.name + ". " + e.getMessage());
                }
            }
            tracerLoader.put(serviceName, openTracer);
            return openTracer;
        }
    }


    private static class TracerGenerator {
        String name;
        OpenTracer tracer;

        TracerGenerator(String name, OpenTracer tracer) {
            this.name = name;
            this.tracer = tracer;
        }

        Tracer generate(String serviceName) {
            return tracer.getTracer(name, serviceName);
        }
    }

//    public boolean isInitialized() {
//        return this.tracerLoader != null;
//    }
}
