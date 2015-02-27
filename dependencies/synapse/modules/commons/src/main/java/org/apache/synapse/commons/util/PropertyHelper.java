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

package org.apache.synapse.commons.util;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.SynapseCommonsException;

import javax.xml.namespace.QName;
import java.lang.reflect.Method;

/**
 * This class will be used as a Helper class to get the properties loaded while building the
 * Synapse Configuration from the XML
 */
public class PropertyHelper {

    /**
     * Log variable for the logging purposes
     */
    private static final Log log = LogFactory.getLog(PropertyHelper.class);

    /**
     * Find and invoke the setter method with the name of form setXXX passing in the value given
     * on the POJO object
     *
     * @param name name of the setter field
     * @param val  value to be set
     * @param obj  POJO instance
     */
    public static void setInstanceProperty(String name, Object val, Object obj) {

        String mName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Method method;

        try {
            Method[] methods = obj.getClass().getMethods();
            boolean invoked = false;

            for (Method method1 : methods) {
                if (mName.equals(method1.getName())) {
                    Class[] params = method1.getParameterTypes();
                    if (params.length != 1) {
                        handleException("Did not find a setter method named : " + mName +
                                "() that takes a single String, int, long, float, double ," +
                                "OMElement or boolean parameter");
                    } else if (val instanceof String) {
                        String value = (String) val;
                        if (String.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, String.class);
                            method.invoke(obj, new String[]{value});
                        } else if (int.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, int.class);
                            method.invoke(obj, new Integer[]{new Integer(value)});
                        } else if (long.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, long.class);
                            method.invoke(obj, new Long[]{new Long(value)});
                        } else if (float.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, float.class);
                            method.invoke(obj, new Float[]{new Float(value)});
                        } else if (double.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, double.class);
                            method.invoke(obj, new Double[]{new Double(value)});
                        } else if (boolean.class.equals(params[0])) {
                            method = obj.getClass().getMethod(mName, boolean.class);
                            method.invoke(obj, new Boolean[]{Boolean.valueOf(value)});
                        } else {
                            continue;
                        }
                    } else if (val instanceof OMElement && OMElement.class.equals(params[0])) {
                        method = obj.getClass().getMethod(mName, OMElement.class);
                        method.invoke(obj, new OMElement[]{(OMElement) val});
                    } else {
                        continue;
                    }
                    invoked = true;
                    break;
                }
            }

            if (!invoked) {
                handleException("Did not find a setter method named : " + mName +
                        "() that takes a single String, int, long, float, double " +
                        "or boolean parameter");
            }

        } catch (Exception e) {
            handleException("Error invoking setter method named : " + mName +
                    "() that takes a single String, int, long, float, double " +
                    "or boolean parameter", e);
        }
    }

    /**
     * This method will set the static property discribed in the OMElement to the specified object.
     * This Object should have the setter method for the specified property name
     *
     * @param property - OMElement specifying the property to be built in to the object
     * @param o        - Object to which the specified property will be set.
     */
    public static void setStaticProperty(OMElement property, Object o) {

        if (("property".equals(property.getLocalName().toLowerCase()))) {

            String propertyName = property.getAttributeValue(new QName("name"));
            String mName = "set"
                    + Character.toUpperCase(propertyName.charAt(0))
                    + propertyName.substring(1);

            // try to set String value first
            if (property.getAttributeValue(new QName("value")) != null) {
                String value = property.getAttributeValue(new QName("value"));
                setInstanceProperty(propertyName, value, o);
            } else {
                // now try XML child
                OMElement value = property.getFirstElement();
                if (value != null) {

                    try {
                        Method method = o.getClass().getMethod(mName, OMElement.class);
                        if (log.isDebugEnabled()) {
                            log.debug("Setting property :: invoking method "
                                    + mName + "(" + value + ")");
                        }
                        method.invoke(o, value);

                    } catch (Exception e) {
                        handleException("Error setting property : " + propertyName
                                + " as an OMElement property into class mediator : "
                                + o.getClass() + " : " + e.getMessage(), e);
                    }

                }

            }
        }
    }

    /**
     * This method will check the given OMElement represent either a static property or not
     *
     * @param property - OMElement to be checked for the static property
     * @return boolean true if the elemet represents a static property element false otherwise
     */
    public static boolean isStaticProperty(OMElement property) {
        return "property".equals(property.getLocalName().toLowerCase())
                && (property.getAttributeValue(new QName("expression")) == null);
    }

    private static void handleException(String message, Throwable e) {
        log.error(message + e.getMessage());
        throw new SynapseCommonsException(message, e);
    }

    private static void handleException(String message) {
        log.error(message);
        throw new SynapseCommonsException(message);
    }
}
