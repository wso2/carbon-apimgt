/*
* Copyright 2005,2006 WSO2, Inc. http://wso2.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/
package org.wso2.carbon.throttle.module;


import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.*;
import org.apache.axis2.modules.Module;
import org.apache.axis2.util.PolicyUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Assertion;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.wso2.carbon.throttle.core.*;

import java.io.InputStream;
import java.util.*;

public class ThrottleModule implements Module {

    private static Log log = LogFactory.getLog(ThrottleModule.class.getName());

    private Policy defaultPolicy = null;
    private Throttle defaultThrottle = null;
    private ConfigurationContext configctx;

    /**
     * initialize the module
     */
    public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {
        this.configctx = configContext;
        initDefaultPolicy();
        initDefaultThrottle();
        Throttle throttle;

        ThrottleObserver observer = new ThrottleObserver(configctx, defaultThrottle);
        AxisConfiguration axisConfiguration = configctx.getAxisConfiguration();
        axisConfiguration.addObservers(observer);

        /**
         * Global policy can be configured through the axis2.xml as well. If it is configured, we
         * give priority to that policy over the one coming from the module.xml.
         * This is done to allow user to modify the global policy without editing the module.xml
         * of the throttle module.
         */
        PolicySubject policySubject =
                ThrottleEnguageUtils.readExternalGlobalPolicy(axisConfiguration);
        if (policySubject == null) {
            policySubject = module.getPolicySubject();
        }
        if (policySubject != null) {
            List list = new ArrayList(policySubject.getAttachedPolicyComponents());
            Policy policy = PolicyUtil.getMergedPolicy(list, null);
            if (policy != null) {
                try {
                    throttle = ThrottleFactory.createModuleThrottle(policy);
                }
                catch (ThrottleException e) {

                    log.error("Error was occurred when initiating throttle" +
                            " module " + e.getMessage());
                    log.info("Throttling will occur using default module policy");

                    String id = policy.getId();
                    policySubject.detachPolicyComponent(id);
                    defaultPolicy.setId(id);
                    policySubject.attachPolicy(defaultPolicy);
                    throttle = defaultThrottle;
                }
                if (throttle != null) {
                    Map throttles =
                            (Map) configctx.getPropertyNonReplicable(
                                    ThrottleConstants.THROTTLES_MAP);
                    if (throttles == null) {
                        throttles = new HashMap();
                        configctx.setNonReplicableProperty(ThrottleConstants.THROTTLES_MAP,
                                throttles);
                    }
                    throttle.setId(ThrottleConstants.GLOBAL_THROTTLE_ID);
                    throttles.put(ThrottleConstants.GLOBAL_THROTTLE_KEY, throttle);
                    ConcurrentAccessController cac = throttle.getConcurrentAccessController();
                    if (cac != null) {
                        String cacKey = ThrottleConstants.THROTTLE_PROPERTY_PREFIX
                                + ThrottleConstants.GLOBAL_THROTTLE_ID +
                                ThrottleConstants.CAC_SUFFIX;
                        configctx.setProperty(cacKey, cac);
                    }
                }
            }
        }

    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
        ThrottleEnguageUtils.enguage(axisDescription, configctx, defaultThrottle);
    }

    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
        //Todo
    }

    public void applyPolicy(Policy policy, AxisDescription axisDescription) throws AxisFault {

        // TODO
    }

    public boolean canSupportAssertion(Assertion assertion) {
        // TODO
        return true;
    }

    private void initDefaultPolicy() throws AxisFault {
        InputStream inputStream =
                this.getClass().getResourceAsStream("/resources/policy/default_module_policy.xml");
        if (inputStream != null) {
            defaultPolicy = PolicyEngine.getPolicy(inputStream);
        } else {
            throw new AxisFault("Couldn't load the default throttle policy.The module is invalid ");
        }
    }

    private void initDefaultThrottle() throws AxisFault {
        try {
            if (defaultPolicy != null) {
                defaultThrottle = ThrottleFactory.createModuleThrottle(defaultPolicy);
                if (defaultThrottle == null) {
                    throw new AxisFault("Couldn't create the default throttle" +
                            ".The module is invalid ");
                }
            } else {
                throw new AxisFault("Couldn't find the default throttle policy " +
                        ".the module is invalid ");
            }
        } catch (ThrottleException e) {
            String msg = "Error during processing default throttle policy + system will not works" +
                    e.getMessage();
            log.error(msg);
            throw new AxisFault(msg);
        }
    }

/*
    private Policy getThrottlePolicy(Collection components) throws AxisFault {
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
*/
}
