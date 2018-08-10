package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class TracerLoader {

    private Map<String, Tracer> tracerLoader;
    private static TracerLoader instance = new TracerLoader();
    private static final Log log = LogFactory.getLog(TracerLoader.class);
    public static TracerLoader getInstance() {
        return instance;
    }

    private TracerLoader() {
    }

    public Tracer getTracer(String openTracerName, APIManagerConfiguration configuration) {

        ServiceLoader<OpenTracer> openTracers = ServiceLoader.load(OpenTracer.class);
        HashMap<String, OpenTracer> tracerMap = new HashMap<>();
        openTracers.forEach(t -> tracerMap.put(t.getName().toLowerCase(), t));

        OpenTracer openTracer = tracerMap.get(openTracerName.toLowerCase());

        return openTracer.getTracer(openTracerName,configuration);

        }

}
