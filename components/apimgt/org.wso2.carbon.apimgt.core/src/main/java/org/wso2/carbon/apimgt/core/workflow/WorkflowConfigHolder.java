/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.core.workflow;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
//import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;

//import java.io.File;
//import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;
//import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
//import javax.xml.stream.XMLStreamReader;

/**
 * This class holds the workflow extensions
 */
public class WorkflowConfigHolder {

    private static final Log log = LogFactory.getLog(WorkflowConfigHolder.class);

    private static final QName PROP_Q = new QName("Property");

    private static final QName ATT_NAME = new QName("name");

    private Map<String, WorkflowExecutor> workflowExecutorMap;

    public WorkflowExecutor getWorkflowExecutor(String workflowExecutorType) {
        return workflowExecutorMap.get(workflowExecutorType);
    }

    public void load()
            throws WorkflowException, FileNotFoundException, NoSuchMethodException, InvocationTargetException {
       // XMLStreamReader xmlReader = null;
        workflowExecutorMap = new ConcurrentHashMap<String, WorkflowExecutor>();

        try {
            //            String fName = "/home/yasima/default-workflow-extensions.xml";
            //            File wfExtensions = new File(fName);
            //            xmlReader = XMLInputFactory.newInstance()
            //                    .createXMLStreamReader(new FileInputStream(wfExtensions));
            //            StAXOMBuilder builder = new StAXOMBuilder(xmlReader);

            OMElement workflowExtensionsElem = AXIOMUtil.stringToOM("<WorkFlowExtensions>\n"
                    + "    <ApplicationCreation executor=\""
                    + "org.wso2.carbon.apimgt.core.workflow.ApplicationCreationSimpleWorkflowExecutor\""
                    + "/></WorkFlowExtensions>");

            //            OMElement workflowExtensionsElem = builder.getDocument()
            //                    .getFirstChildWithName(new QName(WorkflowConstants.WORKFLOW_EXTENSIONS));

            OMElement workflowElem = workflowExtensionsElem
                    .getFirstChildWithName(new QName(WorkflowConstants.APPLICATION_CREATION));
            String executorClass = workflowElem.getAttributeValue(new QName(WorkflowConstants.EXECUTOR));
            Class clazz = WorkflowConfigHolder.class.getClassLoader().loadClass(executorClass);
            WorkflowExecutor workFlowExecutor = (WorkflowExecutor) clazz.newInstance();
            loadProperties(workflowElem, workFlowExecutor);
            workflowExecutorMap.put(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION, workFlowExecutor);

        } catch (XMLStreamException e) {
            log.error("Error building xml from Resource at " + e);
            handleException("Error building xml from Resource at " + e);
        } catch (ClassNotFoundException e) {
            log.error("Unable to find class", e);
            handleException("Unable to find class", e);
        } catch (InstantiationException e) {
            log.error("Unable to instantiate class", e);
            handleException("Unable to instantiate class", e);
        } catch (IllegalAccessException e) {
            log.error("Illegal attempt to invoke class methods", e);
            handleException("Illegal attempt to invoke class methods", e);
        } catch (WorkflowException e) {
            log.error("Unable to load workflow executor class", e);
            handleException("Unable to load workflow executor class", e);
        }
//        } finally {
//            try {
//                xmlReader.close();
//            } catch (XMLStreamException e) {
//                log.error("Cannot close the stream");
//            }
//        }
    }

    private void loadProperties(OMElement executorElem, Object workflowClass)
            throws WorkflowException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {

        for (Iterator it = executorElem.getChildrenWithName(PROP_Q); it.hasNext(); ) {
            OMElement propertyElem = (OMElement) it.next();
            String propName = propertyElem.getAttribute(ATT_NAME).getAttributeValue();
            if (propName == null) {
                handleException("An Executor class property must specify the name attribute");
            } else {
                OMNode omElt = propertyElem.getFirstElement();
                if (omElt != null) {
                    setInstanceProperty(propName, omElt, workflowClass);
                } else if (propertyElem.getText() != null) {
                    String value;
                    value = propertyElem.getText();
                    setInstanceProperty(propName, value, workflowClass);
                } else {
                    handleException("An Executor class property must specify "
                            + "name and text value, or a name and a child XML fragment");
                }
            }
        }
    }

    /**
     * Find and invoke the setter method with the name of form setXXX passing in the value given
     * on the POJO object
     *
     * @param name name of the setter field
     * @param val  value to be set
     * @param obj  POJO instance
     */
    public void setInstanceProperty(String name, Object val, Object obj)
            throws WorkflowException, NoSuchMethodException, InvocationTargetException, IllegalAccessException,
            ClassNotFoundException {

        String mName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Method method;

        //try {
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
                        method.invoke(obj, new String[] { value });
                    } else if (int.class.equals(params[0])) {
                        method = obj.getClass().getMethod(mName, int.class);
                        method.invoke(obj, new Integer[] { Integer.valueOf(value) });
                    } else if (long.class.equals(params[0])) {
                        method = obj.getClass().getMethod(mName, long.class);
                        method.invoke(obj, new Long[] { Long.valueOf(value) });
                    } else if (float.class.equals(params[0])) {
                        method = obj.getClass().getMethod(mName, float.class);
                        method.invoke(obj, new Float[] { Float.valueOf(value) });
                    } else if (double.class.equals(params[0])) {
                        method = obj.getClass().getMethod(mName, double.class);
                        method.invoke(obj, new Double[] { Double.valueOf(value) });
                    } else if (boolean.class.equals(params[0])) {
                        method = obj.getClass().getMethod(mName, boolean.class);
                        method.invoke(obj, new Boolean[] { Boolean.valueOf(value) });
                    } else if (Class.forName("[C").equals(params[0])) {
                        method = obj.getClass().getMethod(mName, Class.forName("[C"));
                        method.invoke(obj, value.toCharArray());
                    } else {
                        continue;
                    }
                } else if (val instanceof OMElement && OMElement.class.equals(params[0])) {
                    method = obj.getClass().getMethod(mName, OMElement.class);
                    method.invoke(obj, new OMElement[] { (OMElement) val });
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
        //        } catch (Exception e) {
        //            handleException("Error invoking setter method named : " + mName +
        //                    "() that takes a single String, int, long, float, double " +
        //                    "or boolean parameter", e);
        //        }
    }

    private static void handleException(String msg) throws WorkflowException {
        log.error(msg);
        throw new WorkflowException(msg);
    }

    private static void handleException(String msg, Exception e) throws WorkflowException {
        log.error(msg, e);
        throw new WorkflowException(msg, e);
    }
}
