package org.wso2.carbon.apimgt.tracing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;


public class OpenTracerService {

    private static final Log log = LogFactory.getLog(OpenTracerService.class) ;

    private APIManagerConfiguration configuration = new APIManagerConfiguration();
    private ServiceRegistration registration;


    protected void activate(ComponentContext componentContextcontext) {

        BundleContext bundleContext = componentContextcontext.getBundleContext();
        bundleContext.registerService(OpenTracer.class.getName(), new ZipkinTracerImpl(), null);

        if (log.isDebugEnabled()) {
            log.debug("OpenTracer service component activated");
        }
        try {
            String filePath = getFilePath();
            configuration.load(filePath);

            String openTracerName = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_NAME); //"OpenTracer.Name"
            String enabled = configuration.getFirstProperty(OpenTracerConstants.OPEN_TRACER_ENABLED);
            log.info(openTracerName);
            if ("jaeger".equalsIgnoreCase(openTracerName)) {}

            log.info(openTracerName);

        } catch (APIManagementException e) {
            e.printStackTrace();
        }
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
