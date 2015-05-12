/*
* Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
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

package org.wso2.carbon.throttle.core;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.All;
import org.apache.neethi.ExactlyOne;
import org.apache.neethi.Policy;
import org.apache.neethi.builders.xml.XmlPrimtiveAssertion;
import org.wso2.carbon.throttle.core.factory.CallerConfigurationFactory;
import org.wso2.carbon.throttle.core.factory.ThrottleConfigurationFactory;
import org.wso2.carbon.throttle.core.factory.ThrottleContextFactory;

import java.util.Iterator;
import java.util.List;


/**
 * @deprecated The class for processing policy that specify throttle configuration
 */

public class ThrottlePolicyProcessor {

    private static Log log = LogFactory.getLog(ThrottlePolicyProcessor.class.getName());


    /**
     * @param policy - policy for throttle
     * @return Throttle        - An object which holds Mata-Data about throttle
     * @throws ThrottleException - throws for errors in policy processing - ex : invalid policy
     * @deprecated process policy and returns throttle object
     */

    public static Throttle processPolicy(Policy policy) throws ThrottleException {


        if (policy == null) {
            //if policy is not available ,then return null for ThrottleConfiguration
            return null; // no policy is available in the module description
        }

        Throttle th = new Throttle();
        ThrottleConfiguration tc = null;  // configuration data

        List al = policy.getPolicyComponents();

        if (al == null || (al != null && al.isEmpty())) {
            handleException("Empty the policy components" +
                    " as ThrottleAssertion's children");
        }
        for (Iterator i = al.iterator(); i.hasNext();) {
            Object tp = i.next();
            if (tp instanceof All) {

                // boolean isOtherConfiguration = false;
                //  // To track default cn for all ips
                CallerConfiguration cn = null; // To create a
                //configurationbean object
                boolean isIPRangeFound = false;
                boolean isExactlyOneFound = false;
                ExactlyOne cp = null;
                List cL = ((All) tp).getAssertions();
                if (cL != null) {
                    for (Iterator ci =
                            cL.iterator(); ci.hasNext();) {
                        Object ca = ci.next();
                        if (ca instanceof XmlPrimtiveAssertion) {
                            XmlPrimtiveAssertion id = (XmlPrimtiveAssertion) ca;
                            OMElement el = id.getValue();

                            String t = el.getAttributeValue(
                                    ThrottleConstants.THROTTLE_TYPE_ATTRIBUTE_QNAME);
                            if (t == null) {
                                handleException("Type of Throtle " +
                                        "in the policy cannot be null");
                            }
                            if (t.equals("IP")) {
                                // create a ip based throttle context and configuration
                                tc = th.getThrottleConfiguration
                                        (ThrottleConstants.IP_BASED_THROTTLE_KEY);
                                if (tc == null) {
                                    tc =
                                            ThrottleConfigurationFactory.
                                                    createThrottleConfiguration(
                                                            ThrottleConstants.IP_BASE);
                                    th.addThrottleContext(
                                            ThrottleConstants.IP_BASED_THROTTLE_KEY,
                                            ThrottleContextFactory.createThrottleContext(
                                                    ThrottleConstants.IP_BASE, tc));
                                    th.addThrottleConfiguration(
                                            ThrottleConstants.IP_BASED_THROTTLE_KEY, tc);
                                }
                                //create a callercontext for ip based throttle
                                cn = CallerConfigurationFactory.
                                        createCallerConfiguration(
                                                ThrottleConstants.IP_BASE);
                            } else if (t.equals("DOMAIN")) {
                                // create a domain based throttle context and configuration
                                tc = th.getThrottleConfiguration(
                                        ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY);
                                if (tc == null) {
                                    tc =
                                            ThrottleConfigurationFactory.
                                                    createThrottleConfiguration(
                                                            ThrottleConstants.DOMAIN_BASE);
                                    th.addThrottleContext(
                                            ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY,
                                            ThrottleContextFactory.createThrottleContext(
                                                    ThrottleConstants.DOMAIN_BASE, tc));
                                    th.addThrottleConfiguration(
                                            ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY, tc);
                                }
                                //create a callercontext for domain based throttl
                                cn =
                                        CallerConfigurationFactory.
                                                createCallerConfiguration(
                                                        ThrottleConstants.DOMAIN_BASE);
                            } else {
                                handleException("Unsupported throttle type : " + t);
                            }
                            if (cn != null) {

                                // Name of the policy assertion
                                String n = el.getLocalName();
                                // Value of the policy assertion
                                String v = el.getText();

                                // If Value and Name  are null,
                                // then it is a invalid policy config
                                if (n == null || v == null) {
                                    handleException("Either Value or" +
                                            " Name of the policy cannot be null");
                                } else if (n.equals(ThrottleConstants.ID_PARAMETER_NAME)) {

                                    if (!v.equals("")) {

                                        isIPRangeFound = true;
                                        cn.setID(v);
                                    } else {
                                        handleException("Value of ID cannot find " +
                                                "- invalid configuration");
                                    }
                                } else {
                                    handleException("Undefined pocilcy property for" +
                                            " throttle - Expect ID  ");
                                }
                            }

                        } else if (ca instanceof ExactlyOne) {
                            cp = (ExactlyOne) ca;
                        }
                    }
                }
                if (cn != null) {
                    if (cp != null) {
                        List cal = cp.getPolicyComponents();
                        boolean haveSelectOneFromExactlyOne = false;
                        for (Iterator ci = cal.iterator(); ci.hasNext()
                                && !haveSelectOneFromExactlyOne;) {
                            Object co = ci.next();
                            if (co instanceof All) {
                                haveSelectOneFromExactlyOne = true;
                                boolean isFoundMaxCount = false;
                                boolean isFoundUnitTime = false;
                                All childAll = (All) co;
                                List cd = childAll.getPolicyComponents();
                                for (Iterator cdl = cd.iterator(); cdl.hasNext();) {
                                    Object d = cdl.next();
                                    if (d instanceof XmlPrimtiveAssertion) {
                                        XmlPrimtiveAssertion adx =
                                                (XmlPrimtiveAssertion) d;
                                        OMElement el = adx.getValue();
                                        // Name of the policy assertion
                                        String n = el.getLocalName();
                                        //Value of the policy assertion
                                        String v = el.getText();

                                        //if Value and Name  are null,then it is a
                                        // invalid policy config
                                        if (n == null || v == null) {
                                            handleException("Either Value or " +
                                                    "Name of the policy cannot be null");
                                        }
                                        if (!v.equals("")) {

                                            if (n.equals(
                                                    ThrottleConstants.
                                                            MAXIMUM_COUNT_PARAMETER_NAME)) {

                                                isFoundMaxCount = true;
                                                try {
                                                    cn.setMaximumRequestPerUnitTime(
                                                            Integer.parseInt(v.trim()));
                                                } catch (NumberFormatException ignored) {
                                                    log.error("Error occurred - " +
                                                            "Invalid number for maximum " +
                                                            "request number ", ignored);
                                                    if (log.isDebugEnabled()) {
                                                        log.debug("Access" +
                                                                " will be fully allowed");
                                                    }
                                                    cn.setAccessState(
                                                            ThrottleConstants.ACCESS_ALLOWED);
                                                }
                                            } else if (n.equals(
                                                    ThrottleConstants.
                                                            UNIT_TIME_PARAMETER_NAME)) {
                                                //TODO need to verify that value is in milisecond
                                                long timeInMiliSec = 0;
                                                try {
                                                    timeInMiliSec =
                                                            Long.parseLong(v.trim());
                                                } catch (NumberFormatException ignored) {
                                                    log.error("Error occurred " +
                                                            "- Invalid number for unit time",
                                                            ignored);
                                                }
                                                if (timeInMiliSec == 0) {
                                                    handleException("Unit Time cannot " +
                                                            "find - invalid throttle " +
                                                            "policy configuration");
                                                }
                                                isFoundUnitTime = true;
                                                cn.setUnitTime(timeInMiliSec);

                                            } else if (n.equals(
                                                    ThrottleConstants.
                                                            PROHIBIT_TIME_PERIOD_PARAMETER_NAME)) {
                                                try {
                                                    cn.setProhibitTimePeriod(
                                                            Long.parseLong(v.trim()));
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
                                            if (!n.equals(
                                                    ThrottleConstants.
                                                            PROHIBIT_TIME_PERIOD_PARAMETER_NAME)) {
                                                handleException("The policy which have " +
                                                        " defined as optional " +
                                                        "should have value ");
                                            }
                                        }
                                    }
                                }
                                if (isFoundUnitTime && isFoundMaxCount) {
                                    isExactlyOneFound = true;
                                } else {
                                    handleException("Maximum Count and UnitTime are " +
                                            "Mandatory in Throttle Policy ");

                                }
                            } else if (co instanceof XmlPrimtiveAssertion) {
                                haveSelectOneFromExactlyOne = true;
                                XmlPrimtiveAssertion alx = (XmlPrimtiveAssertion) co;
                                OMElement ele = alx.getValue();
                                // Name of the policy assertion
                                String n = ele.getLocalName();
                                //Value of the policy assertion
                                String v = ele.getText();

                                //if Value and Name are null,
                                // then it is a invalid policy config
                                if (n == null || v == null) {
                                    handleException("Either Value or" +
                                            " Name of the policy cannot be null");
                                } else if (n.equals(
                                        ThrottleConstants.ISALLOW_PARAMETER_NAME)) {
                                    if (v.equals(Boolean.toString(true))) {
                                        isExactlyOneFound = true;
                                        cn.setAccessState(
                                                ThrottleConstants.ACCESS_ALLOWED);
                                    } else if (v.equals(Boolean.toString(false))) {
                                        isExactlyOneFound = true;
                                        cn.setAccessState(
                                                ThrottleConstants.ACCESS_DENIED);
                                    } else {
                                        handleException("Value for isAllow " +
                                                " component is invalied");
                                    }
                                } else {
                                    handleException("Invalied Throttle" +
                                            " Policy configuration");
                                }
                            }
                        }
                    }

                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Couldn't find a cn for a throttle configuration" +
                                " for an one caller  ");
                    }
                }
                if (isIPRangeFound && isExactlyOneFound) {
                    // If the Throttle Configuration is valid
                    tc.addCallerConfiguration(cn);
                } else {
                    handleException("ID and one of Valid Control policy component are " +
                            "Mandatory in Throttle Policy");
                }
            } else if (tp instanceof XmlPrimtiveAssertion) {

                XmlPrimtiveAssertion mca = (XmlPrimtiveAssertion) tp;
                OMElement ele = mca.getValue();
                // Name of the policy assertion
                String n = ele.getLocalName();
                //Value of the policy assertion
                String v = ele.getText();

                //if Value and Name  are null,then
                // it is a invalid policy configuration
                if (n == null || v == null) {
                    handleException("Either Value or Name of the policy cannot be null");
                } else if (n.equals(
                        ThrottleConstants.MAXIMUM_CONCURRENT_ACCESS_PARAMETER_NAME)) {
                    int intvalue = 0;
                    try {
                        intvalue = Integer.parseInt(v.trim());
                    } catch (NumberFormatException ignored) {
                        log.error("Error occurred - Invalid number for maximum " +
                                "concurrent access ", ignored);
                    }
                    if (intvalue > 0) {
                        th.setConcurrentAccessController(
                                new ConcurrentAccessController(intvalue));
                    }
                } else {
                    handleException("Invalied Throttle Policy configuration");
                }
            }

        }
        return th;
    }

    /**
     * Helper method to handle exception
     *
     * @param message Debug message
     * @throws ThrottleException
     */
    private static void handleException(String message) throws ThrottleException {
        String msg = "Error was occurred during throttle policy processing  " + message;
        log.error(msg);
        throw new ThrottleException(msg);
    }

}
