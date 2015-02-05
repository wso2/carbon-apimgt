package org.wso2.carbon.throttle.module;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEvent;
import org.apache.axis2.engine.AxisObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.throttle.core.Throttle;
import org.wso2.carbon.throttle.core.ThrottleConstants;

import java.util.ArrayList;

public class ThrottleObserver implements AxisObserver {

    private static Log log = LogFactory.getLog(ThrottleObserver.class.getName());

    private ConfigurationContext configctx;
    private Throttle defautThrottle;

    public ThrottleObserver(ConfigurationContext configctx, Throttle defaultThrottle) {
        this.configctx = configctx;
        this.defautThrottle = defaultThrottle;
    }

    public void init(AxisConfiguration axisConfiguration) {
    }

    public void serviceUpdate(AxisEvent axisEvent, AxisService axisService) {
        log.debug("ThrottleObserver notified for a serviceUpdate.");

        AxisDescription axisDescription = axisEvent.getAxisDescription();
        if (axisDescription.isEngaged(axisService.getAxisConfiguration().
                getModule(ThrottleConstants.THROTTLE_MODULE_NAME))) {
            if (axisEvent.getEventType() == AxisEvent.POLICY_ADDED) {
                try {
                    ThrottleEnguageUtils.enguage(axisDescription, configctx, defautThrottle);
                } catch (AxisFault axisFault) {
                    log.error("Error while re-engaging throttling", axisFault);
                }
            }
        }
    }

    public void serviceGroupUpdate(AxisEvent axisEvent, AxisServiceGroup axisServiceGroup) {
    }

    public void moduleUpdate(AxisEvent axisEvent, AxisModule axisModule) {
    }

    public void addParameter(Parameter parameter) throws AxisFault {
    }

    public void removeParameter(Parameter parameter) throws AxisFault {
    }

    public void deserializeParameters(OMElement omElement) throws AxisFault {
    }

    public Parameter getParameter(String s) {
        return null;
    }

    public ArrayList<Parameter> getParameters() {
        return null;
    }

    public boolean isParameterLocked(String s) {
        return false;
    }
}
