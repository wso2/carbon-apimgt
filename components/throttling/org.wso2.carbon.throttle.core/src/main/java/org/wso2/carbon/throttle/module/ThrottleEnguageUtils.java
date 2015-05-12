package org.wso2.carbon.throttle.module;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.PolicyUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.xml.XmlPrimtiveAssertion;
import org.wso2.carbon.throttle.core.ConcurrentAccessController;
import org.wso2.carbon.throttle.core.Throttle;
import org.wso2.carbon.throttle.core.ThrottleConstants;
import org.wso2.carbon.throttle.core.ThrottleException;
import org.wso2.carbon.throttle.core.ThrottleFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ThrottleEnguageUtils {
    private static Log log = LogFactory.getLog(ThrottleEnguageUtils.class.getName());

    public static void enguage(AxisDescription axisDescription, ConfigurationContext configctx,
                               Throttle defaultThrottle) throws AxisFault {
        String currentServiceName;
        if (axisDescription instanceof AxisService) {
            Throttle throttle = null;
            AxisService currentService = ((AxisService) axisDescription);
            PolicySubject policySubject = currentService.getPolicySubject();
            if (policySubject != null) {
                try {
                    List policies = new ArrayList(policySubject.getAttachedPolicyComponents());
                    Policy currentPolicy = PolicyUtil.getMergedPolicy(policies, currentService);
                    if (currentPolicy != null) {
                        throttle = ThrottleFactory.createServiceThrottle(currentPolicy);
                        if(throttle == null) {
                            //this is for the scenario when throttle policy is empty rather than
                            // null (eg: removing the throttle policy via policy editor)
                            throttle = defaultThrottle;
                        }
                        //todo - done by isuru, recheck
                    } else {
                        AxisConfiguration axisConfig = configctx.getAxisConfiguration();
                        AxisModule throttleModule = axisConfig.getModule(
                                ThrottleConstants.THROTTLE_MODULE_NAME);
                        policySubject = throttleModule.getPolicySubject();
                        if (policySubject != null) {
                            currentPolicy = ThrottleEnguageUtils.getThrottlePolicy(
                                    policySubject.getAttachedPolicyComponents());
                            if (currentPolicy != null) {
                                throttle = ThrottleFactory.createModuleThrottle(currentPolicy);
                            }
                        }
                        //todo - done by isuru
                    }
                } catch (ThrottleException e) {

                    log.error("Error was occurred when engaging throttle module for" +
                              " the service :" +
                              currentService.getName() + e.getMessage());
                    log.info("Throttling will occur using default module policy");
                    throttle = defaultThrottle;
                }
                if (throttle != null) {
                    Map throttles =
                            (Map) configctx.getPropertyNonReplicable(
                                    ThrottleConstants.THROTTLES_MAP);
                    if (throttles == null) {
                        throttles = new HashMap();
                        configctx.setNonReplicableProperty(
                                ThrottleConstants.THROTTLES_MAP, throttles);
                    }
                    String serviceName = currentService.getName();
                    throttle.setId(serviceName);
                    throttles.put(serviceName, throttle);
                    ConcurrentAccessController cac = throttle.getConcurrentAccessController();
                    if (cac != null) {
                        String cacKey = ThrottleConstants.THROTTLE_PROPERTY_PREFIX
                                        + serviceName + ThrottleConstants.CAC_SUFFIX;
                        configctx.setProperty(cacKey, cac);
                    }
                }
            }
        } else if (axisDescription instanceof AxisOperation) {

            Throttle throttle = null;
            AxisOperation currentOperation = ((AxisOperation) axisDescription);
            AxisService axisService = (AxisService) currentOperation.getParent();
            if (axisService != null) {
                currentServiceName = axisService.getName();
                PolicySubject policySubject = currentOperation.getPolicySubject();
                if (policySubject != null) {
                    try {
                        List policies = new ArrayList(policySubject.getAttachedPolicyComponents());
                        Policy currentPolicy = PolicyUtil.getMergedPolicy(policies,
                                                                          currentOperation);
                        if (currentPolicy != null) {
                            throttle = ThrottleFactory.createOperationThrottle(currentPolicy);
                        }
                    } catch (ThrottleException e) {
                        log.error("Error was occurred when engaging throttle module " +
                                  "for operation : " +
                                  currentOperation.getName() + " in the service :" +
                                  currentServiceName + e.getMessage());
                        log.info("Throttling will occur using default module policy");
                    }

                    // if current throttle is null, use the default throttle
                    if (throttle == null) {
                        throttle = defaultThrottle;
                    }

                    Map throttles = (Map) configctx
                            .getPropertyNonReplicable(ThrottleConstants.THROTTLES_MAP);
                    if (throttles == null) {
                        throttles = new HashMap();
                        configctx.setNonReplicableProperty(ThrottleConstants.THROTTLES_MAP
                                , throttles);
                    }
                    QName opQName = currentOperation.getName();
                    if (opQName != null) {
                        String opName = opQName.getLocalPart();
                        String key = currentServiceName + opName;
                        throttle.setId(key);
                        throttles.put(key, throttle);
                        ConcurrentAccessController cac =
                                throttle.getConcurrentAccessController();
                        if (cac != null) {
                            String cacKey = ThrottleConstants.THROTTLE_PROPERTY_PREFIX
                                    + key + ThrottleConstants.CAC_SUFFIX;
                            configctx.setProperty(cacKey, cac);
                        }
                    }
                }
            }
        }

    }

    //todo - done by isuru, recheck
    private static Policy getThrottlePolicy(Collection components) throws AxisFault {
        QName assertionName;
        //Finds the policy for throttling
        Iterator i = components.iterator();
        while (i.hasNext()) {
            Object comp = i.next();
//        }
//        for (Object comp : components) {
            Policy policy = (Policy) comp;
            for (Iterator iterator = policy.getAlternatives();
                 iterator.hasNext();) {
                Object object = iterator.next();
                if (object instanceof List) {
                    List list = (List) object;
                    Iterator j = list.iterator();
                    while (j.hasNext()) {
//                    for (Object assertObj : list) {
                        Object assertObj = j.next();
                        if (assertObj instanceof XmlPrimtiveAssertion) {
                            XmlPrimtiveAssertion primitiveAssertion = (XmlPrimtiveAssertion)
                                    assertObj;
                            assertionName = primitiveAssertion.getName();
                            if (assertionName.equals(ThrottleConstants
                                    .SERVICE_THROTTLE_ASSERTION_QNAME) || assertionName
                                    .equals(ThrottleConstants.MODULE_THROTTLE_ASSERTION_QNAME)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Existing ThrottleAssertion found");
                                }
                                return policy;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks whether the given service group is a special service
     *
     * @param axisServiceGroup - ServiceGroup instance
     * @return - true if this is a filtered service
     */
    public static boolean isFilteredOutService(AxisServiceGroup axisServiceGroup) {
        String adminParamValue = (String) axisServiceGroup
                .getParameterValue(ThrottleConstants.ADMIN_SERVICE_PARAM_NAME);
        String hiddenParamValue = (String) axisServiceGroup
                .getParameterValue(ThrottleConstants.HIDDEN_SERVICE_PARAM_NAME);
        String dynamicParamValue = (String) axisServiceGroup
                .getParameterValue(ThrottleConstants.DYNAMIC_SERVICE_PARAM_NAME);
        if (adminParamValue != null && adminParamValue.length() != 0) {
            if (Boolean.parseBoolean(adminParamValue.trim())) {
                return true;
            }
        } else if (hiddenParamValue != null && hiddenParamValue.length() != 0) {
            if (Boolean.parseBoolean(hiddenParamValue.trim())) {
                return true;
            }
        } else if (dynamicParamValue != null && dynamicParamValue.length() != 0){
            if(Boolean.parseBoolean(dynamicParamValue.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * An external policy can be configured using a parameter in the axis2.xml which points to the
     * absolute path of the policy file. This method reads the policy file and creates a
     * PolicySubject.
     *
     * @param axisConfig - AxisConfiguration instance
     * @return - PolicySubject instance if the file found. Otherwise null..
     */
    public static PolicySubject readExternalGlobalPolicy(AxisConfiguration axisConfig) {
        PolicySubject extPolicySubject = null;
        // read the global throttle parameter from axisConfig
        Parameter globalThrottlePolicyParam = axisConfig
                .getParameter(ThrottleConstants.GLOBAL_THROTTLE_PATH_PARAM);
        if (globalThrottlePolicyParam != null &&
                !"".equals(globalThrottlePolicyParam.getValue())) {
            // If the path found, try to read the file
            String policyPath = (String) globalThrottlePolicyParam.getValue();
            File policyFile = new File(policyPath);
            if (policyFile.exists()) {
                try {
                    // If the file exists, try to build the policy
                    Policy globalPolicy = PolicyEngine.getPolicy(new FileInputStream(policyFile));
                    extPolicySubject = new PolicySubject();
                    extPolicySubject.attachPolicy(globalPolicy);
                } catch (FileNotFoundException e) {
                    log.error("Error while reading global policy file..", e);
                }
            }
        }
        return extPolicySubject;
    }

}
