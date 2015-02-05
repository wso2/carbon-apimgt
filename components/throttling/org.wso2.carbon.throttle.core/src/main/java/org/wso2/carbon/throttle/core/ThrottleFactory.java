/*
*  Licensed to the Apache Software Foundation (ASF) under one
*  or more contributor license agreements.  See the NOTICE file
*  distributed with this work for additional information
*  regarding copyright ownership.  The ASF licenses this file
*  to you under the Apache License, Version 2.0 (the
*  "License"); you may not use this file except in compliance
*  with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.throttle.core;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.xml.XmlPrimtiveAssertion;
import org.wso2.carbon.throttle.core.factory.CallerConfigurationFactory;
import org.wso2.carbon.throttle.core.factory.ThrottleConfigurationFactory;
import org.wso2.carbon.throttle.core.factory.ThrottleContextFactory;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;

/**
 * Factory for creating a throttle instance using throttle policy
 */
public class ThrottleFactory {

    private ThrottleFactory() {
    }

    private static Log log = LogFactory.getLog(ThrottleFactory.class);

    /**
     * Abstraction for processing module policy assertion and create a throttle based on it
     *
     * @param policy Throttle policy
     * @return Throttle instance , if there any module policy assertion , otherwise null
     * @throws ThrottleException
     */
    public static Throttle createModuleThrottle(Policy policy) throws ThrottleException {
        return createThrottle(policy, ThrottleConstants.MODULE_THROTTLE_ASSERTION_QNAME);
    }

    /**
     * Abstraction for processing service policy assertion and create a throttle based on it
     *
     * @param policy Throttle policy
     * @return Throttle instance , if there any service policy assertion , otherwise null
     * @throws ThrottleException
     */
    public static Throttle createServiceThrottle(Policy policy) throws ThrottleException {
        return createThrottle(policy, ThrottleConstants.SERVICE_THROTTLE_ASSERTION_QNAME);
    }

    /**
     * Abstraction for processing operation policy assertion and create a throttle based on it
     *
     * @param policy Throttle policy
     * @return Throttle instance , if there any operation policy assertion , otherwise null
     * @throws ThrottleException
     */
    public static Throttle createOperationThrottle(Policy policy) throws ThrottleException {
        return createThrottle(policy, ThrottleConstants.OPERATION_THROTTLE_ASSERTION_QNAME);
    }

    /**
     * Abstraction for processing mediator policy assertion and create a throttle based on it
     *
     * @param policy Throttle policy
     * @return Throttle instance , if there any mediator policy assertion , otherwise null
     * @throws ThrottleException
     */
    public static Throttle createMediatorThrottle(Policy policy) throws ThrottleException {
        return createThrottle(policy, ThrottleConstants.MEDIATOR_THROTTLE_ASSERTION_QNAME);
    }


    /**
     * Factory method to create a throttle from a given throttle policy
     *
     * @param policy    Throttle policy
     * @param forceRoot Root assertion QName for select correct policy assertion
     * @return Throttle instance
     * @throws ThrottleException
     */
    private static Throttle createThrottle(Policy policy, QName forceRoot) throws ThrottleException {

        if (policy == null) {
            if (log.isDebugEnabled()) {
                log.debug("Policy cannot be found");
            }
            //if policy is not available ,then return null for ThrottleConfiguration
            return null; // no policy is available in the module description
        }

        if (forceRoot == null) {
            if (log.isDebugEnabled()) {
                log.debug("Given root assertion QName is null");
            }
            return null;
        }

        for (Iterator iterator = policy.getAlternatives();
             iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof List) {
                List list = (List) object;
                for (Iterator it = list.iterator(); it.hasNext();) {
                    Object assertObj = it.next();
                    if (assertObj instanceof XmlPrimtiveAssertion) {
                        XmlPrimtiveAssertion primitiveAssertion = (XmlPrimtiveAssertion)
                                assertObj;
                        QName qName = primitiveAssertion.getName();

                        if (qName == null) {
                            handleException("Invalid Throttle Policy - QName of the " +
                                    "assertion cannot be null.");
                        }
                        // top policy must contains ThrottleAssertion
                        Policy throttlePolicy = PolicyEngine.
                                getPolicy(primitiveAssertion.getValue());
                        if (ThrottleConstants.THROTTLE_ASSERTION_QNAME.equals(qName)) {
                            return ThrottlePolicyProcessor.processPolicy(throttlePolicy);
                        } else if (forceRoot.equals(qName)) {
                            return buildThrottle(throttlePolicy);
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("There is no throttle policy " +
                                        "for given QName : " + forceRoot);
                            }
                        }
                    }
                }
            }
        }
        return null;

    }


    /**
     * Factory method to help to create a throttle
     *
     * @param throtlePolicy Throttle assertion policy
     * @return Throttle instance
     * @throws ThrottleException
     */
    private static Throttle buildThrottle(Policy throtlePolicy) throws ThrottleException {
        Throttle throttle = new Throttle();   // throttle instance
        ThrottleConfiguration configuration = null;  // configuration data
        List list = throtlePolicy.getPolicyComponents();

        if (list == null || (list != null && list.isEmpty())) {
            handleException("Empty the policy components" +
                    " as ThrottleAssertion's children");
        }
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            if (object instanceof Policy) {

                // boolean isOtherConfiguration = false;
                //  // To track default callerConfiguration for all ips
                CallerConfiguration callerConfiguration = null;
                Policy policy = null;
                List assertList = ((Policy) object).getAssertions();
                if (assertList != null) {
                    for (Iterator assertIterator =
                            assertList.iterator(); assertIterator.hasNext();) {
                        Object ca = assertIterator.next();
                        if (ca instanceof XmlPrimtiveAssertion) {
                            XmlPrimtiveAssertion id = (XmlPrimtiveAssertion) ca;
                            configuration = createThrottleConfiguration(id, throttle);
                            if (configuration == null) {
                                handleException("Invalid throttle - Throttle configuration " +
                                        "cannot be created from given policy");
                            }

                            if (configuration.getType() == ThrottleConstants.IP_BASE) {
                                //create a caller context for ip based throttle
                                callerConfiguration = CallerConfigurationFactory.
                                        createCallerConfiguration(
                                                ThrottleConstants.IP_BASE);
                            } else if (configuration.getType() == ThrottleConstants.DOMAIN_BASE) {
                                //create a caller context for ip based throttle
                                callerConfiguration = CallerConfigurationFactory.
                                        createCallerConfiguration(
                                                ThrottleConstants.DOMAIN_BASE);
                            }
                            else if (configuration.getType() == ThrottleConstants.ROLE_BASE) {
                                //create a caller context for ip based throttle
                                callerConfiguration = CallerConfigurationFactory.
                                        createCallerConfiguration(
                                                ThrottleConstants.ROLE_BASE);
                            } else {
                                handleException("Invalid throttle type - Only" +
                                        " support IP ,DOMAIN and ROLE as types ");
                            }
                            if (callerConfiguration != null) {
                                OMElement element = id.getValue();

                                // Name of the policy assertion
                                String name = element.getLocalName();
                                // Value of the policy assertion
                                String value = element.getText();

                                // If Value and Name  are null,
                                // then it is a invalid policy configuration
                                if (name == null || value == null) {
                                    handleException("Either Value or" +
                                            " Name of the policy cannot be null");
                                } else if (name.equals(ThrottleConstants.ID_PARAMETER_NAME)) {

                                    if (!value.equals("")) {
                                        callerConfiguration.setID(value);
                                    } else {
                                        handleException("Value of ID cannot find " +
                                                "- invalid configuration");
                                    }
                                } else {
                                    handleException("Undefined policy property for" +
                                            " throttle - Expect ID  ");
                                }
                            }


                        } else if (ca instanceof Policy) {
                            policy = (Policy) ca;
                        }
                    }
                }
                if (callerConfiguration != null) {
                    if (policy != null) {
                        fillCallerConfiguration(policy, callerConfiguration);
                        configuration.addCallerConfiguration(callerConfiguration);
                    }

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Couldn't find a callerConfiguration for a throttle" +
                                " configuration for an one caller  ");
                    }
                }

            } else if (object instanceof XmlPrimtiveAssertion) {

                XmlPrimtiveAssertion xmlPrimitiveAssertion = (XmlPrimtiveAssertion) object;
                OMElement element = xmlPrimitiveAssertion.getValue();
                // Name of the policy assertion
                String name = element.getLocalName();
                //Value of the policy assertion
                String value = element.getText();

                //if Value and Name  are null,then
                // it is a invalid policy configuration
                if (name == null || value == null) {
                    handleException("Either value or name of the policy cannot be null");
                } else if (name.equals(
                        ThrottleConstants.MAXIMUM_CONCURRENT_ACCESS_PARAMETER_NAME)) {
                    int intValue = 0;
                    try {
                        intValue = Integer.parseInt(value.trim());
                    } catch (NumberFormatException ignored) {
                        log.error("Error occurred - Invalid number for maximum " +
                                "concurrent access ", ignored);
                    }
                    if (intValue > 0) {
                        throttle.setConcurrentAccessController(
                                new ConcurrentAccessController(intValue));
                    }
                } else {
                    handleException("Invalid throttle policy configuration : unexpected policy element " +
                            "with name " + name);
                }
            }
        }
        return throttle;
    }

    /**
     * Factory method to create a Throttle Configuration instance
     *
     * @param id       Policy assertion relates to a particular caller
     * @param throttle Throttle instance
     * @return Throttle Configuration instance
     * @throws ThrottleException
     */
    private static ThrottleConfiguration createThrottleConfiguration(XmlPrimtiveAssertion id, Throttle throttle) throws ThrottleException {
        OMElement element = id.getValue();
        ThrottleConfiguration configuration = null;

        String type = element.getAttributeValue(
                ThrottleConstants.THROTTLE_TYPE_ATTRIBUTE_QNAME);
        if (type == null) {
            handleException("Type of Throttle " +
                    "in the policy cannot be null");
        }
        if ("IP".equals(type)) {
            // create a ip based throttle context and configuration
            configuration = throttle.getThrottleConfiguration
                    (ThrottleConstants.IP_BASED_THROTTLE_KEY);
            if (configuration == null) {
                configuration =
                        ThrottleConfigurationFactory.
                                createThrottleConfiguration(
                                        ThrottleConstants.IP_BASE);
                throttle.addThrottleContext(
                        ThrottleConstants.IP_BASED_THROTTLE_KEY,
                        ThrottleContextFactory.createThrottleContext(
                                ThrottleConstants.IP_BASE, configuration));
                throttle.addThrottleConfiguration(
                        ThrottleConstants.IP_BASED_THROTTLE_KEY, configuration);
            }

        } else if (("DOMAIN".equals(type))) {
            // create a domain based throttle context and configuration
            configuration = throttle.getThrottleConfiguration(
                    ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY);
            if (configuration == null) {
                configuration =
                        ThrottleConfigurationFactory.
                                createThrottleConfiguration(
                                        ThrottleConstants.DOMAIN_BASE);
                throttle.addThrottleContext(
                        ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY,
                        ThrottleContextFactory.createThrottleContext(
                                ThrottleConstants.DOMAIN_BASE, configuration));
                throttle.addThrottleConfiguration(
                        ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY, configuration);
            }
            //create a caller context for domain based throttle

        } else if (("ROLE".equals(type))) {
            // create a domain based throttle context and configuration
            configuration = throttle.getThrottleConfiguration(
                    ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
            if (configuration == null) {
                configuration =
                        ThrottleConfigurationFactory.
                                createThrottleConfiguration(
                                        ThrottleConstants.ROLE_BASE);
                throttle.addThrottleContext(
                        ThrottleConstants.ROLE_BASED_THROTTLE_KEY,
                        ThrottleContextFactory.createThrottleContext(
                                ThrottleConstants.ROLE_BASE, configuration));
                throttle.addThrottleConfiguration(
                        ThrottleConstants.ROLE_BASED_THROTTLE_KEY, configuration);
            }
            //create a caller context for domain based throttle

        } else {
            handleException("Unsupported throttle type : " + type);
        }

        return configuration;
    }

    /**
     * Fills the caller configuration information based on given policy
     *
     * @param policy              Policy instance
     * @param callerConfiguration Caller configuration instance
     * @throws ThrottleException
     */
    private static void fillCallerConfiguration(Policy policy, CallerConfiguration callerConfiguration) throws ThrottleException {
        List list = policy.getPolicyComponents();
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object object = iterator.next();

            XmlPrimtiveAssertion primitiveAssertion = (XmlPrimtiveAssertion) object;
            OMElement element = primitiveAssertion.getValue();
            // Name of the policy assertion
            String name = element.getLocalName();
            if (name.equals(
                    ThrottleConstants.ALLOW_PARAMETER_NAME)) {

                callerConfiguration.setAccessState(
                        ThrottleConstants.ACCESS_ALLOWED);

            } else if (name.equals(
                    ThrottleConstants.DENY_PARAMETER_NAME)) {

                callerConfiguration.setAccessState(
                        ThrottleConstants.ACCESS_DENIED);

            } else if (name.equals(
                    ThrottleConstants.CONTROL_PARAMETER_NAME)) {

                callerConfiguration.setAccessState(
                        ThrottleConstants.ACCESS_CONTROLLED);
                OMElement controlElement = primitiveAssertion.getValue();
                if (controlElement == null) {
                    handleException("Invalid throttle configuration - " +
                            "Control assertion cannot be empty");
                }

                Policy controlPolicy = PolicyEngine.getPolicy(controlElement);
                if (controlPolicy != null) {
                    fillControlConfiguration(controlPolicy, callerConfiguration);
                } else {
                    handleException("Invalid throttle configuration - " +
                            "Cannot create a policy object(Control Assertion ) " +
                            "form given policy file ");
                }

            } else {
                handleException("Invalid Throttle" +
                        " Policy configuration");
            }
        }
    }

    /**
     * Helper method to process control assertion
     *
     * @param policy              Policy for Control Assertion
     * @param callerConfiguration Caller to whom control need to be applied.
     * @throws ThrottleException
     */
    private static void fillControlConfiguration(Policy policy, CallerConfiguration callerConfiguration) throws ThrottleException {
        boolean isFoundMaxCount = false;
        boolean isFoundUnitTime = false;
        List list = policy.getPolicyComponents();
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            if (obj instanceof Policy) {
                List controlList = ((Policy) obj).getPolicyComponents();
                for (Iterator controlIterator = controlList.iterator(); controlIterator.hasNext();)
                {
                    Object object = controlIterator.next();
                    if (object instanceof XmlPrimtiveAssertion) {
                        XmlPrimtiveAssertion primitiveAssertion =
                                (XmlPrimtiveAssertion) object;
                        OMElement element = primitiveAssertion.getValue();
                        // Name of the policy assertion
                        String name = element.getLocalName();
                        //Value of the policy assertion
                        String value = element.getText();

                        //if Value and Name  are null,then it is a
                        // invalid policy configuration
                        if (name == null || value == null) {
                            handleException("Either Value or " +
                                    "Name of the policy cannot be null");
                        }
                        if (!value.equals("")) {

                            if (name.equals(
                                    ThrottleConstants.
                                            MAXIMUM_COUNT_PARAMETER_NAME)) {

                                isFoundMaxCount = true;
                                try {
                                    callerConfiguration.setMaximumRequestPerUnitTime(
                                            Integer.parseInt(value.trim()));
                                } catch (NumberFormatException ignored) {
                                    log.error("Error occurred - " +
                                            "Invalid number for maximum " +
                                            "request number ", ignored);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Access" +
                                                " will be fully allowed");
                                    }
                                    callerConfiguration.setAccessState(
                                            ThrottleConstants.ACCESS_ALLOWED);
                                }
                            } else if (name.equals(
                                    ThrottleConstants.
                                            UNIT_TIME_PARAMETER_NAME)) {
                                //TODO need to verify that value is in milisecond
                                long timeInMilliSec = 0;
                                try {
                                    timeInMilliSec =
                                            Long.parseLong(value.trim());
                                } catch (NumberFormatException ignored) {
                                    log.error("Error occurred " +
                                            "- Invalid number for unit time",
                                            ignored);
                                }
                                if (timeInMilliSec == 0) {
                                    handleException("Unit Time cannot " +
                                            "find - invalid throttle " +
                                            "policy configuration");
                                }
                                isFoundUnitTime = true;
                                callerConfiguration.setUnitTime(timeInMilliSec);

                            } else if (name.equals(
                                    ThrottleConstants.
                                            PROHIBIT_TIME_PERIOD_PARAMETER_NAME)) {
                                try {
                                    callerConfiguration.setProhibitTimePeriod(
                                            Long.parseLong(value.trim()));
                                } catch (NumberFormatException ignored) {
                                    log.error("Error occurred - Invalid" +
                                            " number for prohibit time ",
                                            ignored);
                                }
                            } else {
                                handleException("Undefined Policy" +
                                        " property for Throttle Policy");
                            }
                        } else {
                            if (!name.equals(
                                    ThrottleConstants.
                                            PROHIBIT_TIME_PERIOD_PARAMETER_NAME)) {
                                handleException("The policy which have " +
                                        " defined as optional " +
                                        "should have value ");
                            }
                        }
                    }
                }
            } else {
                handleException("Invalid policy - " +
                        "Control Assertion must contain a wsp:Policy as child ");
            }

        }
        if (!isFoundUnitTime && !isFoundMaxCount) {
            handleException("Maximum Count and UnitTime are " +
                    "Mandatory in Throttle Policy ");

        }
    }

    /**
     * Helper method to handle exception
     *
     * @param message Debug message
     * @throws ThrottleException
     */
    private static void handleException(String message) throws ThrottleException {
        String msg = "Error was occurred during throttle policy processing :  " + message;
        log.error(msg);
        throw new ThrottleException(msg);
    }
}
