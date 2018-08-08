package org.wso2.carbon.apimgt.tracing;

import io.opentracing.Tracer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

/**
 * @scr.component name="org.wso2.apimgt.impl.services" immediate="true"
 *  * @scr.reference name="api.manager.config.service"
 *  * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService" cardinality="1..1"
 *  * policy="dynamic" bind="setAPIManagerConfigurationService" unbind="unsetAPIManagerConfigurationService"
 */


public class OpenTracerService {

    private static final Log log = LogFactory.getLog(OpenTracerService.class) ;

    private APIManagerConfiguration configuration = new APIManagerConfiguration();
    TracerLoader tracerLoader = TracerLoader.getInstance();
    private ServiceRegistration registration;

    private String openTracerName;
    private String enabled;
    private String hostname;
    private int port;
    private String samplerType;
    private float samplerParam;
    private int reporterFlushInterval;
    private int reporterBufferSize;

    private String apiContext;
    private boolean compressionEnabled;
    private String apiVersion;


    protected void activate(ComponentContext componentContext) {

        BundleContext bundleContext = componentContext.getBundleContext();
        bundleContext.registerService(OpenTracer.class.getName(), new ZipkinTracerImpl(), null);
        bundleContext.registerService(OpenTracer.class.getName(),new JaegerTracerImpl(),null);

        if (log.isDebugEnabled()) {
            log.debug("OpenTracer service component activated");
        }
        try {
            String filePath = getFilePath();
            configuration.load(filePath);

            openTracerName = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_NAME); //"OpenTracer.Name"
            enabled = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_ENABLED);

            if(openTracerName.equalsIgnoreCase("JAEGER") && enabled.equalsIgnoreCase("TRUE")) {

                Tracer tracer = tracerLoader.getTracer(openTracerName, configuration);

            }else if(openTracerName.equalsIgnoreCase("ZIPKIN") && enabled.equalsIgnoreCase("TRUE")) {

                Tracer tracer = tracerLoader.getTracer(openTracerName,configuration);



            }else
                log.error("Invalid Configuration");
            } catch (APIManagementException e) {
            e.printStackTrace();
        }


//        OkHttpSender sender = OkHttpSender.create("http://localhost:9411/api/v1/spans");
//        Tracer tracer = BraveTracer.create(Tracing.newBuilder()
//                .localServiceName("Hello")
//                .spanReporter(AsyncReporter.builder(sender).build())
//                .build());
//
//
//        ServiceReferenceHolder.getInstance().setTracer(tracer);

    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("API handlers component deactivated");
        }

        if (registration != null) {
            log.debug("Unregistering ThrottleDataService...");
            registration.unregister();
        }
    }

    protected String getFilePath() {
        return CarbonUtils.getCarbonConfigDirPath() + File.separator + "api-manager.xml";
    }
}
